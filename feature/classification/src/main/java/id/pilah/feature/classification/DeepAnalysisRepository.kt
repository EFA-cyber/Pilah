package id.pilah.feature.classification

/** Hasil satu sesi Analisis Mendalam. */
data class DeepAnalysisResult(
    val filesAnalyzed: Int = 0,
    val skipped: Boolean = false,
)

/** Orkestrasi Analisis Mendalam via Cloud AI untuk file kategori Ambigu (PRD Fase 6, opsional). */
interface DeepAnalysisRepository {

    /**
     * Analisis seluruh file berkategori Ambigu yang didukung [TextExtractor] via [CloudClassifier].
     * Tidak melakukan apa pun (mengembalikan [DeepAnalysisResult.skipped] = true) jika mode privasi
     * bukan [id.pilah.core.model.PrivacyMode.DEEP_ANALYSIS] atau kunci API belum diatur.
     */
    suspend fun analyzeAmbiguousFiles(): DeepAnalysisResult
}
