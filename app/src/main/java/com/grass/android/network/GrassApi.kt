package com.grass.android.network

import com.grass.android.DataStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException


object GrassApi {
    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val newBuilder = chain.request().newBuilder()
            DataStore.token?.let {
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

    private fun getTrustAllHostsSSLSocketFactory(): Pair<SSLSocketFactory?, Array<X509TrustManager>?> {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {

                override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate> {
                    return arrayOf()
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager

            return Pair(sslContext.socketFactory, trustAllCerts)
        } catch (e: KeyManagementException) {
            e.printStackTrace()
            return Pair(null, null)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return Pair(null, null)
        }

    }
}