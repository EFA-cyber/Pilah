package id.pilah.feature.classification

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultRuleEngineTest {

    private val ruleEngine = DefaultRuleEngine()
    private val weights = RuleWeights()

    @Test
    fun `file tanpa sinyal khusus diklasifikasikan sebagai Ambigu`() {
        val file = fileItem(path = "/storage/emulated/0/Documents/catatan.txt", name = "catatan.txt")

        val result = ruleEngine.classify(file, RuleContext(now = NOW), weights)

        assertEquals(50, result.score)
        assertEquals(FileCategory.AMBIGU, result.category)
    }

    @Test
    fun `file duplikat diklasifikasikan sebagai Layak Dihapus`() {
        val file = fileItem(path = "/storage/emulated/0/DCIM/copy.jpg", name = "copy.jpg", type = "jpg")
        val context = RuleContext(now = NOW, duplicateOfOriginalName = mapOf(file.path to "original.jpg"))

        val result = ruleEngine.classify(file, context, weights)

        assertEquals(0, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    @Test
    fun `file dengan nama dokumen penting diklasifikasikan sebagai Penting`() {
        val file = fileItem(path = "/storage/emulated/0/Documents/ktp_2024.jpg", name = "ktp_2024.jpg", type = "jpg")

        val result = ruleEngine.classify(file, RuleContext(now = NOW), weights)

        assertEquals(85, result.score)
        assertEquals(FileCategory.PENTING, result.category)
    }

    @Test
    fun `foto kenangan di DCIM tanpa sinyal negatif tetap Ambigu`() {
        val file = fileItem(path = "/storage/emulated/0/DCIM/Camera/liburan.jpg", name = "liburan.jpg", type = "jpg")

        val result = ruleEngine.classify(file, RuleContext(now = NOW), weights)

        assertEquals(65, result.score)
        assertEquals(FileCategory.AMBIGU, result.category)
    }

    @Test
    fun `foto kenangan yang juga duplikat tidak mendapat bonus memori`() {
        val file = fileItem(path = "/storage/emulated/0/DCIM/Camera/liburan.jpg", name = "liburan.jpg", type = "jpg")
        val context = RuleContext(now = NOW, duplicateOfOriginalName = mapOf(file.path to "original.jpg"))

        val result = ruleEngine.classify(file, context, weights)

        assertEquals(0, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    @Test
    fun `foto buram diklasifikasikan sebagai Layak Dihapus`() {
        val file = fileItem(path = "/storage/emulated/0/Pictures/blur.jpg", name = "blur.jpg", type = "jpg")
        val context = RuleContext(now = NOW, blurVariance = mapOf(file.path to 50.0))

        val result = ruleEngine.classify(file, context, weights)

        assertEquals(30, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    @Test
    fun `screenshot lama yang belum dibuka diklasifikasikan sebagai Layak Dihapus`() {
        val file = fileItem(
            path = "/storage/emulated/0/Pictures/Screenshots/screenshot_lama.png",
            name = "screenshot_lama.png",
            type = "png",
            createdAt = OLD,
        )

        val result = ruleEngine.classify(file, RuleContext(now = NOW), weights)

        assertEquals(25, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    @Test
    fun `file unduhan lama yang belum dibuka diklasifikasikan sebagai Layak Dihapus`() {
        val file = fileItem(
            path = "/storage/emulated/0/Download/installer.zip",
            name = "installer.zip",
            type = "zip",
            createdAt = OLD,
        )

        val result = ruleEngine.classify(file, RuleContext(now = NOW), weights)

        assertEquals(30, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    @Test
    fun `APK yang sudah terinstal diklasifikasikan sebagai Layak Dihapus`() {
        val file = fileItem(path = "/storage/emulated/0/Download/app.apk", name = "app.apk", type = "apk")
        val context = RuleContext(now = NOW, installedApkPaths = setOf(file.path))

        val result = ruleEngine.classify(file, context, weights)

        assertEquals(10, result.score)
        assertEquals(FileCategory.LAYAK_DIHAPUS, result.category)
    }

    private fun fileItem(
        path: String,
        name: String,
        type: String = "",
        lastOpened: Instant? = null,
        createdAt: Instant = NOW,
    ): FileItem = FileItem(
        path = path,
        name = name,
        type = type,
        sizeBytes = 1_000L,
        lastOpened = lastOpened,
        createdAt = createdAt,
    )

    private companion object {
        val NOW: Instant = Instant.parse("2026-06-12T00:00:00Z")
        val OLD: Instant = NOW.minus(40, ChronoUnit.DAYS)
    }
}
