package com.drasaty.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان الحصة الدراسية
 * dayOfWeek: 0 = السبت, 1 = الأحد, ... 6 = الجمعة
 */
@Entity(tableName = "schedule")
data class ScheduleEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val dayOfWeek: Int,
    val startTime: String, // بصيغة HH:mm
    val endTime: String,
    val room: String = ""
)

/**
 * كيان المهمة
 */
@Entity(tableName = "task")
data class TaskEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val dueDateMillis: Long,
    val priority: Int, // 1=عالي, 2=متوسط, 3=منخفض
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * كيان جلسة البومودورو
 */
@Entity(tableName = "pomodoro_session")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val durationMinutes: Int,
    val type: String // "work" or "break"
)