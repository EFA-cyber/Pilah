package id.pilah.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.pilah.core.database.converter.Converters
import id.pilah.core.database.dao.ActionDao
import id.pilah.core.database.dao.ClassificationDao
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.dao.QuarantineDao
import id.pilah.core.database.dao.UserCorrectionDao
import id.pilah.core.database.entity.ActionEntity
import id.pilah.core.database.entity.ClassificationEntity
import id.pilah.core.database.entity.FileEntity
import id.pilah.core.database.entity.QuarantineEntity
import id.pilah.core.database.entity.UserCorrectionEntity

@Database(
    entities = [
        FileEntity::class,
        ClassificationEntity::class,
        ActionEntity::class,
        QuarantineEntity::class,
        UserCorrectionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PilahDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun classificationDao(): ClassificationDao
    abstract fun actionDao(): ActionDao
    abstract fun quarantineDao(): QuarantineDao
    abstract fun userCorrectionDao(): UserCorrectionDao

    companion object {
        const val DATABASE_NAME = "pilah.db"
    }
}
