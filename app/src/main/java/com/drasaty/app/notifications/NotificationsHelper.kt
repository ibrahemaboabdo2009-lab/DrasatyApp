package com.drasaty.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.drasaty.app.R
import com.drasaty.app.db.TaskEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationsHelper {

    const val CHANNEL_ID = "drasaty_reminders"
    const val TASK_NOTIF_BASE_ID = 1000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_desc)
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showTaskNotification(context: Context, task: TaskEntry) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tasks)
            .setContentTitle(context.getString(R.string.notif_task_reminder))
            .setContentText(task.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(buildBigText(task)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(TASK_NOTIF_BASE_ID + task.id.toInt(), builder.build())
    }

    private fun buildBigText(task: TaskEntry): String {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale("ar"))
        val priorityLabel = when (task.priority) {
            1 -> "أولوية عالية ⚠️"
            2 -> "أولوية متوسطة"
            else -> "أولوية منخفضة"
        }
        return "${task.title}\nالمادة: ${task.subject}\nالتسليم: ${dateFmt.format(Date(task.dueDateMillis))}\n$priorityLabel"
    }

    fun scheduleTaskReminder(context: Context, task: TaskEntry) {
        // ذكرى قبل الموعد بـ 24 ساعة (لو الوقت فات، نضبطها بعد ساعة من الآن)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_subject", task.subject)
            putExtra("task_priority", task.priority)
            putExtra("task_due", task.dueDateMillis)
        }

        val pi = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // لو الموعد بعد أكتر من 24 ساعة، فكّره قبلها بـ 24 ساعة
        // لو الموعد أقل من 24 ساعة، فكّره بعد ساعة
        val now = System.currentTimeMillis()
        val oneHour = 60L * 60 * 1000
        val oneDay = 24 * oneHour
        val triggerAt = when {
            task.dueDateMillis - now > oneDay -> task.dueDateMillis - oneDay
            task.dueDateMillis > now -> now + oneHour
            else -> now + 30 * 60 * 1000L // 30 دقيقة لو الموعد فات
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
    }

    fun scheduleClassReminder(context: Context, dayOfWeek: Int, startTime: String, subjectName: String, entryId: Long) {
        // تذكير قبل الحصة بـ 10 دقايق
        val (hour, minute) = parseTime(startTime) ?: return
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, mapDayOfWeek(dayOfWeek))
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            add(Calendar.MINUTE, -10)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 7)
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("type", "class")
            putExtra("subject", subjectName)
            putExtra("entry_id", entryId)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            (entryId * 100).toInt() + dayOfWeek,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pi
            )
        } catch (e: SecurityException) {
            // fallback
        }
    }

    private fun parseTime(time: String): Pair<Int, Int>? {
        return try {
            val parts = time.split(":")
            Pair(parts[0].trim().toInt(), parts[1].trim().toInt())
        } catch (e: Exception) {
            null
        }
    }

    private fun mapDayOfWeek(day: Int): Int {
        // يوم 0 = السبت، يتحول لقيمة Calendar.SATURDAY
        return when (day) {
            0 -> Calendar.SATURDAY
            1 -> Calendar.SUNDAY
            2 -> Calendar.MONDAY
            3 -> Calendar.TUESDAY
            4 -> Calendar.WEDNESDAY
            5 -> Calendar.THURSDAY
            6 -> Calendar.FRIDAY
            else -> Calendar.SATURDAY
        }
    }
}