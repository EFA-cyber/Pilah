package id.pilah.feature.quarantine

import id.pilah.core.common.DispatcherProvider
import id.pilah.core.common.FileMover
import id.pilah.core.database.dao.ActionDao
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.dao.QuarantineDao
import id.pilah.core.database.model.toDomain
import id.pilah.core.database.model.toEntity
import id.pilah.core.model.ActionType
import id.pilah.core.model.FileAction
import id.pilah.core.model.QuarantineEntry
import id.pilah.core.model.QuarantineStatus
import java.io.File
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DefaultQuarantineRepository @Inject constructor(
    private val fileDao: FileDao,
    private val quarantineDao: QuarantineDao,
    private val actionDao: ActionDao,
    private val dispatchers: DispatcherProvider,
) : QuarantineRepository {

    override fun observeActive(): Flow<List<QuarantineItem>> = combine(
        quarantineDao.observeByStatus(QuarantineStatus.ACTIVE),
        fileDao.observeAll(),
    ) { entries, files ->
        val filesById = files.associateBy { it.id }
        val now = Instant.now()

        entries
            .mapNotNull { entity ->
                val file = filesById[entity.fileId]?.toDomain() ?: return@mapNotNull null
                QuarantineItem(
                    file = file,
                    entry = entity.toDomain(),
                    daysRemaining = QuarantineDaysCalculator.daysRemaining(now, entity.purgeAfter),
                )
            }
            .sortedBy { it.daysRemaining }
    }.flowOn(dispatchers.default)

    override suspend fun restore(item: QuarantineItem) = withContext(dispatchers.io) {
        val originalPath = actionDao.getLastForFile(item.file.id)?.fromPath ?: return@withContext
        val from = File(item.file.path)
        val to = File(originalPath)

        if (FileMover.move(from, to)) {
            fileDao.upsert(item.file.toEntity().copy(path = originalPath))

            actionDao.insert(
                FileAction(
                    fileId = item.file.id,
                    actionType = ActionType.RESTORE,
                    fromPath = item.file.path,
                    toPath = originalPath,
                    executedAt = Instant.now(),
                ).toEntity(),
            )
            quarantineDao.update(item.entry.copy(status = QuarantineStatus.RESTORED).toEntity())
        }
    }

    override suspend fun deleteNow(item: QuarantineItem) = withContext(dispatchers.io) {
        purge(item.file.path, item.entry)
    }

    override suspend fun purgeExpired(): Int = withContext(dispatchers.io) {
        val now = Instant.now()
        val expired = quarantineDao.getExpired(now)

        for (entity in expired) {
            val file = fileDao.getById(entity.fileId) ?: continue
            purge(file.path, entity.toDomain())
        }

        expired.size
    }

    private suspend fun purge(path: String, entry: QuarantineEntry) {
        File(path).delete()

        actionDao.insert(
            FileAction(
                fileId = entry.fileId,
                actionType = ActionType.PURGE,
                fromPath = path,
                toPath = "",
                executedAt = Instant.now(),
            ).toEntity(),
        )
        quarantineDao.update(entry.copy(status = QuarantineStatus.PURGED).toEntity())
    }
}
