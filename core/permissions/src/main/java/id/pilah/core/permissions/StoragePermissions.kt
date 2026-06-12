package id.pilah.core.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Status & permintaan izin akses penyimpanan penuh, dibutuhkan Smart Scan (PRD §3.1)
 * untuk membaca metadata seluruh file di internal storage + SD card.
 *
 * - Android 11+ (API 30+): butuh izin khusus [Manifest.permission.MANAGE_EXTERNAL_STORAGE]
 *   yang hanya bisa diberikan lewat halaman Pengaturan.
 * - Android 10 ke bawah: cukup izin runtime [legacyPermissions].
 */
object StoragePermissions {

    /** True jika aplikasi sudah punya akses baca/tulis penuh ke penyimpanan. */
    fun hasAccess(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            legacyPermissions().all {
                ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        }

    /** Izin runtime yang perlu diminta lewat [androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions] di Android 10 ke bawah. */
    fun legacyPermissions(): Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    /** True jika permintaan izin harus diarahkan ke halaman Pengaturan (Android 11+). */
    fun requiresSettingsRedirect(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    /**
     * Intent ke halaman Pengaturan "Izinkan akses ke semua file" untuk aplikasi ini.
     * Fallback ke halaman daftar aplikasi jika perangkat tidak mendukung intent spesifik-app.
     */
    fun manageAllFilesIntent(context: Context): Intent {
        val specific = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        return if (specific.resolveActivity(context.packageManager) != null) {
            specific
        } else {
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        }
    }
}
