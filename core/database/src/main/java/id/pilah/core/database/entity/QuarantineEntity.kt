package id.pilah.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.pilah.core.model.QuarantineStatus
import java.time.Instant

/**
 * File layak hapus yang menunggu 30 hari sebelum dihapus permanen.
 * Tabel `quarantine` pada ERD PRD.
 */
@Entity(
    tableName = "quarantine",
    foreignKeys = [
        ForeignKey(
            entity = FileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["fileId"], unique = true), Index(value = ["status"])],
)
data class QuarantineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fileId: Long,
    val quarantinedAt: Instant,
    val purgeAfter: Instant,
    val status: QuarantineStatus,
)
