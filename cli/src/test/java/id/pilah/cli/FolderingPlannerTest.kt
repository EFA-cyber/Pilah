package id.pilah.cli

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.io.File
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderingPlannerTest {

    private val root = File("/home/user/Documents")

    @Test
    fun `dokumen penting direncanakan pindah ke Dokumen Penting`() {
        val file = fileItem(path = "/home/user/Documents/ktp.pdf", name = "ktp.pdf", type = "pdf")

        val plan = FolderingPlanner.plan(root, listOf(file), mapOf(file.path to FileCategory.PENTING))

        val item = plan.single()
        assertEquals("Dokumen Penting", item.targetFolder)
        assertEquals("/home/user/Documents/Dokumen Penting/ktp.pdf", item.targetPath)
    }

    @Test
    fun `file layak dihapus direncanakan pindah ke Karantina`() {
        val file = fileItem(path = "/home/user/Documents/Download/installer.zip", name = "installer.zip", type = "zip")

        val plan = FolderingPlanner.plan(root, listOf(file), mapOf(file.path to FileCategory.LAYAK_DIHAPUS))

        val item = plan.single()
        assertEquals("Karantina", item.targetFolder)
        assertEquals("/home/user/Documents/Karantina/installer.zip", item.targetPath)
    }

    @Test
    fun `file ambigu tidak masuk rencana`() {
        val file = fileItem(path = "/home/user/Documents/catatan.txt", name = "catatan.txt", type = "txt")

        val plan = FolderingPlanner.plan(root, listOf(file), mapOf(file.path to FileCategory.AMBIGU))

        assertTrue(plan.isEmpty())
    }

    @Test
    fun `file tanpa kategori tidak masuk rencana`() {
        val file = fileItem(path = "/home/user/Documents/misteri.bin", name = "misteri.bin", type = "bin")

        val plan = FolderingPlanner.plan(root, listOf(file), emptyMap())

        assertTrue(plan.isEmpty())
    }

    @Test
    fun `dua file dengan nama sama ke folder tujuan yang sama diberi suffix unik`() {
        val files = listOf(
            fileItem(path = "/home/user/Documents/Camera/IMG_0001.jpg", name = "IMG_0001.jpg", type = "jpg"),
            fileItem(path = "/home/user/Documents/WhatsApp/IMG_0001.jpg", name = "IMG_0001.jpg", type = "jpg"),
        )
        val categories = files.associate { it.path to FileCategory.LAYAK_DIHAPUS }

        val plan = FolderingPlanner.plan(root, files, categories)

        val targetPaths = plan.map { it.targetPath }
        assertEquals(2, targetPaths.distinct().size)
        assertTrue(targetPaths.contains("/home/user/Documents/Karantina/IMG_0001.jpg"))
        assertTrue(targetPaths.contains("/home/user/Documents/Karantina/IMG_0001 (1).jpg"))
    }

    private fun fileItem(path: String, name: String, type: String): FileItem = FileItem(
        path = path,
        name = name,
        type = type,
        sizeBytes = 1_000L,
        createdAt = Instant.parse("2026-06-12T00:00:00Z"),
    )
}
