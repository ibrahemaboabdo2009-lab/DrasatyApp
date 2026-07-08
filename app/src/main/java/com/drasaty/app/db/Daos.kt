package com.drasaty.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule ORDER BY dayOfWeek, startTime")
    suspend fun getAll(): List<ScheduleEntry>

    @Query("SELECT * FROM schedule WHERE dayOfWeek = :day ORDER BY startTime")
    suspend fun getByDay(day: Int): List<ScheduleEntry>

    @Insert
    suspend fun insert(entry: ScheduleEntry): Long

    @Update
    suspend fun update(entry: ScheduleEntry)

    @Query("DELETE FROM schedule WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM schedule")
    suspend fun deleteAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task ORDER BY isCompleted ASC, dueDateMillis ASC")
    suspend fun getAll(): List<TaskEntry>

    @Query("SELECT * FROM task WHERE isCompleted = 0 AND dueDateMillis >= :startOfDay AND dueDateMillis < :endOfDay ORDER BY priority ASC, dueDateMillis ASC")
    suspend fun getTodayPending(startOfDay: Long, endOfDay: Long): List<TaskEntry>

    @Query("SELECT * FROM task WHERE isCompleted = 0 AND dueDateMillis > :now ORDER BY priority ASC, dueDateMillis ASC")
    suspend fun getUpcoming(now: Long): List<TaskEntry>

    @Query("SELECT * FROM task WHERE isCompleted = 1 ORDER BY dueDateMillis DESC")
    suspend fun getCompleted(): List<TaskEntry>

    @Query("SELECT COUNT(*) FROM task WHERE isCompleted = 0")
    suspend fun countPending(): Int

    @Query("SELECT COUNT(*) FROM task")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM task WHERE isCompleted = 1")
    suspend fun countCompleted(): Int

    @Insert
    suspend fun insert(task: TaskEntry): Long

    @Update
    suspend fun update(task: TaskEntry)

    @Query("DELETE FROM task WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM task WHERE isCompleted = 1")
    suspend fun deleteCompleted()

    @Query("DELETE FROM task")
    suspend fun deleteAll()
}

@Dao
interface PomodoroDao {
    @Query("SELECT * FROM pomodoro_session WHERE date = :date AND type = 'work'")
    suspend fun getTodaySessions(date: String): List<PomodoroSession>

    @Query("SELECT COUNT(*) FROM pomodoro_session WHERE date = :date AND type = 'work'")
    suspend fun countTodaySessions(date: String): Int

    @Query("SELECT COUNT(*) FROM pomodoro_session WHERE type = 'work'")
    suspend fun countAllSessions(): Int

    @Insert
    suspend fun insert(session: PomodoroSession): Long

    @Query("DELETE FROM pomodoro_session")
    suspend fun deleteAll()
}