package com.speedride.driver.modules.home.ui.fragment

import android.Manifest
import com.speedride.driver.base.BaseFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.speedride.driver.interfaces.GpsListener
import androidx.cardview.widget.CardView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.location.places.Place
import com.google.android.gms.common.api.GoogleApiClient
import android.os.Bundle
import com.speedride.driver.utils.PermissionUtils.OnPermissionGrantCallback
import com.bumptech.glide.Glide
import android.location.Geocoder
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import android.view.Gravity
import android.graphics.Typeface
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Looper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import android.content.IntentSender.SendIntentException
import com.google.android.gms.maps.CameraUpdateFactory
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.animation.LinearInterpolator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.location.Address
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.speedride.driver.BuildConfig
import com.speedride.driver.modules.chat.adminchat.AdminChatActivity
import com.speedride.driver.modules.earning.dataModel.TodayEarningResponse
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.ClassCastException
import java.lang.Exception
import java.util.*
import com.speedride.driver.R

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : BaseFragment(), View.OnClickListener, OnMapReadyCallback, GpsListener {
    private var mBtnRetry: Button? = null
    private var mTxtTotalTrips: TextView? = null
    private var mTxtSpendTime: TextView? = null
    private var txtTotalEarning: TextView? = null
    private var mTxtUserName: TextView? = null
    private var mTxtTripTime: TextView? = null
    private var mTxtEarning: TextView? = null
    private var mTxtPaymentMethod: TextView? = null
    private var mImgUserIcon: ImageView? = null
    private var mIngVehicle: ImageView? = null
    private var mllLastTrip: CardView? = null
    private var switcher: ViewSwitcher? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mFabCurrentLocation: FloatingActionButton? = null
    private var fabChat: FloatingActionButton? = null
    private var mPermission: PermissionUtils? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null
    private val mDropOffLocation: Location? = null
    private var mGoogleMap: GoogleMap? = null
    private val mPlace: Place? = null
    private var googleApiClient: GoogleApiClient? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var marker: Marker? = null
    private var markerCount = 0
    private var mApiGetEarningList: Call<TodayEarningResponse>? = null
    private var commonDialogUtils: CommonDialogUtils? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_main

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }
        markerCount = 0
       // permission()

      //loadFirstTimeMap()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser && view!=null){
            permission()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        permission()
    }

    fun loadFirstTimeMap(){
        mPermission = PermissionUtils(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), object : OnPermissionGrantCallback {
            override fun onPermissionGranted() {
                if (ValidationUtils.isInternetAvailable(activityContext!!)) mapLoad() else checkInternet()
            }

            override fun onPermissionError(permission: String?) {
                Toast.makeText(activity, "Need to must Allow permission", Toast.LENGTH_LONG).show()
                // activity!!.finish()
            }
        })
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun initView(view: View?) {
        mPreferenceUtils = PreferenceUtils.getInstance(activityContext!!)
        switcher = view?.findViewById(R.id.viewSwitcher)
        mBtnRetry = view?.findViewById(R.id.btnRetry)
        mBtnRetry!!.setOnClickListener(this)

        mFabCurrentLocation = view?.findViewById(R.id.fabLastTrip)
        mFabCurrentLocation!!.setOnClickListener(this)
        mllLastTrip = view?.findViewById(R.id.llLastTrip)
        mTxtTotalTrips = view?.findViewById(R.id.txtTodayTrips)
        mTxtSpendTime = view?.findViewById(R.id.txtOnlineTime)
        txtTotalEarning = view?.findViewById(R.id.txtTodayEarning)
        fabChat = view?.findViewById(R.id.fabChat)
        commonDialogUtils = CommonDialogUtils(activity)
        fabChat!!.setOnClickListener(this)

        //show todays all trips details
        apiCallTodayEarningList()
//        if (ValidationUtils.isValidString(
//                mPreferenceUtils!!.get(
//                    PreferenceUtils.PREF_KEY_TODAY_TOTAL_TRIPS,
//                    ""
//                )
//            )
//        ) mTxtTotalTrips!!.setText(
//            mPreferenceUtils!!.get(
//                PreferenceUtils.PREF_KEY_TODAY_TOTAL_TRIPS,
//                ""
//            ) + " Trips"
//        )
//        if (ValidationUtils.isValidString(
//                mPreferenceUtils!!.get(
//                    PreferenceUtils.PREF_KEY_TODAY_TOTAL_SPEND_TIME,
//                    ""
//                )
//            )
//        ) mTxtSpendTime!!.setText(
//            mPreferenceUtils!!.get(
//                PreferenceUtils.PREF_KEY_TODAY_TOTAL_SPEND_TIME,
//                ""
//            ) + " hours"
//        )
//        if (ValidationUtils.isValidString(
//                mPreferenceUtils!!.get(
//                    PreferenceUtils.PREF_KEY_TODAY_TOTAL_EARNINGS,
//                    ""
//                )
//            )
//        ) txtTotalEarning!!.text = "$."+
//                mPreferenceUtils!!.get(
//                    PreferenceUtils.PREF_KEY_TODAY_TOTAL_EARNINGS,
//                    ""
//                )
        mTxtUserName = view?.findViewById(R.id.txtUserName)
        mTxtTripTime = view?.findViewById(R.id.txtTripTime)
        mTxtEarning = view?.findViewById(R.id.txtEarning)
        mTxtPaymentMethod = view?.findViewById(R.id.txtPaymentMethod)
        mImgUserIcon = view?.findViewById(R.id.imgUserIcon)
        mIngVehicle = view?.findViewById(R.id.imgVehicle)
        Glide.with(this).load(Common.OTHER_IMAGE_URL + mPreferenceUtils!!.driverData?.vehicle_detail?.typevehicle?.v_image)
            .placeholder(R.drawable.ic_driver_profile).into(
                mIngVehicle!!
            )

        //show last trips details
        if (ValidationUtils.isValidObject(mPreferenceUtils!!.customerRideData) && ValidationUtils.isValidString(
                mPreferenceUtils!!.customerRideData?.bookid
            ) && !mPreferenceUtils!!.customerRideData?.isCanceled!!
        ) {
            val customerRequestModel = mPreferenceUtils!!.customerRideData
//            if (ValidationUtils.isValidString(customerRequestModel?.name)) mTxtUserName!!.setText(
//                customerRequestModel?.name
//            )
//            if (ValidationUtils.isValidString(customerRequestModel?.requestdatetime)) {
//                val convertedDate =
//                    Utils.stringDateToFormatDate(customerRequestModel?.requestdatetime)
//                val strDate = Utils.dateFormatToStringDateForEarning(convertedDate)
//                mTxtTripTime!!.setText(strDate)
//            }
            if (ValidationUtils.isValidString(customerRequestModel?.paymode)) mTxtPaymentMethod!!.setText(
                customerRequestModel?.paymode
            )
//            if (ValidationUtils.isValidString(customerRequestModel?.charge)) {
//                val formatDbl = java.lang.Double.valueOf( customerRequestModel?.charge)
//                val formatStr = String.format("%.2f", formatDbl)
////                mTxtEarning!!.setText("$formatStr Rs.")
//                mTxtEarning!!.setText("$$formatDbl")
//            }
            if (ValidationUtils.isValidString(customerRequestModel?.image)) Glide.with(this).load(
                Common.UPLOAD_URL + customerRequestModel?.image
            ).placeholder(R.drawable.ic_driver_profile).into(mImgUserIcon!!)
        }
        checkInternet()
       // permission()
    }
    fun permission(){
        commonDialogUtils!!.loctionPermissionDialog(activityContext!!,
            getString(R.string.location_enable_text),
            getString(R.string.location_enable),
            getString(R.string.cancel),
            object : CommonDialogUtils.DialogListener {
                override fun onButtonClick(selectedBtnTitle: String) {
                    if(selectedBtnTitle.equals(Common.ENABLE) || selectedBtnTitle.equals(Common.Done)){
                        loadFirstTimeMap()
                        }
                    }
            })
    }

    override fun onMapReady(googleMap: GoogleMap) {

        //add map style
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.map_json)
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
            val geocoder = Geocoder(requireActivity())
            try {
                val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                AppLog.d(TAG, "lat long of Address: $addressList")
                if (addressList != null && addressList.size > 0) {
                    val locality = addressList[0].getAddressLine(0)
                    val address = addressList[0].subLocality
                    val country = addressList[0].countryName
                    val state = addressList[0].adminArea
                    if (locality != null && country != null && !locality.isEmpty() && !country.isEmpty()) {
                        AppLog.i(TAG, "onCameraChange: Locality: $locality Country:$country")

                        /*mGoogleMap.clear();
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title("Your Location");
                                markerOptions.snippet(locality + ", " + state);
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_taxi_icon));
                                mGoogleMap.addMarker(markerOptions).showInfoWindow();
    
                                mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
    
                                    @Override
                                    public View getInfoWindow(Marker arg0) {
                                        return null;
                                    }
    
                                    @Override
                                    public View getInfoContents(Marker marker) {
    
                                        LinearLayout info = new LinearLayout(getActivityContext());
                                        info.setOrientation(LinearLayout.VERTICAL);
    
                                        TextView title = new TextView(getActivityContext());
                                        title.setTextColor(Color.BLACK);
                                        title.setGravity(Gravity.CENTER);
                                        title.setTypeface(null, Typeface.BOLD);
                                        title.setText(marker.getTitle());
    
                                        TextView snippet = new TextView(getActivityContext());
                                        snippet.setTextColor(Color.GRAY);
                                        snippet.setText(marker.getSnippet());
    
                                        info.addView(title);
                                        info.addView(snippet);
    
                                        return info;
                                    }
                                });*/
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

        //set markers custom infoWindow
        googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val info = LinearLayout(activityContext)
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(activityContext)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title
                val snippet = TextView(activityContext)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet
                info.addView(title)
                info.addView(snippet)
                return info
            }
        })
        mGoogleMap = googleMap
    }

    private fun checkInternet() {
        if (!ValidationUtils.isInternetAvailable(activityContext!!)) {
            switcher!!.showNext()
            Log.d("Off", "checkInternet: ")
        }
    }

    private fun checkPermission() {
        mPermission = PermissionUtils(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), object : OnPermissionGrantCallback {
            override fun onPermissionGranted() {
                if (ValidationUtils.isInternetAvailable(activityContext!!)) {
                    switcher!!.showPrevious()
                    currentLocation()
                } else checkInternet()
            }

            override fun onPermissionError(permission: String?) {
                showToastMessage(activity!!, "Need to must Allow permission")
              //  activity!!.finish()
            }
        })
    }

    //for get mLocation update continue
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.lastLocation != null) {
                mCurrentLocation = locationResult.lastLocation
                currentLocation()
                AppLog.d(TAG, "mLocationCallback Location:$mCurrentLocation")
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private fun mapLoad() {
        displayLocationSettingsRequest(activity)
        mFusedLocationClient = FusedLocationProviderClient(activityContext!!)
        //send request for location   add this method for get call back from time interval-->// .setInterval(2000).setFastestInterval(2000)
        mLocationRequest =
            LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000)
                .setFastestInterval(10000)
        if (ActivityCompat.checkSelfPermission(
                activityContext!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activityContext!!, Manifest.permission.ACCESS_COARSE_LOCATION
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
        if (fragmentManager != null) {
            mMapFragment = requireFragmentManager().findFragmentById(R.id.map) as SupportMapFragment?
        }
        if (mMapFragment != null) {
            mMapFragment!!.getMapAsync(this)
        }
    }

    //for show gps location dialog
    private fun displayLocationSettingsRequest(context: Context?) {
        googleApiClient = GoogleApiClient.Builder(requireContext())
            .addApi(LocationServices.API).build()
        googleApiClient!!.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        if (activity != null) {
            val result =
                LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(builder.build())
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
                    activityContext!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    activityContext!!, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mFusedLocationClient!!.lastLocation.addOnSuccessListener(parentActivity!!) { location ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    mCurrentLocation = location
                    currentLocation()
                    AppLog.d(TAG, "onGpsChanged")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermission?.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    private fun selectedLocation(latLng: LatLng) {
        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f), 1000, null)
    }

    //current location
    private fun currentLocation() {
        if (ActivityCompat.checkSelfPermission(
                activityContext!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            if (mCurrentLocation != null) {

                /*mFusedLocationClient.getLastLocation().addOnSuccessListener(getParentActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mDropOffLocation = location;
                        }
                    }
                });*/
                if (markerCount == 1) {
                    animateMarker(mCurrentLocation!!, marker)
                    mGoogleMap!!.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                mCurrentLocation!!.latitude, mCurrentLocation!!.longitude
                            ), 17f
                        ), 1500, null
                    )
                } else {
                    mGoogleMap!!.clear()
                    val latLng = LatLng(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
                    marker = mGoogleMap!!.addMarker(
                        MarkerOptions().position(latLng).title(
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
                                    mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_ID, "")
                                )
                            )
                        )
                    )

                    /* MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                getCompleteAddressString(markerOptions, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_taxi_icon));
                mGoogleMap.addMarker(markerOptions).showInfoWindow();
                */

                    //Set Marker Count to 1 after first time marker is created
                    markerCount = 1
                    mGoogleMap!!.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                mCurrentLocation!!.latitude, mCurrentLocation!!.longitude
                            ), 16f
                        ), 1000, null
                    )
                }
            }
        }
    }

    //return address of given location
    private fun getCompleteAddressString(
        string: String,
        LATITUDE: Double,
        LONGITUDE: Double
    ): String {
        var strTitle = ""
        var strSnipped = ""
        val geocoder = Geocoder(requireActivity(), Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            Log.e(TAG, "getCompleteAddressString: $addresses")
            if (addresses != null) {
                val full_address = addresses[0].getAddressLine(0)
                val houseNum = addresses[0].featureName
                val societyName = addresses[0].thoroughfare
                val areaName = addresses[0].subLocality
                val cityName = addresses[0].locality

                //strAdd = houseNum + " , " + societyName + " \n " + areaName + " , " + cityName;
                if (string == "title") {
                    if (areaName != null) {
                        strTitle = strTitle + houseNum
                        return strTitle
                    } else if (societyName != null) {
                        strTitle = "$strTitle, $societyName"
                        return strTitle
                    }
                } else {
                    if (full_address != null) strSnipped = strSnipped + areaName
                    if (cityName != null) strSnipped = "$strSnipped, $cityName"
                    return strSnipped
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("MyLocationAddress", "Can not get Address!")
        }
        return "Address not found."
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnRetry -> if (ValidationUtils.isInternetAvailable(activityContext!!)) {
                checkPermission()
            } else {
                showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
            }
            R.id.fabChat ->{
                val intent = Intent(activity, AdminChatActivity::class.java)
                startActivity(intent)
            }
            R.id.fabLastTrip ->
                /*displayLocationSettingsRequest(getActivity());
                currentLocation();*/if (mllLastTrip!!.visibility == View.VISIBLE) {
                mllLastTrip!!.visibility = View.GONE
            } else {
                mllLastTrip!!.visibility = View.VISIBLE
          }

        }
    }

    override fun onResume() {
        Log.d("OnR", "onResume: ")
        checkInternet()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        /*if (mPickupLatLong != null) {
            mPickupLatLong = null;
            isPickUpAddress = false;

        }
        if (mDropOffLatLong != null) {
            mDropOffLatLong = null;
            isDropOffAddress = false;
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val fragment: Fragment?
        if (isDetached) if (fragmentManager != null) {
            fragment = requireFragmentManager().findFragmentById(R.id.map)
            if (fragment != null && activity != null) {
                val ft = requireActivity().supportFragmentManager.beginTransaction()
                ft.remove(fragment)
                ft.commit()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLog.d(TAG, "Destroy")
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
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

    private fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng) {
        val PI = 3.14159
        val lat1 = latLng1.latitude * PI / 180
        val long1 = latLng1.longitude * PI / 180
        val lat2 = latLng2.latitude * PI / 180
        val long2 = latLng2.longitude * PI / 180
        val dLon = long2 - long1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon))
        var brng = Math.atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360
    }

    //move vehicle instant
    fun moveVehicle(myMarker: Marker, latLng: LatLng) {
        val startPosition = myMarker.position
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = 3000f
        val hideMarker = false
        handler.post(object : Runnable {
            var elapsed: Long = 0
            var t = 0f
            var v = 0f
            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)
                val currentPosition = LatLng(
                    startPosition.latitude * (1 - t) + latLng.latitude * t,
                    startPosition.longitude * (1 - t) + latLng.longitude * t
                )
                myMarker.position = currentPosition
                // myMarker.setRotation(finalPosition.getBearing());


                // Repeat till progress is completeelse
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                    // handler.postDelayed(this, 100);
                } else {
                    if (hideMarker) {
                        myMarker.isVisible = false
                    } else {
                        myMarker.isVisible = true
                    }
                }
            }
        })
    }

    companion object {
        private val TAG = MainFragment::class.java.simpleName

        //marker move smoothly
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

    private fun apiCallTodayEarningList(){
        mApiGetEarningList = Singleton.restClient.todayEarningList()
        mApiGetEarningList!!.enqueue(object : Callback<TodayEarningResponse> {
            override fun onResponse(
                call: Call<TodayEarningResponse>,
                response: Response<TodayEarningResponse>
            ) {

                //Showing today trips data
                try {
                    val resp1: TodayEarningResponse? = response.body()
                    if (resp1 != null && resp1.status == 200){
                        if(resp1.data.today_trip!=null){
                            mTxtTotalTrips!!.setText(resp1.data.today_trip.total_count.toString() +"Trips.")
                            mTxtSpendTime!!.setText(resp1.data.today_trip.total_times
                                    + " hours"
                            )
                            txtTotalEarning!!.text = "$"+ resp1.data.today_trip.all_cost
                        }

                        //Showing last trip data
                        if(resp1.data.last_trip!=null && resp1.data.last_trip.size>0){
                            mTxtUserName?.setText(
                                resp1.data.last_trip[0].cusers.name
                            )
                            mTxtTripTime!!.setText(resp1.data.last_trip[0].estimate_time)
                            mTxtEarning!!.setText("$${resp1.data.last_trip[0].charge}")
                        }

                    }else{
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

            override fun onFailure(call: Call<TodayEarningResponse>, t: Throwable) {
             t.printStackTrace()
            }

        })
    }
}