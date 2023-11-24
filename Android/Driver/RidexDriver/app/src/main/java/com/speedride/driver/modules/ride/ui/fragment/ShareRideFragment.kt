package com.speedride.driver.modules.ride.ui.fragment

import android.Manifest
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.*
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.mukesh.OtpView
import com.speedride.driver.R
import com.speedride.driver.app.SplashActivity
import com.speedride.driver.base.BaseFragment
import com.speedride.driver.interfaces.GpsListener
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.model.SharedRideRequestData
import com.speedride.driver.modules.chat.ChatActivity
import com.speedride.driver.modules.home.ui.activity.MainActivity
import com.speedride.driver.modules.ride.dataModel.CustomerRequestModel
import com.speedride.driver.modules.ride.ui.activity.OnRideActivity
import com.speedride.driver.modules.ride.ui.adapter.SharedRideAdapter
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.*
import com.speedride.driver.utils.PermissionUtils.OnPermissionGrantCallback
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ShareRideFragment : BaseFragment(), View.OnClickListener, OnMapReadyCallback, GpsListener,
    OnInfoWindowClickListener {
    private var switcher: ViewSwitcher? = null
    private var mOnRideActivity: OnRideActivity? = null
    private var mTxtBtnStartTrip: TextView? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mPermission: PermissionUtils? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null
    private var mGoogleMap: GoogleMap? = null
    private val mPlace: Place? = null
    private var mPickupLatLong: LatLng? = null
    private var mDropOffLatLong: LatLng? = null
    private var mTxtPickUp: TextView? = null
    private var mTxtPickDrop: TextView? = null
    private var mTxtUserName: TextView? = null
    private var mTxtTime: TextView? = null
    private var mTxtDistance: TextView? = null
    private var mImgUser: ImageView? = null
    private var imgChat: ImageView? = null

    private var llRideData:LinearLayout?=null
    private var ivNavigation: FloatingActionButton?=null
    private var markerPickup: Marker? = null
    private var markerDropOff: Marker? = null
    private var markerCount = 0
    private var googleApiClient: GoogleApiClient? = null
    private var start_rotation = 0f
    private var isMarkerRotating = false
    private var customerRequestModel: CustomerRequestModel? = null
    private var dialogPassCode: Dialog? = null
    private var strPassCode: String? = ""
    private var isValidatePassCode = false
    private var mApiReviewRatingTripResponseCall: Call<ServerResponse<Object>>? = null
    private var mApiAcceptRideList: Call<SharedRideRequestData>? = null
    private var routeFound = false
    private var isFirstTimeAfterRideConfirm = false
    private var mRvTripList:RecyclerView? = null
    private var mSharedRideAdapter: SharedRideAdapter?=null
    private var isShared:Int = 0
    private var mSharedRideList: List<SharedRideRequestData.Data>?=null
    private var mApiAdminRide: Call<Object>? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_on_trip

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }
        markerCount = 0
        mOnRideActivity = activity as OnRideActivity?
        mOnRideActivity!!.setTitle(resources.getString(R.string.on_trip))
        mPermission = PermissionUtils(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), object : OnPermissionGrantCallback {
            override fun onPermissionGranted() {
                if (ValidationUtils.isInternetAvailable(parentActivity!!)) {
                    mapLoad()
                    AppLog.d(TAG, "ifNetwork")
                } else {
                    checkInternet()
                    AppLog.d(TAG, "ifNoNetwork")
                }
            }

            override fun onPermissionError(permission: String?) {
                showToastMessage(activity!!, "Need to must Allow permission")
                parentActivity!!.finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        AppLog.d(TAG, "test_ onResume")
        if (mOnRideActivity == null) mOnRideActivity = activity as OnRideActivity?
        if (mOnRideActivity != null) {
            LocalBroadcastManager.getInstance(mOnRideActivity!!).registerReceiver(
                receiverForRideCancel!!,
                IntentFilter(Common.NOTIFY_RIDE_CANCEL_DETAILS)
            )

            LocalBroadcastManager.getInstance(mOnRideActivity!!).registerReceiver(
                receiverForRideAccept!!,
                IntentFilter(Common.NEW_SHARED_RIDE_DETAILS)
            )
        }
    }

    override fun onStop() {
        super.onStop()
        AppLog.d(TAG, "test_ onStop")
        if (receiverForRideCancel != null) LocalBroadcastManager.getInstance(mOnRideActivity!!)
            .unregisterReceiver(receiverForRideCancel)

        if (receiverForRideAccept != null) LocalBroadcastManager.getInstance(mOnRideActivity!!)
            .unregisterReceiver(receiverForRideAccept)
    }

    //receiver cancel ride details
    private val receiverForRideCancel: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (dialogPassCode != null && dialogPassCode!!.isShowing) dialogPassCode!!.hide()
            if (context != null) {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.cancel_trip_by_customer),
                    Toast.LENGTH_SHORT
                ).show()
                // Get extra data included in the Intent
                if (mPreferenceUtils == null) mPreferenceUtils =
                    PreferenceUtils.getInstance(context)
                mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, false)
                mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, false)
                mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, false)
                mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COMPLETE_TRIP, false)
                val intentHome = Intent(context, MainActivity::class.java)
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                animationIntent(context, intentHome)
            }
        }
    }


    //receiver cancel ride details
    private val receiverForRideAccept: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ApiCallSharedData()
        }
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun initView(view: View?) {

        switcher = view?.findViewById(R.id.viewSwitcher)
        mPreferenceUtils = PreferenceUtils.getInstance(parentActivity!!)
        customerRequestModel = mPreferenceUtils!!.customerRideData

        mTxtPickUp = view?.findViewById(R.id.txtPickUp)
        mTxtPickDrop = view?.findViewById(R.id.txtPickDrop)
        mTxtUserName = view?.findViewById(R.id.txtUserName)
        mTxtTime = view?.findViewById(R.id.txtTime)
        mTxtDistance = view?.findViewById(R.id.txtDistance)
        mImgUser = view?.findViewById(R.id.imgUserIcon)
        imgChat = view?.findViewById(R.id.imgChat)
        mTxtBtnStartTrip = view?.findViewById(R.id.btnStartTrip)
        mRvTripList = view?.findViewById(R.id.rvTripList)
        llRideData = view?.findViewById(R.id.llRideData)
        ivNavigation = view?.findViewById(R.id.ivNavigation)
        // mRvTripList?.visibility = View.VISIBLE
        //  mTxtBtnStartTrip?.visibility = View.GONE
        mSharedRideList = arrayListOf()
        mTxtBtnStartTrip!!.setOnClickListener(this)
        ivNavigation!!.setOnClickListener(this)
        imgChat!!.setOnClickListener(this)

        isShared = customerRequestModel!!.is_sharing
        if(isShared.equals(1)){ //This is shared ride
            ivNavigation?.visibility = View.VISIBLE
            mTxtBtnStartTrip?.visibility = View.GONE
            mRvTripList?.visibility = View.VISIBLE
            mTxtUserName?.visibility = View.GONE
            mTxtTime?.visibility = View.GONE
            mImgUser?.visibility = View.GONE
            mTxtDistance?.visibility = View.GONE
            llRideData?.visibility = View.GONE
           // mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, true)
        }else{
            ivNavigation?.visibility = View.GONE
            mRvTripList?.visibility = View.GONE
            mTxtBtnStartTrip?.visibility = View.VISIBLE
            mTxtDistance?.visibility = View.VISIBLE
            if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, false)) {
                mTxtBtnStartTrip!!.setText(resources.getString(R.string.enter_passcode))
            } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_START_TRIP, false)) {
                mTxtBtnStartTrip!!.setText(resources.getString(R.string.start_trip))
            } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_COMPLETE_TRIP, false)) {
                mTxtBtnStartTrip!!.setText(resources.getString(R.string.complete_trip))
            }
        }
        checkInternet()
        if (ValidationUtils.isValidObject(customerRequestModel)) {
            if (ValidationUtils.isValidString(customerRequestModel!!.name)) mTxtUserName!!.setText(
                customerRequestModel!!.name
            )
            if (ValidationUtils.isValidString(customerRequestModel!!.requestdatetime)) {
                val convertedDate =
                    Utils.stringDateToFormatDate(customerRequestModel!!.requestdatetime)
                val strDate = Utils.dateFormatToStringDateForEarning(convertedDate)
                mTxtTime!!.setText(strDate)
            }
            if (ValidationUtils.isValidString(customerRequestModel!!.image)) Glide.with(this)
                .load(
                    Common.UPLOAD_URL + customerRequestModel!!.image
                ).placeholder(R.drawable.ic_driver_profile).into(mImgUser!!)
        }
        if(isShared.equals(1)){
            ApiCallSharedData()
        }

    }

    override fun showToastMessage(context: Context?, message: String?) {

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkInternet() {
        if (!ValidationUtils.isInternetAvailable(parentActivity!!)) {
            switcher!!.showNext()
            Log.d("Off", "checkInternet: ")
        }
    }

    //for current mLocation continue
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.lastLocation != null) {
                mCurrentLocation = locationResult.lastLocation
                AppLog.d("lastLocation", mCurrentLocation.toString())
                currentLocation()
                Log.e("Current Location","1")
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        //add map style
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(), R.raw.map_json
                )
            )
            if (!success) {
                // Handle map style load failure
            }
        } catch (e: Resources.NotFoundException) {
            // Oops, looks like the map style resource couldn't be found!
        }

        //set rotation off every side
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.setOnCameraIdleListener {
            val latLng = googleMap.cameraPosition.target
            AppLog.d(TAG, "lat long:$latLng")
            val geocoder = Geocoder(parentActivity!!)
            try {
                val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                AppLog.d(TAG, "lat long of Address: $addressList")
                if (addressList != null && addressList.size > 0) {
                    val houseNum = addressList[0].featureName
                    val societyName = addressList[0].thoroughfare
                    val areaName = addressList[0].subLocality
                    val cityName = addressList[0].locality
                    val strAdd = "$houseNum , $societyName \n $areaName , $cityName"
                    AppLog.d(TAG, strAdd)
                    val locality = addressList[0].getAddressLine(0)
                    val address = addressList[0].subLocality
                    val country = addressList[0].countryName
                    val state = addressList[0].adminArea
                    if (!locality.isEmpty() && !country.isEmpty()) {
                        //get address
                        mTxtPickUp!!.text = strAdd
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
        googleMap.setOnInfoWindowClickListener(this)

        //set markers custom infoWindow
        googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val info = LinearLayout(parentActivity)
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(parentActivity)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title
                val snippet = TextView(parentActivity)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet
                if (ValidationUtils.isValidObject(markerDropOff) && marker == markerDropOff) {
                    val imageView = ImageView(parentActivity)
                    imageView.setPadding(0, 20, 20, 0)
                    imageView.background =
                        parentActivity!!.resources.getDrawable(R.drawable.ic_call)
                    val layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    val imageViewInfoLL = LinearLayout(parentActivity)
                    imageViewInfoLL.layoutParams = layoutParams
                    imageViewInfoLL.gravity = Gravity.CENTER
                    imageViewInfoLL.addView(imageView)
                    info.addView(imageViewInfoLL)
                    imageView.setOnClickListener { Log.d(TAG, "onClick: Marker image click") }
                }
                info.addView(title)
                info.addView(snippet)
                return info
            }
        })
        mGoogleMap = googleMap

        currentLocation()
        Log.e("Current Location","3")
    }

    //return address of given location
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        val geocoder = Geocoder(parentActivity!!)
        try {
            val addressList = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            AppLog.d(TAG, "lat long of Address: $addressList")
            if (addressList != null && addressList.size > 0) {
                val locality = addressList[0].getAddressLine(0)
                val address = addressList[0].subLocality
                val country = addressList[0].countryName
                val state = addressList[0].adminArea
                var strAdd = ""
                if (!locality.isEmpty() && !country.isEmpty()) {
                    strAdd = "$locality,$country"
                    return strAdd
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "Address not found"
    }

    //return address of given location
    private fun getCompleteAddressString(
        string: String,
        LATITUDE: Double,
        LONGITUDE: Double
    ): String {
        var strTitle = ""
        var strSnipped = ""
        val geocoder = Geocoder(parentActivity!!, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder
                .getFromLocation(LATITUDE, LONGITUDE, 1)
            Log.e(TAG, "getCompleteAddressString: $addresses")
            if (addresses != null) {
                val houseNum = addresses[0].featureName
                val societyName = addresses[0].thoroughfare
                val areaName = addresses[0].subLocality
                val cityName = addresses[0].locality

                //strAdd = houseNum + " , " + societyName + " \n " + areaName + " , " + cityName;
                return if (string == "title") {
                    if (houseNum != null) strTitle = strTitle + houseNum
                    if (societyName != null) strTitle = "$strTitle, $societyName"
                    strTitle
                } else {
                    if (areaName != null) strSnipped = strSnipped + areaName
                    if (cityName != null) strSnipped = "$strSnipped, $cityName"
                    strSnipped
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("MyLocationAddress", "Can not get Address!")
        }
        return ""
    }

    private fun mapLoad() {
        displayLocationSettingsRequest(parentActivity!!)
        mFusedLocationClient = FusedLocationProviderClient(parentActivity!!)
        //send request for location   add this method for get call back from time interval-->// .setInterval(2000).setFastestInterval(2000)
        mLocationRequest =
            LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(7000)
                .setFastestInterval(5000)
        if (ActivityCompat.checkSelfPermission(
                parentActivity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                parentActivity!!, Manifest.permission.ACCESS_COARSE_LOCATION
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
        if (requireActivity().supportFragmentManager != null) {
            mMapFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mMapFragment!!.getMapAsync(this)
        }
    }

    //for show gps location dialog
    private fun displayLocationSettingsRequest(context: Context) {
        googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient!!.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        if (activity != null) {
            val result = LocationServices.getSettingsClient(
                requireActivity()
            ).checkLocationSettings(builder.build())
            result.addOnCompleteListener { task ->
                try {
                    val response = task.getResult(
                        ApiException::class.java
                    )
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (exception: ApiException) {
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                                 // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                val resolvable = exception as ResolvableApiException
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                    requireActivity(),
                                    Common.REQUEST_GPS_CHECK_SETTINGS
                                )
                            } catch (e: SendIntentException) {
                                // Ignore the error.
                            } catch (e: ClassCastException) {
                                // Ignore, should be an impossible error.
                            }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                    }
                }
            }
        }
    }

    override fun onGPSChanged(status: Boolean) {
        //status value come from Main Activity onActivityResult
        //status is true when user click on current location with gps service is on
        if (status) {
            if (ActivityCompat.checkSelfPermission(
                    parentActivity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    parentActivity!!, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                displayLocationSettingsRequest(parentActivity!!)
                return
            } else {
                mFusedLocationClient!!.lastLocation.addOnSuccessListener(parentActivity!!) { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mCurrentLocation = location
                        currentLocation()
                        Log.e("Current Location","2")
                    }
                }
            }
        }
    }

    //set markerPickup for current(pick up) location
    private fun getPickupLocationMarker(latLng: LatLng): MarkerOptions {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(getCompleteAddressString(latLng.latitude, latLng.longitude))
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_taxi_icon))
        return markerOptions
    }

    //set markerPickup for drag off location
    private fun getDestinationLocationMarker(latLng: LatLng): MarkerOptions {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(getCompleteAddressString(latLng.latitude, latLng.longitude))
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin))
        return markerOptions
    }

    //zoom map after start point and end point
    fun zoomRoute(googleMap: GoogleMap?, mPickupLatLong: LatLng?, mDropOffLatLong: LatLng?) {
        if (googleMap == null || mPickupLatLong == null || mDropOffLatLong == null) return
        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(mPickupLatLong)
        boundsBuilder.include(mDropOffLatLong)
        val routePadding = 200
        val latLngBounds = boundsBuilder.build()

        //googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));

        //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100), 2000, null);
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels /* Utilities.dp(309.50f);*/
        val padding = (width * 0.10).toInt() // offset from edges of the map 12% of screen
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBounds,
                width,
                height,
                padding
            )
        )
    }

    //calculates the distance between two locations in MILES
      fun distance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val earthRadius =
            6371.0 // in miles, change to 6371 "3958.75 for mile'for kilometer output
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val a = Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(
            Math.toRadians(
                lat2
            )
        ))
        val c =
            2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        AppLog.d("AAA", "distance: $c         $a")
        return earthRadius * c // output distance, in MILES
    }

    override fun onInfoWindowClick(marker: Marker) {
        if (ValidationUtils.isValidObject(markerDropOff) && marker == markerDropOff) {
            if (ValidationUtils.isValidObject(getCurrentCustomer()) && ValidationUtils.isValidString(
                    getCurrentCustomer()?.mobile
                )
            ) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + (getCurrentCustomer()?.mobile))
                animationIntent(requireActivity(), intent)
            }
        }
    }

    //For map line create : Path Draw
    private inner class DownloadTask : AsyncTask<String?, Void?, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            //mProgressBar.setVisibility(View.VISIBLE);
            AppLog.d(TAG, "onMapClick: For map line create : Path Draw")
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val handler = Handler()
            handler.postDelayed({
                //mProgressBar.setVisibility(View.GONE);
                val parserTask = ParserTask()
                parserTask.execute(result)
            }, 700)
        }

        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try {
                data = url[0]?.let { downloadUrl(it) }.toString()
            } catch (e: Exception) {
                AppLog.d("onMapClick: PATH Draw Line ERROR: ", e.toString())
            }
            return data
        }
    }

    //A class to parse the Google Places in JSON format
    private inner class ParserTask :
        AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {
        // Parsing the data in non-ui thread


        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            var points: ArrayList<LatLng?>? = null
            var lineOptions: PolylineOptions? = null
            val markerOptions = MarkerOptions()
            var distance: String? = ""
            var duration: String? = ""
            if (ValidationUtils.isValidList(result)) {
                routeFound = true
                // Traversing through all the routes
                for (i in result!!.indices) {
                    points = ArrayList()
                    lineOptions = PolylineOptions()

                    // Fetching i-th route
                    val path = result[i]

                    // Fetching all the points in i-th route
                    for (j in path.indices) {
                        val point = path[j]
                        if (j == 0) {    // Get distance from the list
                            distance = point["distance"]
                            continue
                        } else if (j == 1) { // Get duration from the list
                            duration = point["duration"]
                            continue
                        }
                        val lat = point["lat"]!!.toDouble()
                        val lng = point["lng"]!!.toDouble()
                        val position = LatLng(lat, lng)
                        points.add(position)
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points)
                    lineOptions.width(9f)
                    lineOptions.color(R.color.button_color)

                    //mTxtKM.setText("Distance:"+distance + ", Duration:"+duration);

                    // Drawing polyline in the Google Map for the i-th route
                    mGoogleMap!!.addPolyline(lineOptions)
                }
            } else {
                routeFound = false
                //showToastMessage(getActivity(), "No Route found");
            }
        }

        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
                AppLog.e(TAG, "doInBackground: PATH Draw Line ERROR 2 : ", e)
            }
            return routes
        }
    }

    fun redirectToMapAppWithPath(){
        if(mSharedRideList !=null && mSharedRideList!!.size>0){
            var srcAdd = "&origin=" + mSharedRideList!![0]!!.plat + "," + mSharedRideList?.get(0)!!.plong
            var desAdd = "&destination=" + mSharedRideList!![0]!!.dlat.toString() + "," +
                    mSharedRideList!![0]!!.dlong
            var wayPoints = ""
            if(mSharedRideList !=null && mSharedRideList!!.size>1){
                for (j in 0 until mSharedRideList!!.size) {
                    if (j==0){
                        srcAdd = "&origin=" + mSharedRideList!![0]!!.plat + "," + mSharedRideList?.get(0)!!.plong
                        desAdd = "&destination=" + mSharedRideList!![0]!!.dlat.toString() + "," +
                                mSharedRideList!![0]!!.dlong
                    }else {
                        wayPoints = wayPoints + (if (wayPoints == "") "" else "%7C") +
                                mSharedRideList!!.get(j).dlat + "," + mSharedRideList!!.get(j).dlong
                    }
                }
            }

            val paramwayPoints = "&waypoints=$wayPoints"

            val link = "https://www.google.com/maps/dir/?api=1&travelmode=driving$srcAdd$desAdd$paramwayPoints"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
            startActivity(intent)



        }
        /* val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode"

        // Output format
        val output = "json"
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/dir/?api=1$parameters")
        )
        startActivity(intent)*/
    }


    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode"+"&key=" + getString(R.string.google_maps_key);

        // Output format
        val output = "json"

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }


    //A method to download json data from url
    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection!!.connect()
            iStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: Exception) {
            AppLog.d("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermission!!.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    private fun selectedLocation(latLng: LatLng) {
        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f), 1000, null)
    }
    var isDistanceCounting = false
    var currentLocationCallCount = 0
    //current location
    private fun currentLocation() {
        if (ActivityCompat.checkSelfPermission(
                parentActivity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                parentActivity!!, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            displayLocationSettingsRequest(parentActivity!!)
            return
        } else {
            var mCurrentLocation: Location? = this.mCurrentLocation
            if (mCurrentLocation != null && customerRequestModel != null) {
                var dropLatDouble: Double? = null
                var dropLongDouble: Double? = null
                if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, false)) {
                    dropLatDouble = java.lang.Double.valueOf(customerRequestModel!!.plat)
                    dropLongDouble = java.lang.Double.valueOf(customerRequestModel!!.plong)
                    mDropOffLatLong = LatLng(dropLatDouble, dropLongDouble)
                    mTxtPickDrop!!.text = parentActivity!!.resources.getString(R.string.pick_up)
                } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_START_TRIP, false)) { //Trip not started
                    dropLatDouble = java.lang.Double.valueOf(customerRequestModel!!.dlat)
                    dropLongDouble = java.lang.Double.valueOf(customerRequestModel!!.dlong)
                    mDropOffLatLong = LatLng(dropLatDouble, dropLongDouble)
                    mTxtPickDrop!!.text = parentActivity!!.resources.getString(R.string.drop_off)
                    markerCount = if (!isFirstTimeAfterRideConfirm) 0 else 1
                }else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_ON_TRIP, false) && mDropOffLatLong==null) {// Trip on
                    dropLatDouble = java.lang.Double.valueOf(customerRequestModel!!.dlat)
                    dropLongDouble = java.lang.Double.valueOf(customerRequestModel!!.dlong)
                    mDropOffLatLong = LatLng(dropLatDouble, dropLongDouble)
                    markerCount = 0
                }
                mPickupLatLong = LatLng(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
                val mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient!!)
                Log.e(TAG, "mLastLocation: $mLastLocation")
                if (mPickupLatLong != null && mDropOffLatLong != null){
                    if (markerCount == 1 && markerPickup != null) {
                        //rotateMarker(markerPickup, start_rotation);
                        animateMarker(mCurrentLocation!!, markerPickup)
                    } else {
                        val isPassCodeConfirm = mPreferenceUtils!!.get(
                            PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM,
                            false
                        )
                        if (isPassCodeConfirm)
                            isFirstTimeAfterRideConfirm = true
                        mTxtPickUp!!.text = getCompleteAddressString(
                            mCurrentLocation!!.latitude,
                            mCurrentLocation!!.longitude
                        )
                        mGoogleMap!!.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mCurrentLocation!!.latitude, mCurrentLocation!!.longitude
                                ), 16f
                            ), 1000, null
                        )
                        mGoogleMap!!.clear()
                        markerPickup = mGoogleMap!!.addMarker(
                            MarkerOptions().position(mPickupLatLong!!).title(
                                getCompleteAddressString(
                                    "title",
                                    mCurrentLocation!!.latitude,
                                    mCurrentLocation!!.longitude
                                )
                            ).snippet(
                                getCompleteAddressString(
                                    "snippet",
                                    mCurrentLocation!!.latitude,
                                    mCurrentLocation!!.longitude
                                )
                            ).icon(
                                BitmapDescriptorFactory.fromResource(
                                    Utils.checkVehicleImagesTopView(
                                        mPreferenceUtils!!.get(
                                            PreferenceUtils.PREF_KEY_VEHICLE_ID,
                                            ""
                                        )
                                    )
                                )
                            )
                        )
                        markerDropOff = mGoogleMap!!.addMarker(
                            MarkerOptions().position(mDropOffLatLong!!).title(
                                getCompleteAddressString(
                                    "title",
                                    mDropOffLatLong!!.latitude,
                                    mDropOffLatLong!!.longitude
                                )
                            ).snippet(
                                getCompleteAddressString(
                                    "snippet",
                                    mDropOffLatLong!!.latitude,
                                    mDropOffLatLong!!.longitude
                                )
                            ).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin))
                        )
                        markerDropOff!!.showInfoWindow()

                        //save pickup drop addresses
                        val pickUpAddress = getCompleteAddressString(
                            mCurrentLocation!!.latitude,
                            mCurrentLocation!!.longitude
                        )
                        val dropOffAddress = getCompleteAddressString(
                            mDropOffLatLong!!.latitude,
                            mDropOffLatLong!!.longitude
                        )
                        Log.d(TAG, "pickUpAddress:$pickUpAddress\nDropOffAddress: $dropOffAddress")
                        val origin: LatLng = mPickupLatLong!!
                        val dest: LatLng = mDropOffLatLong!!
                        //
                        /* mGoogleMap.addMarker(getDestinationLocationMarker(mDropOffLatLong)).showInfoWindow();
                mGoogleMap.addMarker(getPickupLocationMarker(mPickupLatLong)).showInfoWindow();*/
                       /* val distance = distance(
                            mDropOffLatLong!!.latitude,
                            mDropOffLatLong!!.longitude,
                            mPickupLatLong!!.latitude,
                            mPickupLatLong!!.longitude
                        )*/
