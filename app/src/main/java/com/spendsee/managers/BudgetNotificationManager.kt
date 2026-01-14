package com.spendsee.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.spendsee.MainActivity
import com.spendsee.data.local.entities.Budget
import java.util.*
import java.util.concurrent.TimeUnit

class BudgetNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val workManager = WorkManager.getInstance(context)
    private val prefs = context.getSharedPreferences("notification_preferences", Context.MODE_PRIVATE)

    companion object {
        const val CHANNEL_ID = "budget_reminders"
        const val CHANNEL_NAME = "Budget Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for upcoming budget payments"

        private const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val PREF_DAYS_BEFORE = "days_before_due_date"
        private const val PREF_NOTIFICATION_HOUR = "notification_hour"

        @Volatile
        private var instance: BudgetNotificationManager? = null

        fun getInstance(context: Context): BudgetNotificationManager {
            return instance ?: synchronized(this) {
                instance ?: BudgetNotificationManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    // Notification Preferences
    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getDaysBeforeDueDate(): Int {
        return prefs.getInt(PREF_DAYS_BEFORE, 5)
    }

    fun setDaysBeforeDueDate(days: Int) {
        prefs.edit().putInt(PREF_DAYS_BEFORE, days).apply()
    }

    fun getNotificationHour(): Int {
        return prefs.getInt(PREF_NOTIFICATION_HOUR, 9)
    }

    fun setNotificationHour(hour: Int) {
        prefs.edit().putInt(PREF_NOTIFICATION_HOUR, hour).apply()
    }

    // Schedule notification for a budget
    fun scheduleBudgetNotification(budget: Budget) {
        // Only schedule if notifications are enabled and budget has a due date
        if (!areNotificationsEnabled() || budget.dueDate == null) {
            return
        }

        // Cancel any existing notification for this budget
        if (!budget.notificationId.isNullOrEmpty()) {
            cancelNotification(budget.notificationId)
        }

        // Calculate notification date
        val notificationDate = calculateNotificationDate(
            budget.dueDate,
            budget.notifyDaysBefore ?: getDaysBeforeDueDate()
        )

        // Don't schedule if notification date is in the past
        if (notificationDate <= System.currentTimeMillis()) {
            return
        }

        // Calculate delay
        val delay = notificationDate - System.currentTimeMillis()

        // Create work request
        val workRequestId = UUID.randomUUID()
        val data = workDataOf(
            "budget_id" to budget.id,
            "budget_name" to budget.name,
            "due_date" to budget.dueDate
        )

        val workRequest = OneTimeWorkRequestBuilder<BudgetNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("budget_notification_${budget.id}")
            .build()

        workManager.enqueueUniqueWork(
            "budget_notification_${budget.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        // Note: The workRequestId should be saved to the budget entity
        // This is handled in the repository/viewmodel layer
    }

    fun cancelNotification(notificationId: String) {
        try {
            workManager.cancelWorkById(UUID.fromString(notificationId))
        } catch (e: IllegalArgumentException) {
            // Invalid UUID, ignore
        }
    }

    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag("budget_notification")
    }

    private fun calculateNotificationDate(dueDate: Long, daysBefore: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dueDate

        // Set notification time
        val notificationHour = getNotificationHour()
        calendar.set(Calendar.HOUR_OF_DAY, notificationHour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Go back the specified number of working days
        var workingDaysCount = 0
        while (workingDaysCount < daysBefore) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            if (isWorkingDay(calendar)) {
                workingDaysCount++
            }
        }

        return calendar.timeInMillis
    }

    private fun isWorkingDay(calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
    }

    fun createPendingIntent(budgetId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "budgets")
            putExtra("budget_id", budgetId)
        }

        return PendingIntent.getActivity(
            context,
            budgetId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
