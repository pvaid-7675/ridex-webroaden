package com.speedride.driver.modules.home.ui.activity


import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.speedride.driver.BuildConfig
import com.speedride.driver.R
import com.speedride.driver.app.AppController
import com.speedride.driver.base.BaseActivity
import com.speedride.driver.broadcast_receiver.ReceiverClass
import com.speedride.driver.interfaces.GpsListener
import com.speedride.driver.model.Data
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.modules.home.ui.adapter.ViewPagerAdapter
import com.speedride.driver.modules.home.ui.fragment.MainFragment
import com.speedride.driver.modules.userManagement.ui.fragment.AccountFragment
import com.speedride.driver.modules.userManagement.ui.fragment.EarningsFragment
import com.speedride.driver.modules.userManagement.ui.fragment.RatingsFragment
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {
    private var mTxtOnline: TextView? = null
    private var mSwitch: SwitchCompat? = null
    private var mTabLayout: TabLayout? = null
    private var mViewPager: ViewPager? = null
    private val mCurrentFragment: Fragment? = null
    private var mApiOnlineOfflineCall: Call<ServerResponse<Data>>? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mStrDriverId: String? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mPermission: PermissionUtils? = null
    private var commonDialogUtils: CommonDialogUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppController.activityResumed(this)
        initView(null)

       /* mPermission = PermissionUtils(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), object : PermissionUtils.OnPermissionGrantCallback {
            override fun onPermissionGranted() {
                if (ValidationUtils.isInternetAvailable(activity!!)){
                    mSwitch!!.isChecked = true
                }
            }

            override fun onPermissionError(permission: String?) {
                Toast.makeText(activity, "Need to must Allow permission", Toast.LENGTH_LONG).show()
                mSwitch!!.isChecked = false
                // activity!!.finish()
            }
        })*/
    }
