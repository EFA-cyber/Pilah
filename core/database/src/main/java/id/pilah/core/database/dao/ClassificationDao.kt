package id.pilah.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import id.pilah.core.database.entity.ClassificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassificationDao {

    @Insert
    suspend fun insert(classification: ClassificationEntity): Long

    @Insert
    suspend fun insertAll(classifications: List<ClassificationEntity>): List<Long>

    @Query(
        """
        SELECT * FROM classifications
        WHERE fileId = :fileId
        ORDER BY classifiedAt DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestForFile(fileId: Long): ClassificationEntity?

    @Query("SELECT * FROM classifications WHERE fileId = :fileId ORDER BY classifiedAt DESC")
    fun observeHistoryForFile(fileId: Long): Flow<List<ClassificationEntity>>

    /** Klasifikasi terbaru untuk setiap file, dipakai layar Tinjau Hasil (Penting/Layak Dihapus). */
    @Query(
        """
        SELECT c.* FROM classifications c
        INNER JOIN (
            SELECT fileId, MAX(classifiedAt) AS latestAt
            FROM classifications
            GROUP BY fileId
        ) latest ON c.fileId = latest.fileId AND c.classifiedAt = latest.latestAt
        """,
    )
    fun observeLatestPerFile(): Flow<List<ClassificationEntity>>
}
