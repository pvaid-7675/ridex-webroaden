package com.speedride.driver.app

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.utils.PreferenceUtils
import android.os.Bundle
import android.view.WindowManager
import com.speedride.driver.R
import com.google.firebase.FirebaseApp
import android.content.Intent
import com.speedride.driver.modules.home.ui.activity.MainActivity
import com.speedride.driver.modules.home.ui.activity.HomeActivity
import android.app.ActivityOptions
import android.os.Handler
import android.util.Log
import android.view.Window
import android.widget.Toast
import com.speedride.driver.databinding.ActivitySplashBinding
import com.speedride.driver.model.RideStatusResponse
import com.speedride.driver.modules.auth.ui.activity.VerifyMobileActivity
import com.speedride.driver.modules.ride.dataModel.CustomerRequestModel
import com.speedride.driver.modules.ride.ui.activity.OnRideActivity
import com.speedride.driver.modules.userManagement.ui.activity.AddVehicleDetailsActivity
import com.speedride.driver.modules.userManagement.ui.activity.DocumentActivity
import com.speedride.driver.modules.userManagement.ui.activity.SelectVehicleTypeActivity
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.PreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_TIME_OUT : Long = 3000
    }
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    private val preferenceHelper by lazy {
        PreferenceHelper.init(this)
    }


    private lateinit var preferenceUtils : PreferenceUtils
    private var mApiGetRideStatus: Call<RideStatusResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        preferenceUtils = PreferenceUtils.getInstance(this)!!

        if (preferenceUtils[PreferenceUtils.PREF_KEY_LOGIN, false]) {
            apiCallGetRideStatus()
        }else{
            splashScreen()
        }


    }

    /**
     * Splash Screen Start
     */
    private fun splashScreen() {
        Handler(mainLooper).postDelayed({ init() }, SPLASH_TIME_OUT)
    }

    private fun init() {
        if (preferenceUtils.get<Any>(PreferenceUtils.PREF_KEY_BEARER_TOKEN) == null) {
            preferenceUtils.save(PreferenceUtils.PREF_KEY_BEARER_TOKEN, "")
        }
        Log.e("AccessToken", " Bearer: " + preferenceUtils.get<Any>(PreferenceUtils.PREF_KEY_BEARER_TOKEN))
        if (preferenceUtils[PreferenceUtils.PREF_KEY_RIDE_ON, false]) {
            val intent = Intent(this, OnRideActivity::class.java)
            animationIntent(intent)
            finish()
        } else if (preferenceUtils[PreferenceUtils.PREF_KEY_LOGIN, false]) {
            val intent = Intent(this, MainActivity::class.java)
            animationIntent(intent)
            finish()
        } else if (preferenceUtils[PreferenceUtils.PREF_KEY_DOCUMENT_UPLOAD_SUCCESSFULLY, false]
        ) {
            val intent = Intent(this, MainActivity::class.java)
            animationIntent(intent)
            finish()
        } else if (preferenceUtils[PreferenceUtils.PREF_KEY_VEHICLE_DETAILS, false]) {
            val intent = Intent(this, DocumentActivity::class.java)
            animationIntent(intent)
            finish()
        } else if (preferenceUtils[PreferenceUtils.PREF_KEY_VEHICLE_TYPE, false]) {
            val intent = Intent(this, AddVehicleDetailsActivity::class.java)
            animationIntent(intent)
            finish()
        } else if (preferenceUtils[PreferenceUtils.PREF_KEY_VERIFY_OTP, false]) {
            val intent = Intent(this, SelectVehicleTypeActivity::class.java)
            animationIntent(intent)
            finish()
        } else if (preferenceUtils[PreferenceUtils.PREF_KEY_SIGN_UP, false]) {
            val intent = Intent(this, VerifyMobileActivity::class.java)
            animationIntent(intent)
            finish()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            animationIntent(intent)
            finish()
        }
    }

    fun animationIntent(intent: Intent?) {
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.enter, R.anim.exit)
        startActivity(intent, options.toBundle())
    }

    /**
    findViewById<Button>(R.id.successBtn).setOnClickListener {
    SnackHelper.success(findViewById(android.R.id.content), "Success message !", SnackHelper.LENGTH_LONG).show()
    }
    findViewById<Button>(R.id.infoBtn).setOnClickListener {
    SnackHelper.info(findViewById(android.R.id.content), "Info message !", SnackHelper.LENGTH_LONG).show()
    }
    findViewById<Button>(R.id.warningBtn).setOnClickListener {
    SnackHelper.warning(findViewById(android.R.id.content), "Warning message !", SnackHelper.LENGTH_LONG).show()
    }
    binding.errorBtn.setOnClickListener {
    SnackHelper.error(it, "Error message !", SnackHelper.LENGTH_SHORT).show()
    ToastHelper.error(this, "Error message !", ToastHelper.LENGTH_SHORT).show()
    }

     */

    private fun apiCallGetRideStatus() {

        mApiGetRideStatus = Singleton.restClient.getTripStatus(preferenceUtils?.driverData?.id)

        mApiGetRideStatus?.enqueue(object : Callback<RideStatusResponse> {
            override fun onResponse(
                call: Call<RideStatusResponse>,
                response: Response<RideStatusResponse>
            ) {
                try {
                    val response1 = response.body()
                    Log.e("Ride Response",response1.toString())
                    //Response<ServerResponse>
                    if (response1?.status == Common.STATUS_200) {
                        // val status = response1?.data?.status
                        if(response1.data!=null && response1.data.status!=null) {
                            if (response1?.data?.status!!.equals(Common.RIDE_RIDING)) {
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, true)
                                preferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM,
                                    true
                                )
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, true)
                                saveCustomer(response1.data)
                                //preferenceUtils[PreferenceUtils.PREF_KEY_RIDE_ON, false]
                            } else if (response1?.data?.status!!.equals(Common.RIDE_COMPLETE)) {
                                preferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM,
                                    false
                                )
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, false)
                                preferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_COMPLETE_TRIP,
                                    false
                                )
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_ON_TRIP, true)
                                preferenceUtils?.save(
                                    PreferenceUtils.PREF_KEY_IS_SHARED_RIDE_ON,
                                    false
                                )

                            } else if (response1?.data?.status!!.equals(Common.RIDE_PENDING)) {
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, true)
                                preferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM,
                                    false
                                )
                            } else if (response1?.data?.status.equals(Common.RIDE_CONFIRM)) {
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, true)
                                preferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM,
                                    true
                                )
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, false)
                                saveCustomer(response1.data)
                            } else {
                                preferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM,
                                    false
                                )
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, false)
                                preferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_COMPLETE_TRIP,
                                    false
                                )
                                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_ON_TRIP, true)
                                preferenceUtils?.save(
                                    PreferenceUtils.PREF_KEY_IS_SHARED_RIDE_ON,
                                    false
                                )
                            }
                        }
                    }
                    init()
                } catch (e: Exception) {
                    e.printStackTrace()
                    init()
                }
            }

            override fun onFailure(call: Call<RideStatusResponse>, t: Throwable) {
                Toast.makeText(
                    this@SplashActivity,
                    resources.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
                init()
            }
        })
    }
    public fun saveCustomer(rideStatusResponse: RideStatusResponse.Data){
        var customer: CustomerRequestModel = CustomerRequestModel()
        customer.customer_id = rideStatusResponse.customer_id.toString()
        customer.pickup =  rideStatusResponse.pickup
        customer.bookid = rideStatusResponse.id.toString()
        customer.paymode =  rideStatusResponse.paymode
        customer.dlong =  rideStatusResponse.dlong
        customer.dlat =  rideStatusResponse.dlat
        customer.vt_id = rideStatusResponse.vt_id.toString()
        customer.departure =  rideStatusResponse.departure
        customer.image =  rideStatusResponse.cusers.image
        customer.km =  rideStatusResponse.km
       customer.name =  rideStatusResponse.cusers.name
        customer.plat =  rideStatusResponse.plat
        customer.charge =  rideStatusResponse.charge
       // customer.requestdatetime =  rideStatusResponse.requestdatetime
        customer.esttime = rideStatusResponse.esttime.toString()
        customer.estprice =  rideStatusResponse.estprice
        customer.driver_id =  rideStatusResponse.driver_id
        customer.plong =  rideStatusResponse.plong
       // customer.mobile =  rideStatusResponse.mobile
        customer.estimate_time =  rideStatusResponse.estimate_time
        customer.is_schedule = rideStatusResponse.is_schedule.toString()
        customer.book_id = rideStatusResponse.id.toString()
        customer.is_admin_created =  rideStatusResponse.is_admin_created
        customer.is_sharing =  rideStatusResponse.is_sharing
        preferenceUtils.saveCustomerRideData(customer)
        customer.charge?.let { Log.e("Charge splash", it) }
    }


}