package com.speedride.driver.modules.auth.ui.activity


import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.speedride.driver.databinding.ActivityChangePasswordBinding
import com.speedride.driver.otherUtils.ToastHelper
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ChangePasswordActivity : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityChangePasswordBinding.inflate(layoutInflater)
    }
    private val preferenceUtils by lazy {
        PreferenceUtils.getInstance(this)
    }
    private lateinit var mCtx : Context
    private lateinit var driverId: String

    private var isCurrentPass = true
    private var isNewPass = true
    private var isConfPass = true
    private var mApiChangePassword: Call<Object>? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mCtx = this
        initView()
    }


    fun initView() {
        /**
         * get preferenceUtils data
         */
        preferenceUtils?.let {
           driverId =  it[PREF_KEY_DRIVER_ID,""]
        }
        /**
         * listener
         */
        binding.txtChangePassword.setOnClickListener(this)
        binding.imgCurrentPass.setOnClickListener(this)
        binding.imgNewPass.setOnClickListener(this)
        binding.imgConfNewPass.setOnClickListener(this)
    }


    private fun apiCallResetPassword() {
        mApiChangePassword = Singleton.restClient.changePassword(
            binding.edtCurrentPassword.text.toString(),
            binding.edtNewPassword.text.toString()
        )
        mApiChangePassword?.enqueue(object : Callback<Object> {
            override fun onResponse(
                call: Call<Object>?,
                response: Response<Object>?
            ) {
                ProgressDialogHelper.dismissProgressDialog()
                try {
                  //  ProgressDialogHelper.showProgressDialog(mCtx)
                    val resp = response?.body()
                    val gson = Gson()
                    val json = gson.toJson(resp)
                    val jObject = JSONObject(json)
                    Log.d(TAG, response!!.message())
                    if(jObject.getInt("status") == Common.STATUS_200) {
                     //   ToastHelper.error(mCtx, resp?.message!!, ToastHelper.LENGTH_SHORT).show()

                        Toast.makeText(mCtx,jObject.getString("message"),Toast.LENGTH_LONG).show()
                        preferenceUtils!!.save(
                            PreferenceUtils.PREF_KEY_BEARER_TOKEN, "Bearer " + jObject.getString("accessToken")
                        )
                        finish()
                    } else {
                        if (response.errorBody() != null) {
                            ProgressDialogHelper.dismissProgressDialog()
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                ToastHelper.error(mCtx, jObjError.getString("message"), ToastHelper.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                ToastHelper.error(mCtx, e.message.toString(), ToastHelper.LENGTH_SHORT).show()
                            }
                        }else if(response.body() != null) {
                            ToastHelper.error(mCtx,response.message(), ToastHelper.LENGTH_SHORT).show()
                        }else{
                            ToastHelper.error(mCtx,"Something go wrong",ToastHelper.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    if (response!!.code()==400){
                        Toast.makeText(mCtx,"Current Password Not Match.",Toast.LENGTH_LONG).show()
                    }else{
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Object>, t: Throwable) {
               ProgressDialogHelper.dismissProgressDialog()
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.txtChangePassword -> if (NetworkUtil.hasInternetConnection(mCtx)) {
                if (isValidate) {
                    if(binding.edtNewPassword!!.text.toString().equals(binding.edtConfirmPassword?.text.toString(),true)){
                        ProgressDialogHelper.showProgressDialog(mCtx)
                        apiCallResetPassword()
                    }else{
                        binding.edtConfirmPassword!!.error = getString(R.string.match_password)
                    }

                }
            } else {
                ToastHelper.error(this, resources.getString(R.string.network_error), ToastHelper.LENGTH_SHORT).show()
            }
            R.id.imgCurrentPass -> if (isCurrentPass) {
                binding.edtCurrentPassword.transformationMethod = null
                binding.imgCurrentPass.setImageResource(R.drawable.ic_pass_visible)
                isCurrentPass = false
            } else {
                binding.edtCurrentPassword.transformationMethod = PasswordTransformationMethod()
                binding.imgCurrentPass.setImageResource(R.drawable.ic_pass_hide)
                isCurrentPass = true
            }
            R.id.imgNewPass -> if (isNewPass) {
                binding.edtNewPassword.transformationMethod = null
                binding.imgNewPass.setImageResource(R.drawable.ic_pass_visible)
                isNewPass = false
            } else {
                binding.edtNewPassword.transformationMethod = PasswordTransformationMethod()
                binding.imgNewPass.setImageResource(R.drawable.ic_pass_hide)
                isNewPass = true
            }
            R.id.imgConfNewPass -> if (isConfPass) {
                binding.edtConfirmPassword.transformationMethod = null
                binding.imgConfNewPass.setImageResource(R.drawable.ic_pass_visible)
                isConfPass = false
            } else {
                binding.edtConfirmPassword.transformationMethod = PasswordTransformationMethod()
                binding.imgConfNewPass.setImageResource(R.drawable.ic_pass_hide)
                isConfPass = true
            }
        }
    }

    private val isValidate: Boolean
         get() = if (!ValidationUtils.isValidString(binding.edtCurrentPassword)) {
             binding.edtCurrentPassword.error = getString(R.string.enter_password)
             binding.edtCurrentPassword.requestFocus()
            false
        } else if (binding.edtCurrentPassword.text.toString().length < 6) {
             binding.edtCurrentPassword.error = getString(R.string.password_length_must_be_six_character)
             binding.edtCurrentPassword.requestFocus()
            false
        } else if (!ValidationUtils.isValidString(binding.edtNewPassword)) {
             binding.edtNewPassword.error = getString(R.string.enter_password)
             binding.edtNewPassword.requestFocus()
            false
        } else if (binding.edtNewPassword.text.toString().length < 6) {
             binding.edtNewPassword.error = getString(R.string.password_length_must_be_six_character)
             binding.edtNewPassword.requestFocus()
            false
        } else if (!ValidationUtils.isValidString(binding.edtConfirmPassword)) {
             binding.edtConfirmPassword.error = getString(R.string.enter_password)
             binding.edtConfirmPassword.requestFocus()
            false
        } else if (binding.edtConfirmPassword.text.toString().length < 6) {
             binding.edtConfirmPassword.error = getString(R.string.password_length_must_be_six_character)
             binding.edtConfirmPassword.requestFocus()
            false
        } else {
            true
        }
}