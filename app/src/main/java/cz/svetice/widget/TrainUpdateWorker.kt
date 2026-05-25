package cz.svetice.widget

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class TrainUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val ctx = applicationContext
        val settings = SettingsRepository.load(ctx)
        val inWindow = WindowChecker.isInWindow(settings)

        if (inWindow) {
            // Skutečně načti data z API
            TrainRepository.fetchFromApi(ctx)
        }

        // Vždy aktualizuj widget (alespoň tečku stavu a hodiny)
        TrainWidgetProvider.updateAll(ctx)

        // Plánuj další běh
        val nextDelay = if (inWindow) 30L else 60L  // mimo okno kontrolujeme méně často
        val next = OneTimeWorkRequestBuilder<TrainUpdateWorker>()
            .setInitialDelay(nextDelay, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(
                        if (inWindow) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED
                    )
                    .build()
            )
            .build()
        WorkManager.getInstance(ctx).enqueueUniqueWork(
            "svetice_train_update",
            ExistingWorkPolicy.REPLACE,
            next
        )

        return Result.success()
    }
}
