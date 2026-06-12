package id.pilah.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.pilah.core.database.PilahDatabase
import id.pilah.core.database.dao.ActionDao
import id.pilah.core.database.dao.ClassificationDao
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.dao.QuarantineDao
import id.pilah.core.database.dao.UserCorrectionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePilahDatabase(@ApplicationContext context: Context): PilahDatabase =
        Room.databaseBuilder(context, PilahDatabase::class.java, PilahDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun provideFileDao(database: PilahDatabase): FileDao = database.fileDao()

    @Provides
    fun provideClassificationDao(database: PilahDatabase): ClassificationDao = database.classificationDao()

    @Provides
    fun provideActionDao(database: PilahDatabase): ActionDao = database.actionDao()

    @Provides
    fun provideQuarantineDao(database: PilahDatabase): QuarantineDao = database.quarantineDao()

    @Provides
    fun provideUserCorrectionDao(database: PilahDatabase): UserCorrectionDao = database.userCorrectionDao()
}
