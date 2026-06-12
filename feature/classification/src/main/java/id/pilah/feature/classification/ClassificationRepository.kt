package id.pilah.feature.classification

import kotlinx.coroutines.flow.Flow

/** Orkestrasi klasifikasi rule-based untuk seluruh file hasil Smart Scan (PRD §3.2). */
interface ClassificationRepository {

    /** Mengklasifikasikan seluruh file di tabel `files`, memancarkan [ClassificationProgress] real-time. */
    fun classifyAll(): Flow<ClassificationProgress>
}
