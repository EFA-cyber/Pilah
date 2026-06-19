package id.pilah.feature.scan

/** Progres real-time satu sesi Smart Scan. */
data class ScanProgress(
    val phase: ScanPhase,
    val filesScanned: Int = 0,
    val filesHashed: Int = 0,
    val filesToHash: Int = 0,
)
