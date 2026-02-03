//
//  NotificationCenterViewModelFactory.kt
//  SpendSee
//
//  Created on 2026-02-03.
//

package com.spendsee.ui.screens.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spendsee.data.repository.AppNotificationRepository

class NotificationCenterViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationCenterViewModel::class.java)) {
            return NotificationCenterViewModel(
                AppNotificationRepository.getInstance(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
