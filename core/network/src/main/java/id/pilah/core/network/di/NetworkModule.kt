package id.pilah.core.network.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.pilah.core.network.ClaudeApi
import id.pilah.core.network.ClaudeClient
import id.pilah.core.network.DefaultClaudeClient
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val ANTHROPIC_BASE_URL = "https://api.anthropic.com/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideClaudeApi(okHttpClient: OkHttpClient, json: Json): ClaudeApi = Retrofit.Builder()
        .baseUrl(ANTHROPIC_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ClaudeApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ClaudeClientModule {

    @Binds
    abstract fun bindClaudeClient(impl: DefaultClaudeClient): ClaudeClient
}
