package com.speedride.customer.modules.home.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.speedride.customer.R


import com.speedride.customer.base.BaseActivity
import com.speedride.customer.databinding.ActivityHomeBinding
import com.speedride.customer.modules.login.view.activity.LoginActivity
import com.speedride.customer.modules.register.view.activity.RegisterActivity
import com.speedride.customer.modules.utils.PreferenceUtils

class HomeActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityHomeBinding

    /*private var mTxtLogin: AppCompatTextView? = null
    private var mTxtRegister: AppCompatTextView? = null*/

    private var mPreference: PreferenceUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        //setContentView(R.layout.activity_home)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView(null)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("cvcbvcbv", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                //String msg = getString(R.string.msg_token_fmt, token);
                Log.d("TAG", "fcmToken : $token")
                mPreference!!.save(PreferenceUtils.PREF_KEY_FCM_TOKEN, token)
                //Toast.makeText(HomeActivity.this, token, Toast.LENGTH_SHORT).show();
            })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.txtLogin -> {
                val loginIntent = Intent(this@HomeActivity, LoginActivity::class.java)
                animationIntent(this@HomeActivity, loginIntent)
            }
            R.id.txtRegister -> {
                val registerIntent = Intent(this@HomeActivity, RegisterActivity::class.java)
                animationIntent(this@HomeActivity, registerIntent)
            }
        }
    }

    /*override fun getActionTitle(): String {
        return ""
    }

    override fun isHomeButtonEnable(): Boolean {
        return false
    }*/

    override val actionTitle: String?
        get() = ""

    override val isHomeButtonEnable: Boolean
        get() = false

    override fun setHomeButtonIcon(): Int {
        return 0
    }

    override fun initView(view: View?) {
        mPreference = PreferenceUtils.getInstance(this@HomeActivity)

        /*mTxtLogin = findViewById(R.id.txtLogin)
        mTxtRegister = findViewById(R.id.txtRegister)
        mTxtLogin?.setOnClickListener(this)
        mTxtRegister?.setOnClickListener(this)*/

        binding.txtLogin.setOnClickListener(this)
        binding.txtRegister.setOnClickListener(this)
    }

    override fun showToastMessage(message: String) {}
}