//                        if(mTxtDistance?.text!!.equals("0.0")){
//                            val strdista3nce = "" + DecimalFormat("##.##").format(distance) + " miles"
//                            mTxtDistance?.text = String.format("%.2f", distance)
//                            AppLog.d(TAG, "distance zero")
//                        }else{
//                            AppLog.d(TAG, "distance main : $distance")
//                        }
                        AppLog.d(TAG, "currentLocationCallCount : $currentLocationCallCount")
                        currentLocationCallCount++
                        val distance = distance(
                            mDropOffLatLong!!.latitude,
                            mDropOffLatLong!!.longitude,
                            mPickupLatLong!!.latitude,
                            mPickupLatLong!!.longitude
                        )
                        // TODO: Lat Long gives wrong value here.. need to correct
                        AppLog.d(TAG+"_distance", "lat long :" +  mDropOffLatLong!!.latitude + " | " +
                            mDropOffLatLong!!.longitude + " | " +
                            mPickupLatLong!!.latitude + " | " +
                            mPickupLatLong!!.longitude)
                        AppLog.d(TAG+"_distance", "distance __main : $distance")
                        AppLog.d(TAG, "currentLocationCallCount Resultof: $currentLocationCallCount")
                        mTxtDistance?.text

                        if (ValidationUtils.isValidString(customerRequestModel!!.km)) {
                            val formatDbl = java.lang.Double.valueOf(customerRequestModel!!.km)
                            val formatStr = String.format("%.2f", formatDbl)
                            mTxtDistance!!.text = "$formatStr miles."
                            AppLog.d(TAG+"_distance"," == > $formatStr miles.")
                        }

                        val ss1 = SpannableString(" miles")
                        ss1.setSpan(RelativeSizeSpan(0.0f), 0, 0, 0) // set size

                        AppLog.d(TAG, "distance: mDropOffLatLong $mDropOffLatLong")
                        AppLog.d(TAG, "distance: mDropOffLatLong $mDropOffLatLong")
                        AppLog.d(TAG, "distance: mDropOffLatLong $mPickupLatLong")
                        AppLog.d(TAG, "distance: mDropOffLatLong $mPickupLatLong")
                       // AppLog.d(TAG, "onMapClick: Distance: $strdistance")
                        AppLog.d(TAG, "onMapClick: current LatLog: $origin")
                        AppLog.d(TAG, "onMapClick: dest LatLog: $dest")
                     /*   if (!routeFound) {
                            //mGoogleMap!!.clear()
                            // Getting URL to the Google Directions API
                            val url = getDirectionsUrl(mPickupLatLong!!, mDropOffLatLong!!)
                            val downloadTask = DownloadTask()
                            // Start downloading json data from Google Directions API
                            downloadTask.execute(url)
                        }*/

                        //mGoogleMap!!.clear()
                        // Getting URL to the Google Directions API
                        val url = getDirectionsUrl(mPickupLatLong!!, mDropOffLatLong!!)
                        val downloadTask = DownloadTask()
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url)
                        zoomRoute(mGoogleMap, mPickupLatLong, mDropOffLatLong)

                        //Set Marker Count to 1 after first markerPickup is created
                        markerCount = 1
                    }
                }
            }
        }
    }
    private fun splashScreen() {
        val handler = Handler()
    }

    private interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private fun rotateMarker(marker: Marker, toRotation: Float) {
        if (!isMarkerRotating) {
            val handler = Handler()
            val start = SystemClock.uptimeMillis()
            val startRotation = marker.rotation
            val duration: Long = 1000
            val interpolator: Interpolator = LinearInterpolator()
            handler.post(object : Runnable {
                override fun run() {
                    isMarkerRotating = true
                    val elapsed = SystemClock.uptimeMillis() - start
                    val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                    val rot = t * toRotation + (1 - t) * startRotation
                    start_rotation = if (-rot > 180) rot / 2 else rot
                    marker.rotation = if (-rot > 180) rot / 2 else rot
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16)
                    } else {
                        isMarkerRotating = false
                    }
                }
            })
        }
    }

    private fun passCodeDialog() {
        dialogPassCode = Dialog(parentActivity!!, R.style.SettingsDialogTheme)
        dialogPassCode!!.setCanceledOnTouchOutside(false)
        dialogPassCode!!.setContentView(R.layout.layout_dialog_pin_code)
        val mTxtConfirm = dialogPassCode!!.findViewById<TextView>(R.id.txtConfirm)
        mTxtConfirm.setOnClickListener(this)
        val mTxtCancel = dialogPassCode!!.findViewById<TextView>(R.id.txtCancel)
        mTxtCancel.setOnClickListener(this)
        val otpView = dialogPassCode!!.findViewById<OtpView>(R.id.txt_pin_entry_dialog)
        otpView.requestFocus()
        otpView.setOtpCompletionListener { otp ->
            Log.d(TAG + " onOtpCompleted=>", otp)
            strPassCode = otp
        }
        otpView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 6) {
                    isValidatePassCode = true
                    strPassCode = s.toString()
                    mTxtConfirm.setTextColor(resources.getColor(R.color.colorAccent))
                    hideKeyboard(activity!!)
                } else {
                    isValidatePassCode = false
                    mTxtConfirm.setTextColor(resources.getColor(R.color.colorText))
                }
            }

            override fun afterTextChanged(s: Editable) {
                strPassCode = s.toString()
            }
        })
        dialogPassCode!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogPassCode!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialogPassCode!!.show()
    }

    //api call for add review on trip finish
    private fun apiConfirmRidePassCode() {
        mOnRideActivity!!.hideKeyboard(requireActivity())
        Log.d(
            TAG,
            "apiConfirmRidePassCode: " + strPassCode + " bookid:" + (getCurrentCustomer()?.bookid)
        )
        if (ValidationUtils.isValidString(strPassCode) && ValidationUtils.isValidString(
                getCurrentCustomer()?.bookid
            )
        ) {
            if (dialogPassCode != null && dialogPassCode!!.isShowing) dialogPassCode!!.hide()
            showProgress()
            mApiReviewRatingTripResponseCall = Singleton.restClient.confirmPassCodeRideTrip(
//               getCurrentCustomer()?.bookid,
                getCurrentCustomer()?.bookid,
                strPassCode
            )
            mApiReviewRatingTripResponseCall!!.enqueue(object : Callback<ServerResponse<Object>?> {
                override fun onResponse(
                    call: Call<ServerResponse<Object>?>,
                    response: Response<ServerResponse<Object>?>
                ) {
                    try {
                        val resp = response.body()
                        val resp1: Any? = response.body()

                        val gson = Gson()
                        val json = gson.toJson(resp1)
                        val jObject = JSONObject(json)
                        Log.d(TAG, "onResponse:ob " + Gson().toJson(resp))
                        dismissProgress()
                        if (jObject.getInt("status") == Common.STATUS_200) {
                            AppLog.d(TAG, jObject.getString("status"))
                            if(isShared.equals(1)){
                                ApiCallSharedData()
                            }
                            if (dialogPassCode != null && dialogPassCode!!.isShowing) dialogPassCode!!.dismiss()
                            isValidatePassCode = false
                            strPassCode = null
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, true)
                            mTxtBtnStartTrip!!.text = resources.getString(R.string.start_trip)
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    val jObjError = JSONObject(response.errorBody()!!.string())
                                    showToastMessage(context!!, jObjError.getString("message"))
                                } catch (e: Exception) {
                                    showToastMessage(context!!, e.message!!)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        dismissProgress()
                        showToastMessage(context!!,"You Enter wrong Passcode")
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Object>?>, t: Throwable) {
                    AppLog.d(TAG, "onFailure: $t")
                    dismissProgress()
                    Log.d(TAG, "onResponse:ob dsdsdsdsdsdsdsdsdsd ")
                }
            })
        }
    }

    //api call for add review on trip finish
    private fun apiStartRideWithCustomer() {
        mOnRideActivity!!.hideKeyboard(requireActivity())
        Log.d(
            TAG,
            "apiStartRideWithCustomer bookid:" + (getCurrentCustomer()?.bookid) + " cus.id:" + (getCurrentCustomer()?.customer_id
                    ) + " dri.id:" + (getCurrentCustomer()?.driver_id)
        )
        if (ValidationUtils.isValidString(getCurrentCustomer()?.bookid) && ValidationUtils.isValidString(
                getCurrentCustomer()?.customer_id
            ) && ValidationUtils.isValidString(
                ""+ getCurrentCustomer()?.driver_id
            )
        ) {
            mTxtBtnStartTrip!!.isClickable = false
            showProgress()
            mApiReviewRatingTripResponseCall = Singleton.restClient.startRideWithCustomer(
//                getCurrentCustomer()?.bookid,
                getCurrentCustomer()?.bookid,
                getCurrentCustomer()?.driver_id,
                getCurrentCustomer()?.customer_id,
                //getCurrentCustomer()?.customer_id
            )
            mApiReviewRatingTripResponseCall!!.enqueue(object : Callback<ServerResponse<Object>?> {
                override fun onResponse(
                    call: Call<ServerResponse<Object>?>,
                    response: Response<ServerResponse<Object>?>
                ) {
                    try {
                        val resp = response.body()
                        val resp1: Any? = response.body()

                        val gson = Gson()
                        val json = gson.toJson(resp1)
                        val jObject = JSONObject(json)
                        Log.d(TAG, "onResponse:start ride-- " + Gson().toJson(resp))
                        dismissProgress()
                        mTxtBtnStartTrip!!.isClickable = true
                        if (jObject.getInt("status") == Common.STATUS_200) {
                            AppLog.d(TAG, jObject.getString("status"))

                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, true)
                            if(isShared.equals(1)){
                                ApiCallSharedData()
                            }else{
                                parentActivity!!.finish()
                                animationIntent(requireActivity(), parentActivity!!.intent)
                            }

                            val newVal1 = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_START_TRIP, false)
                            mPreferenceUtils = PreferenceUtils.getInstance(activity!!)
                            val newVal2 = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_START_TRIP, false)
//                            currentLocation()
                            mTxtBtnStartTrip!!.text = resources.getString(R.string.completed_trip)
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    val jObjError = JSONObject(response.errorBody()!!.string())
                                    showToastMessage(context!!, jObjError.getString("message"))
                                } catch (e: Exception) {
                                    showToastMessage(context!!, e.message!!)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Object>?>, t: Throwable) {
                    AppLog.d(TAG, "onFailure: $t")
                    dismissProgress()
                    mTxtBtnStartTrip!!.isClickable = true
                }
            })
        }
    }

    //api call for add review on trip finish
    private fun apiCompleteRideWithCustomer() {
        mOnRideActivity!!.hideKeyboard(requireActivity())
        Log.d(
            TAG,
            "apiCompleteRideWithCustomer bookid:" + (getCurrentCustomer()?.bookid) + " cus.id:" + (getCurrentCustomer()?.customer_id) + " dri.id:" + (getCurrentCustomer()?.driver_id)
        )
        if (ValidationUtils.isValidString(getCurrentCustomer()?.bookid) && ValidationUtils.isValidString(
                getCurrentCustomer()?.customer_id
            ) && ValidationUtils.isValidString(
                ""+getCurrentCustomer()?.driver_id
            )
        ) {
            mTxtBtnStartTrip!!.isClickable = false
            showProgress()
            mApiReviewRatingTripResponseCall = Singleton.restClient.completeRideWithCustomer(
                getCurrentCustomer()?.bookid,
                getCurrentCustomer()?.driver_id,
                getCurrentCustomer()?.customer_id,


                )
            mApiReviewRatingTripResponseCall!!.enqueue(object : Callback<ServerResponse<Object>?> {
                override fun onResponse(
                    call: Call<ServerResponse<Object>?>,
                    response: Response<ServerResponse<Object>?>
                ) {
                    try {
                        val resp = response.body()

                        val gson = Gson()
                        val json = gson.toJson(resp)
                        val jObject = JSONObject(json)
                        Log.d(TAG, "onResponse:start ride-- " + Gson().toJson(resp))
                        dismissProgress()
                        mTxtBtnStartTrip!!.isClickable = true
                        if (jObject.getInt("status") == Common.STATUS_200) {
                            AppLog.d(TAG,jObject.getString("status"))

                            if(isShared.equals(1)){
                                if(getCurrentCustomer()?.paymode.equals("Cash")){
                                    mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, false) //Go to Collect cash
                                    parentActivity!!.finish()
                                    animationIntent(requireActivity(), parentActivity!!.intent)
                                }else {
                                    ApiCallSharedData()
                                }
                            }else{
                                saveCompleteDataFlow();
                            }
                            //below code moved to saveCompleteDataFlow()
//                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, false)
//                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, false)
//                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COMPLETE_TRIP, false)
//                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_ON_TRIP, true)
//                            mPreferenceUtils?.save(PreferenceUtils.PREF_KEY_IS_SHARED_RIDE_ON,false)
//                            parentActivity!!.finish()
//                            animationIntent(activity!!, parentActivity!!.intent)
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    val jObjError = JSONObject(response.errorBody()!!.string())
                                    showToastMessage(context!!, jObjError.getString("message"))
                                } catch (e: Exception) {
                                    showToastMessage(context!!, e.message!!)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Object>?>, t: Throwable) {
                    AppLog.d(TAG, "onFailure: $t")
                    dismissProgress()
                    mTxtBtnStartTrip!!.isClickable = true
                }
            })
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnRetry -> if (ValidationUtils.isInternetAvailable(parentActivity!!)) {
                switcher!!.showPrevious()
            } else {
                showToastMessage(
                    requireActivity(),
                    parentActivity!!.resources.getString(R.string.network_error)
                )
            }
            R.id.btnStartTrip -> if (!mPreferenceUtils!!.get(
                    PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM,
                    false
                )
            ) {
                passCodeDialog()
            } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_START_TRIP, false)) {
                apiStartRideWithCustomer()
            } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_COMPLETE_TRIP, false)) {
                apiCompleteRideWithCustomer()
            }
            R.id.imgChat -> {
                val intent = Intent(activity, ChatActivity::class.java)
                intent.putExtra("book_id",mPreferenceUtils?.customerRideData?.bookid)
                startActivity(intent)
            }
            R.id.txtConfirm -> if (ValidationUtils.isInternetAvailable(parentActivity!!)) {
                if (ValidationUtils.isValidString(strPassCode)) {
                    if (isValidatePassCode && strPassCode!!.length == 6) {

//                        if(isShared.equals(1)){
//                            apiConfirmRidePassCode()
//                        }else{
//                            apiConfirmRidePassCode()
//                        }
                        apiConfirmRidePassCode()

                    } else {
                        showToastMessage(
                            requireActivity(),
                            parentActivity!!.resources.getString(R.string.please_enter_valid_pass_code)
                        )
                    }
                } else {
                    showToastMessage(
                        requireActivity(),
                        parentActivity!!.getString(R.string.please_enter_passcode)
                    )
                }
            } else {
                showToastMessage(
                    requireActivity(),
                    parentActivity!!.resources.getString(R.string.network_error)
                )
            }
            R.id.txtCancel -> {
                if (dialogPassCode != null && dialogPassCode!!.isShowing) dialogPassCode!!.dismiss()
                isValidatePassCode = false
                strPassCode = null
            }
            R.id.ivNavigation ->{
                redirectToMap()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLog.d(TAG, "test_ Destroy")
        onCancelApiCall()
        dismissProgress()
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "test_ onPause: ")
        onCancelApiCall()
        dismissProgress()
        if (mPickupLatLong != null) {
            mPickupLatLong = null
            //isPickUpAddress = false;
        }
        if (mDropOffLatLong != null) {
            mDropOffLatLong = null
            //isDropOffAddress = false;
        }
    }

    private fun onCancelApiCall() {
        if (mApiReviewRatingTripResponseCall != null && !mApiReviewRatingTripResponseCall!!.isCanceled) {
            mApiReviewRatingTripResponseCall!!.cancel()
        }
    }

    companion object {
        private val TAG = ShareRideFragment::class.java.simpleName

        /*

    //for open search view of place search
    private void googlePlaceAutocomplete() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("IN").build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(typeFilter).build(mActivity);
            startActivityForResult(intent, Commans.PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }
*/
        /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Commans.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getParentActivity(), data);
                mPlace = place;
                AppLog.i(TAG, "onActivityResult: " + mPlace);
                AppLog.i(TAG, "Place: " + place.getName());
                AppLog.i(TAG, "onActivityResult: " + place.getLatLng().longitude + " lat: " + place.getLatLng().latitude);
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                mGoogleMap.moveCamera(center);
                mGoogleMap.animateCamera(zoom);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getParentActivity(), data);
                // TODO: Handle the error.
                AppLog.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }*/
        fun animateMarker(destination: Location, marker: Marker?) {
            if (marker != null) {
                val startPosition = marker.position
                val endPosition = LatLng(destination.latitude, destination.longitude)
                val startRotation = marker.rotation
                val latLngInterpolator: LatLngInterpolator = LatLngInterpolator.LinearFixed()
                val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                valueAnimator.duration = 1000 // duration 1 second
                valueAnimator.interpolator = LinearInterpolator()
                valueAnimator.addUpdateListener { animation ->
                    try {
                        val v = animation.animatedFraction
                        val newPosition =
                            latLngInterpolator.interpolate(v, startPosition, endPosition)
                        marker.position = newPosition
                        marker.rotation = computeRotation(v, startRotation, destination.bearing)
                        Log.d(TAG, "onAnimationUpdate: getBearing: " + destination.bearing)
                    } catch (ex: Exception) {
                        // I don't care atm..
                    }
                }
                valueAnimator.start()
            }
        }

        private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
            val normalizeEnd = end - start // rotate start to 0
            val normalizedEndAbs = (normalizeEnd + 360) % 360
            val direction: Float =
                if (normalizedEndAbs > 180) -1F else 1.toFloat() // -1 = anticlockwise, 1 = clockwise
            val rotation: Float
            rotation = if (direction > 0) {
                normalizedEndAbs
            } else {
                normalizedEndAbs - 360
            }
            val result = fraction * rotation + start
            return (result + 360) % 360
        }
    }

    fun ApiCallSharedData() {
        mApiAcceptRideList = Singleton.restClient.getSharedRide(getCurrentCustomer()?.driver_id)
        mApiAcceptRideList!!.enqueue(object : Callback<SharedRideRequestData> {
            override fun onResponse(
                call: Call<SharedRideRequestData>,
                response: Response<SharedRideRequestData>
            ) {
                try {
                    val resp = response.body()
                    val gson = Gson()
                    val json = gson.toJson(resp)
                    val jObject = JSONObject(json)
                    Log.d(ContentValues.TAG, response.message())
                    if(jObject.getInt("status") == Common.STATUS_200) {
                        mSharedRideList = response.body()?.data

                        if(mSharedRideList==null || mSharedRideList!!.size==0){
                            saveCompleteDataFlow()// All shared ride completed if no customer remained
                        }else {
                            mSharedRideAdapter = mSharedRideList?.let {
                                SharedRideAdapter(
                                    activityContext!!,
                                    it as ArrayList<SharedRideRequestData.Data>,
                                    object : SharedRideAdapter.onCancelClick {
                                        override fun onClicked(rideData: SharedRideRequestData.Data) {
                                            setCurrentCustomer(rideData)

                                            if (rideData.status.equals("Pending")) {
                                                passCodeDialog()
                                            } else if (rideData.status.equals("Confirmed")) {
                                                apiStartRideWithCustomer()
                                            } else if (rideData.status.equals("Riding")) {
                                                apiCompleteRideWithCustomer()
                                            }
                                        }

                                        override fun onChatClick(rideId: String) {
                                            val intent = Intent(activity,ChatActivity::class.java)
                                            intent.putExtra("book_id",rideId)
                                            startActivity(intent)

                                        }
                                    })

                            }
                            mRvTripList?.adapter = mSharedRideAdapter
                        }

                    }

                    Log.d("Schedule",response.body().toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<SharedRideRequestData>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    private fun  saveCompleteDataFlow(){
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, false)
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, false)
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COMPLETE_TRIP, false)
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_ON_TRIP, true)
        mPreferenceUtils?.save(PreferenceUtils.PREF_KEY_IS_SHARED_RIDE_ON,false)

        if(customerRequestModel?.paymode.equals("Online")){
            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, true) //Don't collect cash for admin ride
            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_REVIEW_TRIP, false)
        }

        if(customerRequestModel?.is_admin_created!! == 1){
            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, true) //Don't collect cash for admin ride
            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_REVIEW_TRIP, false)
        }
        parentActivity!!.finish()
        animationIntent(requireActivity(), parentActivity!!.intent)
    }

    private var customerRideData: CustomerRequestModel? = null
    private fun getCurrentCustomer(): CustomerRequestModel? {
        if (customerRideData == null){
            return mPreferenceUtils!!.customerRideData
        }
        return  customerRideData
    }
    private fun setCurrentCustomer(rideData: SharedRideRequestData.Data) {

        if( this.customerRideData==null){
            this.customerRideData = getCurrentCustomer()
        }
        this.customerRideData?.driver_id =  rideData.driver_id
        this.customerRideData?.bookid =  rideData.id.toString()
        this.customerRideData?.customer_id =  rideData.customer_id.toString()
        this.customerRideData?.mobile =  rideData.cusers.mobile
        this.customerRideData?.paymode =  rideData.paymode
        this.customerRideData?.pickup =  rideData.pickup
        this.customerRideData?.paymode =  rideData.paymode
        this.customerRideData?.dlong =  rideData.dlong
        this.customerRideData?.dlat =  rideData.dlat
        this.customerRideData?.vt_id =  rideData.vt_id.toString()
        this.customerRideData?.departure =  rideData.departure
        this.customerRideData?.image =  rideData.cusers.image
        this.customerRideData?.km =  rideData.km
        this.customerRideData?.name =  rideData.cusers.name
        this.customerRideData?.plat =  rideData.plat
        this.customerRideData?.charge =  rideData.charge
        this.customerRideData?.plong =  rideData.plong
        this.customerRideData?.mobile =  rideData.cusers.mobile
        this.customerRideData?.estimate_time =  rideData.estimate_time
        this.customerRideData?.is_schedule =  rideData.is_schedule.toString()
        this.customerRideData?.is_sharing =  rideData.is_sharing
        mPreferenceUtils?.saveCustomerRideData(customerRideData)
        this.customerRideData?.charge?.let { Log.e("Charge", it) }
    }

    fun redirectToMap(){
       redirectToMapAppWithPath()
        /*val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345")
        )
        startActivity(intent)*/
    }

    //This api call is for Driver ride Status
  /*  private fun apiCallBookAdminRide(context: Context) {
        mApiAdminRide = Singleton.restClient.getRideStatus(
            customerRequestModel.book_id
        )
        mApiAdminRide!!.enqueue(object : Callback<Object?> {
            override fun onResponse(
                call: Call<Object?>,
                response: Response<Object?>
            ) {
                // val resp: SoketResponse? = response.body()
                dismissProgress()
                val resp1: Any? = response.body()

                val gson = Gson()
                val json = gson.toJson(resp1)
                val jObject = JSONObject(json)
                Log.d("Response", response.message())
                if(jObject.getInt("status") == Common.STATUS_200) {
                    val status = jObject
                } else {
                    dismissProgress()
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(context,jObjError.getString("message"))
                        } catch (e: java.lang.Exception) {
                            showToastMessage(context,e.message)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Object?>, t: Throwable) {
                AppLog.e(TAG, "onFailure: $t")
                showToastMessage(context,resources.getString(R.string.something_went_wrong))
                dismissProgress()
            }
        })
    }*/


}