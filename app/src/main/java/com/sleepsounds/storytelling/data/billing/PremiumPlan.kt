package com.sleepsounds.storytelling.data.billing

import com.android.billingclient.api.BillingClient

/**
 * Google Play Console product catalog for Deep Sleep VIP plans:
 * - [MONTHLY]: Subscription (`somni_premium_monthly`)
 * - [YEARLY]: Subscription (`somni_premium_yearly`)
 * - [LIFETIME]: One-time In-App Purchase (`somni_premium_lifetime`, purchase option `lifetime-buy`)
 */
enum class PremiumPlan(
    val productId: String,
    val productType: String,
    val fallbackPrice: String,
    val inAppPurchaseOptionId: String? = null,
) {
    MONTHLY(
        productId = "somni_premium_monthly",
        productType = BillingClient.ProductType.SUBS,
        fallbackPrice = "$14.99",
    ),
    YEARLY(
        productId = "somni_premium_yearly",
        productType = BillingClient.ProductType.SUBS,
        fallbackPrice = "$89.99",
    ),
    LIFETIME(
        productId = "somni_premium_lifetime",
        productType = BillingClient.ProductType.INAPP,
        fallbackPrice = "$149.00",
        inAppPurchaseOptionId = "lifetime-buy",
    );

    companion object {
        val subscriptionProductIds: List<String> = entries
            .filter { it.productType == BillingClient.ProductType.SUBS }
            .map { it.productId }

        val inAppProductIds: List<String> = entries
            .filter { it.productType == BillingClient.ProductType.INAPP }
            .map { it.productId }

        val allProductIds: Set<String> = entries.map { it.productId }.toSet()

        fun fromProductId(productId: String): PremiumPlan? =
            entries.firstOrNull { it.productId == productId }
    }
}
