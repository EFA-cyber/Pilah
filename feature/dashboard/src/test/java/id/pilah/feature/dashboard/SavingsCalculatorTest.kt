package id.pilah.feature.dashboard

import id.pilah.core.model.ActionType
import id.pilah.core.model.FileAction
import id.pilah.core.model.FileItem
import id.pilah.core.model.QuarantineEntry
import id.pilah.core.model.QuarantineStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class SavingsCalculatorTest {

    @Test
    fun `total ruang dihemat dihitung dari file yang sudah dihapus permanen`() {
        val files = listOf(fileItem(id = 1, sizeBytes = 1_000L), fileItem(id = 2, sizeBytes = 2_000L))
        val actions = listOf(
            fileAction(fileId = 1, actionType = ActionType.PURGE),
            fileAction(fileId = 2, actionType = ActionType.MOVE),
        )

        val result = SavingsCalculator.calculate(files, actions, emptyList())

        assertEquals(1_000L, result.savedBytes)
        assertEquals(0L, result.potentialBytes)
    }

    @Test
    fun `potensi hemat dihitung dari file aktif di karantina`() {
        val files = listOf(fileItem(id = 1, sizeBytes = 1_000L), fileItem(id = 2, sizeBytes = 2_000L))
        val activeQuarantine = listOf(quarantineEntry(fileId = 2))

        val result = SavingsCalculator.calculate(files, emptyList(), activeQuarantine)

        assertEquals(0L, result.savedBytes)
        assertEquals(2_000L, result.potentialBytes)
    }

    @Test
    fun `purge ganda pada file yang sama tidak dihitung dobel`() {
        val files = listOf(fileItem(id = 1, sizeBytes = 1_000L))
        val actions = listOf(
            fileAction(fileId = 1, actionType = ActionType.PURGE),
            fileAction(fileId = 1, actionType = ActionType.PURGE),
        )

        val result = SavingsCalculator.calculate(files, actions, emptyList())

        assertEquals(1_000L, result.savedBytes)
    }

    @Test
    fun `tanpa data menghasilkan ringkasan kosong`() {
        val result = SavingsCalculator.calculate(emptyList(), emptyList(), emptyList())

        assertEquals(0L, result.savedBytes)
        assertEquals(0L, result.potentialBytes)
    }

    private fun fileItem(id: Long, sizeBytes: Long): FileItem = FileItem(
        id = id,
        path = "/storage/emulated/0/Documents/file-$id.pdf",
        name = "file-$id.pdf",
        type = "pdf",
        sizeBytes = sizeBytes,
        createdAt = Instant.parse("2026-06-12T00:00:00Z"),
    )

    private fun fileAction(fileId: Long, actionType: ActionType): FileAction = FileAction(
        fileId = fileId,
        actionType = actionType,
        fromPath = "/storage/emulated/0/Download/file-$fileId.pdf",
        toPath = "",
        executedAt = Instant.parse("2026-06-12T00:00:00Z"),
    )

    private fun quarantineEntry(fileId: Long): QuarantineEntry = QuarantineEntry(
        fileId = fileId,
        quarantinedAt = Instant.parse("2026-06-01T00:00:00Z"),
        purgeAfter = Instant.parse("2026-07-01T00:00:00Z"),
        status = QuarantineStatus.ACTIVE,
    )
}
