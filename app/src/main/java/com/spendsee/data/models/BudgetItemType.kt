package com.spendsee.data.models

enum class BudgetItemType(val value: String) {
    EXPECTED("Expected"),
    UNPLANNED("Unplanned");

    companion object {
        fun from(value: String): BudgetItemType {
            return entries.find { it.value == value } ?: EXPECTED
        }
    }
}
