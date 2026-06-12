package id.pilah.app.navigation

/** Rute navigasi tingkat atas, sesuai alur pengguna pada PRD §4. */
enum class PilahDestination(val route: String) {
    ONBOARDING("onboarding"),
    SCAN("scan"),
    REVIEW("review"),
    DASHBOARD("dashboard"),
    QUARANTINE("quarantine"),
}
