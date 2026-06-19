package id.pilah.feature.classification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ruleWeightsDataStore: DataStore<Preferences> by preferencesDataStore(name = "rule_weights")

class DefaultRuleWeightsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : RuleWeightsRepository {

    override fun weights(): Flow<RuleWeights> = context.ruleWeightsDataStore.data.map { it.toRuleWeights() }

    override suspend fun updateWeights(transform: (RuleWeights) -> RuleWeights) {
        context.ruleWeightsDataStore.edit { prefs ->
            prefs.applyRuleWeights(transform(prefs.toRuleWeights()))
        }
    }

    private fun Preferences.toRuleWeights(): RuleWeights {
        val defaults = RuleWeights()
        return RuleWeights(
            baseScore = this[Keys.BASE_SCORE] ?: defaults.baseScore,
            duplicateDelta = this[Keys.DUPLICATE] ?: defaults.duplicateDelta,
            oldScreenshotDelta = this[Keys.OLD_SCREENSHOT] ?: defaults.oldScreenshotDelta,
            installedApkDelta = this[Keys.INSTALLED_APK] ?: defaults.installedApkDelta,
            unopenedDownloadDelta = this[Keys.UNOPENED_DOWNLOAD] ?: defaults.unopenedDownloadDelta,
            blurryPhotoDelta = this[Keys.BLURRY_PHOTO] ?: defaults.blurryPhotoDelta,
            importantNameDelta = this[Keys.IMPORTANT_NAME] ?: defaults.importantNameDelta,
            memoryPhotoDelta = this[Keys.MEMORY_PHOTO] ?: defaults.memoryPhotoDelta,
            oldFileThresholdDays = this[Keys.OLD_FILE_THRESHOLD_DAYS] ?: defaults.oldFileThresholdDays,
            blurVarianceThreshold = this[Keys.BLUR_VARIANCE_THRESHOLD] ?: defaults.blurVarianceThreshold,
        )
    }

    private fun MutablePreferences.applyRuleWeights(weights: RuleWeights) {
        this[Keys.BASE_SCORE] = weights.baseScore
        this[Keys.DUPLICATE] = weights.duplicateDelta
        this[Keys.OLD_SCREENSHOT] = weights.oldScreenshotDelta
        this[Keys.INSTALLED_APK] = weights.installedApkDelta
        this[Keys.UNOPENED_DOWNLOAD] = weights.unopenedDownloadDelta
        this[Keys.BLURRY_PHOTO] = weights.blurryPhotoDelta
        this[Keys.IMPORTANT_NAME] = weights.importantNameDelta
        this[Keys.MEMORY_PHOTO] = weights.memoryPhotoDelta
        this[Keys.OLD_FILE_THRESHOLD_DAYS] = weights.oldFileThresholdDays
        this[Keys.BLUR_VARIANCE_THRESHOLD] = weights.blurVarianceThreshold
    }

    private object Keys {
        val BASE_SCORE = intPreferencesKey("base_score")
        val DUPLICATE = intPreferencesKey("duplicate_delta")
        val OLD_SCREENSHOT = intPreferencesKey("old_screenshot_delta")
        val INSTALLED_APK = intPreferencesKey("installed_apk_delta")
        val UNOPENED_DOWNLOAD = intPreferencesKey("unopened_download_delta")
        val BLURRY_PHOTO = intPreferencesKey("blurry_photo_delta")
        val IMPORTANT_NAME = intPreferencesKey("important_name_delta")
        val MEMORY_PHOTO = intPreferencesKey("memory_photo_delta")
        val OLD_FILE_THRESHOLD_DAYS = longPreferencesKey("old_file_threshold_days")
        val BLUR_VARIANCE_THRESHOLD = doublePreferencesKey("blur_variance_threshold")
    }
}
