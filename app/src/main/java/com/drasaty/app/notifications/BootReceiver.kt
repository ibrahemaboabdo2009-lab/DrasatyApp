package com.drasaty.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drasaty.app.db.DrasatyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * بعد رستارت الجهاز، يعيد جدولة المنبهات
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DrasatyDatabase.getDatabase(context)
                val tasks = db.taskDao().getAll()
                tasks.filter { !it.isCompleted && it.dueDateMillis > System.currentTimeMillis() }
                    .forEach { NotificationsHelper.scheduleTaskReminder(context, it) }

                val schedule = db.scheduleDao().getAll()
                schedule.forEach {
                    NotificationsHelper.scheduleClassReminder(
                        context, it.dayOfWeek, it.startTime, it.subjectName, it.id
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}