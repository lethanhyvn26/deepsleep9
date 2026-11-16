package com.sleepsounds.storytelling.presentation.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.data.billing.BillingEvent
import com.sleepsounds.storytelling.data.billing.BillingRepository
import com.sleepsounds.storytelling.data.billing.PremiumPlan
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PaywallUiState(
    val selectedPlan: PremiumPlan = PremiumPlan.YEARLY,
    val planPrices: Map<PremiumPlan, String> = PremiumPlan.entries.associateWith { it.fallbackPrice },
    val isBillingBusy: Boolean = false,
    val isPremium: Boolean = false,
    val isPaidSubscriber: Boolean = false,
    val rewardedWatchCount: Int = 0,
    val rewardedAdFreeUntilMs: Long = 0L,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    val adMobManager: AdMobManager,
) : ViewModel() {

    private val selectedPlanFlow = MutableStateFlow(PremiumPlan.YEARLY)

    val events: SharedFlow<BillingEvent> = billingRepository.events

    val uiState: StateFlow<PaywallUiState> = combine(
        selectedPlanFlow,
        billingRepository.planPrices,
        billingRepository.isBillingBusy,
        userPreferencesRepository.isPremium,
        combine(
            userPreferencesRepository.isPaidSubscriber,
            userPreferencesRepository.rewardedAdWatchCount,
            userPreferencesRepository.rewardedAdFreeUntilMs,
        ) { paid, count, untilMs -> Triple(paid, count, untilMs) },
    ) { plan, prices, busy, premium, (paid, count, untilMs) ->
        PaywallUiState(
            selectedPlan = plan,
            planPrices = prices,
            isBillingBusy = busy,
            isPremium = premium,
            isPaidSubscriber = paid,
            rewardedWatchCount = count,
            rewardedAdFreeUntilMs = untilMs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaywallUiState())

    init {
        billingRepository.startConnection()
    }

    fun selectPlan(plan: PremiumPlan) {
        selectedPlanFlow.value = plan
    }

    fun purchaseSelectedPlan(activity: Activity) {
        billingRepository.launchPurchaseFlow(activity, selectedPlanFlow.value)
    }

    fun restorePurchases() {
        billingRepository.restorePurchases()
    }
}
