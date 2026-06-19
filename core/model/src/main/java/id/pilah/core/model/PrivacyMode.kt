package id.pilah.core.model

/**
 * Mode privasi yang dipilih pengguna (PRD §2/§4 - Onboarding & Pengaturan).
 * [ON_DEVICE]: seluruh pemindaian & klasifikasi berjalan lokal, tanpa data keluar perangkat.
 * [DEEP_ANALYSIS]: file kategori Ambigu boleh dianalisis via Cloud AI (Fase 6).
 */
enum class PrivacyMode {
    ON_DEVICE,
    DEEP_ANALYSIS,
}
