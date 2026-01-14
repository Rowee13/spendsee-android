package com.spendsee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "cash", "bank", "creditCard", "ewallet", "savings", "investment", "other"
    var balance: Double,
    val icon: String, // Material icon name
    val colorHex: String = "#007AFF",
    val createdAt: Long = System.currentTimeMillis()
)
