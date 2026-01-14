package com.spendsee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val month: Int, // 1-12
    val year: Int,
    val isRecurring: Boolean = false,
    val dueDate: Long? = null,
    val isPaid: Boolean = false,
    val paidDate: Long? = null,
    val notificationId: String? = null,
    val notifyDaysBefore: Int = 5,
    val createdAt: Long = System.currentTimeMillis()
)
