package id.pilah.feature.classification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker background untuk satu sesi Analisis Mendalam (PRD Fase 6, opsional).
 * Tidak melakukan apa pun jika mode privasi bukan Analisis Mendalam atau kunci API belum diatur
 * (lihat [DefaultDeepAnalysisRepository]).
 */
@HiltWorker
class DeepAnalysisWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val deepAnalysisRepository: DeepAnalysisRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val result = deepAnalysisRepository.analyzeAmbiguousFiles()
        setProgress(
            workDataOf(
                KEY_FILES_ANALYZED to result.filesAnalyzed,
                KEY_SKIPPED to result.skipped,
            ),
        )
        return Result.success()
    }

    companion object {
        const val TAG = "deep_analysis"
        const val KEY_FILES_ANALYZED = "files_analyzed"
        const val KEY_SKIPPED = "skipped"
    }
}
