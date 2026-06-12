package id.pilah.feature.review

import id.pilah.core.model.Classification
import id.pilah.core.model.FileCategory
import kotlinx.coroutines.flow.Flow

/** Orkestrasi layar Tinjau Hasil: dua tumpukan Penting/Layak Dihapus + koreksi kategori (PRD §4). */
interface ReviewRepository {

    /** Tumpukan file berkategori efektif Penting, skor tertinggi lebih dulu. */
    fun observePenting(): Flow<List<ReviewItem>>

    /** Tumpukan file berkategori efektif Layak Dihapus, skor terendah lebih dulu. */
    fun observeLayakDihapus(): Flow<List<ReviewItem>>

    /** Riwayat klasifikasi (semua source) untuk satu file, terbaru lebih dulu. */
    fun observeHistory(fileId: Long): Flow<List<Classification>>

    /** Mencatat koreksi pengguna ke `user_corrections` dan menyesuaikan bobot rule engine secara heuristik. */
    suspend fun correctCategory(item: ReviewItem, newCategory: FileCategory)
}
