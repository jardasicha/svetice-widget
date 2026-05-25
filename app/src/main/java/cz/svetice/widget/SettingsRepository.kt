package cz.svetice.widget

import android.content.Context

data class Settings(
    val activeDays: BooleanArray, // index 0=Po, 6=Ne
    val fromHHmm: String,
    val toHHmm: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Settings) return false
        return activeDays.contentEquals(other.activeDays) &&
                fromHHmm == other.fromHHmm && toHHmm == other.toHHmm
    }
    override fun hashCode(): Int {
        var r = activeDays.contentHashCode()
        r = 31 * r + fromHHmm.hashCode()
        r = 31 * r + toHHmm.hashCode()
        return r
    }
}

object SettingsRepository {
    private const val PREFS = "svetice_settings"
    private const val KEY_DAYS = "active_days"
    private const val KEY_FROM = "from"
    private const val KEY_TO = "to"

    fun load(ctx: Context): Settings {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val daysStr = p.getString(KEY_DAYS, "1111100") ?: "1111100"
        val days = BooleanArray(7) { i -> daysStr.getOrNull(i) == '1' }
        return Settings(
            activeDays = days,
            fromHHmm = p.getString(KEY_FROM, "06:00") ?: "06:00",
            toHHmm = p.getString(KEY_TO, "09:00") ?: "09:00"
        )
    }

    fun save(ctx: Context, s: Settings) {
        val daysStr = s.activeDays.joinToString("") { if (it) "1" else "0" }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_DAYS, daysStr)
            .putString(KEY_FROM, s.fromHHmm)
            .putString(KEY_TO, s.toHHmm)
            .apply()
    }
}
