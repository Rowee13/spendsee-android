package com.spendsee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: String, // "income", "expense", "transfer"
    val category: String,
    val date: Long, // Unix timestamp in milliseconds
    val notes: String = "",
    val accountId: String? = null,
    val toAccountId: String? = null, // For transfers
    val budgetId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
