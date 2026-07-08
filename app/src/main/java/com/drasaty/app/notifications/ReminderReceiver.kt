package com.drasaty.app.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.drasaty.app.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationsHelper.createChannel(context)

        val type = intent.getStringExtra("type")
        val notifId: Int

        if (type == "class") {
            val subject = intent.getStringExtra("subject") ?: "حصة دراسية"
            val builder = NotificationCompat.Builder(context, NotificationsHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_schedule)
                .setContentTitle("🔔 تذكير بحصة")
                .setContentText("حصة $subject بعد 10 دقايق!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 200, 500))
            notifId = (intent.getLongExtra("entry_id", 0) * 100).toInt()
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.notify(notifId, builder.build())
        } else {
            val taskId = intent.getLongExtra("task_id", 0)
            val title = intent.getStringExtra("task_title") ?: ""
            val subject = intent.getStringExtra("task_subject") ?: ""
            val priority = intent.getIntExtra("task_priority", 2)
            val priorityLabel = when (priority) {
                1 -> "⚠️ أولوية عالية"
                else -> ""
            }
            val builder = NotificationCompat.Builder(context, NotificationsHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_tasks)
                .setContentTitle("📝 تذكير بمهمة")
                .setContentText("$title - $subject $priorityLabel".trim())
                .setStyle(NotificationCompat.BigTextStyle().bigText("$title\nالمادة: $subject\n$priorityLabel"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 200, 500))
            notifId = NotificationsHelper.TASK_NOTIF_BASE_ID + taskId.toInt()
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.notify(notifId, builder.build())
        }
    }
}