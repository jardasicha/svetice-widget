package cz.svetice.widget

import java.util.Calendar

object WindowChecker {
    fun isInWindow(s: Settings, cal: Calendar = Calendar.getInstance()): Boolean {
        // Calendar.DAY_OF_WEEK: 1=Sunday..7=Saturday → převod na 0=Po..6=Ne
        val dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        if (!s.activeDays[dow]) return false
        val curMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val fromMin = toMin(s.fromHHmm)
        val toMin = toMin(s.toHHmm)
        return curMin in fromMin..toMin
    }

    private fun toMin(hhmm: String): Int {
        val parts = hhmm.split(":")
        return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 +
                (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }
}
