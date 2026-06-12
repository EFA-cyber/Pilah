package id.pilah.core.model

import java.time.Instant

/**
 * Hasil penilaian AI/lokal untuk satu file.
 * Sesuai tabel `classifications` pada ERD PRD. Riwayat tetap disimpan;
 * klasifikasi yang berlaku adalah entri dengan `classifiedAt` terbaru.
 */
data class Classification(
    val id: Long = 0L,
    val fileId: Long,
    val importanceScore: Int,
    val category: FileCategory,
    val reason: String,
    val source: ClassificationSource,
    val classifiedAt: Instant,
)
