package id.pilah.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import id.pilah.core.database.entity.QuarantineEntity
import id.pilah.core.model.QuarantineStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface QuarantineDao {

    @Insert
    suspend fun insert(entry: QuarantineEntity): Long

    @Update
    suspend fun update(entry: QuarantineEntity)

    @Query("SELECT * FROM quarantine WHERE status = :status ORDER BY purgeAfter ASC")
    fun observeByStatus(status: QuarantineStatus = QuarantineStatus.ACTIVE): Flow<List<QuarantineEntity>>

    /** Entri aktif yang sudah lewat 30 hari, dipakai worker auto-purge harian. */
    @Query("SELECT * FROM quarantine WHERE status = :status AND purgeAfter <= :now")
    suspend fun getExpired(now: Instant, status: QuarantineStatus = QuarantineStatus.ACTIVE): List<QuarantineEntity>

    @Query("SELECT * FROM quarantine WHERE fileId = :fileId")
    suspend fun getByFileId(fileId: Long): QuarantineEntity?
}
