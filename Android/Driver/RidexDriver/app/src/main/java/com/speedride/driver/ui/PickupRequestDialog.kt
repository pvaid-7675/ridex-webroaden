package com.speedride.driver.ui

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import com.speedride.driver.utils.ValidationUtils.isValidObject
import com.speedride.driver.utils.ValidationUtils.isInternetAvailable
import com.speedride.driver.utils.AppLog.i
import com.speedride.driver.utils.AppLog.d
import com.google.android.gms.maps.OnMapReadyCallback
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.speedride.driver.utils.PermissionUtils
import com.google.android.gms.maps.GoogleMap
import com.speedride.driver.modules.ride.dataModel.CustomerRequestModel
import com.google.android.gms.maps.MapView
import android.os.Bundle
import android.view.WindowManager
import com.google.android.gms.maps.MapsInitializer
import com.speedride.driver.utils.PermissionUtils.OnPermissionGrantCallback
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import android.content.IntentSender.SendIntentException
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.CameraUpdateFactory
import android.location.Geocoder
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.Window
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.speedride.driver.app.AppController
import com.speedride.driver.broadcast_receiver.ReceiverClass
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.PreferenceUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import com.speedride.driver.R

class PickupRequestDialog : Dialog, View.OnClickListener, OnMapReadyCallback {
    private var parentActivity: AppCompatActivity? = null
    private var mTxtAccept: TextView? = null
    private var mTxtReject: TextView? = null
    private var mTxtEstTime: TextView? = null
    private var mTxtEstEarn: TextView? = null
    private var mTxtAddress: TextView? = null
    private var mTxtLat: TextView? = null
    private var mTxtLong: TextView? = null
    private var mPermission: PermissionUtils? = null
    private var mGoogleMap: GoogleMap? = null
    private var mCustomerRequestModel: CustomerRequestModel? = null
    private var mapView: MapView? = null
    private var mApiAcceptRideList: Call<Object>? = null
    private var mPreferenceUtils: PreferenceUtils? = null

    constructor(activity: AppCompatActivity) : super(activity) {
        parentActivity = activity
    }

    constructor(
        activity: AppCompatActivity,
        mTxtAccept: TextView?,
        mTxtReject: TextView?,
        mTxtEstTime: TextView?,
        mTxtEstEarn: TextView?,
        mTxtAddress: TextView?,
        mTxtLat: TextView?,
        mTxtLong: TextView?
    ) : super(activity) {
        parentActivity = activity
        this.mTxtAccept = mTxtAccept
        this.mTxtReject = mTxtReject
        this.mTxtEstTime = mTxtEstTime
        this.mTxtEstEarn = mTxtEstEarn
        this.mTxtAddress = mTxtAddress
        this.mTxtLat = mTxtLat
        this.mTxtLong = mTxtLong
    }

    constructor(
        appCompatActivity: AppCompatActivity?,
        customerRequestModel: CustomerRequestModel?
    ) : super(
        appCompatActivity!!
    ) {
        if(AppController.resumedActivity!=null){
            Log.d(TAG,"AppController")
        }else{
            Log.d(TAG,"NotAppController")
        }

        if(customerRequestModel!=null){
            Log.d(TAG,"customerRideData")
        }else{
            Log.d(TAG,"NotcustomerRideData")
        }
        parentActivity = appCompatActivity
        mCustomerRequestModel = customerRequestModel
    }

