package com.spendsee.data.models

enum class Currency(val symbol: String, val code: String, val decimals: Int) {
    USD("$", "USD", 2),
    PHP("₱", "PHP", 2),
    EUR("€", "EUR", 2),
    GBP("£", "GBP", 2),
    JPY("¥", "JPY", 0),
    CNY("¥", "CNY", 2),
    INR("₹", "INR", 2),
    AUD("A$", "AUD", 2),
    CAD("C$", "CAD", 2),
    CHF("CHF", "CHF", 2),
    SEK("kr", "SEK", 2),
    NOK("kr", "NOK", 2),
    DKK("kr", "DKK", 2),
    SGD("S$", "SGD", 2),
    HKD("HK$", "HKD", 2),
    NZD("NZ$", "NZD", 2),
    KRW("₩", "KRW", 0),
    MXN("$", "MXN", 2),
    BRL("R$", "BRL", 2),
    ZAR("R", "ZAR", 2),
    RUB("₽", "RUB", 2),
    TRY("₺", "TRY", 2),
    THB("฿", "THB", 2),
    IDR("Rp", "IDR", 0);

    companion object {
        val DEFAULT = USD

        fun from(code: String): Currency {
            return entries.find { it.code == code } ?: DEFAULT
        }
    }
}
