package cz.svetice.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrainWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Plánovač spustíme jen pokud existuje class WorkManager (defenzivně)
        try {
            TrainUpdateScheduler.schedule(context)
        } catch (_: Throwable) { /* nic */ }

        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        try { TrainUpdateScheduler.schedule(context) } catch (_: Throwable) {}
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        try { TrainUpdateScheduler.cancel(context) } catch (_: Throwable) {}
    }

    companion object {
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, TrainWidgetProvider::class.java))
            for (id in ids) updateWidget(context, mgr, id)
        }

        private fun updateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_train)

            val trains = try { TrainRepository.loadCached(context) } catch (_: Throwable) {
                listOf(Train("06:12", 0), Train("07:42", 4), Train("08:54", 12))
            }

            val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            views.setTextViewText(R.id.last_update, now)

            val rows = listOf(
                Triple(R.id.t1_planned, R.id.t1_delay, R.id.t1_new),
                Triple(R.id.t2_planned, R.id.t2_delay, R.id.t2_new),
                Triple(R.id.t3_planned, R.id.t3_delay, R.id.t3_new)
            )

            for (i in rows.indices) {
                val (plannedId, delayId, newId) = rows[i]
                val t = trains.getOrNull(i)
                when {
                    t == null -> {
                        views.setTextViewText(plannedId, "--:--")
                        views.setTextColor(plannedId, 0xFF5DCAA5.toInt())
                        views.setViewVisibility(delayId, View.GONE)
                        views.setViewVisibility(newId, View.GONE)
                    }
                    t.delayMin == 0 -> {
                        views.setTextViewText(plannedId, t.planned)
                        views.setTextColor(plannedId, 0xFF5DCAA5.toInt())
                        views.setViewVisibility(delayId, View.GONE)
                        views.setViewVisibility(newId, View.GONE)
                    }
                    else -> {
                        views.setTextViewText(plannedId, t.planned)
                        views.setTextColor(plannedId, 0x55FFFFFF.toInt())
                        views.setViewVisibility(delayId, View.VISIBLE)
                        views.setTextViewText(delayId, "+${t.delayMin}")
                        views.setViewVisibility(newId, View.VISIBLE)
                        views.setTextViewText(newId, TimeUtil.addMinutes(t.planned, t.delayMin))
                    }
                }
            }

            // Klik na widget otevře konfiguraci - cílíme na widget_root, ne na android.R.id.background
            val intent = Intent(context, ConfigActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pi = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)

            mgr.updateAppWidget(widgetId, views)
        }
    }
}
