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

/** Susun prompt batch berbahasa Indonesia yang cerdas, meminta balasan JSON array murni. */
internal object ClaudeClassificationPrompt {
    fun build(items: List<CloudClassificationInput>): String {
        val files = items.joinToString(separator = "\n") { item ->
            val snippet = item.textSnippet.replace("\"", "'").take(MAX_SNIPPET_CHARS)
            "- file_id: ${item.fileId}, nama: \"${item.fileName}\", konteks: \"$snippet\""
        }

        return """
            Anda adalah asisten AI cerdas untuk aplikasi Pilah — alat perapian file Android pengguna Indonesia.
            Analisis setiap file dan tentukan tingkat kepentingannya berdasarkan nama, tipe, dan konteksnya.

            Panduan klasifikasi untuk pengguna Indonesia:
            - PENTING (skor >= 70): Dokumen identitas & legal (KTP, ijazah, NPWP, SIM, akta kelahiran, sertifikat, kontrak, perjanjian, SK), foto kenangan berharga (wisuda, pernikahan, ulang tahun, liburan keluarga), file kerja/bisnis aktif (laporan, proposal, invoice, presentasi), dokumen keuangan (rekening, bukti transfer, tagihan penting).
            - LAYAK_DIHAPUS (skor <= 30): Screenshot lama tidak berguna, file duplikat, unduhan yang sudah digunakan/tidak diperlukan lagi, file sementara/cache/thumbnail, APK yang sudah terinstal, video iklan/promosi.
            - AMBIGU (skor 31-69): File yang tujuannya tidak dapat dipastikan dari nama dan konteks yang tersedia.

            Petunjuk tambahan:
            - Jika konteks berisi "Ukuran/Umur/Folder": gunakan usia dan folder asal sebagai sinyal kuat. File tua (>60 hari) di folder Download cenderung LAYAK_DIHAPUS; file di DCIM/Camera cenderung kenangan berharga.
            - Nama file dengan pola tanggal (YYYYMMDD_, IMG_, VID_, Screenshot_) beri konteks penting.
            - Ukuran file besar (>10 MB) di folder yang tepat cenderung lebih PENTING.

            Daftar file:
            $files

            Balas HANYA dengan JSON array tanpa teks atau markdown lain, dengan format persis:
            [{"file_id": <id>, "importance_score": <0-100>, "category": "PENTING|LAYAK_DIHAPUS|AMBIGU", "reason": "<alasan singkat dalam Bahasa Indonesia, maks 15 kata>"}]
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
