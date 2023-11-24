package com.speedride.customer.modules.login.view.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.speedride.customer.R
import com.speedride.customer.base.BaseActivity
import com.speedride.customer.modules.utils.ValidationUtils
import kotlinx.android.synthetic.main.activity_social_login.*

class SocialLoginActivity : BaseActivity(), View.OnClickListener {

    /*private var mEdtEmail: AppCompatEditText? = null
    private var mEdtMobile: AppCompatEditText? = null
    private var mTxtSubmit: AppCompatTextView? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_login)
        initView(null)
    }

    /*override fun getActionTitle(): String {
        return "Verify"
    }

    override fun isHomeButtonEnable(): Boolean {
        return true
    }*/

    override val actionTitle: String?
        get() = "Verify"

    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back_white
    }

    override fun initView(view: View?) {
        setToolbar(findViewById(R.id.appbar))

        /*mEdtEmail = findViewById(R.id.edtEmail)
        mEdtMobile = findViewById(R.id.edtMobile)
        mTxtSubmit = findViewById(R.id.txtSubmit)*/

        txtSubmit?.setOnClickListener(this)
    }

    override fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val isValidate: Boolean
        private get() {
            val isValid: Boolean
            if (edtEmail!!.text.toString().length == 0) {
                edtEmail!!.error = getString(R.string.enter_email_id)
                edtEmail!!.requestFocus()
                isValid = false
            } else if (!ValidationUtils.isEmailValid(edtEmail!!.text.toString())) {
                edtEmail!!.error = getString(R.string.enter_valid_email_id)
                edtEmail!!.requestFocus()
                isValid = false
            } else if (edtMobile!!.text.toString().length == 0) {
                edtMobile!!.error = getString(R.string.enter_mobile_number)
                edtMobile!!.requestFocus()
                isValid = false
            } else if (edtMobile!!.text.toString().length < 10) {
                edtMobile!!.error = getString(R.string.enter_valid_mobile_number)
                edtMobile!!.requestFocus()
                isValid = false
            } else {
                isValid = true
            }
            return isValid
        }

    override fun onClick(view: View) {
        if (view.id == R.id.txtSubmit) {
            if (isValidate) {
                showToastMessage("Submit")
            }
        }
    }
}