    constructor(context: Context?) : super(context!!) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            this.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            setCanceledOnTouchOutside(false)
                                                                                                                                          setContentView(R.layout.activity_pickup_request_dialog)
            init()
            mapView!!.onCreate(savedInstanceState)
            mapView!!.getMapAsync(this)
            mapView!!.onResume()
            try {
                parentActivity?.let { MapsInitializer.initialize(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // mapView.setAlpha(0.8f);
    }

    private fun init() {
        mapView = findViewById(R.id.map_view)
        mTxtAccept = findViewById(R.id.txtAccept)
        mTxtReject = findViewById(R.id.txtReject)
        mTxtEstTime = findViewById(R.id.txtTimePickup)
        mTxtEstEarn = findViewById(R.id.txtEarnPickup)
        mTxtAddress = findViewById(R.id.txtAddressPickup)
        mTxtAccept!!.setOnClickListener(this)
        mTxtReject!!.setOnClickListener(this)
        mPreferenceUtils = PreferenceUtils.getInstance(parentActivity!!)
        if (isValidObject(mCustomerRequestModel)) {
            mTxtEstTime!!.setText(mCustomerRequestModel!!.estimate_time )
            mTxtEstEarn!!.setText("$"+mCustomerRequestModel!!.estprice)
           // mTxtEstEarn!!.setText("$"+mCustomerRequestModel!!.charge)
            mTxtAddress!!.setText(mCustomerRequestModel!!.departure)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mPermission = PermissionUtils(parentActivity, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), object : OnPermissionGrantCallback {
            override fun onPermissionGranted() {
                if (isInternetAvailable(parentActivity!!)) mapLoad() else Toast.makeText(
                    parentActivity,
                    parentActivity!!.resources.getString(R.string.network_error),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onPermissionError(permission: String?) {
                Toast.makeText(
                    parentActivity,
                    "Need to must Allow permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun mapLoad() {
        displayLocationSettingsRequest(parentActivity)
    }

    //for show gps location dialog
    private fun displayLocationSettingsRequest(context: Context?) {
        val googleApiClient = GoogleApiClient.Builder(context!!)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 100
        locationRequest.fastestInterval = (10000 / 2).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> i(
                    TAG,
                    "All location settings are satisfied."
                )
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    i(
                        TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(
                            parentActivity!!,
                            Common.REQUEST_GPS_CHECK_SETTINGS
                        )
                    } catch (e: SendIntentException) {
                        i(TAG, "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> i(
                    TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )
            }
        }
    }

    private fun setMarkerOnCustomerLocation() {

        //mCurrentLocation = new LatLng(23.0183105, 72.5418474);
        val latDouble = java.lang.Double.valueOf(mCustomerRequestModel!!.dlat)
        val longDouble = java.lang.Double.valueOf(mCustomerRequestModel!!.dlong)
        val latLng = LatLng(latDouble, longDouble)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        val t = getCompleteAddressString(latDouble, longDouble)
        markerOptions.title(t)
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin))
        mGoogleMap!!.addMarker(markerOptions) /*.showInfoWindow();*/
        mGoogleMap!!.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latDouble, longDouble),
                15f
            ), 1000, null
        )
    }

    //return address of given location
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        val geocoder = Geocoder(parentActivity!!)
        try {
            val addressList = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            d(TAG, "lat long of Address: $addressList")
            if (addressList != null && addressList.size > 0) {
                val locality = addressList[0].getAddressLine(0)
                val address = addressList[0].subLocality
                val country = addressList[0].countryName
                val state = addressList[0].adminArea
                var strAdd = ""
                if (!locality.isEmpty() && !country.isEmpty()) {
                    strAdd = "$locality,$country"
                    mTxtAddress!!.text = strAdd
                    return strAdd
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "Address not found"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.txtAccept -> {
               if(mPreferenceUtils?.customerRideData!!.is_schedule.equals("1")){
                   ApiCallAcceptRideSchedule()
               }else{
                   if(mPreferenceUtils?.customerRideData!!.is_sharing!!.equals(1) && mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_START_TRIP, false)){
                       mPreferenceUtils?.save(PreferenceUtils.PREF_KEY_IS_SHARED_RIDE_ON,true)
                   }
                   val intentRequest = Intent(parentActivity, ReceiverClass::class.java)
                   intentRequest.action = Common.RIDE_REQUEST_ACCEPT
                   parentActivity!!.sendBroadcast(intentRequest)
               }
            }
            R.id.txtReject -> dismiss()
            else -> {}
        }
        dismiss()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        Log.d(TAG, "onMapReady: ")
        setMarkerOnCustomerLocation()
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.setOnCameraIdleListener {
            val latLng = googleMap.cameraPosition.target
            d(TAG, "lat long:$latLng")
            val geocoder = Geocoder(parentActivity!!)
            try {
                val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                d(TAG, "lat long of Address: $addressList")
                if (addressList != null && addressList.size > 0) {
                    val locality = addressList[0].getAddressLine(0)
                    val address = addressList[0].subLocality
                    val country = addressList[0].countryName
                    val state = addressList[0].adminArea
                    if (!locality.isEmpty() && !country.isEmpty()) {
                        i(TAG, "onCameraChange: Locality: $locality Country:$country")
                        //add marker and address
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            //mRlSearch.setVisibility(View.VISIBLE);
        }
        googleMap.setOnCameraMoveListener {
            //mRlSearch.setVisibility(View.GONE);
        }
    }

    override fun onStop() {
        super.onStop()
        dismiss()
    }

    companion object {
        private val TAG = PickupRequestDialog::class.java.simpleName
    }

    fun ApiCallAcceptRideSchedule() {
        mApiAcceptRideList = Singleton.restClient.acceptRideSchedule(mCustomerRequestModel!!.driver_id,mCustomerRequestModel!!.book_id)
        mApiAcceptRideList!!.enqueue(object : Callback<Object> {
            override fun onResponse(
                call: Call<Object>,
                response: Response<Object>
            ) {
                try {
                    val resp = response.body()
                    val gson = Gson()
                    val json = gson.toJson(resp)
                    val jObject = JSONObject(json)
                    Log.d(ContentValues.TAG, response.message())
                    if(jObject.getInt("status") == Common.STATUS_200) {
                        val intentRequest = Intent(parentActivity, ReceiverClass::class.java)
                        intentRequest.action = Common.RIDE_REQUEST_ACCEPT
                        parentActivity!!.sendBroadcast(intentRequest)
                    }

                    Log.d("Schedule",response.body().toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<Object>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

     fun showToastMessage(context: Context?, message: String?) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
}