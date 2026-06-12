package id.pilah.feature.dashboard

import id.pilah.core.common.DispatcherProvider
import id.pilah.core.database.dao.ActionDao
import id.pilah.core.database.dao.ClassificationDao
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.dao.QuarantineDao
import id.pilah.core.database.dao.UserCorrectionDao
import id.pilah.core.database.model.toDomain
import id.pilah.core.model.FileAction
import id.pilah.core.model.QuarantineStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class DefaultDashboardRepository @Inject constructor(
    private val fileDao: FileDao,
    private val classificationDao: ClassificationDao,
    private val userCorrectionDao: UserCorrectionDao,
    private val actionDao: ActionDao,
    private val quarantineDao: QuarantineDao,
    private val dispatchers: DispatcherProvider,
) : DashboardRepository {

    override fun observeStorageByCategory(): Flow<List<CategoryUsage>> = combine(
        fileDao.observeAll(),
        classificationDao.observeLatestPerFile(),
        userCorrectionDao.observeAll(),
    ) { files, classifications, corrections ->
        StorageUsageAggregator.aggregate(
            files = files.map { it.toDomain() },
            classifications = classifications.map { it.toDomain() },
            corrections = corrections.map { it.toDomain() },
        )
    }.flowOn(dispatchers.default)

    override fun observeSavings(): Flow<SavingsSummary> = combine(
        fileDao.observeAll(),
        actionDao.observeAll(),
        quarantineDao.observeByStatus(QuarantineStatus.ACTIVE),
    ) { files, actions, activeQuarantine ->
        SavingsCalculator.calculate(
            files = files.map { it.toDomain() },
            actions = actions.map { it.toDomain() },
            activeQuarantine = activeQuarantine.map { it.toDomain() },
        )
    }.flowOn(dispatchers.default)

    override fun observeCleanupHistory(): Flow<List<CleanupSession>> = combine(
        fileDao.observeAll(),
        actionDao.observeAll(),
    ) { files, actions ->
        CleanupHistoryAggregator.aggregate(
            files = files.map { it.toDomain() },
            actions = actions.map { it.toDomain() },
        )
    }.flowOn(dispatchers.default)

    override fun observeActionLog(): Flow<List<FileAction>> = actionDao.observeAll()
        .map { actions -> actions.map { it.toDomain() } }
        .flowOn(dispatchers.default)
}
