//
//  NotificationCenterViewModel.kt
//  SpendSee
//
//  Created on 2026-02-03.
//

package com.spendsee.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsee.data.local.entities.AppNotification
import com.spendsee.data.repository.AppNotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NotificationCenterUiState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val groupedNotifications: Map<String, List<AppNotification>>
        get() = notifications.groupBy { notification ->
            formatDateHeader(notification.createdAt)
        }

    private fun formatDateHeader(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        val notificationDay = calendar.get(Calendar.DAY_OF_YEAR)
        val notificationYear = calendar.get(Calendar.YEAR)

        return when {
            notificationYear == todayYear && notificationDay == today -> "Today"
            notificationYear == todayYear && notificationDay == today - 1 -> "Yesterday"
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }
}

class NotificationCenterViewModel(
    private val repository: AppNotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationCenterUiState())
    val uiState: StateFlow<NotificationCenterUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            repository.getAllNotifications()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { notifications ->
                    _uiState.update { it.copy(notifications = notifications, isLoading = false) }
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(notification: AppNotification) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }
}
