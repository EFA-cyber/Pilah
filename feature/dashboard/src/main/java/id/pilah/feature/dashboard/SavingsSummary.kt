package id.pilah.feature.dashboard

/**
 * Ringkasan ruang penyimpanan (PRD §3.4/§5).
 * [savedBytes]: ukuran total file yang sudah dihapus permanen sejak instalasi.
 * [potentialBytes]: ukuran total file yang masih menunggu di Karantina (potensi hemat).
 */
data class SavingsSummary(
    val savedBytes: Long = 0L,
    val potentialBytes: Long = 0L,
)
