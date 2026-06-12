package id.pilah.feature.scan.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.pilah.feature.scan.DefaultScanRepository
import id.pilah.feature.scan.ScanRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class ScanModule {

    @Binds
    abstract fun bindScanRepository(impl: DefaultScanRepository): ScanRepository
}
