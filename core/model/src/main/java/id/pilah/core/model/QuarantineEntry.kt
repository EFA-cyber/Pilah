package id.pilah.core.model

import java.time.Instant

/**
 * Entri file yang menunggu di Karantina sebelum dihapus permanen.
 * Sesuai tabel `quarantine` pada ERD PRD. File dihapus permanen otomatis
 * setelah `purgeAfter` terlewati (30 hari), atau lebih cepat oleh pengguna.
 */
data class QuarantineEntry(
    val id: Long = 0L,
    val fileId: Long,
    val quarantinedAt: Instant,
    val purgeAfter: Instant,
    val status: QuarantineStatus,
)
