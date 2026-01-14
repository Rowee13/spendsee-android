package com.spendsee.utils

data class Currency(
    val code: String,
    val symbol: String,
    val name: String
) {
    companion object {
        val USD = Currency("USD", "$", "US Dollar")
        val EUR = Currency("EUR", "€", "Euro")
        val GBP = Currency("GBP", "£", "British Pound")
        val JPY = Currency("JPY", "¥", "Japanese Yen")
        val CNY = Currency("CNY", "¥", "Chinese Yuan")
        val AUD = Currency("AUD", "A$", "Australian Dollar")
        val CAD = Currency("CAD", "C$", "Canadian Dollar")
        val CHF = Currency("CHF", "Fr", "Swiss Franc")
        val INR = Currency("INR", "₹", "Indian Rupee")
        val PHP = Currency("PHP", "₱", "Philippine Peso")
        val KRW = Currency("KRW", "₩", "South Korean Won")
        val SGD = Currency("SGD", "S$", "Singapore Dollar")
        val HKD = Currency("HKD", "HK$", "Hong Kong Dollar")
        val NZD = Currency("NZD", "NZ$", "New Zealand Dollar")
        val MXN = Currency("MXN", "Mex$", "Mexican Peso")
        val BRL = Currency("BRL", "R$", "Brazilian Real")
        val ZAR = Currency("ZAR", "R", "South African Rand")
        val RUB = Currency("RUB", "₽", "Russian Ruble")
        val SEK = Currency("SEK", "kr", "Swedish Krona")
        val NOK = Currency("NOK", "kr", "Norwegian Krone")
        val DKK = Currency("DKK", "kr", "Danish Krone")
        val PLN = Currency("PLN", "zł", "Polish Zloty")
        val THB = Currency("THB", "฿", "Thai Baht")
        val MYR = Currency("MYR", "RM", "Malaysian Ringgit")
        val IDR = Currency("IDR", "Rp", "Indonesian Rupiah")
        val VND = Currency("VND", "₫", "Vietnamese Dong")
        val TRY = Currency("TRY", "₺", "Turkish Lira")
        val AED = Currency("AED", "د.إ", "UAE Dirham")
        val SAR = Currency("SAR", "﷼", "Saudi Riyal")

        val ALL_CURRENCIES = listOf(
            USD, EUR, GBP, JPY, CNY, AUD, CAD, CHF, INR, PHP,
            KRW, SGD, HKD, NZD, MXN, BRL, ZAR, RUB, SEK, NOK,
            DKK, PLN, THB, MYR, IDR, VND, TRY, AED, SAR
        )

        fun fromCode(code: String): Currency {
            return ALL_CURRENCIES.find { it.code == code } ?: USD
        }
    }
}
