package com.speedride.driver.modules.home.ui.activity

import android.Manifest
import android.app.ActivityOptions
import android.content.Context
import com.speedride.driver.utils.PreferenceUtils
import android.os.Bundle
import com.speedride.driver.R
import com.google.firebase.messaging.FirebaseMessaging
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.speedride.driver.BuildConfig
import com.speedride.driver.databinding.ActivityHomeBinding
import com.speedride.driver.modules.auth.ui.activity.LoginActivity
import com.speedride.driver.modules.auth.ui.activity.RegisterActivity
import com.speedride.driver.utils.CommonDialogUtils
import com.speedride.driver.utils.PermissionUtils
import com.speedride.driver.utils.ValidationUtils

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }
    private lateinit var mCtx : Context
    private lateinit var preferenceUtils : PreferenceUtils
    private var commonDialogUtils: CommonDialogUtils? = null
    private var mPermission: PermissionUtils? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mCtx = this
        initView()
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result
                preferenceUtils.save(PreferenceUtils.PREF_KEY_FCM_TOKEN, token)
            })
        permission()
    }

    fun initView() {
        preferenceUtils = PreferenceUtils.getInstance(mCtx)!!
        binding.txtLogin.setOnClickListener(this)
        binding.txtRegister.setOnClickListener(this)
        commonDialogUtils = CommonDialogUtils(this)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermission?.onRequestPermissionResult(requestCode, permissions, grantResults)
    }
    override fun onClick(view: View) {
        when (view.id) {
            R.id.txtLogin -> {
                val loginIntent = Intent(mCtx, LoginActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(mCtx, R.anim.enter, R.anim.exit)
                startActivity(loginIntent, options.toBundle())
            }
            R.id.txtRegister -> {
                val registerIntent = Intent(mCtx, RegisterActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(mCtx, R.anim.enter, R.anim.exit)
                startActivity(registerIntent, options.toBundle())
            }
        }
    }
fun permission(){
    commonDialogUtils!!.loctionPermissionDialog(this,
        getString(R.string.location_enable_text),
        getString(R.string.location_enable),
        getString(R.string.cancel),
        object : CommonDialogUtils.DialogListener {
            override fun onButtonClick(selectedBtnTitle: String) {
                if (selectedBtnTitle == getString(R.string.location_enable)) {

                    mPermission = PermissionUtils(
                        this@HomeActivity,
                        arrayOf<String>(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        object : PermissionUtils.OnPermissionGrantCallback {
                            override fun onPermissionGranted() {

                            }

                            override fun onPermissionError(permission: String?) {

                            }
                        })
                }
            }
        })
}



}