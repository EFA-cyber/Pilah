package id.pilah.feature.dashboard

import java.time.LocalDate

/** Satu sesi "Rapikan Sekarang" (PRD §3.3/§5), diagregasi per tanggal eksekusi. */
data class CleanupSession(
    val date: LocalDate,
    val fileCount: Int,
    val totalSizeBytes: Long,
)
