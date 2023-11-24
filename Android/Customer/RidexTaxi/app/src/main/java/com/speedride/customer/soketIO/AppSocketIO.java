package com.speedride.customer.soketIO;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.speedride.customer.model.ChatResponse;
import com.speedride.customer.modules.main.model.BookRide;
import com.speedride.customer.modules.main.model.CustomerRequestModel;
import com.speedride.customer.service.ServiceClass;
import com.speedride.customer.modules.utils.AppLog;
import com.speedride.customer.modules.utils.Common;
import com.speedride.customer.modules.utils.PreferenceUtils;
import com.speedride.customer.modules.utils.ValidationUtils;
import com.speedride.customer.modules.main.model.CustomerRequestModel;
import com.speedride.customer.service.ServiceClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class AppSocketIO {

    private static final String TAG = AppSocketIO.class.getSimpleName();

    //public static final String SOCKET_SERVER = "http://192.249.121.94:8000";/* 182.76.80.118 */
    //public static final String SOCKET_SERVER = "http://192.249.121.94:8080";/* 182.76.80.118 */
    //Testing... Payal comment this url
   // public static final String SOCKET_SERVER = "http://205.134.254.135:8000";/* 182.76.80.118 */

    //public static final String SOCKET_SERVER = "http://209.182.213.242:5252";/* 182.76.80.118 */
    public static final String SOCKET_SERVER = "http://34.214.47.133:5252";/* 182.76.80.118 */



    // EVENT STRINGS
    public static final String EVENT_GET_DRIVER_LOCATION = "g                                                                     etdrivers";
        public static final String EVENT_GET_DRIVER_DETAIL = "getdrivers_data_";
    public static final String EVENT_GET_RIDE_REQUEST_CONFIRMATION = "conform_booking_";
    public static final String EVENT_GET_ASSIGN_DRIVER_LOCATION = "getdrivers_data_location_";
    public static final String EVENT_GET_START_RIDE_CONFIRMATION = "get_start_ride_details_";
    public static final String EVENT_GET_COMPLETE_RIDE_CONFIRMATION = "get_complete_ride_details_";
    public static final String EVENT_SCHEDULE_RIDE_DATA = "getscheduledrivers_data_";
    public static final String EVENT_GET_SCHEDULE_RIDE_DATA = "getscheduledrivers";
    // public static final String EVENT_RECEIVE_LOCATION = "message";
    //Testing...PAyal Doing this comment
    public static final String EVENT_RECEIVE_LOCATION = "getdrivers_data_";



    //getdrivers_data_location_"driver_id"

    // BROADCAST RECEIVER
    public static final String BROADCAST_EVENT = "socket_io_broadcast";
    public static final String BROADCAST_EVENT_NAME = "socket_io_broadcast_event_name";
    public static final String BROADCAST_EVENT_DATA = "socket_io_broadcast_event_data";

    public static final String EVENT_GET_MESSAGE = "get_message";
   // public static final String EVENT_RECEIVE_LOCATION = "message";

    // This event is for send Message
    public static final String EVENT_SEND_MESSAGE = "send_message";

    // This event is for received Message
    public static final String EVENT_RECEIVE_MESSAGE = "receive_message_";

    private static Socket mSocket;
    private static AppSocketIO mAppSocketIO;
    private Context mContext;
    private PreferenceUtils mPreferenceUtils;

    private AppSocketIO(Context context) {

        try {

            if (mSocket == null) {

                mContext = context;

                Log.d(TAG, "mSocket == null");

                IO.Options options = new IO.Options();
                options.transports = new String[]{WebSocket.NAME};
               // options.query = "token=my custom token";
                options.reconnection = true;
                options.forceNew = false;
                options.reconnectionDelay = 1000;
                options.timeout = 10000;
                mSocket = IO.socket(SOCKET_SERVER,options);
                mPreferenceUtils = PreferenceUtils.getInstance(context);
            }

        } catch (URISyntaxException e) {
            Log.d(TAG, "AppSocketIO: " + e);
            throw new RuntimeException(e);
        }
    }

    public static AppSocketIO getInstance(Context context) {
        if (mAppSocketIO == null) {
            mAppSocketIO = new AppSocketIO(context);
        }
        return mAppSocketIO;
    }

    public void setDefaultEventsListener() {
        mSocket.once(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
    }

    public void setDisconnectEvent() {
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off(EVENT_GET_DRIVER_DETAIL, onReceivedLocation);
        mSocket.off(EVENT_RECEIVE_MESSAGE, onReceivedMessage);
        mSocket.off(EVENT_GET_RIDE_REQUEST_CONFIRMATION, onReceivedRideConfirmation);
        mSocket.off(EVENT_GET_START_RIDE_CONFIRMATION, onReceivedStartRideConfirmation);
        mSocket.off(EVENT_GET_COMPLETE_RIDE_CONFIRMATION, onReceivedCompleteRideConfirmation);
        mSocket.off(EVENT_GET_ASSIGN_DRIVER_LOCATION, onReceivedGetAssignDriverLocation);
      //  mSocket.off(EVENT_SCHEDULE_RIDE_DATA, onReceivedScheduleRideData);
    }

    public void onConnect() {
        mSocket.connect();
    }

    public static boolean isSocketConnect() {

        return mSocket != null && mSocket.connected();
    }

    private static void emmitData(@NonNull String event, JSONObject object) {
        mSocket.emit(event, object);
    }

    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            AppLog.Companion.d(TAG, "onConnect: ");
        }
    };

    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            AppLog.Companion.d(TAG, "onDisconnect: ");
        }
    };

    private final Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            AppLog.Companion.d(TAG, "onConnectError: " + new Gson().toJson(args[0]));
        }
    };

    public void receivedLocation(String user_id) {
        mSocket.on(EVENT_GET_DRIVER_DETAIL + user_id, onReceivedLocation);
    }



