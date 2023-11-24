package com.speedride.driver.networkHelper.retrofit

import android.content.Context
import com.google.gson.GsonBuilder
import com.speedride.driver.networkHelper.interceptor.HeaderInterceptor
import com.speedride.driver.utils.Common

import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitBuilder {

    private var gson = GsonBuilder()
        .setLenient()
        .create()

    fun buildService(mCtx: Context): ApiClient {

        /**
         * 10 MB
         */

        val cacheSize = 10 * 1024 * 1024
        val cache = Cache(mCtx.cacheDir, cacheSize.toLong())
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)


        val client = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(HeaderInterceptor(mCtx))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Common.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
        return retrofit.create(ApiClient::class.java)
    }

}