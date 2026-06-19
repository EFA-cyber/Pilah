package id.pilah.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.pilah.core.model.FileCategory
import java.time.Instant

/**
 * Koreksi pengguna sebagai sinyal pembelajaran preferensi lokal.
 * Tabel `user_corrections` pada ERD PRD.
 */
@Entity(
    tableName = "user_corrections",
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
data class UserCorrectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fileId: Long,
    val aiCategory: FileCategory,
    val userCategory: FileCategory,
    val correctedAt: Instant,
)
