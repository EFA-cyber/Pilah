package id.pilah.feature.foldering

/** Rencana "Sebelum -> Sesudah" hasil [FolderingPlanner]: daftar file yang dipindah + ringkasan per folder tujuan. */
data class FolderingPlan(
    val items: List<FolderingPlanItem> = emptyList(),
    val summaries: List<FolderingSummary> = emptyList(),
)
