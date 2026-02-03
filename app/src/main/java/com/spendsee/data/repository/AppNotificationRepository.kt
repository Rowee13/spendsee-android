//
//  AppNotificationRepository.kt
//  SpendSee
//
//  Created on 2026-02-03.
//

package com.spendsee.data.repository

import android.content.Context
import com.spendsee.data.local.SpendSeeDatabase
import com.spendsee.data.local.dao.AppNotificationDao
import com.spendsee.data.local.entities.AppNotification
import kotlinx.coroutines.flow.Flow

class AppNotificationRepository private constructor(
    private val appNotificationDao: AppNotificationDao
) {
    fun getAllNotifications(): Flow<List<AppNotification>> = appNotificationDao.getAllFlow()

    fun getUnreadNotifications(): Flow<List<AppNotification>> = appNotificationDao.getUnreadFlow()

    fun getUnreadCount(): Flow<Int> = appNotificationDao.getUnreadCountFlow()

    suspend fun markAsRead(notificationId: String) {
        appNotificationDao.markAsRead(notificationId)
    }

    suspend fun markAllAsRead() {
        appNotificationDao.markAllAsRead()
    }

    suspend fun insertNotification(notification: AppNotification) {
        appNotificationDao.insert(notification)
    }

    suspend fun deleteNotification(notification: AppNotification) {
        appNotificationDao.delete(notification)
    }

    companion object {
        @Volatile
        private var instance: AppNotificationRepository? = null

        fun getInstance(context: Context): AppNotificationRepository {
            return instance ?: synchronized(this) {
                instance ?: AppNotificationRepository(
                    SpendSeeDatabase.getDatabase(context).appNotificationDao()
                ).also { instance = it }
            }
        }
    }
}
