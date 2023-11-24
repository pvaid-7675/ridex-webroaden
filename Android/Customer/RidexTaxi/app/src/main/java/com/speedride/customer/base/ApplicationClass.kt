package com.speedride.customer.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.speedride.customer.interfaces.ICoreApp
import com.speedride.customer.modules.login.view.activity.LoginActivity
import com.speedride.customer.modules.utils.BackgroundManager
import com.speedride.customer.modules.utils.Common
import com.speedride.customer.modules.utils.PreferenceUtils
import com.speedride.customer.soketIO.AppSocketIO

class ApplicationClass : CoreApp() {

    public var mPreference: PreferenceUtils? = null

    companion object : ICoreApp by CoreApp
    {
        private val TAG = ApplicationClass::class.java.simpleName
        var mApp: ApplicationClass? = null
        var sContext: Context? = null
        var isActivityVisible = false
            private set
        var resumedActivity: AppCompatActivity? = null
            private set

        /*@JvmStatic
        val instance: ApplicationClass?
            get() {
                if (mApp == null) {
                    mApp = sContext as ApplicationClass?
                }
                return mApp
            }*/

        fun activityResumed(appCompatActivity: AppCompatActivity?) {
            ApplicationClass.Companion.resumedActivity = appCompatActivity
            ApplicationClass.Companion.isActivityVisible = true
        }

        fun activityPaused() {
            ApplicationClass.Companion.resumedActivity = null
            ApplicationClass.Companion.isActivityVisible = false
        }


        //var instance : CoreApp? = null

        private lateinit var app: ApplicationClass
        fun getInstance(): ApplicationClass {
            return ApplicationClass.Companion.app
        }


        var ctx: Context? = null
        var activity: Activity? = null

        fun startHomeActivity(activity: Activity) {
            if (ApplicationClass.Companion.ctx != null) {
                val intentRelogin = Intent(ApplicationClass.Companion.ctx, LoginActivity::class.java)
                intentRelogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ApplicationClass.Companion.ctx?.startActivity(intentRelogin)
                activity.finish()
            }
        }

        fun setCurrentActivity(activity_: Activity) {
            ApplicationClass.Companion.activity = activity_
        }
    }


    override fun localizedContext(): Context {
        return localeAwareContext(this)
    }

    override fun onCreate() {
        super.onCreate()
        ApplicationClass.Companion.app = this
        ApplicationClass.Companion.sContext = this
        backgroundManager = BackgroundManager(this)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        mPreference = PreferenceUtils.getInstance(applicationContext)

        //connect socket and send current location with start service
        /*Intent intent = new Intent(this, ServiceClass.class);
        intent.setAction(Common.CONNECT_SOCKET);
        startService(intent);*/


        val intent = Intent(this, com.speedride.customer.service.ServiceClass::class.java)
        intent.action = Common.CONNECT_SOCKET
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //startForegroundService(intent);
            startService(intent)
        } else {
            startService(intent)
        }
       /* try {
            var  mAppSocketIO = AppSocketIO(sContext)
            mAppSocketIO.setDefaultEventsListener()
            mAppSocketIO.onConnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
        Log.d(ApplicationClass.Companion.TAG, "onCreate: ")
    }

    override fun onTerminate() {
        Log.d(ApplicationClass.Companion.TAG, "onTerminate: ")
        super.onTerminate()
    }



}