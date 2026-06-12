package id.pilah.feature.classification

import id.pilah.core.model.ClassificationSource
import id.pilah.core.model.FileCategory

/** Satu file kandidat Analisis Mendalam, dikirim sebagai konteks ke Cloud AI. */
data class CloudClassificationInput(
    val fileId: Long,
    val fileName: String,
    val textSnippet: String,
)

/** Hasil penilaian Cloud AI untuk satu file. */
data class CloudClassificationResult(
    val fileId: Long,
    val importanceScore: Int,
    val category: FileCategory,
    val reason: String,
    val source: ClassificationSource,
)

/** Klasifikasi batch via Cloud AI untuk file kategori Ambigu (Fase 6). */
interface CloudClassifier {

    /** Klasifikasikan [items] menggunakan [apiKey]; hasil yang masih Ambigu setelah Haiku dieskalasi ke Sonnet. */
    suspend fun classify(items: List<CloudClassificationInput>, apiKey: String): List<CloudClassificationResult>
}
