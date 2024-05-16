package com.grass.android.network

import com.grass.android.LocalDataStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object GrassApi {
    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val newBuilder = chain.request().newBuilder()
            LocalDataStore.token?.let {
                newBuilder.addHeader("Authorization", it)
            }
            val request = newBuilder.build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.getgrass.io")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val retrofitService: GrassApiService by lazy {
        retrofit.create(GrassApiService::class.java)
    }
}