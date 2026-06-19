package id.pilah.feature.foldering

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/** Worker background untuk eksekusi "Rapikan Sekarang" (PRD §3.3 / Fase 4). */
@HiltWorker
class FolderingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val folderingRepository: FolderingRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val plan = folderingRepository.observePlan().first()
        var last = FolderingProgress(totalFiles = plan.items.size)

        folderingRepository.execute(plan).collect { progress ->
            last = progress
            setProgress(progress.toWorkData())
        }

        return Result.success(last.toWorkData())
    }

    private fun FolderingProgress.toWorkData() = workDataOf(
        KEY_FILES_MOVED to filesMoved,
        KEY_TOTAL_FILES to totalFiles,
    )

    companion object {
        const val WORK_NAME = "foldering"
        const val TAG = "foldering"
        const val KEY_FILES_MOVED = "files_moved"
        const val KEY_TOTAL_FILES = "total_files"
    }
}
