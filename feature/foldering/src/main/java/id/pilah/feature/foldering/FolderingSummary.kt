package id.pilah.feature.foldering

/** Ringkasan jumlah & ukuran file yang akan masuk ke satu folder tujuan, untuk layar "Sebelum -> Sesudah". */
data class FolderingSummary(
    val targetFolder: String,
    val fileCount: Int,
    val totalSizeBytes: Long,
)
