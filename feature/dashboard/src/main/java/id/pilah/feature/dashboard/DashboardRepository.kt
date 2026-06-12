package id.pilah.feature.dashboard

import id.pilah.core.model.FileAction
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {

    /** Pemakaian penyimpanan per kategori efektif (PRD §5). */
    fun observeStorageByCategory(): Flow<List<CategoryUsage>>

    /** Ruang yang sudah dihemat (file ter-purge) & potensi hemat (file aktif di Karantina). */
    fun observeSavings(): Flow<SavingsSummary>

    /** Riwayat sesi "Rapikan Sekarang", diagregasi per tanggal. */
    fun observeCleanupHistory(): Flow<List<CleanupSession>>

    /** Log seluruh aksi pindah/karantina/pulihkan/hapus untuk halaman Pengaturan. */
    fun observeActionLog(): Flow<List<FileAction>>
}
