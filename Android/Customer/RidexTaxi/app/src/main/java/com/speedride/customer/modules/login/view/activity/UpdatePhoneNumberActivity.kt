package com.speedride.customer.modules.login.view.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.speedride.customer.R
import com.speedride.customer.base.BaseActivity
import com.speedride.customer.modules.utils.ValidationUtils
import kotlinx.android.synthetic.main.activity_update_phone_number.*

class UpdatePhoneNumberActivity : BaseActivity(), View.OnClickListener {

    /*private var mEdtMobile: AppCompatEditText? = null
    private var mTxtSaveAndVerify: AppCompatTextView? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_phone_number)
        initView(null)
    }

    /*override fun getActionTitle(): String {
        return getString(R.string.update_phone_number)
    }

    override fun isHomeButtonEnable(): Boolean {
        return true
    }*/

    override val actionTitle: String?
        get() = getString(R.string.update_phone_number)

    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back_white
    }

    override fun initView(view: View?) {
        setToolbar(findViewById(R.id.appbar))

        /*mEdtMobile = findViewById(R.id.edtMobile)
        mTxtSaveAndVerify = findViewById(R.id.txtSaveAndVerify)*/

        txtSaveAndVerify?.setOnClickListener(this)
    }

    override fun showToastMessage(message: String) {}
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private val isValidate: Boolean
        private get() {
            val isValid: Boolean
            if (!ValidationUtils.isValidMobile(edtMobile)) {
                edtMobile!!.error = getString(R.string.enter_mobile_number)
                edtMobile!!.requestFocus()
                isValid = false
            } else {
                isValid = true
            }
            return isValid
        }

    override fun onClick(view: View) {
        if (view.id == R.id.txtSaveAndVerify) {
            if (isValidate) {
                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()
            }
        }
    }
}