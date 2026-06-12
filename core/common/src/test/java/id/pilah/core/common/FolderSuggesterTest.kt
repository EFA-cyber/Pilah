package id.pilah.core.common

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FolderSuggesterTest {

    @Test
    fun `file layak dihapus diusulkan ke Karantina`() {
        val file = fileItem(path = "/storage/emulated/0/Download/installer.zip", name = "installer.zip", type = "zip")

        assertEquals("Karantina", FolderSuggester.suggest(file, FileCategory.LAYAK_DIHAPUS))
    }

    @Test
    fun `file ambigu tidak punya usulan folder`() {
        val file = fileItem(path = "/storage/emulated/0/Documents/catatan.txt", name = "catatan.txt", type = "txt")

        assertNull(FolderSuggester.suggest(file, FileCategory.AMBIGU))
    }

    @Test
    fun `foto penting di DCIM diusulkan ke Foto Kenangan`() {
        val file = fileItem(path = "/storage/emulated/0/DCIM/Camera/liburan.jpg", name = "liburan.jpg", type = "jpg")

        assertEquals("Foto Kenangan", FolderSuggester.suggest(file, FileCategory.PENTING))
    }

    @Test
    fun `dokumen pdf penting diusulkan ke Dokumen Penting`() {
        val file = fileItem(path = "/storage/emulated/0/Documents/ktp.pdf", name = "ktp.pdf", type = "pdf")

        assertEquals("Dokumen Penting", FolderSuggester.suggest(file, FileCategory.PENTING))
    }

    @Test
    fun `spreadsheet penting diusulkan ke Kerja & Bisnis`() {
        val file = fileItem(path = "/storage/emulated/0/Documents/laporan.xlsx", name = "laporan.xlsx", type = "xlsx")

        assertEquals("Kerja & Bisnis", FolderSuggester.suggest(file, FileCategory.PENTING))
    }

    @Test
    fun `file penting lainnya diusulkan ke Arsip`() {
        val file = fileItem(path = "/storage/emulated/0/Documents/catatan.txt", name = "catatan.txt", type = "txt")

        assertEquals("Arsip", FolderSuggester.suggest(file, FileCategory.PENTING))
    }

    private fun fileItem(path: String, name: String, type: String): FileItem = FileItem(
        path = path,
        name = name,
        type = type,
        sizeBytes = 1_000L,
        createdAt = Instant.parse("2026-06-12T00:00:00Z"),
    )
}
