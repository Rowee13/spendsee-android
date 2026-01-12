package com.spendsee.data.models

enum class CategoryType(val value: String) {
    INCOME("income"),
    EXPENSE("expense");

    companion object {
        fun from(value: String): CategoryType {
            return entries.find { it.value == value } ?: EXPENSE
        }
    }
}
