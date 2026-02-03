package com.spendsee.managers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spendsee.R
import com.spendsee.data.local.entities.AppNotification
import com.spendsee.data.repository.AppNotificationRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class BudgetNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val budgetId = inputData.getString("budget_id") ?: return Result.failure()
        val budgetName = inputData.getString("budget_name") ?: "Budget"
        val dueDate = inputData.getLong("due_date", 0L)

        // Create notification
        val notificationManager = BudgetNotificationManager.getInstance(context)
        val pendingIntent = notificationManager.createPendingIntent(budgetId)

        val dueDateText = if (dueDate > 0) {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            dateFormat.format(Date(dueDate))
        } else {
            "soon"
        }

        val notification = NotificationCompat.Builder(context, BudgetNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo) // Using app logo as notification icon
            .setContentTitle("Budget Payment Due")
            .setContentText("$budgetName is due $dueDateText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Don't forget: $budgetName is coming due on $dueDateText. Tap to view details.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        systemNotificationManager.notify(budgetId.hashCode(), notification)

        // Save notification to database for in-app notification center
        val repository = AppNotificationRepository.getInstance(context)
        val appNotification = AppNotification(
            title = "Budget Payment Due",
            message = "Your budget '$budgetName' is due on $dueDateText",
            type = "budgetReminder-$budgetId",
            actionType = "navigateToBudget",
            relatedBudgetId = budgetId
        )

        // Check if notification already exists (avoid duplicates)
        val existingNotifications = repository.getAllNotifications().first()
        val isDuplicate = existingNotifications.any { it.type == appNotification.type }

        if (!isDuplicate) {
            repository.insertNotification(appNotification)
        }

        return Result.success()
    }
}
