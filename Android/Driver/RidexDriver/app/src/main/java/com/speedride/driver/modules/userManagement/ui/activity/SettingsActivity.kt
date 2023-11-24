package com.speedride.driver.modules.userManagement.ui.activity

import android.app.ActivityOptions
import android.content.Context

import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.speedride.driver.databinding.ActivitySettingsBinding
import com.speedride.driver.modules.auth.ui.activity.ChangePasswordActivity
import com.speedride.driver.modules.home.ui.activity.HomeActivity
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.CommonDialogUtils
import com.speedride.driver.utils.PreferenceUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingsActivity : AppCompatActivity(), View.OnClickListener {


    private val binding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }
    private lateinit var mCtx : Context
    private var commonDialogUtils: CommonDialogUtils? = null
    private var mApiAccountDelete: Call<Object>? = null
    private var mPreferenceUtils: PreferenceUtils?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mCtx = this
        initView()
    }


    val activity: AppCompatActivity
        get() = this@SettingsActivity
     val actionTitle: String
        get() = resources.getString(R.string.edit_profile)
     val isHomeButtonEnable: Boolean
        get() = true

     fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    fun initView() {
        commonDialogUtils = CommonDialogUtils(activity)
        mPreferenceUtils = activity?.let { PreferenceUtils.getInstance(it) }
        binding.llChangePassword.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.llAccountDelete.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
      finish()
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.llChangePassword -> {
                val intentChangePass = Intent(this, ChangePasswordActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(mCtx, R.anim.enter, R.anim.exit)
                startActivity(intentChangePass, options.toBundle())
            }
            R.id.btnBack -> {
                onBackPressed()
            }
            R.id.llAccountDelete->{
                openDeleteAccount()
            }
        }
    }
    fun openDeleteAccount(){
        commonDialogUtils!!.deleteAccount(
           activity,
            getString(R.string.delete_account_text),
            getString(R.string.yes),
            getString(R.string.no),
            object : CommonDialogUtils.DialogListener {
                override fun onButtonClick(selectedBtnTitle: String) {
                    if(selectedBtnTitle.equals(getString(R.string.yes))){
                        apicallAccountDelete()
                    }
                }
            })
    }
    fun apicallAccountDelete(){
        mApiAccountDelete = Singleton.restClient.accountDelete(mPreferenceUtils?.driverData!!.id)

        mApiAccountDelete?.enqueue(object :
            Callback<Object> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<Object>,
                response: Response<Object>
            ) {
                try {
                    val resp1: Object? = response.body()
                    val gson = Gson()
                    val json = gson.toJson(resp1)
                    val jObject = JSONObject(json)
                    Log.d("Response", response.message())
                    if (jObject.getInt("status") == Common.STATUS_200) {
                        mPreferenceUtils!!.clear(activity!!)
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_BEARER_TOKEN, "")
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_VERIFY_OTP, false)
                        val intentHome = Intent(activity!!, HomeActivity::class.java)
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intentHome)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }

            override fun onFailure(call: Call<Object>, t: Throwable) {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}