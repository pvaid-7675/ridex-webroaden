package com.speedride.customer.rest;

import androidx.annotation.NonNull;

import com.speedride.customer.BuildConfig;
import com.speedride.customer.base.ApplicationClass;
import com.speedride.customer.modules.utils.PreferenceUtils;
import com.speedride.customer.base.ApplicationClass;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {


    //public static final String BASE_URL = "http://192.249.121.94/~mobile/citycab/api/v1/";
   // public static final String BASE_URL = "http://205.134.254.135/~mobile/citycab-backend/api/v1/";
    // Payal add this base url
//    public static final String BASE_URL = "https://ded3896.inmotionhosting.com/~mobile/ridex-backend/api/v1/";
    //public static final String BASE_URL = "http://54.184.120.212/api/v1/";
    public static final String BASE_URL = "https://speedridellc.com/api/v1/";
    public static final String API_KEY = "oKRsI6LcIUjw242zsVTRn0hCSe7U3C";
    //public static final String BASE_URL = "http://199.250.201.83/~mobile/citycab/api/v1/";
    //public static final String API_KEY = "oKRsI6LcIUjw242zsVTRn0hCSe7U3C";
    public static final int CONNECTION_TIMEOUT = 120000;

    public static Retrofit retrofit = null;
    private static ServiceGenerator mServiceGenerator;

    public static ServiceGenerator getInstance() {
        if (mServiceGenerator == null) {
            mServiceGenerator = new ServiceGenerator();
        }
        return mServiceGenerator;
    }

    public ServiceGenerator() {
        getRetrofit();
    }

    public static Retrofit getRetrofit() {

        PreferenceUtils mPreferenceUtils = PreferenceUtils.getInstance(ApplicationClass.Companion.getInstance());

        if (retrofit == null) {

            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            if (BuildConfig.DEBUG) {
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
            }

            // Add the interceptor to OkHttpClient
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.addInterceptor(httpLoggingInterceptor);

            //Interceptors are a powerful mechanism that can monitor, rewrite, and retry calls.
            // Here's a simple interceptor that logs the outgoing request and the incoming response

            builder.interceptors().add(new Interceptor() {
                @Override
                @NonNull
                public Response intercept(@NonNull Chain chain) throws IOException {

                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("role", "Customer")
                            .addHeader("Authorization", mPreferenceUtils.get(PreferenceUtils.PREF_KEY_BEARER_TOKEN))
                            .build();
                    return chain.proceed(newRequest);
                }
            });

            builder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(builder.build())
                    .build();
        }

        return retrofit;
    }

    public RestClient getRestClient() {
        return retrofit.create(RestClient.class);
    }
}
