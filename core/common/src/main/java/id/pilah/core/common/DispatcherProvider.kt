package id.pilah.core.common

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Sumber [CoroutineDispatcher] yang dapat diganti saat pengujian.
 * Pemindaian file dan hashing (Fase 1) berjalan di [io], sedangkan
 * kalkulasi skor rule engine (Fase 2) berjalan di [default].
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
