package id.pilah.feature.foldering

import androidx.work.WorkInfo

/** State eksekusi [FolderingWorker], diturunkan dari [WorkInfo] untuk [id.pilah.feature.foldering.ui.FolderingScreen]. */
data class FolderingUiState(
    val isRunning: Boolean = false,
    val isDone: Boolean = false,
    val filesMoved: Int = 0,
    val totalFiles: Int = 0,
)

internal fun List<WorkInfo>.toFolderingUiState(): FolderingUiState {
    val info = firstOrNull { FolderingWorker.TAG in it.tags } ?: return FolderingUiState()
    val isDone = info.state == WorkInfo.State.SUCCEEDED
    val data = if (isDone) info.outputData else info.progress

    return FolderingUiState(
        isRunning = info.state == WorkInfo.State.RUNNING,
        isDone = isDone,
        filesMoved = data.getInt(FolderingWorker.KEY_FILES_MOVED, 0),
        totalFiles = data.getInt(FolderingWorker.KEY_TOTAL_FILES, 0),
    )
}
