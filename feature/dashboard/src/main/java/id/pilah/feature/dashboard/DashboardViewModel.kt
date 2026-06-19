package id.pilah.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    dashboardRepository: DashboardRepository,
) : ViewModel() {

    val storageByCategory: StateFlow<List<CategoryUsage>> = dashboardRepository.observeStorageByCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savings: StateFlow<SavingsSummary> = dashboardRepository.observeSavings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavingsSummary())

    val cleanupHistory: StateFlow<List<CleanupSession>> = dashboardRepository.observeCleanupHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
