package id.pilah.core.model

import java.time.Instant

/**
 * Log satu aksi (pindah/karantina/pulihkan/hapus permanen) terhadap sebuah file.
 * Sesuai tabel `actions` pada ERD PRD — basis fitur undo dan audit.
 */
data class FileAction(
    val id: Long = 0L,
    val fileId: Long,
    val actionType: ActionType,
    val fromPath: String,
    val toPath: String,
    val executedAt: Instant,
)
