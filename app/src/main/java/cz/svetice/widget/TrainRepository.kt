package cz.svetice.widget

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Načítá odjezdy ze Světice směr Praha.
 *
 * Zdroj dat: Golemio PID Departure Boards v2
 * https://api.golemio.cz/v2/pid/departureboards
 *
 * Pro skutečné použití potřebuješ API klíč zdarma z https://api.golemio.cz
 * Klíč vlož do BuildConfig nebo nahraď konstantu níže.
 */
object TrainRepository {

    // TODO: nahraď svým klíčem z https://api.golemio.cz (registrace zdarma)
    private const val GOLEMIO_API_KEY = "PASTE_YOUR_GOLEMIO_TOKEN_HERE"

    // Světice (zastávka linky S9) - ASW ID
    // Pokud by se ID v budoucnu změnilo, vyhledej v Golemio /v2/gtfs/stops
    private const val STATION = "Světice"

    private const val PREFS = "trains_cache"
    private const val KEY_DATA = "data"

    fun loadCached(ctx: Context): List<Train> {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_DATA, null) ?: return demoData()
        return raw.split("|").mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size != 2) null
            else Train(parts[0], parts[1].toIntOrNull() ?: 0)
        }.ifEmpty { demoData() }
    }

    fun saveCache(ctx: Context, trains: List<Train>) {
        val raw = trains.joinToString("|") { "${it.planned},${it.delayMin}" }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_DATA, raw).apply()
    }

    /**
     * Reálné volání API. Vrací 3 nejbližší odjezdy do Prahy.
     * Při chybě vrátí cached data.
     */
    fun fetchFromApi(ctx: Context): List<Train> {
        if (GOLEMIO_API_KEY == "PASTE_YOUR_GOLEMIO_TOKEN_HERE") {
            // Klíč nenastaven - vrátíme demo data ať appka neumře
            return demoData().also { saveCache(ctx, it) }
        }

        return try {
            val url = URL(
                "https://api.golemio.cz/v2/pid/departureboards" +
                        "?names=$STATION&limit=10&minutesBefore=0&minutesAfter=180&total=10"
            )
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("X-Access-Token", GOLEMIO_API_KEY)
                connectTimeout = 5000
                readTimeout = 5000
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val departures = json.getJSONArray("departures")
            val parsed = mutableListOf<Train>()
            val timeFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Europe/Prague")
            }
            val display = SimpleDateFormat("HH:mm", Locale.getDefault())

            for (i in 0 until departures.length()) {
                if (parsed.size >= 3) break
                val dep = departures.getJSONObject(i)
                val trip = dep.optJSONObject("trip") ?: continue
                val headsign = trip.optString("headsign", "")
                // Filtrujeme jen směr Praha (libovolná pražská stanice v cílovém směru)
                if (!headsign.contains("Praha", ignoreCase = true)) continue

                val arrDep = dep.optJSONObject("arrival_timestamp")
                    ?: dep.optJSONObject("departure_timestamp") ?: continue
                val scheduled = arrDep.optString("scheduled").take(19)
                val predicted = arrDep.optString("predicted").take(19)

                val schedDate = try { timeFmt.parse(scheduled) } catch (e: Exception) { null }
                val predDate = try { timeFmt.parse(predicted) } catch (e: Exception) { schedDate }
                if (schedDate == null) continue
                val delaySec = ((predDate?.time ?: schedDate.time) - schedDate.time) / 1000
                val delayMin = (delaySec / 60).toInt().coerceAtLeast(0)
                parsed.add(Train(display.format(schedDate), delayMin))
            }

            if (parsed.isNotEmpty()) {
                saveCache(ctx, parsed)
                parsed
            } else {
                loadCached(ctx)
            }
        } catch (e: Exception) {
            loadCached(ctx)
        }
    }

    private fun demoData(): List<Train> = listOf(
        Train("06:12", 0),
        Train("07:42", 4),
        Train("08:54", 12)
    )
}
