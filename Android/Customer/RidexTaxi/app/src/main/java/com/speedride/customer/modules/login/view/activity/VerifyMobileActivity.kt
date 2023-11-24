package com.speedride.customer.modules.login.view.activity

import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.speedride.customer.R
import com.mukesh.OnOtpCompletionListener
import com.speedride.customer.base.BaseActivity
import com.speedride.customer.modules.login.model.Data
import com.speedride.customer.modules.main.view.activity.MainActivity
import com.speedride.customer.modules.utils.AppLog
import com.speedride.customer.modules.utils.Common
import com.speedride.customer.modules.utils.PreferenceUtils
import com.speedride.customer.modules.utils.Singleton.Companion.restClient
import com.speedride.customer.modules.utils.ValidationUtils
import kotlinx.android.synthetic.main.activity_verify_mobile.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyMobileActivity : BaseActivity(), View.OnClickListener, OnOtpCompletionListener {

    /*private var mTxtSubmit: AppCompatTextView? = null
    private var mTxtResendOtp: AppCompatTextView? = null
    private var otpView: OtpView? = null*/


    private var mPreference: PreferenceUtils? = null
    private var mApiOtpResponseCall: Call<com.speedride.customer.model.ServerResponse<Data?>>? = null
    private var mApiOtpResendOnEmailCall: Call<com.speedride.customer.model.ServerResponse<Object>>? = null
    private var mProgressDialog: ProgressDialog? = null
    private var userId: String? = null
    private var userEmail: String? = null
    private var isValidatePassCode = false
    private var strPassCode = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_mobile)
        initView(null)
    }

    /*override fun getActionTitle(): String {
        return getString(R.string.verify_mobile)
    }

    override fun isHomeButtonEnable(): Boolean {
        return true
    }*/

    override val actionTitle: String?
        get() = getString(R.string.verify_mobile)

    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back_white
    }

    override fun initView(view: View?) {
        setToolbar(findViewById(R.id.appbar))
        mPreference = PreferenceUtils.getInstance(this@VerifyMobileActivity)
        userId = mPreference?.get(PreferenceUtils.PREF_KEY_USER_ID, "")
        userEmail = mPreference?.get(PreferenceUtils.PREF_KEY_USER_EMAIL, "")
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(getString(R.string.please_wait_msg))
        mProgressDialog!!.hide()

        /*mTxtResendOtp = findViewById(R.id.txtResendOtp)
        mTxtSubmit = findViewById(R.id.txtSubmit)*/

        txtResendOtp?.setOnClickListener(this)
        txtSubmit?.setOnClickListener(this)


        //otpView = findViewById(R.id.txt_pin_entry_verify)

        txt_pin_entry_verify?.requestFocus()
        txt_pin_entry_verify?.setOtpCompletionListener(this)
        txt_pin_entry_verify?.setOtpCompletionListener(OnOtpCompletionListener { otp ->
            Log.d(TAG + " onOtpCompleted=>", otp)
            strPassCode = otp
        })

        txt_pin_entry_verify?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 6) {
                    isValidatePassCode = true
                    strPassCode = s.toString()
                    hideKeyboard(this@VerifyMobileActivity)
                } else {
                    isValidatePassCode = false
                }
            }

            override fun afterTextChanged(s: Editable) {
                strPassCode = s.toString()
            }
        })

        if (!mPreference?.get(
                PreferenceUtils.PREF_KEY_OTP_SEND,
                false
            )!!
        ) sendOtpIfNotVerifiedAccount()
    }

    //call when already register, but verify not done
    private fun sendOtpIfNotVerifiedAccount() {
        if (ValidationUtils.isInternetAvailable(this)) {
            if (ValidationUtils.isValidString(userEmail)) {
                showProgress()
                apiOtpResendOnEmail(userEmail)
            } else {
                showToastMessage("Email not found.")
            }
        } else {
            showToastMessage(resources.getString(R.string.network_error))
        }
    }

    override fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.txtSubmit -> if (ValidationUtils.isInternetAvailable(this)) {
                if (isValidate) {
                    showProgress()
                    apiVerifyOtp()
                }
            } else {
                showToastMessage(resources.getString(R.string.network_error))
                return
            }
            R.id.txtResendOtp -> if (ValidationUtils.isInternetAvailable(this)) {
                if (ValidationUtils.isValidString(userEmail)) {
                    showProgress()
                    apiOtpResendOnEmail(userEmail)
                } else {
                    showToastMessage("Email not found.")
                }
            } else {
                showToastMessage(resources.getString(R.string.network_error))
                return
            }
        }
    }

    //api call for verify otp
    private fun apiVerifyOtp() {
        hideKeyboard(this@VerifyMobileActivity)
        mApiOtpResponseCall = restClient.verifyOtp(userId, strPassCode /*,Common.ROLE_CUSTOMER*/)
        mApiOtpResponseCall?.enqueue(object : Callback<com.speedride.customer.model.ServerResponse<Data?>> {
            override fun onResponse(
                call: Call<com.speedride.customer.model.ServerResponse<Data?>>,
                response: Response<com.speedride.customer.model.ServerResponse<Data?>>
            ) {
                val resp: com.speedride.customer.model.ServerResponse<*>? = response.body()
                hideProgress()
                if (resp != null && resp.checkResponse_(response) == Common.STATUS_200) {
                    AppLog.Companion.d(
                        TAG, "onResponse:VerifyOtp:  " + response.body()!!
                            .message
                    )
                    mPreference!!.save(PreferenceUtils.PREF_KEY_VERIFY_OTP, true)
                    mPreference!!.saveUserInfo(response.body()!!.data)
                    mPreference!!.save(
                        PreferenceUtils.PREF_KEY_BEARER_TOKEN, "Bearer " + response.body()!!
                            .accessToken
                    )
                    val mainIntent = Intent(this@VerifyMobileActivity, MainActivity::class.java)
                    mainIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    // Check if we're running on Android 5.0 or higher
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Apply activity transition
                        val options = ActivityOptions.makeCustomAnimation(
                            this@VerifyMobileActivity,
                            R.anim.enter,
                            R.anim.exit
                        )
                        startActivity(mainIntent, options.toBundle())
                    } else {
                        // Swap without transition
                        startActivity(mainIntent)
                    }
                    startActivity(mainIntent)
                    finish()
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(
                                response.errorBody()!!.string()
                            )
                            showToastMessage(jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage(e.message!!)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<com.speedride.customer.model.ServerResponse<Data?>>, t: Throwable) {
                AppLog.Companion.d(TAG, "onFailure: $t")
                hideProgress()
            }
        })
    }

    /**
     * api call for Forgot Password
     */
    private fun apiOtpResendOnEmail(email: String?) {
        hideKeyboard(this)

        mApiOtpResendOnEmailCall = restClient.resendOtp(userId)
        mApiOtpResendOnEmailCall?.enqueue(object : Callback<com.speedride.customer.model.ServerResponse<Object>> {
            override fun onResponse(
                call: Call<com.speedride.customer.model.ServerResponse<Object>>,
                response: Response<com.speedride.customer.model.ServerResponse<Object>>
            ) {
                hideProgress()
                try {
                    val resp = response.body()
                    val resp1: Any? = response.body()
                    val gson = Gson()
                    val json = gson.toJson(resp1)
                    val jObject = JSONObject(json)
                    if ( jObject.getInt("status")== Common.STATUS_200) {
                        showToastMessage(response.body()!!.message)
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(
                                    response.errorBody()!!.string()
                                )
                                showToastMessage(jObjError.getString("message"))
                            } catch (e: Exception) {
                                showToastMessage(e.message!!)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<com.speedride.customer.model.ServerResponse<Object>>, t: Throwable) {
                hideProgress()
                showToastMessage(resources.getString(R.string.something_went_wrong))
            }
        })
    }

    override fun onPause() {
        super.onPause()
        hideProgress()
        onCancelApiCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancelApiCall()
        dismissProgress()
    }

    fun showProgress() {
        if (mProgressDialog != null && !mProgressDialog!!.isShowing) mProgressDialog!!.show()
    }

    fun hideProgress() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) mProgressDialog!!.hide()
    }

    fun dismissProgress() {
        if (mProgressDialog != null) mProgressDialog!!.dismiss()
    }

    private fun onCancelApiCall() {
        if (mApiOtpResponseCall != null && !mApiOtpResponseCall!!.isCanceled) {
            mApiOtpResponseCall!!.cancel()
        }
        if (mApiOtpResendOnEmailCall != null && !mApiOtpResendOnEmailCall!!.isCanceled) {
            mApiOtpResendOnEmailCall!!.cancel()
        }
    }

    /**
     * @return Validation
     */
    private val isValidate: Boolean
        private get() = if (!ValidationUtils.isValidString(strPassCode)) {
            showToastMessage(getString(R.string.enter_otp))
            false
        } else if (!isValidatePassCode) {
            showToastMessage(getString(R.string.enter_valid_opt))
            false
        } else {
            true
        }

    override fun onOtpCompleted(s: String) {
        strPassCode = s
    }

    companion object {
        private val TAG = VerifyMobileActivity::class.java.simpleName
    }
}