package com.speedride.driver.networkHelper.interceptor

import android.content.Context
import com.speedride.driver.utils.PREF_KEY_BEARER_TOKEN
import com.speedride.driver.utils.PreferenceHelper
import com.speedride.driver.utils.PreferenceHelper.get
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Request

class HeaderInterceptor(var mCtx:Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        var request:Request = chain.request()
        request = request.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader(
                "Authorization",
                PreferenceHelper.init(mCtx).get(key = PREF_KEY_BEARER_TOKEN)
            )
            .addHeader("role", "Driver")
            .build()
        return chain.proceed(request)
    }
}