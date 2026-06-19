package id.pilah.feature.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.pilah.feature.classification.ClassificationWorker
import id.pilah.feature.classification.DeepAnalysisWorker
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Pilihan folder yang dapat dipilih pengguna sebelum memulai Smart Scan. */
data class FolderOption(val label: String, val subDir: String?)

@HiltViewModel
class ScanViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    /** Daftar folder yang bisa dipilih. null berarti pindai semua penyimpanan. */
    val folderOptions: List<FolderOption> = listOf(
        FolderOption("Semua File", null),
        FolderOption("Unduhan", "Download"),
        FolderOption("Foto & Video (DCIM)", "DCIM"),
        FolderOption("Dokumen", "Documents"),
        FolderOption("Musik", "Music"),
        FolderOption("Video", "Movies"),
    )

    private val _isPickingFolder = MutableStateFlow(true)
    val isPickingFolder: StateFlow<Boolean> = _isPickingFolder.asStateFlow()

    val uiState: StateFlow<ScanUiState> = workManager
        .getWorkInfosForUniqueWorkFlow(ScanWorker.WORK_NAME)
        .map { infos -> infos.toScanUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScanUiState())

    /** Konfirmasi pilihan folder lalu mulai Smart Scan dengan AI. */
    fun confirmAndStartScan(subDir: String?) {
        _isPickingFolder.value = false
        val inputData = if (subDir != null) workDataOf(ScanWorker.KEY_SUB_DIR to subDir) else workDataOf()
        val scanRequest = OneTimeWorkRequestBuilder<ScanWorker>()
            .addTag(ScanWorker.TAG)
            .setInputData(inputData)
            .build()
        val classificationRequest = OneTimeWorkRequestBuilder<ClassificationWorker>()
            .addTag(ClassificationWorker.TAG)
            .build()
        val deepAnalysisRequest = OneTimeWorkRequestBuilder<DeepAnalysisWorker>()
            .addTag(DeepAnalysisWorker.TAG)
            .build()

        workManager
            .beginUniqueWork(ScanWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, scanRequest)
            .then(classificationRequest)
            .then(deepAnalysisRequest)
            .enqueue()
    }

    /** Membatalkan sesi Smart Scan yang sedang berjalan. */
    fun cancelScan() {
        workManager.cancelUniqueWork(ScanWorker.WORK_NAME)
    }
}
