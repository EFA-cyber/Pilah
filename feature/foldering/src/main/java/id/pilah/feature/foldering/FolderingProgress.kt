package id.pilah.feature.foldering

/** Progres real-time eksekusi "Rapikan Sekarang". */
data class FolderingProgress(
    val filesMoved: Int = 0,
    val totalFiles: Int = 0,
)
