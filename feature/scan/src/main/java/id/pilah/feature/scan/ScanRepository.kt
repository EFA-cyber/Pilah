package id.pilah.feature.scan

import kotlinx.coroutines.flow.Flow

/** Orkestrasi satu sesi Smart Scan: pemindaian metadata lalu hashing SHA-256. */
interface ScanRepository {

    /** Menjalankan pemindaian penuh, memancarkan [ScanProgress] secara real-time. */
    fun scan(): Flow<ScanProgress>
}
