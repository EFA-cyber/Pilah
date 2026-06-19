package id.pilah.feature.classification

/** Progres real-time satu sesi klasifikasi rule engine. */
data class ClassificationProgress(
    val filesClassified: Int = 0,
    val totalFiles: Int = 0,
)
