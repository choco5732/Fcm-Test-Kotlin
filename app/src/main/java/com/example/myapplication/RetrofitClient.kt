package com.example.myapplication

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

//    private const val BASE_URL = BuildConfig.HOST + BuildConfig.PORT
    private const val BASE_URL = "http://192.168.0.15:8080"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor(logger = HttpLoggingInterceptor.Logger.DEFAULT).apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

            )
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100,TimeUnit.SECONDS)
            .writeTimeout(100,TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val postgre: PostgreRemoteDataSource by lazy {
        retrofit.create(PostgreRemoteDataSource::class.java)
    }
}