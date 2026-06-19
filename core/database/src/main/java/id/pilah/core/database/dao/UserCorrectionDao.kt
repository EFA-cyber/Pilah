package id.pilah.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import id.pilah.core.database.entity.UserCorrectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCorrectionDao {

    @Insert
    suspend fun insert(correction: UserCorrectionEntity): Long

    @Query("SELECT * FROM user_corrections ORDER BY correctedAt DESC")
    fun observeAll(): Flow<List<UserCorrectionEntity>>

    @Query("SELECT * FROM user_corrections WHERE fileId = :fileId ORDER BY correctedAt DESC")
    fun observeForFile(fileId: Long): Flow<List<UserCorrectionEntity>>
}
