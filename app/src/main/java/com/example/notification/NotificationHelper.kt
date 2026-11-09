package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) {
  companion object {
    const val CHANNEL_BUDGET = "budget_alerts_channel"
    const val CHANNEL_RECURRING = "recurring_alerts_channel"
    private const val NOTIFICATION_ID_BUDGET_BASE = 1000
    private const val NOTIFICATION_ID_RECURRING_BASE = 2000
  }

  init {
    createNotificationChannels()
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      val budgetChannel = NotificationChannel(
        CHANNEL_BUDGET,
        "Budget & Milestone Alerts",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts when approaching or exceeding monthly spending limits"
        enableVibration(true)
      }

      val recurringChannel = NotificationChannel(
        CHANNEL_RECURRING,
        "Recurring Bill Reminders",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Reminders for recurring subscriptions and upcoming payments"
        enableVibration(true)
      }

      notificationManager.createNotificationChannel(budgetChannel)
      notificationManager.createNotificationChannel(recurringChannel)
    }
  }

  fun sendBudgetAlert(categoryName: String, percent: Int, spent: Double, limit: Double, currencySymbol: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val isOver = percent >= 100
    val title = if (isOver) {
      "⚠️ Over Budget Alert: $categoryName"
    } else {
      "📊 Budget Warning ($percent%): $categoryName"
    }

    val message = if (isOver) {
      "You have exceeded your $categoryName budget! Spent $currencySymbol${String.format("%.2f", spent)} of $currencySymbol${String.format("%.2f", limit)}."
    } else {
      "You have reached $percent% of your $categoryName budget ($currencySymbol${String.format("%.2f", spent)} / $currencySymbol${String.format("%.2f", limit)})."
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET)
      .setSmallIcon(android.R.drawable.ic_dialog_alert)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)

    try {
      val manager = NotificationManagerCompat.from(context)
      manager.notify(NOTIFICATION_ID_BUDGET_BASE + (categoryName.hashCode() % 100), builder.build())
    } catch (_: SecurityException) {
      // Gracefully ignore if user denied notifications
    }
  }

  fun sendRecurringBillReminder(title: String, amount: Double, currencySymbol: String, daysUntilDue: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val dueText = if (daysUntilDue <= 0) "is due today!" else "is due in $daysUntilDue day${if (daysUntilDue > 1) "s" else ""}."
    val message = "Upcoming payment: $currencySymbol${String.format("%.2f", amount)} for $title $dueText"

    val builder = NotificationCompat.Builder(context, CHANNEL_RECURRING)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle("🔔 Upcoming Bill: $title")
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)

    try {
      val manager = NotificationManagerCompat.from(context)
      manager.notify(NOTIFICATION_ID_RECURRING_BASE + (title.hashCode() % 100), builder.build())
    } catch (_: SecurityException) {
      // Gracefully ignore
    }
  }

  fun sendMilestoneNotification(title: String, message: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle("🎯 $title")
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)

    try {
      val manager = NotificationManagerCompat.from(context)
      manager.notify(9999, builder.build())
    } catch (_: SecurityException) {
      // Gracefully ignore
    }
  }
}
