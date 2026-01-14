package com.spendsee.managers

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class BudgetProgressData(
    val name: String,
    val spent: Double,
    val planned: Double,
    val percentage: Float
)

@Serializable
data class WidgetData(
    val totalBalance: Double,
    val accountsCount: Int,
    val monthlyExpenses: Double,
    val monthlyIncome: Double,
    val monthlyNet: Double,
    val budgets: List<BudgetProgressData>,
    val selectedCurrency: String,
    val currencySymbol: String,
    val isPremium: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

object WidgetDataManager {
    private const val PREFS_NAME = "widget_data"
    private const val KEY_WIDGET_DATA = "data"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun getWidgetData(context: Context): WidgetData? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_WIDGET_DATA, null) ?: return null

        return try {
            json.decodeFromString<WidgetData>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    fun updateWidgetData(context: Context, data: WidgetData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = json.encodeToString(data)
        prefs.edit().putString(KEY_WIDGET_DATA, jsonString).apply()
    }

    fun refreshWidgetData(context: Context) {
        // This will be called from ViewModels when data changes
        val accountRepository = com.spendsee.data.repository.AccountRepository.getInstance(context)
        val transactionRepository = com.spendsee.data.repository.TransactionRepository.getInstance(context)
        val budgetRepository = com.spendsee.data.repository.BudgetRepository.getInstance(context)
        val premiumManager = PremiumManager.getInstance(context)
        val currencyManager = CurrencyManager.getInstance(context)

        // Note: This is synchronous for simplicity. In production, you'd want to use coroutines.
        try {
            // Get all accounts synchronously (you'll need to add getAllSync() methods to repositories)
            // For now, we'll store empty/default data
            // This will be populated properly when we integrate with the repositories

            val data = WidgetData(
                totalBalance = 0.0,
                accountsCount = 0,
                monthlyExpenses = 0.0,
                monthlyIncome = 0.0,
                monthlyNet = 0.0,
                budgets = emptyList(),
                selectedCurrency = "USD",
                currencySymbol = "$",
                isPremium = false
            )

            updateWidgetData(context, data)
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    fun formatCurrency(amount: Double, symbol: String): String {
        return if (amount >= 0) {
            "$symbol${String.format("%.2f", amount)}"
        } else {
            "-$symbol${String.format("%.2f", Math.abs(amount))}"
        }
    }

    fun getMonthYearText(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }
}
