package com.speedride.driver.modules.auth.ui.activity


import com.speedride.driver.base.BaseActivity
import com.mukesh.OnOtpCompletionListener
import android.widget.TextView
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.model.ServerResponse
import android.app.ProgressDialog
import android.content.Context
import com.mukesh.OtpView
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import android.text.TextWatcher
import android.text.Editable
import android.widget.Toast
import com.speedride.driver.utils.ValidationUtils
import com.speedride.driver.utils.AppLog
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.speedride.driver.modules.userManagement.ui.activity.SelectVehicleTypeActivity
import com.speedride.driver.rest.Singleton
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class VerifyMobileActivity : BaseActivity(), View.OnClickListener, OnOtpCompletionListener {
    private var mTxtSubmit: TextView? = null
    private var mTxtResendOtp: TextView? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mApiOtpResponseCall: Call<ServerResponse<Object>>? = null
    private var mApiOtpResendOnEmailCall: Call<ServerResponse<Object>>? = null
    private var mStrDriverId: String? = null
    private var mStrDriverEmail: String? = null
    private var mProgressDialog: ProgressDialog? = null
    private var isValidatePassCode = false
    private var strPassCode = ""
    private var otpView: OtpView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_mobile)
        initView(null)
    }

    override val activity: AppCompatActivity
        get() = this@VerifyMobileActivity
    override val actionTitle: String
        get() = getString(R.string.verify_mobile)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun initView(view: View?) {
        setToolbar(findViewById(R.id.rlToolbar))
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))
        mPreferenceUtils = PreferenceUtils.getInstance(this@VerifyMobileActivity)
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mStrDriverEmail = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_EMAIL, "")
        mTxtSubmit = findViewById(R.id.txtSubmit)
        mTxtSubmit!!.setOnClickListener(this)
        mTxtResendOtp = findViewById(R.id.txtResendOtp)
        mTxtResendOtp!!.setOnClickListener(this)
        otpView = findViewById(R.id.txt_pin_entry_verify)
        otpView!!.requestFocus()
        otpView!!.setOtpCompletionListener(this)
        otpView!!.setOtpCompletionListener(OnOtpCompletionListener { otp ->
            Log.d(TAG + " onOtpCompleted=>", otp)
            strPassCode = otp
        })
        otpView!!.addTextChangedListener(object : TextWatcher {
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
        if (!mPreferenceUtils!!.get(
                PreferenceUtils.PREF_KEY_OTP_SEND,
                false
            )
        ) sendOtpIfNotVerifiedAccount()
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    //call when already register, but verify not done
    private fun sendOtpIfNotVerifiedAccount() {
        if (ValidationUtils.isInternetAvailable(this)) {
            if (ValidationUtils.isValidString(mStrDriverEmail)) {
                showProgress()
                apiOtpResendOnEmail(mStrDriverEmail)
            } else {
                showToastMessage(activity, "Email not found.")
            }
        } else {
            showToastMessage(activity, activity.resources.getString(R.string.network_error))
        }
    }

    private fun apiVerifyOtp() {
        hideKeyboard(this)
        if (ValidationUtils.isValidString(mStrDriverId)) {
            mApiOtpResponseCall = Singleton.restClient.verifyOtp(mStrDriverId, strPassCode)
            mApiOtpResponseCall!!.enqueue(object : Callback<ServerResponse<Object>> {
                override fun onResponse(
                    call: Call<ServerResponse<Object>>,
                    response: Response<ServerResponse<Object>>
                ) {
                    val resp = response.body()
                    dismissProgress()
                    if (resp != null && resp.status == 200) {
                        AppLog.d(
                            TAG, "onResponse:VerifyOtp:  " + response.body()!!
                                .message
                        )
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_VERIFY_OTP, true)
                        showToastMessage(this@VerifyMobileActivity, resp.message!!)
                        //mPreferenceUtils.saveDriverData(response.body().getData());
                        //mPreferenceUtils.save(PreferenceUtils.PREF_KEY_BEARER_TOKEN, "Bearer " + response.body().getAccessToken());
                        val verifyIntent =
                            Intent(this@VerifyMobileActivity, SelectVehicleTypeActivity::class.java)
                        verifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        animationIntent(this@VerifyMobileActivity, verifyIntent)
                        finish()
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@VerifyMobileActivity,
                                    jObjError.getString("message")

                                )
                                showToastMessage(this@VerifyMobileActivity, jObjError.getString("message")!!)
                            } catch (e: Exception) {
                                showToastMessage(this@VerifyMobileActivity, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Object>>, t: Throwable) {
                    AppLog.d(TAG, "onFailure: $t")
                    dismissProgress()
                }
            })
        }
    }

    /**
     * api call for Forgot Password
     */
    private fun apiOtpResendOnEmail(email: String?) {
        hideKeyboard(this)
        if (ValidationUtils.isValidString(mStrDriverId)) {
            mApiOtpResendOnEmailCall = Singleton.restClient.resendOtp(mStrDriverId /*, email*/)
            mApiOtpResendOnEmailCall!!.enqueue(object : Callback<ServerResponse<Object>> {
                override fun onResponse(
                    call: Call<ServerResponse<Object>>,
                    response: Response<ServerResponse<Object>>
                ) {
                    dismissProgress()
                    val resp = response.body()
                    if (resp != null && resp.status == 200) {
                        showToastMessage(activity, response.body()!!.message)
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@VerifyMobileActivity,
                                    jObjError.getString("message")
                                )
                            } catch (e: Exception) {
                                showToastMessage(this@VerifyMobileActivity, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Object>>, t: Throwable) {
                    dismissProgress()
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                    Log.e(TAG, "onFailure: $t")
                }
            })
        }
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
                if (isValidateOtp) {
                    showProgress()
                    apiVerifyOtp()
                }
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error))
                return
            }
            R.id.txtResendOtp -> if (ValidationUtils.isInternetAvailable(this)) {
                if (ValidationUtils.isValidString(mStrDriverEmail)) {
                    showProgress()
                    apiOtpResendOnEmail(mStrDriverEmail)
                } else {
                    showToastMessage(activity, "Email not found.")
                }
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error))
                return
            }
        }
    }

    /**
     * @return Validation
     */
    private val isValidateOtp: Boolean
        private get() = if (!ValidationUtils.isValidString(strPassCode)) {
            showToastMessage(this, getString(R.string.enter_otp))
            false
        } else if (!isValidatePassCode) {
            showToastMessage(this, getString(R.string.enter_valid_opt))
            false
        } else {
            true
        }

    fun showProgress() {
        if (mProgressDialog != null) mProgressDialog!!.show()
    }

    fun dismissProgress() {
        if (mProgressDialog != null) mProgressDialog!!.dismiss()
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

    private fun onCancelApiCall() {
        if (mApiOtpResponseCall != null && !mApiOtpResponseCall!!.isCanceled) {
            mApiOtpResponseCall!!.cancel()
        }
        if (mApiOtpResendOnEmailCall != null && !mApiOtpResendOnEmailCall!!.isCanceled) {
            mApiOtpResendOnEmailCall!!.cancel()
        }
    }

    override fun onOtpCompleted(s: String) {
        strPassCode = s
    }

    companion object {
        private val TAG = VerifyMobileActivity::class.java.simpleName
    }
}