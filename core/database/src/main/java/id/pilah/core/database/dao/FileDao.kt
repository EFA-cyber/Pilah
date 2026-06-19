package id.pilah.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import id.pilah.core.database.entity.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Upsert
    suspend fun upsert(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(files: List<FileEntity>): List<Long>

    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getById(id: Long): FileEntity?

    @Query("SELECT * FROM files WHERE path = :path")
    suspend fun getByPath(path: String): FileEntity?

    @Query("SELECT * FROM files ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE hash = :hash")
    suspend fun getByHash(hash: String): List<FileEntity>

    @Query("SELECT COUNT(*) FROM files")
    suspend fun count(): Int

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteById(id: Long)
}
