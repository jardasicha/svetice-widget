package cz.svetice.widget

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Plánovač aktualizací.
 *
 * Pozn.: WorkManager má minimální periodu 15 minut pro PeriodicWorkRequest.
 * Pro 30s aktualizaci proto používáme řetězené OneTimeWorkRequest s 30s zpožděním,
 * který sám sebe znovu naplánuje, dokud jsme v aktivním okně.
 */
object TrainUpdateScheduler {

    private const val UNIQUE = "svetice_train_update"

    fun schedule(ctx: Context) {
        val req = OneTimeWorkRequestBuilder<TrainUpdateWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(ctx).enqueueUniqueWork(
            UNIQUE,
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    fun cancel(ctx: Context) {
        WorkManager.getInstance(ctx).cancelUniqueWork(UNIQUE)
    }
}
