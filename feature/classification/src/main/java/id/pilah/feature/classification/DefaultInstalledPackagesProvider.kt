package id.pilah.feature.classification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Aplikasi PILAH memegang `MANAGE_EXTERNAL_STORAGE`, sehingga otomatis dikecualikan
 * dari pembatasan visibilitas paket Android 11+ — tidak perlu `QUERY_ALL_PACKAGES`.
 */
class DefaultInstalledPackagesProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : InstalledPackagesProvider {

    override fun isInstalled(apkPath: String): Boolean {
        val packageInfo = context.packageManager.getPackageArchiveInfo(apkPath, 0) ?: return false
        return runCatching {
            context.packageManager.getPackageInfo(packageInfo.packageName, 0)
        }.isSuccess
    }
}
