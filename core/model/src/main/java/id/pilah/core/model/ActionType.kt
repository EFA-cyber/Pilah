package id.pilah.core.model

/** Jenis aksi yang dijalankan terhadap sebuah file dan dicatat di log `actions`. */
enum class ActionType {
    MOVE,
    QUARANTINE,
    RESTORE,
    PURGE,
}
