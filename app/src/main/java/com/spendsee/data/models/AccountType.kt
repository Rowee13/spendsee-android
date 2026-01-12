package com.spendsee.data.models

enum class AccountType(val value: String, val displayName: String) {
    CASH("cash", "Cash"),
    BANK("bank", "Bank Account"),
    CREDIT_CARD("creditCard", "Credit Card"),
    EWALLET("ewallet", "E-Wallet"),
    SAVINGS("savings", "Savings"),
    INVESTMENT("investment", "Investment"),
    OTHER("other", "Other");

    companion object {
        fun from(value: String): AccountType {
            return entries.find { it.value == value } ?: OTHER
        }
    }
}
