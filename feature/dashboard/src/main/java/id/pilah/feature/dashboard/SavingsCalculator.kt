package id.pilah.feature.dashboard

import id.pilah.core.model.ActionType
import id.pilah.core.model.FileAction
import id.pilah.core.model.FileItem
import id.pilah.core.model.QuarantineEntry

/** Hitung ruang yang sudah dihemat (file ter-purge) dan potensi hemat (file aktif di Karantina). */
object SavingsCalculator {
    fun calculate(
        files: List<FileItem>,
        actions: List<FileAction>,
        activeQuarantine: List<QuarantineEntry>,
    ): SavingsSummary {
        val sizeByFileId = files.associate { it.id to it.sizeBytes }

        val purgedFileIds = actions
            .filter { it.actionType == ActionType.PURGE }
            .map { it.fileId }
            .toSet()

        val savedBytes = purgedFileIds.sumOf { sizeByFileId[it] ?: 0L }
        val potentialBytes = activeQuarantine.sumOf { sizeByFileId[it.fileId] ?: 0L }

        return SavingsSummary(savedBytes = savedBytes, potentialBytes = potentialBytes)
    }
}
