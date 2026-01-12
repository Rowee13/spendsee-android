package com.spendsee.data.models

enum class TransactionType(val value: String) {
    INCOME("income"),
    EXPENSE("expense"),
    TRANSFER("transfer");

    companion object {
        fun from(value: String): TransactionType {
            return entries.find { it.value == value } ?: EXPENSE
        }
    }
}
