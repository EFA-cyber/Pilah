package id.pilah.feature.classification

import id.pilah.core.model.ClassificationSource
import id.pilah.core.model.FileCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudeClassificationResponseParserTest {

    @Test
    fun `parse mengurai balasan JSON array murni`() {
        val response = """[{"file_id": 1, "importance_score": 80, "category": "PENTING", "reason": "Dokumen identitas"}]"""

        val results = ClaudeClassificationResponseParser.parse(response, setOf(1L), ClassificationSource.CLOUD_HAIKU)

        assertEquals(1, results.size)
        val result = results.first()
        assertEquals(1L, result.fileId)
        assertEquals(80, result.importanceScore)
        assertEquals(FileCategory.PENTING, result.category)
        assertEquals("Dokumen identitas", result.reason)
        assertEquals(ClassificationSource.CLOUD_HAIKU, result.source)
    }

    @Test
    fun `parse mengurai balasan yang diapit teks lain`() {
        val response = """
            Berikut hasil analisisnya:
            [{"file_id": 2, "importance_score": 20, "category": "LAYAK_DIHAPUS", "reason": "Catatan sementara"}]
            Semoga membantu!
        """.trimIndent()

        val results = ClaudeClassificationResponseParser.parse(response, setOf(2L), ClassificationSource.CLOUD_SONNET)

        assertEquals(1, results.size)
        assertEquals(FileCategory.LAYAK_DIHAPUS, results.first().category)
        assertEquals(ClassificationSource.CLOUD_SONNET, results.first().source)
    }

    @Test
    fun `parse mengabaikan item dengan file_id yang tidak valid`() {
        val response = """[{"file_id": 99, "importance_score": 50, "category": "AMBIGU", "reason": "tidak jelas"}]"""

        val results = ClaudeClassificationResponseParser.parse(response, setOf(1L, 2L), ClassificationSource.CLOUD_HAIKU)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `parse mengabaikan item dengan kategori tidak dikenal`() {
        val response = """[{"file_id": 1, "importance_score": 50, "category": "TIDAK_DIKENAL", "reason": "x"}]"""

        val results = ClaudeClassificationResponseParser.parse(response, setOf(1L), ClassificationSource.CLOUD_HAIKU)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `parse menerima kategori dengan huruf kecil dan spasi`() {
        val response = """[{"file_id": 1, "importance_score": 50, "category": " ambigu ", "reason": "tidak jelas"}]"""

        val results = ClaudeClassificationResponseParser.parse(response, setOf(1L), ClassificationSource.CLOUD_HAIKU)

        assertEquals(FileCategory.AMBIGU, results.first().category)
    }

    @Test
    fun `parse membatasi importance_score ke rentang 0-100`() {
        val response = """
            [
              {"file_id": 1, "importance_score": 150, "category": "PENTING", "reason": "x"},
              {"file_id": 2, "importance_score": -10, "category": "LAYAK_DIHAPUS", "reason": "y"}
            ]
        """.trimIndent()

        val results = ClaudeClassificationResponseParser.parse(response, setOf(1L, 2L), ClassificationSource.CLOUD_HAIKU)

        assertEquals(100, results.first { it.fileId == 1L }.importanceScore)
        assertEquals(0, results.first { it.fileId == 2L }.importanceScore)
    }

    @Test
    fun `parse mengembalikan list kosong jika respons tidak memuat JSON array`() {
        val response = "Maaf, saya tidak dapat memproses permintaan ini."

        val results = ClaudeClassificationResponseParser.parse(response, setOf(1L), ClassificationSource.CLOUD_HAIKU)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `parse mengembalikan list kosong jika JSON tidak valid`() {
        val response = "[{ini bukan json valid}]"

        val results = ClaudeClassificationResponseParser.parse(response, setOf(1L), ClassificationSource.CLOUD_HAIKU)

        assertTrue(results.isEmpty())
    }
}
