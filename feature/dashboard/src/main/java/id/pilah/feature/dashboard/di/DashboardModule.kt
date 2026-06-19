package id.pilah.feature.dashboard.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.pilah.feature.dashboard.DashboardRepository
import id.pilah.feature.dashboard.DefaultDashboardRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    abstract fun bindDashboardRepository(impl: DefaultDashboardRepository): DashboardRepository
}
