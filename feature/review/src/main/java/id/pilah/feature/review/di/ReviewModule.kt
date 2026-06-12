package id.pilah.feature.review.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.pilah.feature.review.DefaultReviewRepository
import id.pilah.feature.review.ReviewRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class ReviewModule {

    @Binds
    abstract fun bindReviewRepository(impl: DefaultReviewRepository): ReviewRepository
}
