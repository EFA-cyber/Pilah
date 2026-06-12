package id.pilah.feature.quarantine

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Worker harian: hapus permanen entri Karantina yang sudah melewati 30 hari (PRD §3.4 / Fase 4). */
@HiltWorker
class QuarantinePurgeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val quarantineRepository: QuarantineRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        quarantineRepository.purgeExpired()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "quarantine_auto_purge"
    }
}
