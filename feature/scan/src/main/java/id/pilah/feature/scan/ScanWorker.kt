package id.pilah.feature.scan

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Worker background untuk satu sesi Smart Scan (PRD §3.1). */
@HiltWorker
class ScanWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val scanRepository: ScanRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        scanRepository.scan().collect { progress -> setProgress(progress.toWorkData()) }
        return Result.success()
    }

    private fun ScanProgress.toWorkData() = workDataOf(
        KEY_PHASE to phase.name,
        KEY_FILES_SCANNED to filesScanned,
        KEY_FILES_HASHED to filesHashed,
        KEY_FILES_TO_HASH to filesToHash,
    )

    companion object {
        const val WORK_NAME = "smart_scan"
        const val TAG = "scan"
        const val KEY_PHASE = "phase"
        const val KEY_FILES_SCANNED = "files_scanned"
        const val KEY_FILES_HASHED = "files_hashed"
        const val KEY_FILES_TO_HASH = "files_to_hash"
    }
}
