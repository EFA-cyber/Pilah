package id.pilah.feature.classification

/**
 * Bobot sinyal rule engine (PRD §3.2), disimpan di DataStore agar dapat
 * disesuaikan dari akumulasi koreksi pengguna (Fase 3).
 */
data class RuleWeights(
    val baseScore: Int = 50,
    val duplicateDelta: Int = -50,
    val oldScreenshotDelta: Int = -25,
    val installedApkDelta: Int = -40,
    val unopenedDownloadDelta: Int = -20,
    val blurryPhotoDelta: Int = -20,
    val importantNameDelta: Int = 35,
    val memoryPhotoDelta: Int = 15,
    val oldFileThresholdDays: Long = 30,
    val blurVarianceThreshold: Double = 100.0,
)
