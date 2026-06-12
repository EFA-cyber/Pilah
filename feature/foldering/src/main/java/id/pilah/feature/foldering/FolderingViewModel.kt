package id.pilah.feature.foldering

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FolderingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    folderingRepository: FolderingRepository,
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    val plan: StateFlow<FolderingPlan> = folderingRepository.observePlan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderingPlan())

    val executionState: StateFlow<FolderingUiState> = workManager
        .getWorkInfosForUniqueWorkFlow(FolderingWorker.WORK_NAME)
        .map { infos -> infos.toFolderingUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderingUiState())

    /** Mulai eksekusi "Rapikan Sekarang" berdasarkan [plan] terkini. */
    fun rapikanSekarang() {
        val request = OneTimeWorkRequestBuilder<FolderingWorker>()
            .addTag(FolderingWorker.TAG)
            .build()

        workManager.enqueueUniqueWork(FolderingWorker.WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
