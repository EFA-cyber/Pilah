package id.pilah.core.model

import java.time.Instant

/**
 * Representasi domain dari satu file hasil Smart Scan.
 * Sesuai tabel `files` pada ERD PRD.
 */
data class FileItem(
    val id: Long = 0L,
    val path: String,
    val name: String,
    val type: String,
    val sizeBytes: Long,
    val hash: String? = null,
    val lastOpened: Instant? = null,
    val createdAt: Instant,
)
