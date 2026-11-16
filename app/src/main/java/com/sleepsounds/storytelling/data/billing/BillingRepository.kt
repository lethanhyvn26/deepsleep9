package com.sleepsounds.storytelling.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.di.ApplicationScope
import com.sleepsounds.storytelling.util.R2Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BillingEvent {
    data object PurchaseSucceeded : BillingEvent
    data object PurchaseRestored : BillingEvent
    data object NoPurchasesFound : BillingEvent
    data object PurchaseCancelled : BillingEvent
    data class BillingError(val debugMessage: String) : BillingEvent
}

/**
 * Clean Google Play Billing v9 repository handling subscriptions (`somni_premium_monthly`,
 * `somni_premium_yearly`) and one-time lifetime unlock (`somni_premium_lifetime`).
 */
@Singleton
class BillingRepository @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : PurchasesUpdatedListener {

    private val productDetailsMap = MutableStateFlow<Map<PremiumPlan, ProductDetails>>(emptyMap())

    private val _planPrices = MutableStateFlow(
        PremiumPlan.entries.associateWith { it.fallbackPrice },
    )
    val planPrices: StateFlow<Map<PremiumPlan, String>> = _planPrices.asStateFlow()

    private val _isBillingBusy = MutableStateFlow(false)
    val isBillingBusy: StateFlow<Boolean> = _isBillingBusy.asStateFlow()

    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

