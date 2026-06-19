package id.pilah.feature.quarantine

import kotlinx.coroutines.flow.Flow

/** Pengelolaan Karantina: daftar aktif, pemulihan, hapus manual, dan auto-purge (PRD §3.4 / Fase 4). */
interface QuarantineRepository {

    /** Entri Karantina yang masih aktif, diurutkan dari yang paling cepat di-purge. */
    fun observeActive(): Flow<List<QuarantineItem>>

    /** Kembalikan [item] ke lokasi semula sebelum dikarantina. */
    suspend fun restore(item: QuarantineItem)

    /** Hapus permanen [item] sekarang, sebelum 30 hari. */
    suspend fun deleteNow(item: QuarantineItem)

    /** Hapus permanen seluruh entri yang sudah melewati `purgeAfter`. Mengembalikan jumlah entri yang dihapus. */
    suspend fun purgeExpired(): Int
}
