package id.pilah.feature.review

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem

/**
 * Usulan folder tujuan auto-foldering (PRD §3.3) berdasarkan kategori efektif & jenis file.
 * Heuristik awal untuk layar Tinjau Hasil; struktur folder final & eksekusi pindah file
 * baru ditentukan pada Fase 4.
 */
object FolderSuggester {

    fun suggest(file: FileItem, category: FileCategory): String? = when (category) {
        FileCategory.LAYAK_DIHAPUS -> "Karantina"
        FileCategory.AMBIGU -> null
        FileCategory.PENTING -> when {
            isInDcim(file.path) && file.type in MEDIA_EXTENSIONS -> "Foto Kenangan"
            file.type in DOCUMENT_EXTENSIONS -> "Dokumen Penting"
            file.type in WORK_EXTENSIONS -> "Kerja & Bisnis"
            else -> "Arsip"
        }
    }

    private fun isInDcim(path: String): Boolean = path.lowercase().contains("/dcim")

    private val MEDIA_EXTENSIONS = setOf("jpg", "jpeg", "png", "heic", "webp", "mp4", "mov", "3gp", "mkv")
    private val DOCUMENT_EXTENSIONS = setOf("pdf", "doc", "docx")
    private val WORK_EXTENSIONS = setOf("xls", "xlsx", "ppt", "pptx", "csv")
}
