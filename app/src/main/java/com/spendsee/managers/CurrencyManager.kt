package com.spendsee.managers

import android.content.Context
import com.spendsee.utils.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrencyManager private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    private val _selectedCurrency = MutableStateFlow(loadCurrency())
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()

    private fun loadCurrency(): Currency {
        val code = prefs.getString(KEY_CURRENCY_CODE, Currency.USD.code) ?: Currency.USD.code
        return Currency.fromCode(code)
    }

    fun setCurrency(currency: Currency) {
        prefs.edit().putString(KEY_CURRENCY_CODE, currency.code).apply()
        _selectedCurrency.value = currency
    }

    fun formatAmount(amount: Double): String {
        val currency = _selectedCurrency.value
        return "${currency.symbol}${String.format("%.2f", amount)}"
    }

    companion object {
        private const val KEY_CURRENCY_CODE = "selected_currency_code"

        @Volatile
        private var instance: CurrencyManager? = null

        fun getInstance(context: Context): CurrencyManager {
            return instance ?: synchronized(this) {
                instance ?: CurrencyManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
