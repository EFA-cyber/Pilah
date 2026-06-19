package id.pilah.feature.foldering

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderingPlannerTest {

    @Test
    fun `dokumen penting direncanakan pindah ke Dokumen Penting`() {
        val file = fileItem(id = 1, path = "/storage/emulated/0/Documents/ktp.pdf", name = "ktp.pdf", type = "pdf")

        val plan = FolderingPlanner.plan(listOf(file), mapOf(1L to FileCategory.PENTING))

        assertEquals(1, plan.items.size)
        val item = plan.items.single()
        assertEquals("Dokumen Penting", item.targetFolder)
        assertEquals("/storage/emulated/0/Dokumen Penting/ktp.pdf", item.targetPath)
    }

    @Test
    fun `file layak dihapus direncanakan pindah ke Karantina`() {
        val file = fileItem(id = 2, path = "/storage/emulated/0/Download/installer.zip", name = "installer.zip", type = "zip")

        val plan = FolderingPlanner.plan(listOf(file), mapOf(2L to FileCategory.LAYAK_DIHAPUS))

        val item = plan.items.single()
        assertEquals("Karantina", item.targetFolder)
        assertEquals("/storage/emulated/0/Karantina/installer.zip", item.targetPath)
    }

    @Test
    fun `file ambigu tidak masuk rencana`() {
        val file = fileItem(id = 3, path = "/storage/emulated/0/Documents/catatan.txt", name = "catatan.txt", type = "txt")

        val plan = FolderingPlanner.plan(listOf(file), mapOf(3L to FileCategory.AMBIGU))

        assertTrue(plan.items.isEmpty())
        assertTrue(plan.summaries.isEmpty())
    }

    @Test
    fun `file yang sudah berada di folder tujuan tidak masuk rencana`() {
        val file = fileItem(id = 4, path = "/storage/emulated/0/Karantina/lama.zip", name = "lama.zip", type = "zip")

        val plan = FolderingPlanner.plan(listOf(file), mapOf(4L to FileCategory.LAYAK_DIHAPUS))

        assertTrue(plan.items.isEmpty())
    }

    @Test
    fun `file tanpa kategori efektif tidak masuk rencana`() {
        val file = fileItem(id = 5, path = "/storage/emulated/0/Documents/misteri.bin", name = "misteri.bin", type = "bin")

        val plan = FolderingPlanner.plan(listOf(file), emptyMap())

        assertTrue(plan.items.isEmpty())
    }

    @Test
    fun `ringkasan menggabungkan jumlah file dan ukuran per folder tujuan`() {
        val files = listOf(
            fileItem(id = 10, path = "/storage/emulated/0/Documents/ktp.pdf", name = "ktp.pdf", type = "pdf", sizeBytes = 1_000L),
            fileItem(id = 11, path = "/storage/emulated/0/Documents/ijazah.pdf", name = "ijazah.pdf", type = "pdf", sizeBytes = 2_000L),
        )
        val categories = mapOf(10L to FileCategory.PENTING, 11L to FileCategory.PENTING)

        val plan = FolderingPlanner.plan(files, categories)

        assertEquals(1, plan.summaries.size)
        val summary = plan.summaries.single()
        assertEquals("Dokumen Penting", summary.targetFolder)
        assertEquals(2, summary.fileCount)
        assertEquals(3_000L, summary.totalSizeBytes)
    }

    @Test
    fun `dua file dengan nama sama ke folder tujuan yang sama diberi suffix unik`() {
        val files = listOf(
            fileItem(id = 30, path = "/storage/emulated/0/DCIM/Camera/IMG_0001.jpg", name = "IMG_0001.jpg", type = "jpg"),
            fileItem(id = 31, path = "/storage/emulated/0/WhatsApp/Media/IMG_0001.jpg", name = "IMG_0001.jpg", type = "jpg"),
        )
        val categories = mapOf(30L to FileCategory.LAYAK_DIHAPUS, 31L to FileCategory.LAYAK_DIHAPUS)

        val plan = FolderingPlanner.plan(files, categories)

        val targetPaths = plan.items.map { it.targetPath }
        assertEquals(2, targetPaths.distinct().size)
        assertTrue(targetPaths.contains("/storage/emulated/0/Karantina/IMG_0001.jpg"))
        assertTrue(targetPaths.contains("/storage/emulated/0/Karantina/IMG_0001 (1).jpg"))
    }

    @Test
    fun `file yang akan dipindah tidak bertabrakan dengan path file lain yang sudah ada di folder tujuan`() {
        val files = listOf(
            fileItem(id = 40, path = "/storage/emulated/0/Karantina/IMG_0002.jpg", name = "IMG_0002.jpg", type = "jpg"),
            fileItem(id = 41, path = "/storage/emulated/0/DCIM/Camera/IMG_0002.jpg", name = "IMG_0002.jpg", type = "jpg"),
        )
        val categories = mapOf(40L to FileCategory.LAYAK_DIHAPUS, 41L to FileCategory.LAYAK_DIHAPUS)

        val plan = FolderingPlanner.plan(files, categories)

        // File 40 sudah di Karantina, jadi tidak masuk rencana; file 41 harus dapat nama baru.
        val item = plan.items.single()
        assertEquals(41L, item.file.id)
        assertEquals("/storage/emulated/0/Karantina/IMG_0002 (1).jpg", item.targetPath)
    }

    @Test
    fun `foto penting di kartu SD direncanakan pindah ke Foto Kenangan di volume yang sama`() {
        val file = fileItem(id = 20, path = "/storage/ABCD-1234/DCIM/Camera/liburan.jpg", name = "liburan.jpg", type = "jpg")

        val plan = FolderingPlanner.plan(listOf(file), mapOf(20L to FileCategory.PENTING))

        val item = plan.items.single()
        assertEquals("Foto Kenangan", item.targetFolder)
        assertEquals("/storage/ABCD-1234/Foto Kenangan/liburan.jpg", item.targetPath)
    }

    private fun fileItem(id: Long, path: String, name: String, type: String, sizeBytes: Long = 1_000L): FileItem = FileItem(
        id = id,
        path = path,
        name = name,
        type = type,
        sizeBytes = sizeBytes,
        createdAt = Instant.parse("2026-06-12T00:00:00Z"),
    )
}
