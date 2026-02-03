//
//  AppNotification.kt
//  SpendSee
//
//  Created on 2026-02-03.
//

package com.spendsee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: String, // "budgetReminder", "paymentDue", etc.
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val isRead: Boolean = false,
    val actionType: String? = null, // "navigateToBudget"
    val relatedBudgetId: String? = null
)
