package cz.svetice.widget

object TimeUtil {
    fun addMinutes(hhmm: String, min: Int): String {
        val parts = hhmm.split(":")
        if (parts.size != 2) return hhmm
        val h = parts[0].toIntOrNull() ?: return hhmm
        val m = parts[1].toIntOrNull() ?: return hhmm
        val total = (h * 60 + m + min) % (24 * 60)
        val newH = total / 60
        val newM = total % 60
        return "%02d:%02d".format(newH, newM)
    }
}
