package com.speedride.driver.service

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.speedride.customer.model.ChatResponse
import com.speedride.driver.R
import com.speedride.driver.app.AppController
import com.speedride.driver.broadcast_receiver.ReceiverClass
import com.speedride.driver.model.AdminChatResponse
import com.speedride.driver.modules.ride.dataModel.CustomerRequestModel
import com.speedride.driver.modules.ride.ui.activity.OnRideActivity
import com.speedride.driver.socketIO.AppSocketIO
import com.speedride.driver.ui.PickupRequestDialog
import com.speedride.driver.utils.AppLog
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.utils.ValidationUtils

class ServiceClass : Service() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null
    private var mAppSocketIO: AppSocketIO? = null
    private var preferenceUtils: PreferenceUtils? = null
    private var isOnline = false
    private var mDriverId: String? = null
    private var mBuilder: NotificationCompat.Builder? = null
    private val notificationId = 0
    private var vibrator: Vibrator? = null
    private var requestDialogActivity: PickupRequestDialog? = null
    private var customerRequestModel: CustomerRequestModel? = null
    private var chatResponseItem: ChatResponse.ChatResponseItem?= null
    private var adminChatResponseItem: AdminChatResponse.AdminChatResponseItem?= null
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Start " + TAG)
        init()
        if (ValidationUtils.isValidObject(intent) && ValidationUtils.isValidObject(intent.action)) {
            val action = intent.action
            when (action) {
                Common.CONNECT_SOCKET -> connectSocket()
                Common.DISCONNECT_SOCKET -> disconnectSocket()
                Common.RIDE_REQUEST_FROM_CUSTOMER -> {
                    AppLog.e(
                        TAG,
                        "customerRequestModel " + (preferenceUtils!!.customerRideData?.customer_id)
                    )
                    if (AppController.isActivityVisible && AppController.resumedActivity != null) {
                        showRequestDialog(
                            AppController.resumedActivity!!,
                            preferenceUtils!!.customerRideData
                        )
                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isDialogShow = true;
                            }
                        }, 3000);*/
                    } else {
                        buildRequestNotification(customerRequestModel)
                    }
                }
                Common.RIDE_REQUEST_ACCEPT -> {

                    //stopForeground(true);
                    //send accepted request to customer
                    mAppSocketIO!!.onSendAcceptRequestToCustomer(customerRequestModel)
                }
                Common.SEND_MESSAGE_CHAT -> {
                    val chatItemstr = intent.extras!!.getString("chatItem")
                    chatResponseItem = Gson().fromJson(chatItemstr, ChatResponse.ChatResponseItem::class.java)
                    chatResponseItem?.let { mAppSocketIO!!.sendMessage(it) }
                }
                Common.RIDE_REQUEST_REJECT -> {

                    //stopForeground(true);
                    ValidationUtils.cancelOffNotification(this, notificationId)
                }
                Common.RECIEVE_CHAT_MESSAGE ->
                    if (ValidationUtils.isValidObject(preferenceUtils!!.driverData)) {
                        Log.d(TAG, "onStartCommand: preferenceUtils.getCustomerRideConfirmData()")
                        if (ValidationUtils.isValidString(preferenceUtils!!.customerRideData!!.bookid)) {
                            if (ValidationUtils.isValidObject(mAppSocketIO)) {
                                // Log.d(TAG, "onStartmmand: mAppSocketIO " + preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                                preferenceUtils!!.customerRideData!!.bookid?.let {
                                    mAppSocketIO!!.receivedMessage(
                                        it
                                    )
                                }
                            }
                        }
                    }
                Common.RECIEVE_CHAT_MESSAGE_TO_ADMIN ->
                    mAppSocketIO?.receivedMessageToAdmin(mDriverId!!)

                Common.SEND_MESSAGE_TO_ADMIN_CHAT -> {
                    val chatItemstr = intent.extras!!.getString("chatItemAdmin")
                    adminChatResponseItem = Gson().fromJson(chatItemstr, AdminChatResponse.AdminChatResponseItem::class.java)
                    adminChatResponseItem?.let { mAppSocketIO!!.sendToAdminMessage(it) }
                }
            }
        }

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun init() {
        preferenceUtils = PreferenceUtils.getInstance(this)
        mDriverId = preferenceUtils?.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        customerRequestModel = preferenceUtils?.customerRideData
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            setLocationRequest()
        }
    }

    private fun setLocationRequest() {
        //displayLocationSettingsRequest(this);
        mFusedLocationClient = FusedLocationProviderClient(this)
        //send request for location   add this method for get call back from time interval-->
        mLocationRequest =
            LocationRequest().setInterval(12000).setFastestInterval(10000).setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY
            )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        //send current location request
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest!!,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    //for current mLocation continue
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.lastLocation != null) {
                mCurrentLocation = locationResult.lastLocation
                val mCurrLat: String
                val mCurrLong: String
                if (isOnline) {
                    mDriverId = preferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")

                    //check if current location, driver id and driver is duty On so send current location.
                    if (ValidationUtils.isValidObject(mCurrentLocation) && ValidationUtils.isValidString(
                            mDriverId
                        ) && preferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_STATUS, false)
                    ) {
                        mCurrLat = mCurrentLocation!!.getLatitude().toString()
                        mCurrLong = mCurrentLocation!!.getLongitude().toString()

                        //send current location to socket
                        mAppSocketIO!!.sendLocation(
                            mDriverId,
                            mCurrLat,
                            mCurrLong,
                            mCurrentLocation!!.getBearing()
                        )
                        Log.e(TAG, "mLocationCallback connectSocket: $mCurrLat $mCurrLong")
                    }
                }
            }
        }
    }

    private fun showRequestDialog(
        appCompatActivity: AppCompatActivity,
        customerRequestModel: CustomerRequestModel?
    ) {
        try {// if (isDialogShow) {
            requestDialogActivity = PickupRequestDialog(appCompatActivity, customerRequestModel)
            requestDialogActivity!!.show()
            if(!customerRequestModel?.is_admin_created!!.equals(1)){
                Handler().postDelayed({
                    if (requestDialogActivity != null && requestDialogActivity!!.isShowing()) {
                        requestDialogActivity!!.dismiss()
                    }
                }, (30 * 1000).toLong())
            }
            //isDialogShow = false;
            // }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //build notification for customer request
    private fun buildRequestNotification(customerRequestModel: CustomerRequestModel?) {

        //String alarmTime = Constants.getCurrentHour() + ":" + Constants.getCurrentMinute();

        //pending intent for cancel alarm and notification
        val intentCancel = Intent(this@ServiceClass, ReceiverClass::class.java)
        intentCancel.action = Common.RIDE_REQUEST_REJECT
        val pendingIntentCancel = PendingIntent.getBroadcast(
            this,
            0, intentCancel, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val intentAccept = Intent(this@ServiceClass, ReceiverClass::class.java)
        intentAccept.action = Common.RIDE_REQUEST_ACCEPT
        val pendingIntentAccept = PendingIntent.getBroadcast(
            this,
            0, intentAccept, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val contentActivityLaunchIntent = Intent(this@ServiceClass, OnRideActivity::class.java)
        contentActivityLaunchIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentActivityLaunchIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val contentView =
            RemoteViews(this.packageName, R.layout.notification_custom_remoteview_item)
        contentView.setTextViewText(R.id.tv_title_item_address, customerRequestModel!!.departure)
        contentView.setTextViewText(
            R.id.tv_min_time_to_pick_up,
            customerRequestModel.estimate_time + " Min"
        )
        contentView.setTextViewText(R.id.tv_estimate_earn,  "$"+customerRequestModel.estprice )
        contentView.setOnClickPendingIntent(R.id.btn_reject_item_request, pendingIntentCancel)
        contentView.setOnClickPendingIntent(R.id.btn_accept_item_request, pendingIntentAccept)
        val `when` = System.currentTimeMillis()
        val inboxStyle = NotificationCompat.BigPictureStyle()
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationIdInt = "0"
        mBuilder = NotificationCompat.Builder(
            this,
            notificationIdInt
        ) //.setContentIntent(contentPendingIntent)
            //.setSmallIcon(R.drawable.ic_icon_status_bar)
            //.setContentTitle(alarmTitle)
            //.setTicker(this.getString(R.string.app_name))
            //.setCustomContentView(contentView)
            .setContent(contentView) //.setContentText(getLeftAlarmTime(alarmHour, alarmMinute) + " , You can close this alarm in advance")
            // .addAction(R.drawable.alarm_off_icon_notification_action, "Turn off", pendingIntentCancel)
            .setChannelId("CHANNEL_ID")
            .setWhen(`when`) //.setStyle(inboxStyle)
            //Vibration
            //.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
            /*.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("That is for learning a notification widget, Much longer text that cannot fit one line..."))*/
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setSound(uri)
            .setOnlyAlertOnce(false)
        /*.setStyle(inboxStyle)*/if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder!!.setSmallIcon(R.drawable.ic_taxi)
        } else {
            mBuilder!!.setSmallIcon(R.drawable.ic_taxi)
        }
        //  .setOngoing(true)
        //.setSound(path)
        //.setOnlyAlertOnce(false);

        //final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (mNotificationManager != null) //check device version Oreo or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name: CharSequence = getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val notification = mBuilder!!.build()
                notification.flags = Notification.FLAG_INSISTENT
                val mChannel = NotificationChannel("", name, importance)
                // set the notification channel.
                mNotificationManager.createNotificationChannel(mChannel)
                mNotificationManager.notify(notificationId, notification)
                startForeground(notificationId, notification)
            } else {
                // notificationId is a unique int for each notification that you must define
                val notification = mBuilder!!.build()
                notification.flags = Notification.FLAG_INSISTENT
                mNotificationManager.notify(notificationId, notification)
                startForeground(notificationId, notification)
            }
        Log.d(TAG, "buildRequestNotification: " + customerRequestModel.customer_id)
        vibrate()
    }

    /* private void generateNotification(String title, String content, PendingIntent intent, int alarmId, boolean isNotification) {

         // store id in temp variable
         if (isNotification) {
             NotificationManager mNotificationManager;
             // builder to set notification name,content name etc.
             NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "");
             builder.setContentTitle(title)
                     .setContentText(content)
                     .setFullScreenIntent(intent, true)
                     .setSmallIcon(R.drawable.ic_alarm_clock)
                     .setContentIntent(intent);

             // set up notification for OREO version or higher
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 String CHANNEL_ID = "my_channel_01";
                 int importance = NotificationManager.IMPORTANCE_HIGH;

                 NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Alarm WakeUp", importance);

                 builder.setChannelId(CHANNEL_ID);
                 Notification notification = builder.build();
                 notification.flags = Notification.FLAG_INSISTENT;
                 mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                 mNotificationManager.createNotificationChannel(mChannel);
                 mNotificationManager.notify(alarmId, notification);
                 startForeground(alarmId, notification);
             } else {
                 Notification notification = builder.build();
                 notification.flags = Notification.FLAG_INSISTENT;
                 mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                 mNotificationManager.notify(alarmId, notification);
                 startForeground(alarmId, notification);
             }
         }


     }
 */
    //vibrate on alarm play
    private fun vibrate() {
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator != null) {
            val pattern = LongArray(4)
            pattern[0] = 1000 // Wait one second
            pattern[1] = 500 // Vibrate for most a second
            pattern[2] = 1000 // A pause long enough to feel distinction
            pattern[3] = 500 // Repeat 3 more times

            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator!!.vibrate(
                    VibrationEffect.createWaveform(
                        pattern,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                //deprecated in API 26
                vibrator!!.vibrate(pattern, -1)
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        Log.e(TAG, "onTaskRemoved: ")
        disconnectSocket()
        ValidationUtils.cancelOffNotification(this, notificationId)
    }

    /* //for show gps location dialog
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        AppLog.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        AppLog.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(AppController.getInstance(), Common.REQUEST_GPS_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            AppLog.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        AppLog.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }*/
    private fun connectSocket() {
        if (ValidationUtils.isValidString(mDriverId)) {
            mAppSocketIO = AppSocketIO(this)
            mAppSocketIO!!.setDefaultEventsListener()
            mAppSocketIO!!.onConnect()
            isOnline = true
            mAppSocketIO!!.onGetRequestFromCustomer(mDriverId!!)
            mAppSocketIO!!.onGetConfirmRideDetailsFromCustomer(mDriverId!!)
            mAppSocketIO!!.onGetCancelRideDetailsFromCustomer(mDriverId!!)
            mAppSocketIO!!.receivedMessageToAdmin(mDriverId!!)

            //get Received message
            //  mAppSocketIO.receivedMessage(preferenceUtils.getCustomerDriverRideInfo().id);
            if (ValidationUtils.isValidObject(preferenceUtils!!.driverData)) {
                Log.d(TAG, "onStartCommand: Recieved Message")
                if (ValidationUtils.isValidString(preferenceUtils?.customerRideData?.bookid)) {
                    if (ValidationUtils.isValidObject(mAppSocketIO)) {
                        // Log.d(TAG, "onStartmmand: mAppSocketIO " + preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                        preferenceUtils!!.customerRideData!!.bookid?.let {
                            mAppSocketIO!!.receivedMessage(
                                it
                            )
                        }
                    }
                }
            }

        }
    }

    private fun disconnectSocket() {
        if (mAppSocketIO != null) {
            mAppSocketIO!!.setDisconnectEvent()
            isOnline = false
            stopService()
            preferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCKET, false)
        }
    }

    private fun stopService() {
        //check version for service start as foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
        Log.d(TAG, "onDestroy: ")
        //disconnectSocket();
    }

    companion object {
        private val TAG = ServiceClass::class.java.simpleName
    }
}