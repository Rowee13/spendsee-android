//
//  AppNotificationDao.kt
//  SpendSee
//
//  Created on 2026-02-03.
//

package com.spendsee.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendsee.data.local.entities.AppNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<AppNotification>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadFlow(): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCountFlow(): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String, readAt: Long = System.currentTimeMillis())

    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt")
    suspend fun markAllAsRead(readAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotification)

    @Delete
    suspend fun delete(notification: AppNotification)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
