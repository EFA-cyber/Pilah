package id.pilah.core.model

import java.time.Instant

/**
 * Koreksi kategori oleh pengguna terhadap usulan AI untuk satu file.
 * Sesuai tabel `user_corrections` pada ERD PRD — sinyal penyesuaian
 * bobot rule engine lokal.
 */
data class UserCorrection(
    val id: Long = 0L,
    val fileId: Long,
    val aiCategory: FileCategory,
    val userCategory: FileCategory,
    val correctedAt: Instant,
)
