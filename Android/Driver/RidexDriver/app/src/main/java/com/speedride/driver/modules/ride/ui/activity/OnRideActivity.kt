package com.speedride.driver.modules.ride.ui.activity


import com.speedride.driver.base.BaseActivity
import com.speedride.driver.utils.PreferenceUtils
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import android.widget.RelativeLayout
import com.speedride.driver.modules.ride.ui.fragment.OnTripFragment
import com.speedride.driver.modules.userManagement.ui.fragment.CollectCashFragment
import android.content.Intent
import com.speedride.driver.interfaces.GpsListener
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.speedride.driver.app.AppController
import com.speedride.driver.databinding.ActivityOnTripBinding
import com.speedride.driver.modules.home.ui.activity.MainActivity
import com.speedride.driver.modules.ride.ui.fragment.ShareRideFragment
import com.speedride.driver.utils.Common

class OnRideActivity : BaseActivity() {
    private val mCurrentFragment: Fragment? = null

    private val binding by lazy {
       ActivityOnTripBinding.inflate(layoutInflater)
    }
    private lateinit var mCtx : Context
    private lateinit var preferenceUtils :PreferenceUtils



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mCtx = this
        preferenceUtils = PreferenceUtils.getInstance(this)!!
        initView(null)
    }

    //for set title
    fun setTitle(title: String?) {
        setToolbarTitle(title)
    }

    override val activity: AppCompatActivity
        get() = this@OnRideActivity
    override val actionTitle: String?
        get() = null
    override val isHomeButtonEnable: Boolean
        get() = false

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun initView(view: View?) {
        setToolbar((findViewById<View>(R.id.rlToolbar) as RelativeLayout))
        preferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, true)
        checkTripStatus()
    }

    override fun showToastMessage(context: Context?, message: String?) {}
    private fun checkTripStatus() {
        if (!preferenceUtils!!.get(PreferenceUtils.PREF_KEY_ON_TRIP, false) //Trip on
            && !preferenceUtils!!.get(PreferenceUtils.PREF_KEY_COLLECT_CASH, false) //Collect cash is ON
            && preferenceUtils!!.get(PreferenceUtils.PREF_KEY_IS_SHARED_RIDE_ON, false)) /*Shared tide is on*/ {
            //Shared + collect cash + On trip
            pushFragment(CollectCashFragment())
        } else if (!preferenceUtils!!.get(PreferenceUtils.PREF_KEY_ON_TRIP, false)) { //Trip on
            pushFragment(ShareRideFragment())
        } else if (!preferenceUtils!!.get(PreferenceUtils.PREF_KEY_COLLECT_CASH, false)) {//Collect cash is ON
            //Go to collect cash
            pushFragment(CollectCashFragment())
        }else if (!preferenceUtils!!.get(PreferenceUtils.PREF_KEY_REVIEW_TRIP, false)) { //No need to review trip
           // pushFragment(ReviewTripFragment())
            preferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, false)
            preferenceUtils!!.save(PreferenceUtils.PREF_KEY_ON_TRIP, false)
            preferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, false)
            preferenceUtils!!.save(PreferenceUtils.PREF_KEY_REVIEW_TRIP, false)

            if(preferenceUtils!!.customerRideData?.is_admin_created!!.equals(1)) {
                preferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, true)
            }
            val intentHome = Intent(activity, MainActivity::class.java)
            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(activity!!, intentHome)
        }

    }

    private fun pushFragment(fragment: Fragment?) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_contain, fragment!!)
            .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Common.REQUEST_GPS_CHECK_SETTINGS) {
            if (mCurrentFragment is OnTripFragment) {
                (mCurrentFragment as GpsListener).onGPSChanged(resultCode == RESULT_OK)
            }
        }
    }

    companion object {
        private val TAG = OnRideActivity::class.java.simpleName
    }
    override fun onResume() {
        super.onResume()
        AppController.activityResumed(activity)
    }
}