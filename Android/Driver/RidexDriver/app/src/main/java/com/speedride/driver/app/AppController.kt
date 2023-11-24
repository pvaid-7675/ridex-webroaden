package com.speedride.driver.app

import com.speedride.driver.utils.PreferenceUtils.Companion.getInstance
import android.app.Application
import android.content.Context
import com.speedride.driver.utils.PreferenceUtils
import androidx.multidex.MultiDex
import android.content.Intent
import android.util.Log
import com.speedride.driver.broadcast_receiver.ReceiverClass
import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.utils.Common
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class AppController : Application() {
     var preferenceUtils: PreferenceUtils? = null
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        sContext = this
        preferenceUtils = getInstance(sContext as AppController)
        //connect socket and send current location with start service
        val intent = Intent(this, ReceiverClass::class.java)
        intent.action = Common.CONNECT_SOCKET
        sendBroadcast(intent)
        Log.d(TAG, "onCreate: ")
    }

    override fun onTerminate() {
        Log.d(TAG, "onTerminate: ")
        super.onTerminate()
    }

    companion object {
        private val TAG = AppController::class.java.simpleName
        var mApp: AppController? = null
        var sContext: Context? = null
        var isActivityVisible = false
            private set
        var resumedActivity: AppCompatActivity? = null
            private set
        val instance: AppController?
            get() {
                if (mApp == null) {
                    mApp = sContext as AppController?
                }
                return mApp
            }

        fun activityResumed(appCompatActivity: AppCompatActivity?) {
            resumedActivity = appCompatActivity
            isActivityVisible = true
        }

        fun activityPaused() {
            resumedActivity = null
            isActivityVisible = false
        }
    }

}