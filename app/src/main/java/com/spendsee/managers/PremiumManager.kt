package com.spendsee.managers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PremiumManager(private val context: Context) {

    private val billingClient: BillingClient
    private val prefs: SharedPreferences = context.getSharedPreferences("premium", Context.MODE_PRIVATE)

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    companion object {
        const val PRODUCT_ID = "com.spendsee.premium"
        private const val PREF_DEVELOPER_MODE = "developer_premium_override"

        @Volatile
        private var INSTANCE: PremiumManager? = null

        fun getInstance(context: Context): PremiumManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PremiumManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Loading : PurchaseState()
        object Success : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Error("Purchase cancelled")
            }
            else -> {
                _purchaseState.value = PurchaseState.Error("Purchase failed: ${billingResult.debugMessage}")
            }
        }
    }

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        // Check developer mode first
        if (isDeveloperModeEnabled()) {
            _isPremium.value = true
        } else {
            startConnection()
        }
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to reconnect
                startConnection()
            }
        })
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any {
                    it.products.contains(PRODUCT_ID) &&
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _isPremium.value = hasPremium || isDeveloperModeEnabled()
            }
        }
    }

    suspend fun purchase(activity: Activity): Boolean {
        _purchaseState.value = PurchaseState.Loading

        return suspendCancellableCoroutine { continuation ->
            // Query product details
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                    productDetailsList.isNotEmpty()
                ) {
                    val productDetails = productDetailsList[0]

                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)

                    if (launchResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(true)
                    } else {
                        _purchaseState.value = PurchaseState.Error("Failed to launch purchase flow")
                        continuation.resume(false)
                    }
                } else {
                    _purchaseState.value = PurchaseState.Error("Product not found")
                    continuation.resume(false)
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _isPremium.value = true
                        _purchaseState.value = PurchaseState.Success
                    }
                }
            } else {
                _isPremium.value = true
                _purchaseState.value = PurchaseState.Success
            }
        }
    }

    fun restorePurchases() {
        queryPurchases()
    }

    fun hasFeature(feature: Feature): Boolean {
        return if (feature.isPremium) {
            _isPremium.value
        } else {
            true
        }
    }

    // Developer Mode
    fun isDeveloperModeEnabled(): Boolean {
        return prefs.getBoolean(PREF_DEVELOPER_MODE, false)
    }

    fun setDeveloperMode(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_DEVELOPER_MODE, enabled).apply()
        _isPremium.value = enabled || checkActualPremiumStatus()
    }

    private fun checkActualPremiumStatus(): Boolean {
        // This will be populated by queryPurchases
        return false
    }

    fun destroy() {
        billingClient.endConnection()
    }
}
