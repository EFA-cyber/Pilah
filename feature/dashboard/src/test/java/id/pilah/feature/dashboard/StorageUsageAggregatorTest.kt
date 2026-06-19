package id.pilah.feature.dashboard

import id.pilah.core.model.Classification
import id.pilah.core.model.ClassificationSource
import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import id.pilah.core.model.UserCorrection
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageUsageAggregatorTest {

    @Test
    fun `file dikelompokkan berdasarkan kategori klasifikasi terbaru`() {
        val files = listOf(
            fileItem(id = 1, sizeBytes = 1_000L),
            fileItem(id = 2, sizeBytes = 2_000L),
            fileItem(id = 3, sizeBytes = 500L),
        )
        val classifications = listOf(
            classification(fileId = 1, category = FileCategory.PENTING),
            classification(fileId = 2, category = FileCategory.PENTING),
            classification(fileId = 3, category = FileCategory.LAYAK_DIHAPUS),
        )

        val result = StorageUsageAggregator.aggregate(files, classifications, emptyList())

        assertEquals(2, result.size)
        val penting = result.single { it.category == FileCategory.PENTING }
        assertEquals(2, penting.fileCount)
        assertEquals(3_000L, penting.totalSizeBytes)
        val layakDihapus = result.single { it.category == FileCategory.LAYAK_DIHAPUS }
        assertEquals(1, layakDihapus.fileCount)
        assertEquals(500L, layakDihapus.totalSizeBytes)
    }

    @Test
    fun `koreksi pengguna terbaru menggantikan kategori dari klasifikasi AI`() {
        val files = listOf(fileItem(id = 1, sizeBytes = 1_000L))
        val classifications = listOf(classification(fileId = 1, category = FileCategory.LAYAK_DIHAPUS))
        val corrections = listOf(
            userCorrection(fileId = 1, userCategory = FileCategory.PENTING, correctedAt = Instant.parse("2026-06-10T00:00:00Z")),
        )

        val result = StorageUsageAggregator.aggregate(files, classifications, corrections)

        assertEquals(1, result.size)
        assertEquals(FileCategory.PENTING, result.single().category)
    }

    @Test
    fun `file tanpa klasifikasi tidak dihitung`() {
        val files = listOf(fileItem(id = 1, sizeBytes = 1_000L), fileItem(id = 2, sizeBytes = 500L))
        val classifications = listOf(classification(fileId = 1, category = FileCategory.PENTING))

        val result = StorageUsageAggregator.aggregate(files, classifications, emptyList())

        assertEquals(1, result.size)
        assertEquals(1, result.single().fileCount)
    }

    @Test
    fun `hasil diurutkan dari ukuran terbesar`() {
        val files = listOf(
            fileItem(id = 1, sizeBytes = 500L),
            fileItem(id = 2, sizeBytes = 5_000L),
            fileItem(id = 3, sizeBytes = 1_500L),
        )
        val classifications = listOf(
            classification(fileId = 1, category = FileCategory.AMBIGU),
            classification(fileId = 2, category = FileCategory.PENTING),
            classification(fileId = 3, category = FileCategory.LAYAK_DIHAPUS),
        )

        val result = StorageUsageAggregator.aggregate(files, classifications, emptyList())

        assertEquals(listOf(FileCategory.PENTING, FileCategory.LAYAK_DIHAPUS, FileCategory.AMBIGU), result.map { it.category })
    }

    @Test
    fun `tanpa file tidak menghasilkan kategori apapun`() {
        val result = StorageUsageAggregator.aggregate(emptyList(), emptyList(), emptyList())

        assertTrue(result.isEmpty())
    }

    private fun fileItem(id: Long, sizeBytes: Long): FileItem = FileItem(
        id = id,
        path = "/storage/emulated/0/Documents/file-$id.pdf",
        name = "file-$id.pdf",
        type = "pdf",
        sizeBytes = sizeBytes,
        createdAt = Instant.parse("2026-06-12T00:00:00Z"),
    )

    private fun classification(fileId: Long, category: FileCategory): Classification = Classification(
        fileId = fileId,
        importanceScore = 50,
        category = category,
        reason = "uji",
        source = ClassificationSource.LOCAL_RULE,
        classifiedAt = Instant.parse("2026-06-12T00:00:00Z"),
    )

    private fun userCorrection(fileId: Long, userCategory: FileCategory, correctedAt: Instant): UserCorrection = UserCorrection(
        fileId = fileId,
        aiCategory = FileCategory.AMBIGU,
        userCategory = userCategory,
        correctedAt = correctedAt,
    )
}
