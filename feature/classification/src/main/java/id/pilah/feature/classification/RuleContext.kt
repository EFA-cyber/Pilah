package id.pilah.feature.classification

import java.time.Instant

/**
 * Data lintas-file yang sudah dihitung lebih dulu oleh [ClassificationRepository],
 * dipakai [RuleEngine] untuk menilai satu file tanpa akses database/I/O tambahan.
 */
data class RuleContext(
    val now: Instant,
    /** Path file duplikat -> nama file asli yang dipertahankan. */
    val duplicateOfOriginalName: Map<String, String> = emptyMap(),
    /** Path file APK yang package-nya sudah terpasang di perangkat. */
    val installedApkPaths: Set<String> = emptySet(),
    /** Path file gambar -> varians Laplacian (semakin rendah = semakin buram). */
    val blurVariance: Map<String, Double> = emptyMap(),
)
