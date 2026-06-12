package id.pilah.feature.foldering

import id.pilah.core.common.FolderSuggester
import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.io.File

/** Menyusun rencana "Sebelum -> Sesudah": file mana pindah ke folder mana (PRD §3.3 / Fase 4). */
object FolderingPlanner {

    /**
     * [categories] memuat kategori efektif (hasil rule engine + koreksi pengguna) per [FileItem.id].
     * File tanpa kategori, berkategori Ambigu, atau yang sudah berada di lokasi tujuan dilewati.
     */
    fun plan(files: List<FileItem>, categories: Map<Long, FileCategory>): FolderingPlan {
        val items = files.mapNotNull { file ->
            val category = categories[file.id] ?: return@mapNotNull null
            val targetFolder = FolderSuggester.suggest(file, category) ?: return@mapNotNull null
            val targetPath = File(File(VolumeRootResolver.resolve(file.path), targetFolder), file.name).path
            if (targetPath == file.path) return@mapNotNull null

            FolderingPlanItem(file = file, category = category, targetFolder = targetFolder, targetPath = targetPath)
        }

        val summaries = items
            .groupBy { it.targetFolder }
            .map { (folder, group) ->
                FolderingSummary(
                    targetFolder = folder,
                    fileCount = group.size,
                    totalSizeBytes = group.sumOf { it.file.sizeBytes },
                )
            }
            .sortedBy { it.targetFolder }

        return FolderingPlan(items = items, summaries = summaries)
    }
}