//    public void scheduleRideData(String user_id) {
//        mSocket.on(EVENT_SCHEDULE_RIDE_DATA + user_id, onReceivedScheduleRideData);
//    }
    public void getRideConfirmationFromDriver(String user_id) {
        mSocket.on(EVENT_GET_RIDE_REQUEST_CONFIRMATION + user_id, onReceivedRideConfirmation);
    }
    public void receivedMessage(String rideid) {
        mSocket.on(EVENT_RECEIVE_MESSAGE + rideid, onReceivedMessage);
    }
    public void getStartRideConfirmationFromDriver(String user_id) {
        mSocket.on(EVENT_GET_START_RIDE_CONFIRMATION + user_id, onReceivedStartRideConfirmation);
    }

    public void getCompleteRideConfirmationFromDriver(String user_id) {
        mSocket.on(EVENT_GET_COMPLETE_RIDE_CONFIRMATION + user_id, onReceivedCompleteRideConfirmation);
    }

    public void getAssignDriverLocationDriver(String driver_id) {
        mSocket.on(EVENT_GET_ASSIGN_DRIVER_LOCATION + driver_id, onReceivedGetAssignDriverLocation);
    }

    public void stopToGetAssignDriverLocationDriver() {
        Log.d(TAG, "stopToGetAssignDriverLocationDriver: ");
        mSocket.off(EVENT_GET_ASSIGN_DRIVER_LOCATION, onReceivedGetAssignDriverLocation);
    }

    public static void sendLocation(String user_id, String lat, String lng) {
        JSONObject object = new JSONObject();
//        lat = "23.0122749";
//        lng = "72.5084836";
        if (isSocketConnect()) {
            try {
                object.put("user_id", user_id);
                object.put("lat", lat);
                object.put("lng", lng);
            } catch (JSONException e) {
                AppLog.Companion.e(TAG, e.getMessage(), e);
            }

            AppLog.Companion.d(TAG, "sendLocation: " + object.toString());
            emmitData(EVENT_GET_DRIVER_LOCATION, object);
        }

    }

    public static void sendScheduleData(String user_id, String socket_event) {
        JSONObject object = new JSONObject();
        if (isSocketConnect()) {
            try {
                object.put("user_id", user_id);
                object.put("socket_event", socket_event);
            } catch (JSONException e) {
                AppLog.Companion.e(TAG, e.getMessage(), e);
            }

            AppLog.Companion.d(TAG, "sendScheduleData: " + object.toString());
            emmitData(EVENT_GET_SCHEDULE_RIDE_DATA, object);
        }

    }

    public static void sendMessage(ChatResponse.ChatResponseItem chatResponse) {
        JSONObject object = new JSONObject();
        if (isSocketConnect()) {
            try {
                object.put("user_id", chatResponse.getUser_id());
                object.put("message_type", chatResponse.getMessage_type());
                object.put("details", chatResponse.getDetails());
                object.put("ride_id", chatResponse.getRide_id());
                object.put("date_time", chatResponse.getDate_time());
            } catch (JSONException e) {
                AppLog.Companion.e(TAG, e.getMessage(), e);
            }

            AppLog.Companion.d(TAG, "sendMessage: " + object.toString());
            emmitData(EVENT_SEND_MESSAGE, object);
        }

    }

    private final Emitter.Listener onReceivedLocation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.d(TAG, "call:get onReceivedLocation " + args[0]);
            if (ValidationUtils.INSTANCE.isValidString(args[0].toString())) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                    sendBroadCastEventData(EVENT_RECEIVE_LOCATION, jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
//        }
    };

    /**
     * get Location Event
     */
    private final Emitter.Listener onReceivedMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.d(TAG, "call:get onReceivedMessage " + args[0]);
            if (ValidationUtils.INSTANCE.isValidString(args[0].toString())) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                    if(!args[0].toString().equals("{}")){
                        Intent intent = new Intent(Common.RECIEVE_CHAT_MESSAGE);
                        intent.putExtra(BROADCAST_EVENT_NAME, Common.RECIEVE_CHAT_MESSAGE);
                        intent.putExtra(BROADCAST_EVENT_DATA,  ""+jsonObject);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }

                  // sendBroadCastEventData(Common.RECIEVE_CHAT_MESSAGE, jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

