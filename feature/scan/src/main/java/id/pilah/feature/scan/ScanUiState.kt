package id.pilah.feature.scan

import androidx.work.WorkInfo
import id.pilah.feature.classification.ClassificationWorker

/** Tahapan pipeline Smart Scan + Klasifikasi yang ditampilkan di [id.pilah.feature.scan.ui.ScanScreen]. */
enum class ScanPipelinePhase {
    SCANNING,
    HASHING,
    CLASSIFYING,
}

/** State UI [id.pilah.feature.scan.ui.ScanScreen], diturunkan dari [WorkInfo] [ScanWorker] dan [ClassificationWorker]. */
data class ScanUiState(
    val phase: ScanPipelinePhase = ScanPipelinePhase.SCANNING,
    val filesScanned: Int = 0,
    val filesHashed: Int = 0,
    val filesToHash: Int = 0,
    val filesClassified: Int = 0,
    val totalFiles: Int = 0,
    val isDone: Boolean = false,
    val isFailed: Boolean = false,
)

internal fun List<WorkInfo>.toScanUiState(): ScanUiState {
    val scanInfo = firstOrNull { ScanWorker.TAG in it.tags }
    val classificationInfo = firstOrNull { ClassificationWorker.TAG in it.tags }
    val isDone = isNotEmpty() && all { it.state == WorkInfo.State.SUCCEEDED }
    val isFailed = any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }

    if (classificationInfo != null && classificationInfo.state != WorkInfo.State.BLOCKED) {
        return ScanUiState(
            phase = ScanPipelinePhase.CLASSIFYING,
            filesClassified = classificationInfo.progress.getInt(ClassificationWorker.KEY_FILES_CLASSIFIED, 0),
            totalFiles = classificationInfo.progress.getInt(ClassificationWorker.KEY_TOTAL_FILES, 0),
            isDone = isDone,
            isFailed = isFailed,
        )
    }

    val scanPhase = scanInfo?.progress?.getString(ScanWorker.KEY_PHASE)
        ?.let { runCatching { ScanPhase.valueOf(it) }.getOrNull() }
        ?: ScanPhase.SCANNING

    return ScanUiState(
        phase = when (scanPhase) {
            ScanPhase.SCANNING -> ScanPipelinePhase.SCANNING
            ScanPhase.HASHING, ScanPhase.DONE -> ScanPipelinePhase.HASHING
        },
        filesScanned = scanInfo?.progress?.getInt(ScanWorker.KEY_FILES_SCANNED, 0) ?: 0,
        filesHashed = scanInfo?.progress?.getInt(ScanWorker.KEY_FILES_HASHED, 0) ?: 0,
        filesToHash = scanInfo?.progress?.getInt(ScanWorker.KEY_FILES_TO_HASH, 0) ?: 0,
        isDone = isDone,
        isFailed = isFailed,
    )
}
