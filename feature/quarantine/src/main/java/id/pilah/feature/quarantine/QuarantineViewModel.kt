package id.pilah.feature.quarantine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class QuarantineViewModel @Inject constructor(
    private val quarantineRepository: QuarantineRepository,
) : ViewModel() {

    val items: StateFlow<List<QuarantineItem>> = quarantineRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Kembalikan [item] ke lokasi semula sebelum dikarantina. */
    fun restore(item: QuarantineItem) {
        viewModelScope.launch { quarantineRepository.restore(item) }
    }

    /** Hapus permanen [item] sekarang, sebelum 30 hari. */
    fun deleteNow(item: QuarantineItem) {
        viewModelScope.launch { quarantineRepository.deleteNow(item) }
    }
}
