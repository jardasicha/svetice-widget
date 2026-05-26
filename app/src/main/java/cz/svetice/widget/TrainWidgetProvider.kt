package cz.svetice.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class TrainWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_train)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            // No-op pro test
        }
    }
}
