package id.pilah.feature.dashboard

import id.pilah.core.model.ActionType
import id.pilah.core.model.FileAction
import id.pilah.core.model.FileItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanupHistoryAggregatorTest {

    @Test
    fun `aksi pindah dan karantina pada tanggal yang sama dikelompokkan menjadi satu sesi`() {
        val files = listOf(fileItem(id = 1, sizeBytes = 1_000L), fileItem(id = 2, sizeBytes = 2_000L))
        val executedAt = Instant.parse("2026-06-12T10:00:00Z")
        val actions = listOf(
            fileAction(fileId = 1, actionType = ActionType.MOVE, executedAt = executedAt),
            fileAction(fileId = 2, actionType = ActionType.QUARANTINE, executedAt = executedAt),
        )

        val result = CleanupHistoryAggregator.aggregate(files, actions)

        assertEquals(1, result.size)
        val session = result.single()
        assertEquals(2, session.fileCount)
        assertEquals(3_000L, session.totalSizeBytes)
        assertEquals(executedAt.atZone(ZoneId.systemDefault()).toLocalDate(), session.date)
    }

    @Test
    fun `aksi pulihkan dan hapus permanen tidak masuk riwayat rapikan`() {
        val files = listOf(fileItem(id = 1, sizeBytes = 1_000L))
        val actions = listOf(
            fileAction(fileId = 1, actionType = ActionType.RESTORE, executedAt = Instant.parse("2026-06-12T10:00:00Z")),
            fileAction(fileId = 1, actionType = ActionType.PURGE, executedAt = Instant.parse("2026-06-12T11:00:00Z")),
        )

        val result = CleanupHistoryAggregator.aggregate(files, actions)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `sesi diurutkan dari tanggal terbaru`() {
        val files = listOf(fileItem(id = 1, sizeBytes = 1_000L), fileItem(id = 2, sizeBytes = 1_000L))
        val actions = listOf(
            fileAction(fileId = 1, actionType = ActionType.MOVE, executedAt = Instant.parse("2026-06-10T10:00:00Z")),
            fileAction(fileId = 2, actionType = ActionType.MOVE, executedAt = Instant.parse("2026-06-12T10:00:00Z")),
        )

        val result = CleanupHistoryAggregator.aggregate(files, actions)

        assertEquals(2, result.size)
        assertEquals(
            Instant.parse("2026-06-12T10:00:00Z").atZone(ZoneId.systemDefault()).toLocalDate(),
            result.first().date,
        )
    }

    @Test
    fun `ukuran file menggunakan 0 jika file tidak ditemukan`() {
        val actions = listOf(
            fileAction(fileId = 99, actionType = ActionType.MOVE, executedAt = Instant.parse("2026-06-12T10:00:00Z")),
        )

        val result = CleanupHistoryAggregator.aggregate(emptyList(), actions)

        assertEquals(1, result.size)
        assertEquals(0L, result.single().totalSizeBytes)
    }

    private fun fileItem(id: Long, sizeBytes: Long): FileItem = FileItem(
        id = id,
        path = "/storage/emulated/0/Documents/file-$id.pdf",
        name = "file-$id.pdf",
        type = "pdf",
        sizeBytes = sizeBytes,
        createdAt = Instant.parse("2026-06-12T00:00:00Z"),
    )

    private fun fileAction(fileId: Long, actionType: ActionType, executedAt: Instant): FileAction = FileAction(
        fileId = fileId,
        actionType = actionType,
        fromPath = "/storage/emulated/0/Download/file-$fileId.pdf",
        toPath = "/storage/emulated/0/Karantina/file-$fileId.pdf",
        executedAt = executedAt,
    )
}
