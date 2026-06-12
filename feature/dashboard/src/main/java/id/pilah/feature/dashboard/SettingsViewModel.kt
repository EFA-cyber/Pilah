package id.pilah.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.pilah.core.datastore.UserPreferencesRepository
import id.pilah.core.model.FileAction
import id.pilah.core.model.PrivacyMode
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    dashboardRepository: DashboardRepository,
) : ViewModel() {

    val privacyMode: StateFlow<PrivacyMode> = userPreferencesRepository.observePrivacyMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrivacyMode.ON_DEVICE)

    val actionLog: StateFlow<List<FileAction>> = dashboardRepository.observeActionLog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setPrivacyMode(mode: PrivacyMode) {
        viewModelScope.launch { userPreferencesRepository.setPrivacyMode(mode) }
    }
}