//    /**
//     * get Location Event
//     */
//    private final Emitter.Listener onReceivedScheduleRideData = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//
//           // Log.d(TAG, "call:get onReceivedScheduleRideData " + args[0]);
//        }
//    };
    /**
     * get Confirmation after driver accept ride request Event
     */
    private final Emitter.Listener onReceivedRideConfirmation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.d(TAG, "call:get onReceivedRideConfirmation " + args[0]);
            String responseStr = args[0].toString();
            if (ValidationUtils.INSTANCE.isValidString(responseStr)) {
                CustomerRequestModel customerRequestModel;

                Gson gson = new Gson();
                customerRequestModel = gson.fromJson(responseStr, CustomerRequestModel.class);
                //setBookData(customerRequestModel);

                if (ValidationUtils.isValidObject(customerRequestModel)) {
                    if (mPreferenceUtils == null)
                        mPreferenceUtils = PreferenceUtils.getInstance(mContext);
                    mPreferenceUtils.saveCustomerRideConfirmData(customerRequestModel);
                    Log.e(TAG, "onReceivedRideConfirmation driver id: " + customerRequestModel.getDriver_id() + " Charge: " + customerRequestModel.getCharge());
                    mPreferenceUtils.save(PreferenceUtils.PREF_KEY_BOOK_ID,customerRequestModel.getBook_id());
                    mPreferenceUtils.save(PreferenceUtils.PREF_KEY_CHARGE,customerRequestModel.getCharge());
                    Intent intent = new Intent(Common.NOTIFY_RIDE_CONFIRMATION);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            }
        }
    };

    private final Emitter.Listener onReceivedStartRideConfirmation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.d(TAG, "call:get onReceivedStartRideConfirmation " + args[0]);
            if (ValidationUtils.INSTANCE.isValidString(args[0].toString())) {
                Intent intent = new Intent(Common.START_RIDE_CONFIRMATION);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

        }
    };

    private final Emitter.Listener onReceivedCompleteRideConfirmation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.d(TAG, "call:get onReceivedCompleteRideConfirmation " + args[0]);
            if (ValidationUtils.INSTANCE.isValidString(args[0].toString())) {

                //STOP TO GET assign driver location with socket
                Intent intentService = new Intent(mContext, ServiceClass.class);
                intentService.setAction(Common.ASSIGN_DRIVER_LOCATION_STOP_GETTING);
                mContext.startService(intentService);

                Intent intent = new Intent(Common.COMPLETE_RIDE_CONFIRMATION);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }
    };

    private final Emitter.Listener onReceivedGetAssignDriverLocation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.d(TAG, "call:get onReceivedGetAssignDriverLocation " + args[0]);
            if (ValidationUtils.INSTANCE.isValidString(args[0].toString())) {
                sendBroadCastEventData(EVENT_GET_ASSIGN_DRIVER_LOCATION, args[0].toString());
            }
        }
    };

    public void sendBroadCastEventData(String event, String data) {
        Intent localIntent = new Intent(BROADCAST_EVENT);
        localIntent.putExtra(BROADCAST_EVENT_NAME, event);
        localIntent.putExtra(BROADCAST_EVENT_DATA, data);
        if (ValidationUtils.isValidObject(mContext))
            mContext.sendBroadcast(localIntent);
    }

}
