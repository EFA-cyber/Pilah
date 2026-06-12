package id.pilah.feature.classification

import id.pilah.core.model.ClassificationSource
import id.pilah.core.model.FileCategory
import id.pilah.core.network.ClaudeClient
import id.pilah.core.network.ClaudeModel
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/** Klasifikasi file Ambigu via Claude: batch Haiku, lalu eskalasi sisanya ke Sonnet (PRD Fase 6). */
class ClaudeCloudClassifier @Inject constructor(
    private val claudeClient: ClaudeClient,
) : CloudClassifier {

    override suspend fun classify(items: List<CloudClassificationInput>, apiKey: String): List<CloudClassificationResult> {
        if (items.isEmpty()) return emptyList()

        val haikuResults = runBatch(items, apiKey, ClaudeModel.HAIKU, ClassificationSource.CLOUD_HAIKU)
        val resolved = haikuResults.filter { it.category != FileCategory.AMBIGU }
        val resolvedIds = resolved.map { it.fileId }.toSet()

        val needsEscalation = items.filterNot { it.fileId in resolvedIds }
        if (needsEscalation.isEmpty()) return resolved

        val sonnetResults = runBatch(needsEscalation, apiKey, ClaudeModel.SONNET, ClassificationSource.CLOUD_SONNET)
        return resolved + sonnetResults
    }

    private suspend fun runBatch(
        items: List<CloudClassificationInput>,
        apiKey: String,
        model: ClaudeModel,
        source: ClassificationSource,
    ): List<CloudClassificationResult> {
        val prompt = ClaudeClassificationPrompt.build(items)
        val response = claudeClient.sendPrompt(apiKey, model, prompt)
        return ClaudeClassificationResponseParser.parse(response, items.map { it.fileId }.toSet(), source)
    }
}

/** Susun prompt batch berbahasa Indonesia, meminta balasan JSON array murni. */
internal object ClaudeClassificationPrompt {
    fun build(items: List<CloudClassificationInput>): String {
        val files = items.joinToString(separator = "\n") { item ->
            val snippet = item.textSnippet.replace("\"", "'").take(MAX_SNIPPET_CHARS)
            "- file_id: ${item.fileId}, nama: \"${item.fileName}\", cuplikan: \"$snippet\""
        }

        return """
            Anda membantu pengguna ponsel Android merapikan penyimpanan. Untuk setiap file di bawah,
            nilai tingkat kepentingannya (0-100), tentukan kategori berdasarkan ambang batas
            (skor >= 70 -> PENTING, skor <= 30 -> LAYAK_DIHAPUS, selainnya -> AMBIGU),
            dan berikan alasan singkat dalam Bahasa Indonesia.

            Daftar file:
            $files

            Balas HANYA dengan JSON array tanpa teks atau markdown lain, dengan format persis:
            [{"file_id": <id>, "importance_score": <0-100>, "category": "PENTING|LAYAK_DIHAPUS|AMBIGU", "reason": "<alasan singkat>"}]
        """.trimIndent()
    }

    private const val MAX_SNIPPET_CHARS = 1_000
}

@Serializable
internal data class CloudClassificationItemDto(
    @SerialName("file_id") val fileId: Long,
    @SerialName("importance_score") val importanceScore: Int,
    val category: String,
    val reason: String,
)

/** Urai balasan Claude (JSON array, mungkin diapit teks lain) menjadi [CloudClassificationResult]. */
internal object ClaudeClassificationResponseParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(response: String, validFileIds: Set<Long>, source: ClassificationSource): List<CloudClassificationResult> {
        val jsonArray = extractJsonArray(response) ?: return emptyList()
        val items = runCatching { json.decodeFromString<List<CloudClassificationItemDto>>(jsonArray) }.getOrNull()
            ?: return emptyList()

        return items.mapNotNull { dto ->
            if (dto.fileId !in validFileIds) return@mapNotNull null
            val category = runCatching { FileCategory.valueOf(dto.category.trim().uppercase()) }.getOrNull()
                ?: return@mapNotNull null

            CloudClassificationResult(
                fileId = dto.fileId,
                importanceScore = dto.importanceScore.coerceIn(0, 100),
                category = category,
                reason = dto.reason,
                source = source,
            )
        }
    }

    private fun extractJsonArray(text: String): String? {
        val start = text.indexOf('[')
        val end = text.lastIndexOf(']')
        if (start == -1 || end == -1 || end < start) return null
        return text.substring(start, end + 1)
    }
}
