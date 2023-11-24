package com.speedride.customer.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.speedride.customer.modules.utils.PreferenceUtils

class MyFirebaseInstanceIDService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseInstanceIDService"
    }

    private var preferenceUtils: PreferenceUtils? = null

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.e("NEW_TOKEN", s)
        if (preferenceUtils == null) preferenceUtils =
            PreferenceUtils.getInstance(this@MyFirebaseInstanceIDService)
        preferenceUtils!!.save(PreferenceUtils.PREF_KEY_FCM_TOKEN, s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
    }
}