package id.pilah.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.pilah.core.model.Classification
import id.pilah.core.model.FileCategory
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    val penting: StateFlow<List<ReviewItem>> = reviewRepository.observePenting()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val layakDihapus: StateFlow<List<ReviewItem>> = reviewRepository.observeLayakDihapus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Riwayat klasifikasi untuk satu file, dipakai bottom sheet detail. */
    fun history(fileId: Long): Flow<List<Classification>> = reviewRepository.observeHistory(fileId)

    /** Geser kategori [item] hasil koreksi swipe pengguna (PRD §4). */
    fun correctCategory(item: ReviewItem, newCategory: FileCategory) {
        viewModelScope.launch {
            reviewRepository.correctCategory(item, newCategory)
        }
    }
}
