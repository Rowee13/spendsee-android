//
//  NotificationCenterScreen.kt
//  SpendSee
//
//  Created on 2026-02-03.
//

package com.spendsee.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendsee.data.local.entities.AppNotification
import com.spendsee.managers.ThemeManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onNavigateToBudget: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: NotificationCenterViewModel = viewModel(
        factory = NotificationCenterViewModelFactory(context)
    )
    val themeManager = remember { ThemeManager.getInstance(context) }
    val uiState by viewModel.uiState.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(currentTheme.getSurface(isDarkMode))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = currentTheme.getText(isDarkMode)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.getText(isDarkMode)
                )
            }

            if (uiState.notifications.any { !it.isRead }) {
                TextButton(onClick = { viewModel.markAllAsRead() }) {
                    Text(
                        text = "Mark All Read",
                        color = currentTheme.getAccent(isDarkMode)
                    )
                }
            }
        }

        // Content
        if (uiState.notifications.isEmpty()) {
            EmptyNotificationsState(
                modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            NotificationList(
                groupedNotifications = uiState.groupedNotifications,
                onNotificationClick = { notification ->
                    viewModel.markAsRead(notification.id)
                    notification.relatedBudgetId?.let(onNavigateToBudget)
                },
                onDelete = { viewModel.deleteNotification(it) },
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun NotificationList(
    groupedNotifications: Map<String, List<AppNotification>>,
    onNotificationClick: (AppNotification) -> Unit,
    onDelete: (AppNotification) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groupedNotifications.forEach { (dateHeader, notifications) ->
            item {
                DateHeader(dateHeader)
            }

            items(
                items = notifications,
                key = { it.id }
            ) { notification ->
                NotificationRow(
                    notification = notification,
                    onClick = { onNotificationClick(notification) },
                    onDelete = { onDelete(notification) }
                )
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    Text(
        text = date,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = currentTheme.getAccent(isDarkMode),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        background = {
            DismissBackground(dismissState)
        },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = if (!notification.isRead) {
                    CardDefaults.cardColors(
                        containerColor = currentTheme.getAccent(isDarkMode).copy(alpha = 0.1f)
                    )
                } else {
                    CardDefaults.cardColors(
                        containerColor = currentTheme.getSurface(isDarkMode)
                    )
                },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = currentTheme.getAccent(isDarkMode),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                                color = currentTheme.getText(isDarkMode),
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (!notification.isRead) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(currentTheme.getAccent(isDarkMode), CircleShape)
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.getInactive(isDarkMode)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = formatRelativeTime(notification.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getInactive(isDarkMode)
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: DismissState) {
    val color = when (dismissState.dismissDirection) {
        DismissDirection.StartToEnd -> Color.Red.copy(alpha = 0.8f)
        DismissDirection.EndToStart -> Color.Red.copy(alpha = 0.8f)
        null -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = when (dismissState.dismissDirection) {
            DismissDirection.StartToEnd -> Alignment.CenterStart
            DismissDirection.EndToStart -> Alignment.CenterEnd
            null -> Alignment.Center
        }
    ) {
        if (dismissState.dismissDirection != null) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.White
            )
        }
    }
}

@Composable
fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = currentTheme.getInactive(isDarkMode).copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "No Notifications",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = currentTheme.getText(isDarkMode),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "You're all caught up!",
            style = MaterialTheme.typography.bodyMedium,
            color = currentTheme.getInactive(isDarkMode),
            textAlign = TextAlign.Center
        )
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        }
        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "$days ${if (days == 1L) "day" else "days"} ago"
        }
        else -> {
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_YEAR)
            val todayYear = calendar.get(Calendar.YEAR)

            calendar.timeInMillis = timestamp
            val notificationDay = calendar.get(Calendar.DAY_OF_YEAR)
            val notificationYear = calendar.get(Calendar.YEAR)

            when {
                notificationYear == todayYear && notificationDay == today - 1 -> "Yesterday"
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }
}
