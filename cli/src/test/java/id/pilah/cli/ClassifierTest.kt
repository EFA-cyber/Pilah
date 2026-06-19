package id.pilah.cli

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class ClassifierTest {

    private val weights = RuleWeights()

    @Test
    fun `file tanpa sinyal khusus diklasifikasikan sebagai Ambigu`() {
        val file = fileItem(path = "/home/user/Documents/catatan.txt", name = "catatan.txt")

        val result = Classifier.classify(file, RuleContext(now = NOW), weights)

        assertEquals(50, result.score)
        assertEquals(FileCategory.AMBIGU, result.category)
    }

    @Test
    fun `file duplikat diklasifikasikan sebagai Layak Dihapus`() {
        val file = fileItem(path = "/home/user/Pictures/copy.jpg", name = "copy.jpg", type = "jpg")
        val context = RuleContext(now = NOW, duplicateOfOriginalName = mapOf(file.path to "original.jpg"))

        val result = Classifier.classify(file, context, weights)

        assertEquals(0, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    @Test
    fun `file dengan nama dokumen penting diklasifikasikan sebagai Penting`() {
        val file = fileItem(path = "/home/user/Documents/ktp_2024.jpg", name = "ktp_2024.jpg", type = "jpg")

        val result = Classifier.classify(file, RuleContext(now = NOW), weights)

        assertEquals(85, result.score)
        assertEquals(FileCategory.PENTING, result.category)
    }

    @Test
    fun `foto kenangan di DCIM tanpa sinyal negatif tetap Ambigu`() {
        val file = fileItem(path = "/home/user/DCIM/Camera/liburan.jpg", name = "liburan.jpg", type = "jpg")

        val result = Classifier.classify(file, RuleContext(now = NOW), weights)

        assertEquals(65, result.score)
        assertEquals(FileCategory.AMBIGU, result.category)
    }

    @Test
    fun `screenshot lama yang belum dibuka diklasifikasikan sebagai Layak Dihapus`() {
        val file = fileItem(
            path = "/home/user/Pictures/Screenshots/screenshot_lama.png",
            name = "screenshot_lama.png",
            type = "png",
            createdAt = OLD,
        )

        val result = Classifier.classify(file, RuleContext(now = NOW), weights)

        assertEquals(25, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    @Test
    fun `file unduhan lama yang belum dibuka diklasifikasikan sebagai Layak Dihapus`() {
        val file = fileItem(
            path = "/home/user/Downloads/installer.zip",
            name = "installer.zip",
            type = "zip",
            createdAt = OLD,
        )

        val result = Classifier.classify(file, RuleContext(now = NOW), weights)

        assertEquals(30, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    private fun fileItem(
        path: String,
        name: String,
        type: String = "",
        createdAt: Instant = NOW,
    ): FileItem = FileItem(
        path = path,
        name = name,
        type = type,
        sizeBytes = 1_000L,
        createdAt = createdAt,
    )

    private companion object {
        val NOW: Instant = Instant.parse("2026-06-12T00:00:00Z")
        val OLD: Instant = NOW.minus(40, ChronoUnit.DAYS)
    }
}
