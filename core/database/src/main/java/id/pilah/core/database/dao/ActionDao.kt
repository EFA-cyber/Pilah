package id.pilah.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import id.pilah.core.database.entity.ActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {

    @Insert
    suspend fun insert(action: ActionEntity): Long

    @Query("SELECT * FROM actions WHERE fileId = :fileId ORDER BY executedAt DESC")
    fun observeForFile(fileId: Long): Flow<List<ActionEntity>>

    @Query("SELECT * FROM actions ORDER BY executedAt DESC")
    fun observeAll(): Flow<List<ActionEntity>>

    @Query(
        """
        SELECT * FROM actions
        WHERE fileId = :fileId
        ORDER BY executedAt DESC
        LIMIT 1
        """,
    )
    suspend fun getLastForFile(fileId: Long): ActionEntity?
}
