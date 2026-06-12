package id.pilah.feature.classification

import id.pilah.core.common.DispatcherProvider
import id.pilah.core.database.dao.ClassificationDao
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.entity.FileEntity
import id.pilah.core.database.model.toDomain
import id.pilah.core.database.model.toEntity
import id.pilah.core.model.Classification
import id.pilah.core.model.ClassificationSource
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DefaultClassificationRepository @Inject constructor(
    private val fileDao: FileDao,
    private val classificationDao: ClassificationDao,
    private val ruleEngine: RuleEngine,
    private val ruleWeightsRepository: RuleWeightsRepository,
    private val blurDetector: BlurDetector,
    private val installedPackages: InstalledPackagesProvider,
    private val dispatchers: DispatcherProvider,
) : ClassificationRepository {

    override fun classifyAll(): Flow<ClassificationProgress> = flow {
        val files = fileDao.observeAll().first()
        val weights = ruleWeightsRepository.weights().first()
        val now = Instant.now()
        val context = buildContext(files, now)

        emit(ClassificationProgress(totalFiles = files.size))

        files.forEachIndexed { index, entity ->
            val result = ruleEngine.classify(entity.toDomain(), context, weights)
            classificationDao.insert(
                Classification(
                    fileId = entity.id,
                    importanceScore = result.score,
                    category = result.category,
                    reason = result.reason,
                    source = ClassificationSource.LOCAL_RULE,
                    classifiedAt = now,
                ).toEntity(),
            )
            emit(ClassificationProgress(filesClassified = index + 1, totalFiles = files.size))
        }
    }.flowOn(dispatchers.default)

    /** Pra-hitung sinyal lintas-file (duplikat, APK terinstal, kebururaman) sekali untuk seluruh hasil scan. */
    private fun buildContext(files: List<FileEntity>, now: Instant): RuleContext {
        val duplicateOfOriginalName = files
            .filter { it.hash != null }
            .groupBy { it.hash }
            .values
            .filter { it.size > 1 }
            .flatMap { group ->
                val original = group.minBy { it.createdAt }
                group.filterNot { it.path == original.path }.map { it.path to original.name }
            }
            .toMap()

        val installedApkPaths = files
            .asSequence()
            .filter { it.type == "apk" }
            .filter { installedPackages.isInstalled(it.path) }
            .map { it.path }
            .toSet()

        val blurVariance = files
            .asSequence()
            .filter { it.type in IMAGE_EXTENSIONS }
            .mapNotNull { entity -> blurDetector.varianceOf(entity.path)?.let { entity.path to it } }
            .toMap()

        return RuleContext(
            now = now,
            duplicateOfOriginalName = duplicateOfOriginalName,
            installedApkPaths = installedApkPaths,
            blurVariance = blurVariance,
        )
    }

    private companion object {
        val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "heic", "webp")
    }
}
