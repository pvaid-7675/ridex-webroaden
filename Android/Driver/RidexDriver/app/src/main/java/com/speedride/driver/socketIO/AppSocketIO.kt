package com.speedride.driver.socketIO

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.speedride.customer.model.ChatResponse.ChatResponseItem
import com.speedride.driver.app.AppController
import com.speedride.driver.broadcast_receiver.ReceiverClass
import com.speedride.driver.model.AdminChatResponse
import com.speedride.driver.modules.ride.dataModel.CustomerRequestModel
import com.speedride.driver.modules.ride.ui.activity.OnRideActivity
import com.speedride.driver.utils.*
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class AppSocketIO(context: Context?) {
    private var mSocket: Socket? = null
    private var mAppSocketIO: AppSocketIO? = null
    private var mContext: Context? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    val instance: AppSocketIO
        get() {
            if (mAppSocketIO == null) {
                mAppSocketIO = AppSocketIO(null)
            }
            return mAppSocketIO!!
        }

    fun setDefaultEventsListener() {
        mSocket!!.once(Socket.EVENT_CONNECT, onConnect)
        mSocket!!.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
    }

    fun setDisconnectEvent() {
        mSocket!!.disconnect()
        mSocket!!.off(Socket.EVENT_CONNECT, onConnect)
        mSocket!!.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket!!.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket!!.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket!!.off(EVENT_GET_BOOKING_REQUEST, onGetRequestFromCustomer)
        mSocket!!.off(EVENT_GET_CONFIRM_RIDE_DETAILS, onGetConfirmRideDetailsFromCustomer)
        mSocket!!.off(EVENT_GET_CANCEL_RIDE_DETAILS, onGetCancelRideDetailsFromCustomer)
        mSocket!!.off(EVENT_RECEIVE_MESSAGE, onReceivedMessage)
        mSocket!!.off(EVENT_RECEIVE_MESSAGE_TO_ADMIN, onReceivedMessageToAdmin)
    }

    fun onConnect() {
        mSocket!!.connect()
    }

    private val isSocketConnected: Boolean
        get() = mSocket != null && mSocket!!.connected()

    /*socket*/
    private val onConnect = Emitter.Listener { AppLog.d(TAG, "onConnect: ") }
    private val onDisconnect = Emitter.Listener { AppLog.d(TAG, "onDisconnect: ") }
    private val onConnectError =
        Emitter.Listener { args -> AppLog.d(TAG, "onConnectError: " + Gson().toJson(args[0])) }
    private val onMessageSendEventListener = Emitter.Listener {
        AppLog.d(TAG, "onMessageSendEventListener: " + it.toString())
        //sendBroadCastEventData(EVENT_ON_ERROR, (String) args[0]);
    }

    /*All events for drriver and customer*/ //receive pickup request from customer
    fun onGetRequestFromCustomer(strDriverId: String) {
        mSocket!!.on(EVENT_GET_BOOKING_REQUEST + strDriverId, onGetRequestFromCustomer)
    }

    //receive ride request confirmation details from customer
    fun onGetConfirmRideDetailsFromCustomer(strDriverId: String) {
        mSocket!!.on(
            EVENT_GET_CONFIRM_RIDE_DETAILS + strDriverId,
            onGetConfirmRideDetailsFromCustomer
        )
    }

    //receive ride cancel details from customer
    fun onGetCancelRideDetailsFromCustomer(strDriverId: String) {
        mSocket!!.on(
            EVENT_GET_CANCEL_RIDE_DETAILS + strDriverId,
            onGetCancelRideDetailsFromCustomer
        )
    }

    //emit data to server
    private fun emmitData(event: String, `object`: JSONObject?) {
        mSocket!!.emit(event, `object`)
    }

    fun setSendMessageEventListener() {
        mSocket!!.on(EVENT_SEND_MESSAGE, onMessageSendEventListener)
    }
    public fun sendMessage(chatResponse: ChatResponseItem) {
        val `object` = JSONObject()
         if (isSocketConnected) {
            try {
                `object`.put("user_id", chatResponse.user_id)
                `object`.put("message_type", chatResponse.message_type)
                `object`.put("details", chatResponse.details)
                `object`.put("ride_id", chatResponse.ride_id)
                `object`.put("date_time", chatResponse.date_time)
            } catch (e: JSONException) {
                AppLog.e(TAG, e.message, e)
            }
            AppLog.d(TAG, "sendMessage: $`object`")
            emmitData(EVENT_CHAT_SEND_MESSAGE, `object`)
        }
    }

    public fun sendToAdminMessage(chatResponse: AdminChatResponse.AdminChatResponseItem) {
        val `object` = JSONObject()
        if (isSocketConnected) {
            try {
                `object`.put("sender_id", chatResponse.sender_id)
                `object`.put("receiver_id", chatResponse.receiver_id)
                `object`.put("message_type", chatResponse.message_type)
                `object`.put("details", chatResponse.details)
                `object`.put("date_time", chatResponse.date_time)
            } catch (e: JSONException) {
                AppLog.e(TAG, e.message, e)
            }
            AppLog.d(TAG, "sendMessage: $`object`")
            emmitData(EVENT_CHAT_SEND_MESSAGE_TO_ADMIN, `object`)
        }
    }
    fun receivedMessage(rideid: String) {
        mSocket!!.on(EVENT_RECEIVE_MESSAGE + rideid, onReceivedMessage)
    }

    fun receivedMessageToAdmin(user_id: String) {
        mSocket!!.on(EVENT_RECEIVE_MESSAGE_TO_ADMIN + user_id, onReceivedMessageToAdmin)
    }

    fun sendLocation(user_id: String?, lat: String?, lng: String?, bearing: Float) {
        if (isSocketConnected) {
            val `object` = JSONObject()

            /* lat = "23.0122749";
            lng = "72.5084836";*/try {
                `object`.put("user_id", user_id)
                `object`.put("lat", lat)
                `object`.put("lng", lng)
                `object`.put("bearing", bearing.toDouble())
            } catch (e: JSONException) {
                AppLog.e(TAG, e.message, e)
            }
            emmitData(EVENT_DRIVER_LOCATION, `object`)
            AppLog.d(TAG, "sendLocation: $`object`")
        }
    }

    //on accept customers ride request
    fun onSendAcceptRequestToCustomer(customerRequestModel: CustomerRequestModel?) {
        if (isSocketConnected) {
            var jsonObj: JSONObject? = null
            try {
                val strJson = Gson().toJson(customerRequestModel)
                /*Gson gson = new GsonBuilder().create();
                String json = gson.toJson(customerRequestModel);*/jsonObj = JSONObject(strJson)
            } catch (e: JSONException) {
                AppLog.e(TAG, e.message, e)
            }
            emmitData(EVENT_ACCEPT_BOOKING_REQUEST, jsonObj)
            AppLog.d(TAG, "onSendAcceptRequestToCustomer: " + jsonObj.toString())
            /*onSendAcceptRequestToCustomer: {"charge":"80","customer_id":"277","departure":"11, Jodhpur Village, Jodhpur, Ahmedabad, Gujarat 380015, India  India","dlat":"23.01438075732041","dlong":"72.51619722694159","driver_id":"287","estprice":"2.46","esttime":"4","first_name":"Customer","image":"users\/5b9b932e1641b-driver-63966.jpg","km":"0.82","last_name":"ccc","mobile":"1234567890","paymode":"cash","plat":"23.012319049024068","plong":"72.50851571559906","requestdatetime":"2018-10-02 11:41:09","vt_id":"1"}*/
        }
    }

    /**
     * get ride request from customer Event
     */
 /*   private val onGetRequestFromCustomer = Emitter.Listener { args ->
        if (ValidationUtils.isValidString(args[0].toString())) {
            Log.d(TAG, "onGetRequestFromCustomer: " + args[0])
            val data = args[0] as JSONObject
            Log.e(TAG, "call: data")
            val customerRequestModel: CustomerRequestModel
            try {
                Log.e(
                    TAG,
                    "call: try driver_id: " + data.getString("driver_id") + " charge: " + data.getString(
                        "charge"
                    )
                )
                val gson = Gson()
                customerRequestModel =
                    gson.fromJson(args[0].toString(), CustomerRequestModel::class.java)
//                    mRequestRefCode = customerRequestModel.getUnique_ref();
                mPreferenceUtils!!.saveCustomerRideData(customerRequestModel)
                if(data?.getInt("is_admin_created")!! != 1){
                    val intentRequest = Intent(mContext, ReceiverClass::class.java)
                    intentRequest.action = Common.RIDE_REQUEST_FROM_CUSTOMER
                    mContext!!.sendBroadcast(intentRequest)
                }else{
                    val intentRequest = Intent(mContext, ReceiverClass::class.java)
                    intentRequest.action = Common.RIDE_REQUEST_ACCEPT
                    mContext!!.sendBroadcast(intentRequest)
                }

                *//* if (!mRequestRefCode.equalsIgnoreCase(customerRequestModel.getUnique_ref())) {
                         Intent intentRequest = new Intent(mContext, ReceiverClass.class);
                         intentRequest.setAction(Common.RIDE_REQUEST_FROM_CUSTOMER);
                         mContext.sendBroadcast(intentRequest);
                         mRequestRefCode = customerRequestModel.getUnique_ref();
                     }http://205.134.254.135
                     *//*
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //sendBroadCastEventData(EVENT_SEND_MESSAGE, data.toString());
        *//*onGetRequestFromCustomer: {"driver_id":287,"customer_id":"277","first_name":"Customer","last_name":"ccc","mobile":"1234567890","image":"users\/5b9b932e1641b-driver-63966.jpg","requestdatetime":"2018-10-02 11:41:09","vt_id":"1","plat":"23.012319049024068","plong":"72.50851571559906","dlat":"23.01438075732041","dlong":"72.51619722694159","charge":"80","km":"0.82","esttime":4,"estprice":"2.46","paymode":"cash","departure":"11, Jodhpur Village, Jodhpur, Ahmedabad, Gujarat 380015, India  India"}*//*
    }*/


    /**
     * get ride request from customer Event
     */
    private val onGetRequestFromCustomer = Emitter.Listener { args ->
        if (ValidationUtils.isValidString(args[0].toString())) {
            Log.d(TAG, "onGetRequestFromCustomer: " + args[0])
            val data = args[0] as JSONObject
            Log.e(TAG, "call: data")
            val customerRequestModel: CustomerRequestModel
            try {
                Log.e(
                    TAG,
                    "call: try driver_id: " + data.getString("driver_id") + " charge: " + data.getString(
                        "charge"
                    )
                )
                val gson = Gson()
                customerRequestModel =
                    gson.fromJson(args[0].toString(), CustomerRequestModel::class.java)
//                    mRequestRefCode = customerRequestModel.getUnique_ref();
                mPreferenceUtils!!.saveCustomerRideData(customerRequestModel)
                customerRequestModel.charge?.let { Log.e("customerRequestModel?.charge: onGetRequestFromCustomer", it) }
                if(customerRequestModel.is_admin_created != 1){
                    val intentRequest = Intent(mContext, ReceiverClass::class.java)
                    intentRequest.action = Common.RIDE_REQUEST_FROM_CUSTOMER
                    mContext!!.sendBroadcast(intentRequest)
                }else{
                    //Ride created by Admin
                    val intentRequest = Intent(mContext, ReceiverClass::class.java)
                    intentRequest.action = Common.RIDE_REQUEST_ACCEPT
                    mContext!!.sendBroadcast(intentRequest)
                }

                /* if (!mRequestRefCode.equalsIgnoreCase(customerRequestModel.getUnique_ref())) {
                         Intent intentRequest = new Intent(mContext, ReceiverClass.class);
                         intentRequest.setAction(Common.RIDE_REQUEST_FROM_CUSTOMER);
                         mContext.sendBroadcast(intentRequest);
                         mRequestRefCode = customerRequestModel.getUnique_ref();
                     }http://205.134.254.135
                     */
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //sendBroadCastEventData(EVENT_SEND_MESSAGE, data.toString());
        /*onGetRequestFromCustomer: {"driver_id":287,"customer_id":"277","first_name":"Customer","last_name":"ccc","mobile":"1234567890","image":"users\/5b9b932e1641b-driver-63966.jpg","requestdatetime":"2018-10-02 11:41:09","vt_id":"1","plat":"23.012319049024068","plong":"72.50851571559906","dlat":"23.01438075732041","dlong":"72.51619722694159","charge":"80","km":"0.82","esttime":4,"estprice":"2.46","paymode":"cash","departure":"11, Jodhpur Village, Jodhpur, Ahmedabad, Gujarat 380015, India  India"}*/
    }

    /**
     * get ride request confirmation from customer Event
     */
    private val onGetConfirmRideDetailsFromCustomer = Emitter.Listener { args ->
        if (ValidationUtils.isValidString(args[0].toString())) {
            Log.d(TAG, "onGetConfirmRideDetailsFromCustomer: " + args[0])
            val data = args[0] as JSONObject
            val customerRequestModel = mPreferenceUtils!!.customerRideData
            try {

                /* Gson gson = new Gson();
                     customerRequestModel = gson.fromJson(args[0].toString(), CustomerRequestModel.class);*/
                if (ValidationUtils.isValidObject(customerRequestModel)) {
                    val bookId = data.getString("bookid")
                    if (ValidationUtils.isValidString(bookId)) {
                        customerRequestModel?.bookid = bookId
                        mPreferenceUtils!!.saveCustomerRideData(customerRequestModel)
                        customerRequestModel!!.charge?.let { Log.e("customerRequestModel?.charge; onGetConfirmRideDetailsFromCustomer", it) }
                        ValidationUtils.cancelOffNotification(mContext!!, 0)

                        if(customerRequestModel?.is_sharing!!.equals(1) && AppController.instance?.preferenceUtils!!.get(PreferenceUtils.PREF_KEY_IS_SHARED_RIDE_ON, false)){
                            val intent = Intent(Common.NEW_SHARED_RIDE_DETAILS)
                            intent.putExtra("customerRequestModel",data.toString())
                            LocalBroadcastManager.getInstance(mContext!!).sendBroadcast(intent)

                        }else{

                            val ongoingIntent = Intent(mContext, OnRideActivity::class.java)
                            ongoingIntent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            Utils.animationIntent(mContext!!, ongoingIntent)
                        }
                        Log.e(TAG, "onGetConfirmRideDetailsFromCustomer : bookid: " + mPreferenceUtils!!.customerRideData?.bookid)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        /*onGetConfirmRideDetailsFromCustomer: {"bookid":81,"driver_id":"287"}*/
    }

    /**
     * get ride cancel from customer Event
     */
    private val onGetCancelRideDetailsFromCustomer = Emitter.Listener { args ->
        if (ValidationUtils.isValidString(args[0].toString())) {
            Log.d(TAG, "onGetCancelRideDetailsFromCustomer: " + args[0])
            val data = args[0] as JSONObject
            val customerRequestModel = mPreferenceUtils!!.customerRideData
            customerRequestModel?.isCanceled = true
            mPreferenceUtils!!.saveCustomerRideData(customerRequestModel)
            customerRequestModel!!.charge?.let { Log.e("customerRequestModel?.charge onGetCancelRideDetailsFromCustomer", it) }
            val intent = Intent(Common.NOTIFY_RIDE_CANCEL_DETAILS)
            LocalBroadcastManager.getInstance(mContext!!).sendBroadcast(intent)
        }

        /*onGetCancelRideDetailsFromCustomer: {"bookid":"81","driver_id":"287"}*/
    }

    init {
        try {
            if (mSocket == null) {
                mContext = context
                Log.d(TAG, "mSocket == null")
                val options = IO.Options()
                options.transports = arrayOf(WebSocket.NAME)
                options.query = "token=my custom token"
                options.reconnection = true
                options.forceNew = false
                options.reconnectionDelay = 1000
                options.timeout = 10000
                mSocket = IO.socket(SOCKET_SERVER,options)
                mPreferenceUtils = PreferenceUtils.getInstance(context!!)
            }
        } catch (e: URISyntaxException) {
            Log.d(TAG, "AppSocketIO: $e")
            throw RuntimeException(e)
        }
    }

    fun sendBroadCastEventData(event: String?, data: String?) {
        val localIntent = Intent(BROADCAST_EVENT)
        localIntent.putExtra(BROADCAST_EVENT_NAME, event)
        localIntent.putExtra(BROADCAST_EVENT_DATA, data)
        mContext!!.sendBroadcast(localIntent)
    }

    private val onReceivedMessage =
        Emitter.Listener { args ->
            Log.d(TAG, "call:get onReceivedMessage " + args[0])
            if (ValidationUtils.isValidString(args[0].toString())) {
                try {
                    val jsonObject = JSONObject(args[0].toString())
                    if (args[0].toString() != "{}") {
                        val intent = Intent(Common.RECIEVE_CHAT_MESSAGE)
                        intent.putExtra(BROADCAST_EVENT_NAME, Common.RECIEVE_CHAT_MESSAGE)
                        intent.putExtra(BROADCAST_EVENT_DATA, "" + jsonObject)
                        LocalBroadcastManager.getInstance(mContext!!).sendBroadcast(intent)
                    }

                    // sendBroadCastEventData(Common.RECIEVE_CHAT_MESSAGE, jsonObject.toString());
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

    private val onReceivedMessageToAdmin =
        Emitter.Listener { args ->
            Log.d(TAG, "call:get onReceivedMessage " + args[0])
            if (ValidationUtils.isValidString(args[0].toString())) {
                try {
                    val jsonObject = JSONObject(args[0].toString())
                    if (args[0].toString() != "{}") {
                        val intent = Intent(Common.RECIEVE_CHAT_MESSAGE_TO_ADMIN)
                        intent.putExtra(BROADCAST_EVENT_NAME, Common.RECIEVE_CHAT_MESSAGE_TO_ADMIN)
                        intent.putExtra(BROADCAST_EVENT_DATA, "" + jsonObject)
                        LocalBroadcastManager.getInstance(mContext!!).sendBroadcast(intent)
                    }

                    // sendBroadCastEventData(Common.RECIEVE_CHAT_MESSAGE, jsonObject.toString());
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    companion object {
        private val TAG = AppSocketIO::class.java.simpleName

        //public static final String SOCKET_SERVER = "http://192.249.121.94:8080";/* 182.76.80.118 */
        //public static final String SOCKET_SERVER = "http://192.249.121.94:8080";/* 182.76.80.118 */
      //Testing...Payal Comment this socket url
    //    const val SOCKET_SERVER = "http://205.134.254.135:8000" /* 182.76.80.118 */

      //  const val SOCKET_SERVER = "http://209.182.213.242:5252" /* 182.76.80.118 */

        //const val SOCKET_SERVER = "http://209.182.213.242:5252" /* 182.76.80.118 */
        //const val SOCKET_SERVER = "http://54.184.120.212:5252" /* 182.76.80.118 */
        const val SOCKET_SERVER = "http://34.214.47.133:5252" /* 182.76.80.118 */

        // EVENT STRINGS
        const val EVENT_SEND_MESSAGE = "message"
        const val EVENT_DRIVER_LOCATION = "setdriverlocation"
        const val EVENT_GET_BOOKING_REQUEST = "get_booking_request_"
        const val EVENT_ACCEPT_BOOKING_REQUEST = "accept_booking_request"
        const val EVENT_GET_CONFIRM_RIDE_DETAILS = "get_confirm_ride_details_"
        const val EVENT_GET_CANCEL_RIDE_DETAILS = "get_cancel_ride_details_"

        // BROADCAST RECEIVER
        const val BROADCAST_EVENT = "socket_io_broadcast"
        const val BROADCAST_EVENT_NAME = "socket_io_broadcast_event_name"
        const val BROADCAST_EVENT_DATA = "socket_io_broadcast_event_data"

        // public static final String EVENT_RECEIVE_LOCATION = "message";
        const val EVENT_CHAT_SEND_MESSAGE = "send_message"
        const val EVENT_CHAT_SEND_MESSAGE_TO_ADMIN = "admindriver_send_message"

        // This event is for received Message
        const val EVENT_RECEIVE_MESSAGE = "receive_message_"

        const val EVENT_RECEIVE_MESSAGE_TO_ADMIN = "admindriver_receive_message_"
    }
}