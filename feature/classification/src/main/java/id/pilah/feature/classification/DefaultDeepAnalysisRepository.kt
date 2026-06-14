package id.pilah.feature.classification

import id.pilah.core.common.DispatcherProvider
import id.pilah.core.database.dao.ClassificationDao
import id.pilah.core.database.dao.FileDao
import id.pilah.core.database.model.toDomain
import id.pilah.core.database.model.toEntity
import id.pilah.core.datastore.ApiKeyRepository
import id.pilah.core.datastore.UserPreferencesRepository
import id.pilah.core.model.Classification
import id.pilah.core.model.FileCategory
import id.pilah.core.model.PrivacyMode
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class DefaultDeepAnalysisRepository @Inject constructor(
    private val fileDao: FileDao,
    private val classificationDao: ClassificationDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val apiKeyRepository: ApiKeyRepository,
    private val textExtractor: TextExtractor,
    private val cloudClassifier: CloudClassifier,
    private val dispatchers: DispatcherProvider,
) : DeepAnalysisRepository {

    override suspend fun analyzeAmbiguousFiles(): DeepAnalysisResult = withContext(dispatchers.io) {
        // Analisis Mendalam adalah fitur tambahan (PRD Fase 6, opsional): kegagalan apa pun di
        // sini (API key korup di Keystore, error jaringan/API, dll.) tidak boleh menggagalkan
        // worker atau memblokir alur Smart Scan -> Tinjau Hasil.
        runCatching {
            if (userPreferencesRepository.observePrivacyMode().first() != PrivacyMode.DEEP_ANALYSIS) {
                return@runCatching DeepAnalysisResult(skipped = true)
            }

            val apiKey = apiKeyRepository.getApiKey()
            if (apiKey.isNullOrBlank()) {
                return@runCatching DeepAnalysisResult(skipped = true)
            }

            val files = fileDao.observeAll().first().map { it.toDomain() }
            val latestCategoryByFileId = classificationDao.observeLatestPerFile().first()
                .associate { it.fileId to it.category }

            val candidates = files.filter { file ->
                latestCategoryByFileId[file.id] == FileCategory.AMBIGU && textExtractor.supports(file.type)
            }
            if (candidates.isEmpty()) return@runCatching DeepAnalysisResult()

            val inputs = candidates.mapNotNull { file ->
                textExtractor.extract(file.path, file.type, MAX_SNIPPET_CHARS)?.let { snippet ->
                    CloudClassificationInput(fileId = file.id, fileName = file.name, textSnippet = snippet)
                }
            }
            if (inputs.isEmpty()) return@runCatching DeepAnalysisResult()

            val results = runCatching { cloudClassifier.classify(inputs, apiKey) }.getOrDefault(emptyList())
            val now = Instant.now()
            results.forEach { result ->
                classificationDao.insert(
                    Classification(
                        fileId = result.fileId,
                        importanceScore = result.importanceScore,
                        category = result.category,
                        reason = result.reason,
                        source = result.source,
                        classifiedAt = now,
                    ).toEntity(),
                )
            }

            DeepAnalysisResult(filesAnalyzed = results.size)
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            DeepAnalysisResult(skipped = true)
        }
    }

    private companion object {
        const val MAX_SNIPPET_CHARS = 2_000
    }
}
