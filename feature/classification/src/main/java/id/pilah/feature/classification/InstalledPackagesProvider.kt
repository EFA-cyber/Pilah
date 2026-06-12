package id.pilah.feature.classification

/** Akses ke [android.content.pm.PackageManager] untuk sinyal "APK sudah terinstal" (PRD §3.2). */
interface InstalledPackagesProvider {

    /** True jika package dari berkas APK di [apkPath] sudah terpasang di perangkat. */
    fun isInstalled(apkPath: String): Boolean
}
