package id.pilah.core.database.model

import id.pilah.core.database.entity.ActionEntity
import id.pilah.core.database.entity.ClassificationEntity
import id.pilah.core.database.entity.FileEntity
import id.pilah.core.database.entity.QuarantineEntity
import id.pilah.core.database.entity.UserCorrectionEntity
import id.pilah.core.model.ActionType
import id.pilah.core.model.Classification
import id.pilah.core.model.ClassificationSource
import id.pilah.core.model.FileAction
import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import id.pilah.core.model.QuarantineEntry
import id.pilah.core.model.QuarantineStatus
import id.pilah.core.model.UserCorrection
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMappersTest {

    @Test
    fun `FileEntity dan FileItem saling konversi tanpa kehilangan data`() {
        val entity = FileEntity(
            id = 1L,
            path = "/storage/emulated/0/Documents/laporan.pdf",
            name = "laporan.pdf",
            type = "pdf",
            sizeBytes = 2_048L,
            hash = "abc123",
            lastOpened = NOW.minusSeconds(60),
            createdAt = NOW,
        )

        val domain = entity.toDomain()

        assertEquals(FileItem::class, domain::class)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun `ClassificationEntity dan Classification saling konversi tanpa kehilangan data`() {
        val entity = ClassificationEntity(
            id = 2L,
            fileId = 1L,
            importanceScore = 80,
            category = FileCategory.PENTING,
            reason = "Dokumen identitas",
            source = ClassificationSource.CLOUD_HAIKU,
            classifiedAt = NOW,
        )

        val domain = entity.toDomain()

        assertEquals(Classification::class, domain::class)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun `ActionEntity dan FileAction saling konversi tanpa kehilangan data`() {
        val entity = ActionEntity(
            id = 3L,
            fileId = 1L,
            actionType = ActionType.MOVE,
            fromPath = "/storage/emulated/0/Download/file.pdf",
            toPath = "/storage/emulated/0/Documents/file.pdf",
            executedAt = NOW,
        )

        val domain = entity.toDomain()

        assertEquals(FileAction::class, domain::class)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun `QuarantineEntity dan QuarantineEntry saling konversi tanpa kehilangan data`() {
        val entity = QuarantineEntity(
            id = 4L,
            fileId = 1L,
            quarantinedAt = NOW,
            purgeAfter = NOW.plusSeconds(30L * 24 * 60 * 60),
            status = QuarantineStatus.ACTIVE,
        )

        val domain = entity.toDomain()

        assertEquals(QuarantineEntry::class, domain::class)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun `UserCorrectionEntity dan UserCorrection saling konversi tanpa kehilangan data`() {
        val entity = UserCorrectionEntity(
            id = 5L,
            fileId = 1L,
            aiCategory = FileCategory.AMBIGU,
            userCategory = FileCategory.PENTING,
            correctedAt = NOW,
        )

        val domain = entity.toDomain()

        assertEquals(UserCorrection::class, domain::class)
        assertEquals(entity, domain.toEntity())
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-06-12T00:00:00Z")
    }
}
