package id.pilah.feature.dashboard

import id.pilah.core.model.ActionType
import id.pilah.core.model.FileAction
import id.pilah.core.model.FileItem
import java.time.ZoneId

/** Kelompokkan aksi "Rapikan Sekarang" (MOVE/QUARANTINE) per tanggal eksekusi. */
object CleanupHistoryAggregator {
    fun aggregate(files: List<FileItem>, actions: List<FileAction>): List<CleanupSession> {
        val sizeByFileId = files.associate { it.id to it.sizeBytes }

        return actions
            .filter { it.actionType == ActionType.MOVE || it.actionType == ActionType.QUARANTINE }
            .groupBy { it.executedAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (date, group) ->
                CleanupSession(
                    date = date,
                    fileCount = group.size,
                    totalSizeBytes = group.sumOf { sizeByFileId[it.fileId] ?: 0L },
                )
            }
            .sortedByDescending { it.date }
    }
}
