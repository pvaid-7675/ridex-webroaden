package com.speedride.driver.rest



import retrofit2.Retrofit
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.app.AppController
import com.speedride.driver.BuildConfig
import com.speedride.driver.utils.Common
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitBuilder {
    val instance: RetrofitBuilder?
        get() {
            if (mServeiceGenerator == null) {
                mServeiceGenerator = RetrofitBuilder()
            }
            return mServeiceGenerator
        }

    init {
        generateRetrofit()
    }

    val restClient: RestClient
        get() = retrofit!!.create(RestClient::class.java)

    companion object {
        const val BASE_URL = Common.BASE_URL
        const val API_KEY = Common.API_KEY
        const val CONNECTION_TIMEOUT = 120000
        private var retrofit: Retrofit? = null
        private var mServeiceGenerator: RetrofitBuilder? = null
        fun generateRetrofit(): Retrofit? {
            if (retrofit == null) {
                // set your desired log level
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                if (BuildConfig.DEBUG) {
                    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                } else {
                   httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE)
               }
                // Add the interceptor to OkHttpClient
                val builder = OkHttpClient().newBuilder()
                builder.addInterceptor(httpLoggingInterceptor)
                builder.interceptors().add(Interceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader(
                            "Authorization",
                            AppController.instance?.preferenceUtils?.get(PreferenceUtils.PREF_KEY_BEARER_TOKEN)!!
                        )
                        .addHeader("role", "Driver")
                        .build()
                    chain.proceed(newRequest)
                })
                builder.readTimeout(CONNECTION_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                    .connectTimeout(
                        CONNECTION_TIMEOUT.toLong(), TimeUnit.MILLISECONDS
                    )

                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(builder.build())
                    .build()
            }
            return retrofit
        }
    }
}