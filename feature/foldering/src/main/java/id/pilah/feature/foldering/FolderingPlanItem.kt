package id.pilah.feature.foldering

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem

/** Satu file yang akan dipindahkan oleh "Rapikan Sekarang" beserta tujuannya (PRD §3.3 / Fase 4). */
data class FolderingPlanItem(
    val file: FileItem,
    val category: FileCategory,
    val targetFolder: String,
    val targetPath: String,
)
