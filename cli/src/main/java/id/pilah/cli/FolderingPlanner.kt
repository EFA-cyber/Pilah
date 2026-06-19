package id.pilah.cli

import id.pilah.core.common.FolderSuggester
import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.io.File

/** Rencana pemindahan satu file ke subfolder kategori di bawah folder yang dipindai. */
data class FolderingPlanItem(
    val file: FileItem,
    val targetFolder: String,
    val targetPath: String,
)

/** Menyusun rencana pemindahan file ke subfolder kategori (lihat [FolderSuggester]) di bawah [root]. */
object FolderingPlanner {

    /**
     * [categories] memuat kategori hasil [Classifier] per [FileItem.path].
     * File tanpa kategori, berkategori Ambigu, atau yang sudah berada di lokasi tujuan dilewati.
     */
    fun plan(root: File, files: List<FileItem>, categories: Map<String, FileCategory>): List<FolderingPlanItem> {
        // Path target antar item (dan terhadap file lain yang tidak ikut pindah) wajib unik agar
        // dua file bernama sama (mis. IMG_xxxx.jpg) yang dipindah ke folder yang sama tidak bertabrakan.
        val occupiedPaths = files.map { it.path }.toMutableSet()

        return files.mapNotNull { file ->
            val category = categories[file.path] ?: return@mapNotNull null
            val targetFolder = FolderSuggester.suggest(file, category) ?: return@mapNotNull null
            val targetDir = File(root, targetFolder)
            val targetPath = File(targetDir, file.name).path
            if (targetPath == file.path) return@mapNotNull null

            occupiedPaths.remove(file.path)
            val resolvedPath = if (targetPath in occupiedPaths) {
                uniquePath(targetDir, file.name, occupiedPaths)
            } else {
                targetPath
            }
            occupiedPaths.add(resolvedPath)

            FolderingPlanItem(file = file, targetFolder = targetFolder, targetPath = resolvedPath)
        }
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
