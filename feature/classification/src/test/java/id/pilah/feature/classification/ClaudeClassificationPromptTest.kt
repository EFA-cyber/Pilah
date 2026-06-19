package id.pilah.feature.classification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudeClassificationPromptTest {

    @Test
    fun `build menyertakan file_id, nama, dan cuplikan setiap item`() {
        val items = listOf(
            CloudClassificationInput(fileId = 1L, fileName = "laporan.pdf", textSnippet = "Isi laporan tahunan"),
            CloudClassificationInput(fileId = 2L, fileName = "draft.docx", textSnippet = "Draft skripsi"),
        )

        val prompt = ClaudeClassificationPrompt.build(items)

        assertTrue(prompt.contains("file_id: 1, nama: \"laporan.pdf\", cuplikan: \"Isi laporan tahunan\""))
        assertTrue(prompt.contains("file_id: 2, nama: \"draft.docx\", cuplikan: \"Draft skripsi\""))
    }

    @Test
    fun `build mengganti tanda kutip ganda pada cuplikan dengan kutip tunggal`() {
        val items = listOf(
            CloudClassificationInput(fileId = 1L, fileName = "catatan.txt", textSnippet = "Judul \"Penting\" di sini"),
        )

        val prompt = ClaudeClassificationPrompt.build(items)

        assertTrue(prompt.contains("cuplikan: \"Judul 'Penting' di sini\""))
        assertFalse(prompt.contains("\\\""))
    }

    @Test
    fun `build memotong cuplikan yang lebih panjang dari batas maksimum`() {
        val longSnippet = "a".repeat(2_000)
        val items = listOf(
            CloudClassificationInput(fileId = 1L, fileName = "panjang.txt", textSnippet = longSnippet),
        )

        val prompt = ClaudeClassificationPrompt.build(items)

        assertTrue(prompt.contains("cuplikan: \"${"a".repeat(1_000)}\""))
        assertFalse(prompt.contains("a".repeat(1_001)))
    }

    @Test
    fun `build menyertakan kontrak format JSON balasan`() {
        val prompt = ClaudeClassificationPrompt.build(emptyList())

        assertTrue(prompt.contains("JSON array"))
        assertTrue(prompt.contains("\"file_id\""))
        assertTrue(prompt.contains("PENTING|LAYAK_DIHAPUS|AMBIGU"))
    }
}
