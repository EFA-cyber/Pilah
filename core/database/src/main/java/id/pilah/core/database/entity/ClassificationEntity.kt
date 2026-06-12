package id.pilah.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.pilah.core.model.ClassificationSource
import id.pilah.core.model.FileCategory
import java.time.Instant

/**
 * Hasil penilaian AI/lokal per file beserta skor dan alasan.
 * Tabel `classifications` pada ERD PRD. Riwayat tetap disimpan; klasifikasi
 * yang berlaku adalah entri dengan `classifiedAt` terbaru per `fileId`.
 */
@Entity(
    tableName = "classifications",
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
data class ClassificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fileId: Long,
    val importanceScore: Int,
    val category: FileCategory,
    val reason: String,
    val source: ClassificationSource,
    val classifiedAt: Instant,
)
