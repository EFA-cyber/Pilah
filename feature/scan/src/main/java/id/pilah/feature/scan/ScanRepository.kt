package id.pilah.feature.scan

import kotlinx.coroutines.flow.Flow

/** Orkestrasi satu sesi Smart Scan: pemindaian metadata lalu hashing SHA-256. */
interface ScanRepository {

    /** Menjalankan pemindaian, memancarkan [ScanProgress] secara real-time. [subDir] membatasi ke subfolder tertentu. */
    fun scan(subDir: String? = null): Flow<ScanProgress>
}
