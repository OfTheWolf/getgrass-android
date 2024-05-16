package com.grass.android

import com.grass.android.network.WebSocketFlow
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.Timer

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
//        .addInterceptor(logging)
            .build()
    }

    @Provides
    fun provideWebSocketFlow(
        okHttpClient: OkHttpClient,
        ticker: Ticker,
        logger: Logger
    ): WebSocketFlow {
        return WebSocketFlow(okHttpClient, ticker, logger)
    }

    @Provides
    fun provideTimer(): Timer {
        return Timer()
    }

    @Provides
    fun provideLogger(): Logger {
        return DebugLogger("GetGrass")
    }

    @Provides
    fun provideTicker(timer: Timer): Ticker {
        return Ticker(timer)
    }
}