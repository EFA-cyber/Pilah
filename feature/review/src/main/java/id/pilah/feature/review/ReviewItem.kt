package id.pilah.feature.review

import id.pilah.core.model.Classification
import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem

/** Satu entri pada tumpukan Tinjau Hasil — Penting atau Layak Dihapus (PRD §4). */
data class ReviewItem(
    val file: FileItem,
    val classification: Classification,
    val effectiveCategory: FileCategory,
    val suggestedFolder: String?,
)
