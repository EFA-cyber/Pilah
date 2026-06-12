package id.pilah.feature.classification.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.pilah.feature.classification.BlurDetector
import id.pilah.feature.classification.ClassificationRepository
import id.pilah.feature.classification.DefaultClassificationRepository
import id.pilah.feature.classification.DefaultInstalledPackagesProvider
import id.pilah.feature.classification.DefaultRuleEngine
import id.pilah.feature.classification.DefaultRuleWeightsRepository
import id.pilah.feature.classification.InstalledPackagesProvider
import id.pilah.feature.classification.LaplacianBlurDetector
import id.pilah.feature.classification.RuleEngine
import id.pilah.feature.classification.RuleWeightsRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class ClassificationModule {

    @Binds
    abstract fun bindClassificationRepository(impl: DefaultClassificationRepository): ClassificationRepository

    @Binds
    abstract fun bindRuleEngine(impl: DefaultRuleEngine): RuleEngine

    @Binds
    abstract fun bindBlurDetector(impl: LaplacianBlurDetector): BlurDetector

    @Binds
    abstract fun bindInstalledPackagesProvider(impl: DefaultInstalledPackagesProvider): InstalledPackagesProvider

    @Binds
    abstract fun bindRuleWeightsRepository(impl: DefaultRuleWeightsRepository): RuleWeightsRepository
}
