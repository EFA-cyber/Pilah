package id.pilah.feature.scan

import id.pilah.core.common.DispatcherProvider
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.model.toEntity
import id.pilah.core.model.FileItem
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DefaultScanRepository @Inject constructor(
    private val scanner: FileSystemScanner,
    private val hasher: FileHasher,
    private val fileDao: FileDao,
    private val dispatchers: DispatcherProvider,
) : ScanRepository {

    override fun scan(subDir: String?): Flow<ScanProgress> = flow {
        var filesScanned = 0
        emit(ScanProgress(phase = ScanPhase.SCANNING))

        val scannedIds = mutableListOf<Long>()
        scanner.scan(subDir).collect { fileItem ->
            scannedIds += upsertAndGetId(fileItem)
            filesScanned++
            emit(ScanProgress(phase = ScanPhase.SCANNING, filesScanned = filesScanned))
        }

        var filesHashed = 0
        emit(ScanProgress(phase = ScanPhase.HASHING, filesScanned = filesScanned, filesToHash = scannedIds.size))
        for (id in scannedIds) {
            val entity = fileDao.getById(id)
            val hash = entity?.let { hasher.hash(File(it.path)) }
            if (entity != null && hash != null) {
                fileDao.upsert(entity.copy(hash = hash))
            }
            filesHashed++
            emit(
                ScanProgress(
                    phase = ScanPhase.HASHING,
                    filesScanned = filesScanned,
                    filesHashed = filesHashed,
                    filesToHash = scannedIds.size,
                ),
            )
        }

        emit(
            ScanProgress(
                phase = ScanPhase.DONE,
                filesScanned = filesScanned,
                filesHashed = filesHashed,
                filesToHash = scannedIds.size,
            ),
        )
    }.flowOn(dispatchers.io)

    /** Upsert dan kembalikan id baris; [androidx.room.Upsert] hanya memberi id valid untuk insert baru. */
    private suspend fun upsertAndGetId(fileItem: FileItem): Long {
        val rowId = fileDao.upsert(fileItem.toEntity())
        return if (rowId != -1L) rowId else fileDao.getByPath(fileItem.path)?.id ?: rowId
    }
}
