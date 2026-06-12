package id.pilah.feature.foldering.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.pilah.feature.foldering.DefaultFolderingRepository
import id.pilah.feature.foldering.FolderingRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class FolderingModule {

    @Binds
    abstract fun bindFolderingRepository(impl: DefaultFolderingRepository): FolderingRepository
}
