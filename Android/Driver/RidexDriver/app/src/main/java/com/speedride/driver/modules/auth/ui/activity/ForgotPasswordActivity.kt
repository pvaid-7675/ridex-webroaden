package com.speedride.driver.modules.auth.ui.activity


import android.app.ActivityOptions
import com.speedride.driver.model.ServerResponse
import android.content.Context
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.speedride.driver.utils.ValidationUtils
import android.content.Intent
import android.view.View
import com.speedride.driver.databinding.ActivityForgotPasswordBinding
import com.speedride.driver.networkHelper.retrofit.RetrofitBuilder
import org.json.JSONObject
import com.speedride.driver.utils.NetworkUtil
import com.speedride.driver.utils.ProgressDialogHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityForgotPasswordBinding.inflate(layoutInflater)
    }
    private lateinit var mCtx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mCtx = this
        initView()
    }


    fun initView() {
        binding.txtResetPassword.setOnClickListener(this)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.txtResetPassword -> {
                if (NetworkUtil.hasInternetConnection(mCtx)) {
                    if (isValidate) {
                        apiCallForgotPassword()
                    }
                } else {
                    Toast.makeText(
                        mCtx,
                        mCtx.resources.getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }



    /**
     * @return Validation
     */
    private val isValidate: Boolean
        get() = if (!ValidationUtils.isValidEmail(binding.edtEmailOrMobile) && !ValidationUtils.isValidMobile(
                binding.edtEmailOrMobile
            )
        ) {
            binding.edtEmailOrMobile.error = getString(R.string.enter_email_or_mobile_number)
            binding.edtEmailOrMobile.requestFocus()
            false
        } else {
            true
        }

    /**
     * api call for Forgot Password
     */
    private fun apiCallForgotPassword() {
        ProgressDialogHelper.showProgressDialog(mCtx)
        val retrofit = RetrofitBuilder.buildService(mCtx)
        val call = retrofit.forgotPassword(binding.edtEmailOrMobile.text.toString())
        call.enqueue(object : Callback<ServerResponse<Any>> {
            override fun onResponse(
                call: Call<ServerResponse<Any>>,
                response: Response<ServerResponse<Any>>
            ) {
                ProgressDialogHelper.dismissProgressDialog()
                val resp = response.body()
                if (resp != null && resp.status == 200) {
                    val homeIntent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                    homeIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    val options =
                        ActivityOptions.makeCustomAnimation(mCtx, R.anim.enter, R.anim.exit)
                    startActivity(homeIntent, options.toBundle())
                    finish()
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            Toast.makeText(mCtx, jObjError.getString("message"), Toast.LENGTH_SHORT)
                                .show()
                        } catch (e: Exception) {
                            Toast.makeText(mCtx, e.message!!, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ServerResponse<Any>>, t: Throwable) {
                ProgressDialogHelper.dismissProgressDialog()
            }
        })
    }
}