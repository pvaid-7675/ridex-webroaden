package com.speedride.driver.modules.userManagement.ui.fragment

import android.content.Context

import com.speedride.driver.base.BaseFragment
import android.widget.ProgressBar
import android.widget.TextView
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.R
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.engine.GlideException
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.google.android.gms.tasks.OnCompleteListener
import com.speedride.driver.modules.home.ui.activity.HomeActivity
import org.json.JSONObject
import com.speedride.driver.broadcast_receiver.ReceiverClass
import com.speedride.driver.modules.ride.ui.activity.TripHistoryActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.speedride.driver.model.Data
import com.speedride.driver.modules.chat.adminchat.AdminChatActivity
import com.speedride.driver.modules.home.ui.activity.HelpActivity
import com.speedride.driver.modules.home.ui.fragment.AboutFragment
import com.speedride.driver.modules.userManagement.ui.activity.*
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class AccountFragment : BaseFragment(), View.OnClickListener {
    private var mProgressBar: ProgressBar? = null
    private var mImgProfilePhoto: ImageView? = null
    private var mImgVehicle: ImageView? = null
    private var mTxtEditVehicleDetails: TextView? = null
    private var mTxtDriverName: TextView? = null
    private var mTxtEditProfile: TextView? = null
    private var mTxtWayBill: TextView? = null
    private var mTxtDocument: LinearLayout? = null
    private var mTxtHistory: LinearLayout? = null
    private var mTxtSetting: LinearLayout? = null
    private var mTxtAbout: LinearLayout? = null
    private var mTxtHelp: LinearLayout? = null
    private var mTxtLogout: LinearLayout? = null
    private var txtShiftTime: LinearLayout? = null
    private var txtChat: LinearLayout? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mApiLogoutCall: Call<ServerResponse<Object>>? = null
    private var mApiOnlineOfflineCall: Call<ServerResponse<Data>>? = null
    private var mStrDriverId: String? = null
    private var mCallFragment: Fragment? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_account

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun initView(view: View?) {
        if (view != null) {
            init(view)
        }
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun init(view: View) {
        mPreferenceUtils = activity?.let { PreferenceUtils.getInstance(it) }
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mImgVehicle = view.findViewById(R.id.imgVehicle)
        mImgProfilePhoto = view.findViewById(R.id.imgProfilePhoto)
        mProgressBar = view.findViewById(R.id.progress_bar)
        mTxtEditVehicleDetails = view.findViewById(R.id.txtEditVehicleDetails)
        mTxtDriverName = view.findViewById(R.id.txtDriverName)
        mTxtEditProfile = view.findViewById(R.id.txtEditProfile)
        mTxtWayBill = view.findViewById(R.id.txtWayBill)
        mTxtDocument = view.findViewById(R.id.txtDocument)
        mTxtHistory = view.findViewById(R.id.txtHistory)
        mTxtSetting = view.findViewById(R.id.txtSetting)
        mTxtAbout = view.findViewById(R.id.txtAbout)
        mTxtHelp = view.findViewById(R.id.txtHelp)
        mTxtLogout = view.findViewById(R.id.txtLogout)
        txtShiftTime = view.findViewById(R.id.txtShiftTime)
        txtChat = view.findViewById(R.id.txtChat)
        mTxtEditVehicleDetails!!.setOnClickListener(this)
        mTxtEditProfile!!.setOnClickListener(this)
        mTxtWayBill!!.setOnClickListener(this)
        mTxtDocument!!.setOnClickListener(this)
        mTxtHistory!!.setOnClickListener(this)
        mTxtSetting!!.setOnClickListener(this)
        mTxtAbout!!.setOnClickListener(this)
        mTxtHelp!!.setOnClickListener(this)
        mTxtLogout!!.setOnClickListener(this)
        txtShiftTime!!.setOnClickListener(this)
        txtChat!!.setOnClickListener(this)
        Log.e(TAG, "init: ")
        Glide.with(this).load(Common.OTHER_IMAGE_URL + mPreferenceUtils!!.driverData?.vehicle_detail?.typevehicle?.v_image)
            .into(
                mImgVehicle!!
            )
        userDetailPref
    }

    private val userDetailPref: Unit
        private get() {


      /*      val path = mPreferenceUtils!!.driverData?.image?.replace("'\'", "")
            mImgProfilePhoto?.let {
                Glide.with(this) .load(Common.UPLOAD_URL + path)
                    .placeholder(R.drawable.ic_driver_profile).into(it)
            };

            Log.e("ImageCheck", " userDetailPref: Image: " + mPreferenceUtils!!.driverData?.image)
            Log.e("ImageCheck", " userDetailPref: Common Image: " + Common.UPLOAD_URL + path)
*/
            // history_new_item.xml.xml image url : https://via.placeholder.com/300.png

            if (ValidationUtils.isValidString(mPreferenceUtils!!.driverData?.image)) {
                Log.e("ImageCheck", " isValidString:#: Image: " + mPreferenceUtils!!.driverData?.image)

                if (mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
                    if (mPreferenceUtils!!.driverData?.image?.contains("users") == true) {
                        Log.e("ImageCheck", " users:#: Image: " + mPreferenceUtils!!.driverData?.image)
                        val path = mPreferenceUtils!!.driverData?.image?.replace("'\'", "")
                        Glide.with(this)
                            .load(Common.UPLOAD_URL + path)
                            .placeholder(R.drawable.ic_driver_profile)
                            .listener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any,
                                    target: Target<Drawable?>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    mProgressBar!!.visibility = View.GONE
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any,
                                    target: Target<Drawable?>,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    mProgressBar!!.visibility = View.GONE
                                    return false
                                }
                            })
                            .into(mImgProfilePhoto!!)
                    } else {
                        val path = mPreferenceUtils!!.driverData?.image?.replace("'\'", "")
                        Glide.with(this)
                            .load(path)
                            .placeholder(R.drawable.ic_driver_profile)
                            .listener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any,
                                    target: Target<Drawable?>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    mProgressBar!!.visibility = View.GONE
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any,
                                    target: Target<Drawable?>,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    mProgressBar!!.visibility = View.GONE
                                    return false
                                }
                            })
                            .into(mImgProfilePhoto!!)
                    }
                } else {
                    val path = mPreferenceUtils!!.driverData?.image?.replace("'\'", "")
                    Log.d(TAG, "getUserDetailPref: Path: $path")

                    Glide.with(this)
                        .load(Common.UPLOAD_URL + path)
                        .placeholder(R.drawable.ic_driver_profile)
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                mProgressBar!!.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any,
                                target: Target<Drawable?>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                mProgressBar!!.visibility = View.GONE
                                return false
                            }
                        })
                        .into(mImgProfilePhoto!!)
                }
            }
            if (ValidationUtils.isValidString(mPreferenceUtils!!.driverData?.name)) mTxtDriverName!!.text =
                mPreferenceUtils!!.driverData?.name
            if (ValidationUtils.isValidString(
                    mPreferenceUtils!!.get(
                        PreferenceUtils.PREF_KEY_VEHICLE_ID,
                        ""
                    )
                )
            ) {
//                mImgVehicle!!.setImageResource(
//                    Utils.checkVehicleImagesSelected(
//                        mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_ID, "")
//                    )
                //)
            }
        }

    /**
     * api call for Device app Detail
     */
    private fun apiCallLogout() {
        if (activity?.let { ValidationUtils.isInternetAvailable(it) } == true) {
            mApiLogoutCall = Singleton.restClient.logOut()
            mApiLogoutCall!!.enqueue(object : Callback<ServerResponse<Object>?> {
                override fun onResponse(
                    call: Call<ServerResponse<Object>?>,
                    response: Response<ServerResponse<Object>?>
                ) {

                    try {
                        val resp = response.body()
                        val gson = Gson()
                        val json = gson.toJson(resp)
                        val jObject = JSONObject(json)
                        if(jObject.getInt("status") == Common.STATUS_200) {
                            showToastMessage(activity!!, resp?.message)
                            mPreferenceUtils!!.clear(activity!!)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_BEARER_TOKEN, "")
                            val intentHome = Intent(activity, HomeActivity::class.java)
                            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            animationIntent(activity!!, intentHome)

                            /*if (mPreferenceUtils.get(PreferenceUtils.PREF_KEY_DRIVER_STATUS, false)) {
                                apiOnlineOfflineCall(Common.DRIVER_ONLINE_OFF);
                            } else {
                                showToastMessage(getActivity(), resp.getMsg());
                                mPreferenceUtils.clear(getActivity());
                                Intent intentHome = new Intent(getActivity(), HomeActivity.class);
                                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                animationIntent(getActivity(), intentHome);
                            }*/
                        } else {
                            dismissProgress()
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
                    AppLog.d(TAG, "onResponse:---- $t")
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                    dismissProgress()
                }
            })
        } else {
            showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
            dismissProgress()
        }
    }

    //call api for driver status online/offline
    private fun apiOnlineOfflineCall(status: String) {
        if (ValidationUtils.isValidString(mStrDriverId) && ValidationUtils.isValidString(status)) {
            mApiOnlineOfflineCall = Singleton.restClient.driverStatus( /*mStrDriverId,*/status)
            mApiOnlineOfflineCall!!.enqueue(object : Callback<ServerResponse<Data>> {
                override fun onResponse(
                    call: Call<ServerResponse<Data>>,
                    response: Response<ServerResponse<Data>>
                ) {
                    dismissProgress()
                    val resp: ServerResponse<*>? = response.body()
                    //AppLog.d(TAG, new Gson().toJson(response));
                    if (resp != null && resp.status == 200) {
                        showToastMessage(activity!!, "Logout Successfully")
                        if (ValidationUtils.isValidString(response.body()!!.data?.on_duty)) {
                            if (response.body()!!.data?.on_duty == Common.DRIVER_ONLINE_ON) {
                                mPreferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_DRIVER_STATUS,
                                    false
                                )
                                //connect socket and send current location with start service
                                val intent = Intent(activityContext, ReceiverClass::class.java)
                                intent.action = Common.DISCONNECT_SOCKET
                                activityContext!!.sendBroadcast(intent)
                                /*mPreferenceUtils.clear(getActivity());
                                Intent intentHome = new Intent(getActivity(), HomeActivity.class);
                                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                animationIntent(getActivity(), intentHome);*/
                            } else {
                                mPreferenceUtils!!.save(
                                    PreferenceUtils.PREF_KEY_DRIVER_STATUS,
                                    false
                                )
                                //disconnect socket with start service
                                val intent = Intent(activityContext, ReceiverClass::class.java)
                                intent.action = Common.DISCONNECT_SOCKET
                                activityContext!!.sendBroadcast(intent)
                                /*mPreferenceUtils.clear(getActivity());
                                Intent intentHome = new Intent(getActivity(), HomeActivity.class);
                                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                animationIntent(getActivity(), intentHome);*/
                            }
                        }
                        apiCallLogout()
                    } else {
                        dismissProgress()
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(context!!, jObjError.getString("message"))
                            } catch (e: Exception) {
                                showToastMessage(context!!, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                    dismissProgress()
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                }
            })
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.txtEditVehicleDetails -> {
                val intent = Intent(activity, SelectVehicleTypeActivity::class.java)
                animationIntent(requireActivity(), intent)
            }
            R.id.txtEditProfile -> {
                val intentEdit = Intent(activity, EditProfileActivity::class.java)
                animationIntent(requireActivity(), intentEdit)
            }
            R.id.txtWayBill -> {}
            R.id.txtDocument -> {
                val intentDocument = Intent(activity, DocumentDisplayActivity::class.java)
                animationIntent(requireActivity(), intentDocument)
            }
            R.id.txtHistory -> {
                val intentHistory = Intent(activity, TripHistoryActivity::class.java)
                animationIntent(requireActivity(), intentHistory)
            }
            R.id.txtSetting -> {
                val intentSetting = Intent(activity, SettingsActivity::class.java)
                animationIntent(requireActivity(), intentSetting)
            }
            R.id.txtAbout -> {
                val intentSetting = Intent(activity, AboutFragment::class.java)
                animationIntent(requireActivity(), intentSetting)
            }
            R.id.txtHelp -> {
                val intentSetting = Intent(activity, HelpActivity::class.java)
                animationIntent(requireActivity(), intentSetting)
            }
            R.id.txtShiftTime ->{
                val intentSetting = Intent(activity, ShiftTimeActivity::class.java)
                animationIntent(requireActivity(), intentSetting)
            }
            R.id.txtChat->{
                val intent = Intent(activity, AdminChatActivity::class.java)
                startActivity(intent)
            }
            R.id.txtLogout -> if (activity?.let { ValidationUtils.isInternetAvailable(it) } == true) {
                if (!requireActivity().isFinishing) showProgress()
                if (ValidationUtils.isValidString(
                        mPreferenceUtils!!.get(
                            PreferenceUtils.PREF_KEY_FCM_TOKEN,
                            ""
                        )
                    )
                ) {
                    apiOnlineOfflineCall(Common.DRIVER_ONLINE_OFF)

                    //apiCallLogout();
                } else {
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                return@OnCompleteListener
                            }
                            // Get new FCM registration token
                            val token = task.result
                            apiCallLogout()
                        })
                }
            } else {
                showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
                dismissProgress()
            }
        }
    }

    private fun onCancelApiCall() {
        if (mApiLogoutCall != null && !mApiLogoutCall!!.isCanceled) {
            mApiLogoutCall!!.cancel()
        }
        if (mApiOnlineOfflineCall != null && !mApiOnlineOfflineCall!!.isCanceled) {
            mApiOnlineOfflineCall!!.cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: ")
        userDetailPref
    }

    override fun onPause() {
        super.onPause()
        dismissProgress()
        onCancelApiCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancelApiCall()
        dismissProgress()
    }

    companion object {
        private val TAG = AccountFragment::class.java.simpleName
    }
}