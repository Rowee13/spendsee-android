package com.spendsee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "budget_items")
data class BudgetItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val budgetId: String,
    val name: String,
    val amount: Double,
    val note: String = "",
    val type: String = "Expected", // "Expected" or "Unplanned"
    val createdAt: Long = System.currentTimeMillis()
)
