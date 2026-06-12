package id.pilah.feature.foldering

import kotlinx.coroutines.flow.Flow

/** Orkestrasi rencana & eksekusi "Rapikan Sekarang" (PRD §3.3 / Fase 4). */
interface FolderingRepository {

    /** Rencana "Sebelum -> Sesudah" saat ini, diturunkan dari kategori efektif setiap file. */
    fun observePlan(): Flow<FolderingPlan>

    /** Jalankan [plan]: pindahkan file, catat ke `actions`, dan masukkan file Layak Dihapus ke `quarantine`. */
    fun execute(plan: FolderingPlan): Flow<FolderingProgress>
}
