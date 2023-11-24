package com.speedride.customer.modules.login.view.activity

import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.speedride.customer.R
import com.speedride.customer.base.BaseActivity
import com.speedride.customer.model.ServerResponse
import com.speedride.customer.modules.utils.AppLog
import com.speedride.customer.modules.utils.Common
import com.speedride.customer.modules.utils.Singleton.Companion.restClient
import com.speedride.customer.modules.utils.ValidationUtils
import kotlinx.android.synthetic.main.activity_forgot_password.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : BaseActivity(), View.OnClickListener {

    /*private var mEdtEmailOrMobile: AppCompatEditText? = null
    private var mTxtResetPassword: AppCompatTextView? = null*/

    private var mApiForgotPassResponseCall: Call<com.speedride.customer.model.ServerResponse<Object>>? = null
    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        initView(null)
    }

    /*override fun getActionTitle(): String {
        return getString(R.string.forget_password_small)
    }

    override fun isHomeButtonEnable(): Boolean {
        return true
    }*/

    override val actionTitle: String?
        get() = getString(R.string.forget_password_small)

    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back_white
    }

    override fun initView(view: View?) {
        setToolbar(findViewById(R.id.appbar))
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))

        /*mTxtResetPassword = findViewById(R.id.txtResetPassword)
        mEdtEmailOrMobile = findViewById(R.id.edtEmailOrMobile)*/

        txtResetPassword?.setOnClickListener(this)
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
        if (view.id == R.id.txtResetPassword) {
            if (ValidationUtils.isInternetAvailable(this)) {
                if (isValidate) {
                    showProgress()
                    apiCallForgotPassword()
                }
            } else {
                showToastMessage(resources.getString(R.string.network_error))
            }
        }
    }

    private fun apiCallForgotPassword() {
        hideKeyboard(this@ForgotPasswordActivity)
        if (ValidationUtils.isInternetAvailable(this)) {
            mApiForgotPassResponseCall = restClient.forgotPassword(
                edtEmailOrMobile!!.text.toString()
            )
            mApiForgotPassResponseCall?.enqueue(object : Callback<com.speedride.customer.model.ServerResponse<Object>> {
                override fun onResponse(
                    call: Call<com.speedride.customer.model.ServerResponse<Object>>,
                    response: Response<com.speedride.customer.model.ServerResponse<Object>>
                ) {
                    try {
                        hideProgress()
                        val resp1: Any? = response.body()
                        val gson = Gson()
                        val json = gson.toJson(resp1)
                        val jObject = JSONObject(json)
                        Log.d("Response", response.message())
                        if(jObject.getInt("status") == Common.STATUS_200) {
                            showToastMessage(response.body()!!.message)
                            /*String message = response.body().getMessage();
                            if (message.equalsIgnoreCase("We have e-mailed your password reset link!")) {
                                finish();
                                showToastMessage(response.body().getMessage());
                            }*/
                            val homeIntent =
                                Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                            homeIntent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            // Check if we're running on Android 5.0 or higher
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                // Apply activity transition
                                val options = ActivityOptions.makeCustomAnimation(
                                    this@ForgotPasswordActivity,
                                    R.anim.enter,
                                    R.anim.exit
                                )
                                startActivity(homeIntent, options.toBundle())
                            } else {
                                // Swap without transition
                                startActivity(homeIntent)
                            }
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<com.speedride.customer.model.ServerResponse<Object>>, t: Throwable) {
                    AppLog.Companion.d(TAG, "onResponse:+++++$t")
                    hideProgress()
                    showToastMessage(resources.getString(R.string.something_went_wrong))
                }
            })
        } else {
            hideProgress()
            showToastMessage(resources.getString(R.string.network_error))
        }
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
        if (mApiForgotPassResponseCall != null && !mApiForgotPassResponseCall!!.isCanceled) {
            mApiForgotPassResponseCall!!.cancel()
        }
    }

    /**
     * @return Validation
     */
    private val isValidate: Boolean
        private get() {
            val isValid: Boolean
            if (!ValidationUtils.isEmailValid(edtEmailOrMobile!!.text.toString()) && !ValidationUtils.isValidMobile(
                    edtEmailOrMobile
                )
            ) {
                edtEmailOrMobile!!.error = getString(R.string.enter_email_or_mobile_number)
                edtEmailOrMobile!!.requestFocus()
                isValid = false
            } else {
                isValid = true
            }
            return isValid
        }

    companion object {
        private val TAG = ForgotPasswordActivity::class.java.simpleName
    }
}