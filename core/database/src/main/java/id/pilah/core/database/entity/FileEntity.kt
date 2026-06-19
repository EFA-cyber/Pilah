package id.pilah.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/** Indeks metadata seluruh file hasil pemindaian. Tabel `files` pada ERD PRD. */
@Entity(
    tableName = "files",
    indices = [Index(value = ["path"], unique = true), Index(value = ["hash"])],
)
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val path: String,
    val name: String,
    val type: String,
    val sizeBytes: Long,
    val hash: String? = null,
    val lastOpened: Instant? = null,
    val createdAt: Instant,
)
