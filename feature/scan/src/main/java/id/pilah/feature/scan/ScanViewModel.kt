package id.pilah.feature.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.getWorkInfosForUniqueWorkFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.pilah.feature.classification.ClassificationWorker
import id.pilah.feature.classification.DeepAnalysisWorker
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ScanViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    val uiState: StateFlow<ScanUiState> = workManager
        .getWorkInfosForUniqueWorkFlow(ScanWorker.WORK_NAME)
        .map { infos -> infos.toScanUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScanUiState())

    /**
     * Memulai sesi Smart Scan baru jika belum berjalan, dilanjutkan otomatis dengan klasifikasi
     * rule engine lalu Analisis Mendalam (file Ambigu, hanya berjalan jika mode privasi
     * Analisis Mendalam aktif & kunci API tersedia — lihat [DeepAnalysisWorker]).
     */
    fun startScan() {
        val scanRequest = OneTimeWorkRequestBuilder<ScanWorker>()
            .addTag(ScanWorker.TAG)
            .build()
        val classificationRequest = OneTimeWorkRequestBuilder<ClassificationWorker>()
            .addTag(ClassificationWorker.TAG)
            .build()
        val deepAnalysisRequest = OneTimeWorkRequestBuilder<DeepAnalysisWorker>()
            .addTag(DeepAnalysisWorker.TAG)
            .build()

        workManager
            .beginUniqueWork(ScanWorker.WORK_NAME, ExistingWorkPolicy.KEEP, scanRequest)
            .then(classificationRequest)
            .then(deepAnalysisRequest)
            .enqueue()
    }

    /** Membatalkan sesi Smart Scan yang sedang berjalan. */
    fun cancelScan() {
        workManager.cancelUniqueWork(ScanWorker.WORK_NAME)
    }
}
