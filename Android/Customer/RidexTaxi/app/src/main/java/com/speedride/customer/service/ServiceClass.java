package com.speedride.customer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.speedride.customer.base.ApplicationClass;
import com.speedride.customer.soketIO.AppSocketIO;
import com.speedride.customer.modules.utils.AppLog;
import com.speedride.customer.modules.utils.Common;
import com.speedride.customer.modules.utils.PreferenceUtils;
import com.speedride.customer.modules.utils.ValidationUtils;

import java.util.Objects;

public class ServiceClass extends Service {

    private static final String TAG = ServiceClass.class.getSimpleName();
    private AppSocketIO mAppSocketIO;
    private PreferenceUtils preferenceUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceUtils = PreferenceUtils.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ValidationUtils.isValidObject(intent) && ValidationUtils.isValidObject(intent.getAction())) {
            String action = intent.getAction();
            Log.d(TAG, "Start action" + action);

            switch (action) {
                case Common.CONNECT_SOCKET:
                    connectSocket();
                    break;

                case Common.DISCONNECT_SOCKET:
                    disconnectSocket();
                    break;

                case Common.ASSIGN_DRIVER_LOCATION:
                    //get assign driver location
                    if (ValidationUtils.isValidObject(preferenceUtils.getCustomerRideConfirmData())) {
                        Log.d(TAG, "onStartCommand: preferenceUtils.getCustomerRideConfirmData()");
                        if (ValidationUtils.INSTANCE.isValidString(preferenceUtils.getCustomerRideConfirmData().getDriver_id())) {
                            if ((ValidationUtils.isValidObject(mAppSocketIO))) {
                                Log.d(TAG, "onStartCommand: mAppSocketIO " + preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                                mAppSocketIO.getAssignDriverLocationDriver(preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                            }
                        }
                    }
                    break;

                case Common.RECIEVE_CHAT_MESSAGE:
                    //get assign driver location
                    if (ValidationUtils.isValidObject(preferenceUtils.getCustomerDriverRideInfo())) {
                        Log.d(TAG, "onStartCommand: preferenceUtils.getCustomerRideConfirmData()");
                        if (ValidationUtils.INSTANCE.isValidString(preferenceUtils.getCustomerDriverRideInfo().id)) {
                            if ((ValidationUtils.isValidObject(mAppSocketIO))) {
                               // Log.d(TAG, "onStartmmand: mAppSocketIO " + preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                                mAppSocketIO.receivedMessage(preferenceUtils.getCustomerDriverRideInfo().id);
                            }
                        }
                    }
                    break;

                case Common.ASSIGN_DRIVER_LOCATION_STOP_GETTING:
                    //get assign driver location
                    if (ValidationUtils.isValidObject(preferenceUtils.getCustomerRideConfirmData())) {
                        Log.d(TAG, "onStartCommand: preferenceUtils.getCustomerRideConfirmData()");
                        if (ValidationUtils.INSTANCE.isValidString(preferenceUtils.getCustomerRideConfirmData().getDriver_id())) {
                            if ((ValidationUtils.isValidObject(mAppSocketIO))) {
                                Log.d(TAG, "onStartCommand: mAppSocketIO " + preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                                mAppSocketIO.stopToGetAssignDriverLocationDriver();
                            }
                        }
                    }
                    break;
            }
        }

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        disconnectSocket();
        preferenceUtils.save(PreferenceUtils.PREF_KEY_SOCKET, false);
        Log.d(TAG, "onTaskRemoved: ");
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
                        AppLog.Companion.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        AppLog.Companion.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(ApplicationClass.getInstance(), Common.REQUEST_GPS_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            AppLog.Companion.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        AppLog.Companion.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }*/

    private void connectSocket() {
        if (ValidationUtils.isInternetAvailable(this)) {

            if (ValidationUtils.isValidObject(preferenceUtils.getUserInfo())) {

                if (ValidationUtils.INSTANCE.isValidString(preferenceUtils.getUserInfo().getId())) {

                    //mAppSocketIO = new AppSocketIO(this)
                    mAppSocketIO = AppSocketIO.getInstance(ApplicationClass.Companion.getInstance().getApplicationContext());
                    mAppSocketIO.setDefaultEventsListener();
                             mAppSocketIO.onConnect();

                    preferenceUtils.save(PreferenceUtils.PREF_KEY_SOCKET, true);
                    AppLog.Companion.e(TAG, "connectSocket: ");

                    //get online drivers locations
                    mAppSocketIO.receivedLocation(preferenceUtils.getUserInfo().getId());



                    //get ride request accepted confirmation from driver
                    mAppSocketIO.getRideConfirmationFromDriver(preferenceUtils.getUserInfo().getId());

                    //get Received message
                  //  mAppSocketIO.receivedMessage(preferenceUtils.getCustomerDriverRideInfo().id);
                    if (ValidationUtils.isValidObject(preferenceUtils.getCustomerDriverRideInfo())) {
                        Log.d(TAG, "onStartCommand: preferenceUtils.getCustomerRideConfirmData()");
                        if (ValidationUtils.INSTANCE.isValidString(preferenceUtils.getCustomerDriverRideInfo().id)) {
                            if ((ValidationUtils.isValidObject(mAppSocketIO))) {
                                // Log.d(TAG, "onStartmmand: mAppSocketIO " + preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                                mAppSocketIO.receivedMessage(preferenceUtils.getCustomerDriverRideInfo().id);
                            }
                        }
                    }

                    //get start ride confirmation from driver3
                    mAppSocketIO.getStartRideConfirmationFromDriver(preferenceUtils.getUserInfo().getId());

                    //get complete ride confirmation from driver
                    mAppSocketIO.getCompleteRideConfirmationFromDriver(preferenceUtils.getUserInfo().getId());

                    //get assign driver location
                    if (ValidationUtils.isValidObject(preferenceUtils.getCustomerRideConfirmData())) {
                        Log.d(TAG, "onStartCommand: preferenceUtils.getCustomerRideConfirmData()");
                        if (ValidationUtils.INSTANCE.isValidString(preferenceUtils.getCustomerRideConfirmData().getDriver_id())) {
                            if ((ValidationUtils.isValidObject(mAppSocketIO))) {
                                Log.d(TAG, "onStartCommand: mAppSocketIO " + preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                                mAppSocketIO.getAssignDriverLocationDriver(preferenceUtils.getCustomerRideConfirmData().getDriver_id());
                            }
                        }
                    }
                }
            }
        }
    }

    private void disconnectSocket() {
        if (mAppSocketIO != null) {
            mAppSocketIO.setDisconnectEvent();
            stopService();
            preferenceUtils.save(PreferenceUtils.PREF_KEY_SOCKET, false);
        }
    }

    private void stopService() {
        //check version for service start as foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
            stopSelf();
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        //disconnectSocket();
    }
}
