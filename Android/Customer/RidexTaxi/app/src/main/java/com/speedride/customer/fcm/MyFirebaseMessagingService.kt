package com.speedride.customer.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.speedride.customer.R
import com.speedride.customer.modules.splash.view.activity.SplashActivity
import com.speedride.customer.modules.utils.PreferenceUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var preferenceUtils: PreferenceUtils? = null

    // OnToken refresh call back
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        if (preferenceUtils == null) preferenceUtils =
            PreferenceUtils.getInstance(this@MyFirebaseMessagingService)
        preferenceUtils!!.save(PreferenceUtils.PREF_KEY_FCM_TOKEN, s)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val image = message.notification!!.icon
        val title = message.notification!!.title
        val text = message.notification!!.body
        val sound = message.notification!!.sound
        var id = 0
        val obj: Any? = message.data["id"]
        if (obj != null) {
            id = Integer.valueOf(obj.toString())
        }
        sendNotification(NotificationData(image, id, title, text, sound))
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param notificationData GCM message received.
     */
    private fun sendNotification(notificationData: NotificationData) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra(NotificationData.Companion.TEXT, notificationData.textMessage)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        var notificationBuilder: NotificationCompat.Builder? = null
        try {
            notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(URLDecoder.decode(notificationData.title, "UTF-8"))
                .setContentText(URLDecoder.decode(notificationData.textMessage, "UTF-8"))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        if (notificationBuilder != null) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationData.id, notificationBuilder.build())
        } else {
            Log.d(MyFirebaseMessagingService.Companion.TAG, "Não foi possível criar objeto notificationBuilder")
        }
    }

    companion object {
        private const val TAG = "MyGcmListenerService"
    }
}