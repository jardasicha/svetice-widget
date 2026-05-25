package cz.svetice.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrainWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Zajistit, že běží WorkManager job
        TrainUpdateScheduler.schedule(context)

        // Vyrenderovat aktuální stav (z cache, async načte nové)
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        TrainUpdateScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        TrainUpdateScheduler.cancel(context)
    }

    companion object {
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                ComponentName(context, TrainWidgetProvider::class.java)
            )
            for (id in ids) updateWidget(context, mgr, id)
        }

        private fun updateWidget(
            context: Context,
            mgr: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_train)
            val trains = TrainRepository.loadCached(context)
            val prefs = SettingsRepository.load(context)
            val active = WindowChecker.isInWindow(prefs)

            // Stav tečky podle okna
            views.setInt(
                R.id.live_dot,
                "setBackgroundResource",
                if (active) R.drawable.dot_active else R.drawable.dot_inactive
            )

            val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            views.setTextViewText(R.id.last_update, now)

            // Naplnit 3 řádky
            val rows = listOf(
                Triple(R.id.t1_planned, R.id.t1_delay, R.id.t1_new),
                Triple(R.id.t2_planned, R.id.t2_delay, R.id.t2_new),
                Triple(R.id.t3_planned, R.id.t3_delay, R.id.t3_new)
            )
            for (i in rows.indices) {
                val (plannedId, delayId, newId) = rows[i]
                val t = trains.getOrNull(i)
                if (t == null) {
                    views.setTextViewText(plannedId, "--:--")
                    views.setTextColor(plannedId, 0xFF5DCAA5.toInt())
                    views.setViewVisibility(delayId, android.view.View.GONE)
                    views.setViewVisibility(newId, android.view.View.GONE)
                } else if (t.delayMin == 0) {
                    views.setTextViewText(plannedId, t.planned)
                    views.setTextColor(plannedId, 0xFF5DCAA5.toInt())
                    views.setViewVisibility(delayId, android.view.View.GONE)
                    views.setViewVisibility(newId, android.view.View.GONE)
                } else {
                    views.setTextViewText(plannedId, t.planned)
                    views.setTextColor(plannedId, 0x55FFFFFF.toInt())
                    views.setViewVisibility(delayId, android.view.View.VISIBLE)
                    views.setTextViewText(delayId, "+${t.delayMin}")
                    views.setViewVisibility(newId, android.view.View.VISIBLE)
                    views.setTextViewText(newId, TimeUtil.addMinutes(t.planned, t.delayMin))
                }
            }

            // Klik na widget otevře konfiguraci
            val intent = Intent(context, ConfigActivity::class.java)
            val pi = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)

            mgr.updateAppWidget(widgetId, views)
        }
    }
}
