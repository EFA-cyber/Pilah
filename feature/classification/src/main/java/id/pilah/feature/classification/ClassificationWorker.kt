package id.pilah.feature.classification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Worker background untuk satu sesi klasifikasi rule-based (PRD §3.2). */
@HiltWorker
class ClassificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val classificationRepository: ClassificationRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        classificationRepository.classifyAll().collect { progress -> setProgress(progress.toWorkData()) }
        return Result.success()
    }

    private fun ClassificationProgress.toWorkData() = workDataOf(
        KEY_FILES_CLASSIFIED to filesClassified,
        KEY_TOTAL_FILES to totalFiles,
    )

    companion object {
        const val TAG = "classification"
        const val KEY_FILES_CLASSIFIED = "files_classified"
        const val KEY_TOTAL_FILES = "total_files"
    }
}
