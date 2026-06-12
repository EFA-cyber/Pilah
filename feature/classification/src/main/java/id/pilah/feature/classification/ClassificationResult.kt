package id.pilah.feature.classification

import id.pilah.core.model.FileCategory

/** Hasil penilaian [RuleEngine] untuk satu file. */
data class ClassificationResult(
    val score: Int,
    val category: FileCategory,
    val reason: String,
)
