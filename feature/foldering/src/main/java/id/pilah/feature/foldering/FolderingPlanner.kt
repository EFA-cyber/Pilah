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
        // `files.path` punya unique index di database, jadi target path antar item (dan
        // terhadap file lain yang tidak ikut pindah) wajib unik agar worker tidak crash
        // saat dua file bernama sama (mis. IMG_xxxx.jpg) dipindah ke folder yang sama.
        val occupiedPaths = files.map { it.path }.toMutableSet()

        val items = files.mapNotNull { file ->
            val category = categories[file.id] ?: return@mapNotNull null
            val targetFolder = FolderSuggester.suggest(file, category) ?: return@mapNotNull null
            val targetDir = File(VolumeRootResolver.resolve(file.path), targetFolder)
            val targetPath = File(targetDir, file.name).path
            if (targetPath == file.path) return@mapNotNull null

            occupiedPaths.remove(file.path)
            val resolvedPath = if (targetPath in occupiedPaths) {
                uniquePath(targetDir, file.name, occupiedPaths)
            } else {
                targetPath
            }
            occupiedPaths.add(resolvedPath)

            FolderingPlanItem(file = file, category = category, targetFolder = targetFolder, targetPath = resolvedPath)
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

    /** Tambahkan suffix " (n)" sebelum ekstensi sampai path di [dir] untuk [name] tidak ada di [occupied]. */
    private fun uniquePath(dir: File, name: String, occupied: Set<String>): String {
        val dotIndex = name.lastIndexOf('.')
        val base = if (dotIndex > 0) name.substring(0, dotIndex) else name
        val extension = if (dotIndex > 0) name.substring(dotIndex) else ""

        var counter = 1
        var candidate: String
        do {
            candidate = File(dir, "$base ($counter)$extension").path
            counter++
        } while (candidate in occupied)
        return candidate
    }
}
