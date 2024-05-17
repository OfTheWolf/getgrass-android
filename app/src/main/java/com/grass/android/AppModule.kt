package com.grass.android

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.grass.android.network.GrassApiService
import com.grass.android.network.WebSocketFlow
import com.grass.android.repository.EarningsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private const val USER_PREFERENCES = "user-data"

@Qualifier
annotation class SocketClient

@Qualifier
annotation class ApiClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    @Singleton
    @SocketClient
    fun provideSocketClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @ApiClient
    fun provideApiClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val newBuilder = chain.request().newBuilder()
                InMemoryDataStore.token?.let {
                    newBuilder.addHeader("Authorization", it)
                }
                val request = newBuilder.build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@ApiClient okHttpClient: OkHttpClient): GrassApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.getgrass.io")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        val retrofitService: GrassApiService by lazy {
            retrofit.create(GrassApiService::class.java)
        }
        return retrofitService
    }

    @Provides
    @Singleton
    fun provideWebSocketFlow(
        @SocketClient okHttpClient: OkHttpClient,
        ticker: Ticker,
        logger: Logger,
        coroutineScope: CoroutineScope
    ): WebSocketFlow {
        return WebSocketFlow(okHttpClient, ticker, logger, coroutineScope)
    }

    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return DebugLogger("GetGrass")
    }

    @Provides
    fun provideTicker(): Ticker {
        return Ticker()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(appContext, USER_PREFERENCES)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    @Singleton // Provide always the same instance
    @Provides
    fun providesCoroutineScope(): CoroutineScope {
        // Run this code when providing an instance of CoroutineScope
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Singleton // Provide always the same instance
    @Provides
    fun provideEarningsRepo(apiService: GrassApiService): EarningsRepository {
        return EarningsRepository(apiService, 10.seconds)
    }

    @Singleton // Provide always the same instance
    @Provides
    fun provideDecimalFormatter(): DecimalFormat {
        return DecimalFormat("#,###.##")
    }
}