package id.pilah.feature.quarantine.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.pilah.feature.quarantine.DefaultQuarantineRepository
import id.pilah.feature.quarantine.QuarantineRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class QuarantineModule {

    @Binds
    abstract fun bindQuarantineRepository(impl: DefaultQuarantineRepository): QuarantineRepository
}
