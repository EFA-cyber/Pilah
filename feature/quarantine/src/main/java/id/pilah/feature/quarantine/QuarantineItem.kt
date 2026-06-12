package id.pilah.feature.quarantine

import id.pilah.core.model.FileItem
import id.pilah.core.model.QuarantineEntry

/** Satu entri di Karantina beserta sisa hari sebelum dihapus permanen (PRD §3.4 / Fase 4). */
data class QuarantineItem(
    val file: FileItem,
    val entry: QuarantineEntry,
    val daysRemaining: Long,
)
