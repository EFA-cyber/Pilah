package id.pilah.feature.review

import id.pilah.core.common.DispatcherProvider
import id.pilah.core.common.FolderSuggester
import id.pilah.core.database.dao.ClassificationDao
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.dao.QuarantineDao
import id.pilah.core.database.dao.UserCorrectionDao
import id.pilah.core.database.model.toDomain
import id.pilah.core.database.model.toEntity
import id.pilah.core.model.Classification
import id.pilah.core.model.FileCategory
import id.pilah.core.model.QuarantineStatus
import id.pilah.core.model.UserCorrection
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class DefaultReviewRepository @Inject constructor(
    private val fileDao: FileDao,
    private val classificationDao: ClassificationDao,
    private val userCorrectionDao: UserCorrectionDao,
    private val quarantineDao: QuarantineDao,
    private val ruleWeightsAdjuster: RuleWeightsAdjuster,
    private val dispatchers: DispatcherProvider,
) : ReviewRepository {

    override fun observePenting(): Flow<List<ReviewItem>> = observeReviewItems(FileCategory.PENTING)

    override fun observeLayakDihapus(): Flow<List<ReviewItem>> = observeReviewItems(FileCategory.LAYAK_DIHAPUS)

    override fun observeHistory(fileId: Long): Flow<List<Classification>> =
        classificationDao.observeHistoryForFile(fileId).map { history -> history.map { it.toDomain() } }

    override suspend fun correctCategory(item: ReviewItem, newCategory: FileCategory) {
        userCorrectionDao.insert(
            UserCorrection(
                fileId = item.file.id,
                aiCategory = item.effectiveCategory,
                userCategory = newCategory,
                correctedAt = Instant.now(),
            ).toEntity(),
        )
        ruleWeightsAdjuster.adjust(item.classification.reason, item.effectiveCategory, newCategory)
    }

    private fun observeReviewItems(category: FileCategory): Flow<List<ReviewItem>> = combine(
        fileDao.observeAll(),
        classificationDao.observeLatestPerFile(),
        userCorrectionDao.observeAll(),
        quarantineDao.observeByStatus(QuarantineStatus.ACTIVE),
    ) { files, classifications, corrections, quarantined ->
        val classificationByFileId = classifications.associateBy { it.fileId }
        val latestCorrectionByFileId = corrections.groupBy { it.fileId }.mapValues { it.value.first() }
        val quarantinedFileIds = quarantined.map { it.fileId }.toSet()

        val items = files.mapNotNull { file ->
            if (file.id in quarantinedFileIds) return@mapNotNull null
            val classification = classificationByFileId[file.id]?.toDomain() ?: return@mapNotNull null
            val effectiveCategory = latestCorrectionByFileId[file.id]?.userCategory ?: classification.category
            if (effectiveCategory != category) return@mapNotNull null

            val fileItem = file.toDomain()
            ReviewItem(
                file = fileItem,
                classification = classification,
                effectiveCategory = effectiveCategory,
                suggestedFolder = FolderSuggester.suggest(fileItem, effectiveCategory),
            )
        }

        when (category) {
            FileCategory.PENTING -> items.sortedByDescending { it.classification.importanceScore }
            else -> items.sortedBy { it.classification.importanceScore }
        }
    }.flowOn(dispatchers.default)
}
