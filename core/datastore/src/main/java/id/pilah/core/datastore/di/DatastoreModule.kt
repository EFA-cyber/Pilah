package id.pilah.core.datastore.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.pilah.core.datastore.DefaultUserPreferencesRepository
import id.pilah.core.datastore.UserPreferencesRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DatastoreModule {

    @Binds
    abstract fun bindUserPreferencesRepository(impl: DefaultUserPreferencesRepository): UserPreferencesRepository
}
