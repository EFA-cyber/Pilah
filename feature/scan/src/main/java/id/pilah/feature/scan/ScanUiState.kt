package id.pilah.feature.scan

import androidx.work.WorkInfo

/** State UI [id.pilah.feature.scan.ui.ScanScreen], diturunkan dari [WorkInfo] terbaru [ScanWorker]. */
data class ScanUiState(
    val phase: ScanPhase = ScanPhase.SCANNING,
    val filesScanned: Int = 0,
    val filesHashed: Int = 0,
    val filesToHash: Int = 0,
    val isDone: Boolean = false,
)

internal fun WorkInfo?.toScanUiState(): ScanUiState {
    if (this == null) return ScanUiState()
    val phase = progress.getString(ScanWorker.KEY_PHASE)?.let { runCatching { ScanPhase.valueOf(it) }.getOrNull() }
    return ScanUiState(
        phase = phase ?: ScanPhase.SCANNING,
        filesScanned = progress.getInt(ScanWorker.KEY_FILES_SCANNED, 0),
        filesHashed = progress.getInt(ScanWorker.KEY_FILES_HASHED, 0),
        filesToHash = progress.getInt(ScanWorker.KEY_FILES_TO_HASH, 0),
        isDone = state == WorkInfo.State.SUCCEEDED,
    )
}
