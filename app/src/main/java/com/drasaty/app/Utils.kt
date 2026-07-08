package com.drasaty.app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Utils {

    val ARABIC_DAYS = arrayOf(
        "السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة"
    )

    val ARABIC_DAYS_SHORT = arrayOf(
        "سبت", "أحد", "إثن", "ثلا", "أرب", "خمي", "جمع"
    )

    /**
     * يحول يوم الـ Calendar (1=الأحد..7=السبت) لنظامنا (0=السبت..6=الجمعة)
     */
    fun calendarDayToAppDay(calendarDay: Int): Int {
        // Calendar.SUNDAY=1, MONDAY=2, ..., SATURDAY=7
        return when (calendarDay) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 0
        }
    }

    /**
     * يحول يوم نظامنا (0=السبت) لـ Calendar
     */
    fun appDayToCalendarDay(appDay: Int): Int {
        return when (appDay) {
            0 -> Calendar.SATURDAY
            1 -> Calendar.SUNDAY
            2 -> Calendar.MONDAY
            3 -> Calendar.TUESDAY
            4 -> Calendar.WEDNESDAY
            5 -> Calendar.THURSDAY
            else -> Calendar.FRIDAY
        }
    }

    fun getTodayAppDay(): Int {
        return calendarDayToAppDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
    }

    fun formatArabicDate(): String {
        val day = getTodayAppDay()
        val dateFmt = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
        return "${ARABIC_DAYS[day]}، ${dateFmt.format(Date())}"
    }

    fun startOfDay(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun endOfDay(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}