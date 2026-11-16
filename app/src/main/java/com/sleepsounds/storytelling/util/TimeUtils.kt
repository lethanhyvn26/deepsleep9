package com.sleepsounds.storytelling.util

import com.sleepsounds.storytelling.domain.model.TimeOfDay
import java.util.Calendar

/**
 * Utility functions for resolving the user's local time-of-day period and greeting message.
 */
object TimeUtils {

    fun currentTimeOfDay(): TimeOfDay {
        val currentHour24 = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            currentHour24 in 5..11 -> TimeOfDay.MORNING
            currentHour24 in 12..16 -> TimeOfDay.AFTERNOON
            currentHour24 in 17..19 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }

    fun greetingForTimeOfDay(period: TimeOfDay, displayName: String): String {
        val prefix = when (period) {
            TimeOfDay.MORNING -> "Good morning"
            TimeOfDay.AFTERNOON -> "Good afternoon"
            TimeOfDay.EVENING -> "Good evening"
            TimeOfDay.NIGHT -> "Sweet dreams"
        }
        return "$prefix, $displayName"
    }

    fun isNightMode(): Boolean = currentTimeOfDay() == TimeOfDay.NIGHT
}