    @Volatile
    private var isConnecting = false

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build(),
        )
        .enableAutoServiceReconnection()
        .build()

    fun startConnection() {
        if (billingClient.isReady || isConnecting) {
            if (billingClient.isReady) {
                refreshCatalogAndPurchases()
            }
            return
        }
        isConnecting = true
        runCatching {
            billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        isConnecting = false
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            R2Log.i("Play Billing connected")
                            refreshCatalogAndPurchases()
                        } else {
                            R2Log.w("Play Billing setup code=${result.responseCode}: ${result.debugMessage}")
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        isConnecting = false
                        R2Log.w("Play Billing service disconnected")
                    }
                },
            )
        }.onFailure { error ->
            isConnecting = false
            R2Log.w("Play Billing startConnection error: ${error.message}")
        }
    }

    fun refreshCatalogAndPurchases() {
        if (!billingClient.isReady) {
            startConnection()
            return
        }
        querySubscriptionProductDetails()
        queryInAppProductDetails()
        queryActivePurchases(emitRestoreEvent = false)
    }

    fun restorePurchases() {
        if (!billingClient.isReady) {
            startConnection()
            appScope.launch {
                _events.emit(BillingEvent.BillingError("Google Play Billing is connecting, please try again."))
            }
            return
        }
        _isBillingBusy.value = true
        queryActivePurchases(emitRestoreEvent = true)
    }

    fun launchPurchaseFlow(activity: Activity, plan: PremiumPlan) {
        if (activity.isFinishing || activity.isDestroyed) return
        val details = productDetailsMap.value[plan]
        if (!billingClient.isReady || details == null) {
            _isBillingBusy.value = true
            startConnection()
            if (billingClient.isReady) {
                querySubscriptionProductDetails()
                queryInAppProductDetails()
            }
            appScope.launch(Dispatchers.Main) {
                var waitedMs = 0L
                while ((
                        !billingClient.isReady ||
                            productDetailsMap.value[plan] == null
                        ) && waitedMs < 3_500L
                ) {
                    delay(100L)
                    waitedMs += 100L
                }
                val readyDetails = productDetailsMap.value[plan]
                if (billingClient.isReady && readyDetails != null && !activity.isFinishing && !activity.isDestroyed) {
                    launchWithProductDetails(activity, readyDetails, plan)
                } else {
                    _isBillingBusy.value = false
                    _events.emit(
                        BillingEvent.BillingError(
                            "Google Play Store is not ready or product is unavailable on this device.",
                        ),
                    )
                }
            }
            return
        }
        launchWithProductDetails(activity, details, plan)
    }

    private fun launchWithProductDetails(
        activity: Activity,
        details: ProductDetails,
        plan: PremiumPlan,
    ) {
        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (plan.productType == BillingClient.ProductType.SUBS) {
            val selectedOffer = selectSubscriptionOffer(details, plan)
            val offerToken = selectedOffer?.offerToken
            if (offerToken.isNullOrBlank()) {
                _isBillingBusy.value = false
                appScope.launch {
                    _events.emit(BillingEvent.BillingError("Subscription offer is unavailable."))
                }
                return
            }
            productParamsBuilder.setOfferToken(offerToken)
        } else {
            val oneTimeToken = selectOneTimeOfferToken(details, plan)
            if (!oneTimeToken.isNullOrBlank()) {
                productParamsBuilder.setOfferToken(oneTimeToken)
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        _isBillingBusy.value = true
        val launchResult = billingClient.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            _isBillingBusy.value = false
            appScope.launch {
                _events.emit(BillingEvent.BillingError(launchResult.debugMessage))
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        _isBillingBusy.value = false
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    handlePurchases(purchases, fromInteractiveFlow = true)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                appScope.launch { _events.emit(BillingEvent.PurchaseCancelled) }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryActivePurchases(emitRestoreEvent = true)
            }
            else -> {
                appScope.launch {
                    _events.emit(BillingEvent.BillingError(billingResult.debugMessage))
                }
            }
        }
    }

    private fun querySubscriptionProductDetails() {
        val products = PremiumPlan.subscriptionProductIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        if (products.isEmpty()) return
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                mergeProductDetails(queryResult.productDetailsList)
            }
        }
    }

    private fun queryInAppProductDetails() {
        val products = PremiumPlan.inAppProductIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        if (products.isEmpty()) return
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                mergeProductDetails(queryResult.productDetailsList)
            }
        }
    }

    private fun mergeProductDetails(detailsList: List<ProductDetails>) {
        if (detailsList.isEmpty()) return
        val updatedDetails = productDetailsMap.value.toMutableMap()
        val updatedPrices = _planPrices.value.toMutableMap()

        for (details in detailsList) {
            val plan = PremiumPlan.fromProductId(details.productId) ?: continue
            updatedDetails[plan] = details
            val formattedPrice = extractFormattedPrice(details, plan)
            if (!formattedPrice.isNullOrBlank()) {
                updatedPrices[plan] = formattedPrice
            }
        }
        productDetailsMap.value = updatedDetails
        _planPrices.value = updatedPrices
    }

    private fun extractFormattedPrice(
        details: ProductDetails,
        plan: PremiumPlan,
    ): String? {
        return if (plan.productType == BillingClient.ProductType.SUBS) {
            val offer = selectSubscriptionOffer(details, plan)
            val paidPhase = offer?.pricingPhases?.pricingPhaseList
                ?.lastOrNull { it.priceAmountMicros > 0L }
                ?: offer?.pricingPhases?.pricingPhaseList?.lastOrNull()
            paidPhase?.formattedPrice
        } else {
            details.oneTimePurchaseOfferDetailsList
                ?.firstOrNull {
                    plan.inAppPurchaseOptionId == null ||
                        it.purchaseOptionId == plan.inAppPurchaseOptionId
                }
                ?.formattedPrice
                ?: details.oneTimePurchaseOfferDetails?.formattedPrice
        }
    }

    private fun selectSubscriptionOffer(
        details: ProductDetails,
        @Suppress("UNUSED_PARAMETER") plan: PremiumPlan,
    ): ProductDetails.SubscriptionOfferDetails? {
        val offers = details.subscriptionOfferDetails.orEmpty()
        if (offers.isEmpty()) return null
        // Always prefer the direct paid base plan offer without any free-trial phase
        return offers.firstOrNull { offer ->
            offer.offerId.isNullOrBlank() &&
                offer.pricingPhases.pricingPhaseList.none { it.priceAmountMicros == 0L }
        } ?: offers.firstOrNull { offer ->
            offer.pricingPhases.pricingPhaseList.none { it.priceAmountMicros == 0L }
        } ?: offers.firstOrNull { offer ->
            offer.offerId.isNullOrBlank()
        } ?: offers.firstOrNull()
    }

    private fun selectOneTimeOfferToken(
        details: ProductDetails,
        plan: PremiumPlan,
    ): String? {
        val list = details.oneTimePurchaseOfferDetailsList.orEmpty()
        if (list.isNotEmpty()) {
            val matched = list.firstOrNull {
                !plan.inAppPurchaseOptionId.isNullOrBlank() &&
                    it.purchaseOptionId == plan.inAppPurchaseOptionId
            } ?: list.firstOrNull()
            return matched?.offerToken
        }
        return details.oneTimePurchaseOfferDetails?.offerToken
    }

    private fun queryActivePurchases(emitRestoreEvent: Boolean) {
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(subsParams) { subsResult, subsPurchases ->
            billingClient.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                _isBillingBusy.value = false
                val combined = buildList {
                    if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        addAll(subsPurchases)
                    }
                    if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        addAll(inAppPurchases)
                    }
                }
                val hasActiveVip = handlePurchases(combined, fromInteractiveFlow = false)
                if (!hasActiveVip &&
                    subsResult.responseCode == BillingClient.BillingResponseCode.OK &&
                    inAppResult.responseCode == BillingClient.BillingResponseCode.OK
                ) {
                    appScope.launch {
                        userPreferencesRepository.setPremium(false)
                        if (emitRestoreEvent) {
                            _events.emit(BillingEvent.NoPurchasesFound)
                        }
                    }
                } else if (hasActiveVip && emitRestoreEvent) {
                    appScope.launch {
                        _events.emit(BillingEvent.PurchaseRestored)
                    }
                }
            }
        }
    }

    private fun handlePurchases(
        purchases: List<Purchase>,
        fromInteractiveFlow: Boolean,
    ): Boolean {
        var entitled = false
        for (purchase in purchases) {
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) continue
            val matchesVipProduct = purchase.products.any { it in PremiumPlan.allProductIds }
            if (!matchesVipProduct) continue
            entitled = true

            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams) { ackResult ->
                    R2Log.i("Play Billing purchase acknowledged: code=${ackResult.responseCode}")
                }
            }
        }

        if (entitled) {
            appScope.launch {
                userPreferencesRepository.setPremium(true)
                if (fromInteractiveFlow) {
                    _events.emit(BillingEvent.PurchaseSucceeded)
                }
            }
        }
        return entitled
    }
}
