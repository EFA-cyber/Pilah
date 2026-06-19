package id.pilah.feature.foldering

import id.pilah.core.common.DispatcherProvider
import id.pilah.core.common.FileMover
import id.pilah.core.database.dao.ActionDao
import id.pilah.core.database.dao.ClassificationDao
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.dao.QuarantineDao
import id.pilah.core.database.dao.UserCorrectionDao
import id.pilah.core.database.model.toDomain
import id.pilah.core.database.model.toEntity
import id.pilah.core.model.ActionType
import id.pilah.core.model.FileAction
import id.pilah.core.model.FileCategory
import id.pilah.core.model.QuarantineEntry
import id.pilah.core.model.QuarantineStatus
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DefaultFolderingRepository @Inject constructor(
    private val fileDao: FileDao,
    private val classificationDao: ClassificationDao,
    private val userCorrectionDao: UserCorrectionDao,
    private val actionDao: ActionDao,
    private val quarantineDao: QuarantineDao,
    private val dispatchers: DispatcherProvider,
) : FolderingRepository {

    override fun observePlan(): Flow<FolderingPlan> = combine(
        fileDao.observeAll(),
        classificationDao.observeLatestPerFile(),
        userCorrectionDao.observeAll(),
        quarantineDao.observeByStatus(QuarantineStatus.ACTIVE),
    ) { files, classifications, corrections, quarantined ->
        val classificationByFileId = classifications.associateBy { it.fileId }
        val latestCorrectionByFileId = corrections.groupBy { it.fileId }.mapValues { it.value.first() }
        val quarantinedFileIds = quarantined.map { it.fileId }.toSet()

        val categories = files.mapNotNull { file ->
            if (file.id in quarantinedFileIds) return@mapNotNull null
            val classification = classificationByFileId[file.id] ?: return@mapNotNull null
            val category = latestCorrectionByFileId[file.id]?.userCategory ?: classification.category
            file.id to category
        }.toMap()

        FolderingPlanner.plan(files.map { it.toDomain() }, categories)
    }.flowOn(dispatchers.default)

    override fun execute(plan: FolderingPlan): Flow<FolderingProgress> = flow {
        val total = plan.items.size
        emit(FolderingProgress(totalFiles = total))

        plan.items.forEachIndexed { index, item ->
            val from = File(item.file.path)
            val to = File(item.targetPath)

            if (FileMover.move(from, to)) {
                fileDao.upsert(item.file.toEntity().copy(path = item.targetPath))

                val now = Instant.now()
                val actionType = if (item.category == FileCategory.LAYAK_DIHAPUS) ActionType.QUARANTINE else ActionType.MOVE
                actionDao.insert(
                    FileAction(
                        fileId = item.file.id,
                        actionType = actionType,
                        fromPath = item.file.path,
                        toPath = item.targetPath,
                        executedAt = now,
                    ).toEntity(),
                )

                if (item.category == FileCategory.LAYAK_DIHAPUS) {
                    val existing = quarantineDao.getByFileId(item.file.id)
                    val entry = QuarantineEntry(
                        id = existing?.id ?: 0L,
                        fileId = item.file.id,
                        quarantinedAt = now,
                        purgeAfter = now.plus(QUARANTINE_DAYS, ChronoUnit.DAYS),
                        status = QuarantineStatus.ACTIVE,
                    )
                    if (existing != null) {
                        quarantineDao.update(entry.toEntity())
                    } else {
                        quarantineDao.insert(entry.toEntity())
                    }
                }
            }

            emit(FolderingProgress(filesMoved = index + 1, totalFiles = total))
        }
    }.flowOn(dispatchers.io)

    private companion object {
        const val QUARANTINE_DAYS = 30L
    }
}