/*
    fun permission(){
        commonDialogUtils!!.loctionPermissionDialog(this,
            getString(R.string.location_enable_text),
            getString(R.string.location_enable),
            getString(R.string.cancel),
            object : CommonDialogUtils.DialogListener {
                override fun onButtonClick(selectedBtnTitle: String) {
                    if (selectedBtnTitle == getString(R.string.location_enable)) {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        )
                    }

                }
            })
    }
*/


    override val activity: AppCompatActivity
        get() = this@MainActivity
    override val actionTitle: String?
        get() = null
    override val isHomeButtonEnable: Boolean
        get() = false

    override fun setHomeButtonIcon(): Int {
        return 0
    }

    override fun initView(view: View?) {
        init()
    }

    //for set title
    fun setTitle(title: String?) {
        setToolbarTitle(title)
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun init() {
        setToolbar((findViewById<View>(R.id.rlToolbar) as RelativeLayout))
        commonDialogUtils = CommonDialogUtils(activity)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))
        mPreferenceUtils = PreferenceUtils.getInstance(this@MainActivity)

        //fcm token
        Log.e("fcm token", mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_FCM_TOKEN, ""))
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        Log.e(TAG, "init: $mStrDriverId")
        mTxtOnline = findViewById(R.id.txtOnline)
        mSwitch = findViewById(R.id.switchOnline)
       // permission()
        checkDriverStatus()
        mSwitch!!.setOnCheckedChangeListener(this)
        mViewPager = findViewById(R.id.viewpager)
        mTabLayout = findViewById(R.id.tabs_layout)

        //set viewpager adapters all fragment to viewpager
        setupViewPager(mViewPager)
        mTabLayout!!.setupWithViewPager(mViewPager)
        //set text text color
        mTabLayout!!.setTabTextColors(
            resources.getColor(R.color.colorText),
            resources.getColor(R.color.colorAccent)
        )
        //set tab icon and textStyle
        setupTabIcons()

        mTabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val view = tab.customView
                if (view != null) {
                    val textView = view.findViewById<TextView>(R.id.tab)
                    textView.setTextColor(resources.getColor(R.color.colorAccent))
                    //set action bar title
                    setTitle(tab)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val view = tab.customView
                if (view != null) {
                    val textView = view.findViewById<TextView>(R.id.tab)
                    textView.setTextColor(resources.getColor(R.color.colorText))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                val view = tab.customView
                if (view != null) {
                    val textView = view.findViewById<TextView>(R.id.tab)
                    textView.setTextColor(resources.getColor(R.color.colorAccent))
                }
            }
        })
    }

    private fun setTitle(tab: TabLayout.Tab) {
        if (tab.position == 0) {
            setTitle(resources.getString(R.string.home))
        } else if (tab.position == 1) {
            setTitle(resources.getString(R.string.earnings))
        } else if (tab.position == 2) {
            setTitle(resources.getString(R.string.ratings))
        } else if (tab.position == 3) {
            setTitle(resources.getString(R.string.account))
        }
    }

    private fun setupTabIcons() {

        /* LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.box_middle, null);
*/
        val tabOne = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        tabOne.text = resources.getString(R.string.home)
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home_tab_selecter, 0, 0)
        tabOne.setTextColor(resources.getColor(R.color.colorAccent))
        setTitle(resources.getString(R.string.home))
        mTabLayout!!.getTabAt(0)!!.customView = tabOne
        val tabTwo = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        tabTwo.text = resources.getString(R.string.earnings)
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.earnings_tab_selecter, 0, 0)
        mTabLayout!!.getTabAt(1)!!.customView = tabTwo
        val tabThree = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        tabThree.text = resources.getString(R.string.ratings)
        tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ratings_tab_selecter, 0, 0)
        mTabLayout!!.getTabAt(2)!!.customView = tabThree
        val tabFour = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        tabFour.text = resources.getString(R.string.account)
        tabFour.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.account_tab_selecter, 0, 0)
        mTabLayout!!.getTabAt(3)!!.customView = tabFour
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(MainFragment(), "Home")
        adapter.addFragment(EarningsFragment(), "Earnings")
        adapter.addFragment(RatingsFragment(), "Ratings")
        adapter.addFragment(AccountFragment(), "Account")
        viewPager!!.adapter = adapter
    }
    private fun refreshMainFragment(){
        try {
            var mainFragment = ((mViewPager?.adapter as ViewPagerAdapter).getItem(0) as MainFragment)
            mainFragment?.loadFirstTimeMap()
            mainFragment?.onGPSChanged(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun checkDriverStatus() {
        if (ValidationUtils.isInternetAvailable(this)) {
            if (mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_STATUS, false) != null) {
                Log.d(TAG, "checkDriverStatus: status not null")
                if (mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_STATUS, false)) {
                    Log.d(TAG, "checkDriverStatus: status on")
                    mSwitch!!.isChecked = true
                    mTxtOnline!!.text = resources.getString(R.string.online)
                } else {
                    Log.d(TAG, "checkDriverStatus: status off")
                    mSwitch!!.isChecked = false
                    mTxtOnline!!.text = resources.getString(R.string.offline)
                }
            } else {
                if (mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_STATUS, false)) {
                    showProgress()
                    apiOnlineOfflineCall(Common.DRIVER_ONLINE_ON)
                }
            }
        } else {
            mSwitch!!.isChecked = false
            mTxtOnline!!.text = resources.getString(R.string.offline)
            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_DRIVER_STATUS, false)
            showToastMessage(activity, activity.resources.getString(R.string.network_error))
        }
    }

    //call api for driver status online/offline
    private fun apiOnlineOfflineCall(status: String) {
        hideKeyboard(this)
        if (ValidationUtils.isValidString(mStrDriverId) && ValidationUtils.isValidString(status)) {
            mApiOnlineOfflineCall = Singleton.restClient.driverStatus( /*mStrDriverId,*/status)
            mApiOnlineOfflineCall!!.enqueue(object : Callback<ServerResponse<Data>> {
                override fun onResponse(
                    call: Call<ServerResponse<Data>>,
                    response: Response<ServerResponse<Data>>
                ) {
                    mSwitch!!.isEnabled = true
                    dismissProgress()
                    val resp: ServerResponse<*>? = response.body()
                    //AppLog.d(TAG, new Gson().toJson(response));
                    if (resp != null && resp.status == 200) {
                        if (ValidationUtils.isValidString(response.body()!!.data?.on_duty)) {
                            if (response.body()!!.data?.on_duty == Common.DRIVER_ONLINE_ON) {
                                refreshMainFragment()
                                mSwitch!!.isChecked = true
                                mTxtOnline!!.text = resources.getString(R.string.online)
                                mPreferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_DRIVER_STATUS,
                                    true
                                )
                                //connect socket and send current location with start service
                                if (!mPreferenceUtils!!.get(
                                        PreferenceUtils.PREF_KEY_SOCKET,
                                        false
                                    )
                                ) {
                                    val intent =
                                        Intent(this@MainActivity, ReceiverClass::class.java)
                                    intent.action = Common.CONNECT_SOCKET
                                    sendBroadcast(intent)
                                }
                                Log.d(
                                    TAG,
                                    "onResponse: Api Call in Driver Online MainActivity Socket Connect"
                                )
                            } else {
                                mSwitch!!.isChecked = false
                                mTxtOnline!!.text = resources.getString(R.string.offline)
                                mPreferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_DRIVER_STATUS,
                                    false
                                )
                                //disconnect socket with start service
                                if (mPreferenceUtils!!.get(
                                        PreferenceUtils.PREF_KEY_SOCKET,
                                        false
                                    )
                                ) {
                                    val intent =
                                        Intent(this@MainActivity, ReceiverClass::class.java)
                                    intent.action = Common.DISCONNECT_SOCKET
                                    sendBroadcast(intent)
                                }
                            }
                        }else{
                            mSwitch!!.isChecked = false
                        }
                    } else {
                        dismissProgress()
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(this@MainActivity, jObjError.getString("message"))
                            } catch (e: Exception) {
                                showToastMessage(this@MainActivity, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                    mSwitch!!.isEnabled = true
                    dismissProgress()
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                    AppLog.d(TAG, "onFailure:---- $t")
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Common.REQUEST_GPS_CHECK_SETTINGS) {
            if (mCurrentFragment is MainFragment) {
                if (resultCode == RESULT_OK) (mCurrentFragment as GpsListener).onGPSChanged(true) else (mCurrentFragment as GpsListener).onGPSChanged(
                    false
                )
            }
        }
    }


    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.id == R.id.switchOnline) {
          if (mSwitch!!.isChecked) {
            commonDialogUtils!!.loctionPermissionDialog(activity,
                getString(R.string.location_enable_text),
                getString(R.string.location_enable),
                getString(R.string.cancel),
                object : CommonDialogUtils.DialogListener {
                    override fun onButtonClick(selectedBtnTitle: String) {
                        if (selectedBtnTitle == getString(R.string.location_enable)) {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            )
                            //  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(intent, 101)
                            goOffline()
                        } else if (selectedBtnTitle == "Done") {
                            mPermission = PermissionUtils(
                                activity,
                                arrayOf<String>(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ),
                                object : PermissionUtils.OnPermissionGrantCallback {
                                    override fun onPermissionGranted() {
                                        if (ValidationUtils.isInternetAvailable(activity)) {
                                            if (isChecked) {
                                                goOnline()
                                            } else {
                                                goOffline()
                                            }
                                        } else {
                                            showToastMessage(
                                                activity,
                                                activity.resources.getString(R.string.network_error)
                                            )
                                            mSwitch!!.isChecked = false
                                        }
                                    }

                                    override fun onPermissionError(permission: String?) {
                                        goOffline()
                                    }
                                })
                        } else {
                            goOffline()
                        }
                    }
                })
        }else{
                goOffline()
        }

//            if (ValidationUtils.isInternetAvailable(this)) {
//
//                if (isChecked) {
//                    mSwitch!!.isEnabled = false
//                    showProgress()
//                    apiOnlineOfflineCall(Common.DRIVER_ONLINE_ON)
//                } else {
//                    mSwitch!!.isEnabled = false
//                    showProgress()
//                    apiOnlineOfflineCall(Common.DRIVER_ONLINE_OFF)
//                }
//            } else {
//                showToastMessage(activity, activity.resources.getString(R.string.network_error))
//                mSwitch!!.isChecked = false
//            }
        }
    }

    private fun goOffline(){
        mSwitch!!.isChecked = false
        mSwitch!!.isEnabled = false
        showProgress()
        apiOnlineOfflineCall(Common.DRIVER_ONLINE_OFF)
    }
    private fun goOnline(){
        showProgress()
        apiOnlineOfflineCall(Common.DRIVER_ONLINE_ON)
    }

 /*   fun checkpermissionGrantornot(){
        commonDialogUtils!!.loctionPermissionDialog(
           activity,
            getString(R.string.location_enable_text),
            getString(R.string.location_enable),
            getString(R.string.cancel),
            object : CommonDialogUtils.DialogListener() {
                override fun onButtonClick(selectedBtnTitle: String) {
                    if (selectedBtnTitle == getString(R.string.location_enable)) {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        )
                        //  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, 101)
                    } else if (selectedBtnTitle == "Done") {
                        mPermission = PermissionUtils(
                            activity,
                            arrayOf<String>(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ),
                            object : PermissionUtils.OnPermissionGrantCallback() {
                                override fun onPermissionGranted() {
                                    if (ValidationUtils.isInternetAvailable(this)) {

                                        if (isChecked) {
                                            mSwitch!!.isEnabled = false
                                            showProgress()
                                            apiOnlineOfflineCall(Common.DRIVER_ONLINE_ON)
                                        } else {
                                            mSwitch!!.isEnabled = false
                                            showProgress()
                                            apiOnlineOfflineCall(Common.DRIVER_ONLINE_OFF)
                                        }
                                    } else {
                                        showToastMessage(activity, activity.resources.getString(R.string.network_error))
                                        mSwitch!!.isChecked = false
                                    }
                                }
                                override fun onPermissionError(permission: String?) {

                                }
                            })
                    } else {
                    }
                }
            })
    }*/
    private fun onCancelApiCall() {
        if (mApiOnlineOfflineCall != null && !mApiOnlineOfflineCall!!.isCanceled) {
            mApiOnlineOfflineCall!!.cancel()
        }
    }

    fun showProgress() {
        if (mProgressDialog != null) mProgressDialog!!.show()
    }

    fun dismissProgress() {
        if (mProgressDialog != null) mProgressDialog!!.dismiss()
    }

    override fun onResume() {
        super.onResume()
        AppController.activityResumed(activity)
    }

    override fun onPause() {
        super.onPause()
        AppController.activityPaused()
        onCancelApiCall()
        dismissProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ")
        onCancelApiCall()
        dismissProgress()
        if (this.window.currentFocus != null) this.window.currentFocus!!.clearFocus()
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount == 1 || fragmentManager.backStackEntryCount == 0) {
            exitDialog()
        }
    }

    private fun exitDialog() {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle("Exit")
        alertDialog.setMessage("Are you sure you want Exit?")
        alertDialog.setPositiveButton("Yes") { dialog, which -> finish() }
            .setNegativeButton("No") { dialog, which -> }.create().show()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}