package id.pilah.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.pilah.core.model.ActionType
import java.time.Instant

/**
 * Log semua aksi pindah/karantina/pulihkan/hapus untuk audit dan undo.
 * Tabel `actions` pada ERD PRD.
 */
@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = FileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["fileId"])],
)
data class ActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fileId: Long,
    val actionType: ActionType,
    val fromPath: String,
    val toPath: String,
    val executedAt: Instant,
)
