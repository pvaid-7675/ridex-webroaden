package com.speedride.driver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.speedride.driver.modules.home.ui.activity.MainActivity
import com.speedride.driver.utils.PreferenceUtils

const val channelId = "notification_channel"
const val channelName = "com.speedride.driver"

class FirebaseMessagingService : FirebaseMessagingService(){

    private var preferenceUtils: PreferenceUtils? = null
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        if (preferenceUtils == null) preferenceUtils =
            PreferenceUtils.getInstance(this@FirebaseMessagingService)
        preferenceUtils!!.save(PreferenceUtils.PREF_KEY_FCM_TOKEN, s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.getNotification() != null) {
            generatingNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!)
        }
    }

    fun getRemoteView(title: String, message: String): RemoteViews? {
        val remoteView = RemoteViews("com.speedride.driver", R.layout.notification)
        remoteView.setTextViewText(R.id.title_noti, title)
        remoteView.setTextViewText(R.id.message_noti, message)
        remoteView.setImageViewResource(R.id.icon_noti, R.drawable.ridex_logo_icon)
        return remoteView
    }

    fun generatingNotification(title: String, message: String) {

        val intent = Intent (this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ridex_logo_icon)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(title, message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)  {
            val notificationChannel = NotificationChannel(channelId, channelName, notificationManager.importance)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0,builder.build())
    }


}