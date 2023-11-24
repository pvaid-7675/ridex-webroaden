package com.speedride.driver.modules.userManagement.ui.activity


import com.speedride.driver.base.BaseActivity
import androidx.cardview.widget.CardView
import com.speedride.driver.utils.PreferenceUtils
import android.app.ProgressDialog
import android.content.Context
import com.speedride.driver.model.ServerResponse
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.utils.ValidationUtils
import org.json.JSONObject
import android.content.Intent
import android.view.View
import android.widget.*
import com.speedride.driver.modules.auth.ui.activity.LoginActivity
import com.speedride.driver.model.Data
import com.speedride.driver.modules.home.ui.activity.MainActivity
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class DocumentActivity : BaseActivity(), View.OnClickListener {
    private var mTxtBtnContinue: TextView? = null
    private var mTxtDriverLicense: TextView? = null
    private var mTxtVehicleInsurance: TextView? = null
    private var mTxtVehiclePermit: TextView? = null
    private var mTxtVehicleRegistration: TextView? = null
    private var mcardViewDrivingLicence: CardView? = null
    private var mcardViewVehicleInsurance: CardView? = null
    private var mcardViewVehiclePermit: CardView? = null
    private var mcardViewVehicleRegistration: CardView? = null
    private var mImgCheckLicence1: ImageView? = null
    private var mImgCheckLicence2: ImageView? = null
    private var mImgCheckVehicleInsurance: ImageView? = null
    private var mImgCheckVehiclePermit: ImageView? = null
    private var mImgCheckVehicleRegistration: ImageView? = null
    private var mImgArrowLicence: ImageView? = null
    private var mImgArrowInsurance: ImageView? = null
    private var mImgArrowPermit: ImageView? = null
    private var mImgArrowRegistration: ImageView? = null
    private var mLlUploadLicense: LinearLayout? = null
    private var mLlUploadInsurance: LinearLayout? = null
    private var mLlUploadPermit: LinearLayout? = null
    private var mLlUploadRegistration: LinearLayout? = null
    private var mLlDrivingLicence: LinearLayout? = null
    private var mLlVehicleInsurance: LinearLayout? = null
    private var mLlVehiclePermit: LinearLayout? = null
    private var mLlVehicleRegistration: LinearLayout? = null
    private var mLlDrivingLicenceDetail: LinearLayout? = null
    private var mLlVehicleInsuranceDetail: LinearLayout? = null
    private var mLlVehiclePermitDetail: LinearLayout? = null
    private var mLlVehicleRegistrationDetail: LinearLayout? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mApiGetVehicleDetailsCall: Call<ServerResponse<Data>>? = null
    private var mStrDriverId: String? = null
    private var mStrVehicleId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        initView(null)
    }

    override val activity: AppCompatActivity
        get() = this@DocumentActivity
    override val actionTitle: String
        get() = resources.getString(R.string.document_txt)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun initView(view: View?) {
        setToolbar((findViewById<View>(R.id.rlToolbar) as RelativeLayout))
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))
        mPreferenceUtils = PreferenceUtils.getInstance(this@DocumentActivity)
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mStrVehicleId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_ID, "")
        mTxtBtnContinue = findViewById(R.id.btn_Continue)
        mTxtDriverLicense = findViewById(R.id.txtDrivingLicence)
        mTxtVehicleInsurance = findViewById(R.id.txtVehicleInsurance)
        mTxtVehiclePermit = findViewById(R.id.txtVehiclePermit)
        mTxtVehicleRegistration = findViewById(R.id.txtVehicleRegistration)
        mcardViewDrivingLicence = findViewById(R.id.cardViewDrivingLicence)
        mcardViewVehicleInsurance = findViewById(R.id.cardViewVehicleInsurance)
        mcardViewVehiclePermit = findViewById(R.id.cardViewVehiclePermit)
        mcardViewVehicleRegistration = findViewById(R.id.cardViewVehicleRegistration)
        mcardViewDrivingLicence!!.setOnClickListener(this)
        mcardViewVehicleInsurance!!.setOnClickListener(this)
        mcardViewVehiclePermit!!.setOnClickListener(this)
        mcardViewVehicleRegistration!!.setOnClickListener(this)
        mImgCheckLicence1 = findViewById(R.id.imgCheckLicence1)
       // mImgCheckLicence2 = findViewById(R.id.imgCheckLicence2)
        mImgCheckVehicleInsurance = findViewById(R.id.imgCheckInsurance1)
        mImgCheckVehiclePermit = findViewById(R.id.imgCheckVehiclePermit1)
        mImgCheckVehicleRegistration = findViewById(R.id.imgCheckVehicleRegistration1)
        mImgArrowLicence = findViewById(R.id.imgArrowIconLicence)
        mImgArrowInsurance = findViewById(R.id.imgArrowIconInsurance)
        mImgArrowPermit = findViewById(R.id.imgArrowIconVehiclePermit)
        mImgArrowRegistration = findViewById(R.id.imgArrowIconVehicleRegistration)
        mLlDrivingLicence = findViewById(R.id.llLicence)
        mLlVehicleInsurance = findViewById(R.id.llInsurance)
        mLlVehiclePermit = findViewById(R.id.llPermit)
        mLlVehicleRegistration = findViewById(R.id.llRegistration)
        mLlDrivingLicence!!.setOnClickListener(this)
        mLlVehicleInsurance!!.setOnClickListener(this)
        mLlVehiclePermit!!.setOnClickListener(this)
        mLlVehicleRegistration!!.setOnClickListener(this)
        mLlDrivingLicenceDetail = findViewById(R.id.llLicenceDetail)
        mLlVehicleInsuranceDetail = findViewById(R.id.llInsuranceDetail)
        mLlVehiclePermitDetail = findViewById(R.id.llPermitDetail)
        mLlVehicleRegistrationDetail = findViewById(R.id.llRegistrationDetail)
        mLlUploadLicense = findViewById(R.id.llLicenceUpload)
        mLlUploadInsurance = findViewById(R.id.llInsuranceUpload)
        mLlUploadPermit = findViewById(R.id.llPermitUpload)
        mLlUploadRegistration = findViewById(R.id.llRegistrationUpload)
        mLlUploadLicense!!.setOnClickListener(this)
        mLlUploadInsurance!!.setOnClickListener(this)
        mLlUploadPermit!!.setOnClickListener(this)
        mLlUploadRegistration!!.setOnClickListener(this)
        mTxtBtnContinue!!.setOnClickListener(this)
        if (ValidationUtils.isInternetAvailable(this)) {
            showProgress()
            apiGetVehicleDetails()
        } else {
            showToastMessage(activity, activity.resources.getString(R.string.network_error))
        }
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    //api call for send selected vehicle details
    private fun apiGetVehicleDetails() {
        hideKeyboard(this)
        if (ValidationUtils.isValidString(mStrDriverId) && ValidationUtils.isValidString(
                mStrVehicleId
            )
        ) {
            mApiGetVehicleDetailsCall = Singleton.restClient.vehicleDetails
            mApiGetVehicleDetailsCall!!.enqueue(object : Callback<ServerResponse<Data>> {
                override fun onResponse(
                    call: Call<ServerResponse<Data>>,
                    response: Response<ServerResponse<Data>>
                ) {
                    dismissProgress()
                    val resp = response.body()
                    if (resp != null && resp.status == 200) {
                        mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                        checkDocument()
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@DocumentActivity,
                                    jObjError.getString("message")
                                )
                            } catch (e: Exception) {
                                showToastMessage(this@DocumentActivity, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                    dismissProgress()
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                }
            })
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.llLicenceUpload -> {
                val intentDriverLicence =
                    Intent(this@DocumentActivity, DocumentUploadActivity::class.java)
                intentDriverLicence.putExtra(Common.FRAGMENT_POSITION, "0")
                animationIntent(this@DocumentActivity, intentDriverLicence)
            }
            R.id.llInsuranceUpload -> {
                val intentVehicleInsurance =
                    Intent(this@DocumentActivity, DocumentUploadActivity::class.java)
                intentVehicleInsurance.putExtra(Common.FRAGMENT_POSITION, "1")
                animationIntent(this@DocumentActivity, intentVehicleInsurance)
            }
            R.id.llPermitUpload -> {
                val intentVehiclePermit =
                    Intent(this@DocumentActivity, DocumentUploadActivity::class.java)
                intentVehiclePermit.putExtra(Common.FRAGMENT_POSITION, "2")
                animationIntent(this@DocumentActivity, intentVehiclePermit)
            }
            R.id.llRegistrationUpload -> {
                val intentVehicleRegistration =
                    Intent(this@DocumentActivity, DocumentUploadActivity::class.java)
                intentVehicleRegistration.putExtra(Common.FRAGMENT_POSITION, "3")
                animationIntent(this@DocumentActivity, intentVehicleRegistration)
            }
            R.id.btn_Continue -> if (checkDocumentUploaded()) {
                //document upload success save in preference
                mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_DOCUMENT_UPLOAD_SUCCESSFULLY, true)
                if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_LOGIN, false)) {
                    val intentHomeActivity =
                        Intent(this@DocumentActivity, LoginActivity::class.java)
                    intentHomeActivity.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    animationIntent(this@DocumentActivity, intentHomeActivity)
                } else {
                    val intentHomeActivity = Intent(this@DocumentActivity, MainActivity::class.java)
                    intentHomeActivity.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    animationIntent(this@DocumentActivity, intentHomeActivity)
                }
                finish()
            }
            R.id.llLicence -> {
                //manage licence text and images
                if (mLlDrivingLicenceDetail!!.visibility == View.VISIBLE) {
                    mLlDrivingLicenceDetail!!.visibility = View.GONE
                    mImgArrowLicence!!.background = resources.getDrawable(R.drawable.ic_arrow_down)
                    mTxtDriverLicense!!.setTextColor(resources.getColor(R.color.gray))
                } else {
                    mLlDrivingLicenceDetail!!.visibility = View.VISIBLE
                    mImgArrowLicence!!.background = resources.getDrawable(R.drawable.ic_arrow_up)
                    mTxtDriverLicense!!.setTextColor(resources.getColor(R.color.black))
                }
                checkDocument()
            }
            R.id.llInsurance -> {
                //manage Insurance text and images
                if (mLlVehicleInsuranceDetail!!.visibility == View.VISIBLE) {
                    mLlVehicleInsuranceDetail!!.visibility = View.GONE
                    mImgArrowInsurance!!.background =
                        resources.getDrawable(R.drawable.ic_arrow_down)
                    mTxtVehicleInsurance!!.setTextColor(resources.getColor(R.color.gray))
                } else {
                    mLlVehicleInsuranceDetail!!.visibility = View.VISIBLE
                    mImgArrowInsurance!!.background = resources.getDrawable(R.drawable.ic_arrow_up)
                    mTxtVehicleInsurance!!.setTextColor(resources.getColor(R.color.black))
                }
                checkDocument()
            }
            R.id.llPermit -> {
                //manage Permit text and images
                if (mLlVehiclePermitDetail!!.visibility == View.VISIBLE) {
                    mLlVehiclePermitDetail!!.visibility = View.GONE
                    mImgArrowPermit!!.background = resources.getDrawable(R.drawable.ic_arrow_down)
                    mTxtVehiclePermit!!.setTextColor(resources.getColor(R.color.gray))
                } else {
                    mLlVehiclePermitDetail!!.visibility = View.VISIBLE
                    mImgArrowPermit!!.background = resources.getDrawable(R.drawable.ic_arrow_up)
                    mTxtVehiclePermit!!.setTextColor(resources.getColor(R.color.black))
                }
                checkDocument()
            }
            R.id.llRegistration -> {
                //manage Registration text and images
                if (mLlVehicleRegistrationDetail!!.visibility == View.VISIBLE) {
                    mLlVehicleRegistrationDetail!!.visibility = View.GONE
                    mImgArrowRegistration!!.background =
                        resources.getDrawable(R.drawable.ic_arrow_down)
                    mTxtVehicleRegistration!!.setTextColor(resources.getColor(R.color.gray))
                } else {
                    mLlVehicleRegistrationDetail!!.visibility = View.VISIBLE
                    mImgArrowRegistration!!.background =
                        resources.getDrawable(R.drawable.ic_arrow_up)
                    mTxtVehicleRegistration!!.setTextColor(resources.getColor(R.color.black))
                }
                checkDocument()
            }
        }
    }

    //check that driver document uploaded or not
    private fun checkDocumentUploaded(): Boolean {
        val driverLicence =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_DRIVER_LICENCE, "")
        val driverProfilePhoto =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_PROFILE_PHOTO, "")
        val driverVehicleInsurance =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_VEHICLE_INSURANCE, "")
        val driverVehiclePermit =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_VEHICLE_PERMIT, "")
        val driverVehicleRegistration =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_VEHICLE_REGISTRATION, "")
        if (!ValidationUtils.isValidString(driverLicence)) {
            showToastMessage(
                activity,
                activity.resources.getString(R.string._1_please_upload_your_driving_licence)
            )
            return false
        } else if (!ValidationUtils.isValidString(driverVehicleInsurance)) {
            showToastMessage(
                activity,
                activity.resources.getString(R.string.please_upload_your_vehicle_insurance)
            )
            return false
        } else if (!ValidationUtils.isValidString(driverVehiclePermit)) {
            showToastMessage(
                activity,
                activity.resources.getString(R.string.please_upload_your_vehicle_permit)
            )
            return false
        } else if (!ValidationUtils.isValidString(driverVehicleRegistration)) {
            showToastMessage(
                activity,
                activity.resources.getString(R.string.please_upload_your_vehicle_registration)
            )
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        checkDocument()
    }

    //check upload document, then change views color
    private fun checkDocument() {
        val driverLicence =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_DRIVER_LICENCE, "")
        val driverProfilePhoto =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_PROFILE_PHOTO, "")
        val driverVehicleInsurance =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_VEHICLE_INSURANCE, "")
        val driverVehiclePermit =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_VEHICLE_PERMIT, "")
        val driverVehicleRegistration =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_VEHICLE_REGISTRATION, "")
        if (ValidationUtils.isValidString(driverLicence)) {
            mImgCheckLicence1!!.background = resources.getDrawable(R.drawable.ic_check_thumb)
          //  mImgCheckLicence2!!.background = resources.getDrawable(R.drawable.ic_check_thumb)
            mTxtDriverLicense!!.setTextColor(resources.getColor(R.color.colorPrimary))
        }
        if (ValidationUtils.isValidString(driverVehicleInsurance)) {
            mImgCheckVehicleInsurance!!.background =
                resources.getDrawable(R.drawable.ic_check_thumb)
            mTxtVehicleInsurance!!.setTextColor(resources.getColor(R.color.colorPrimary))
        }
        if (ValidationUtils.isValidString(driverVehiclePermit)) {
            mImgCheckVehiclePermit!!.background = resources.getDrawable(R.drawable.ic_check_thumb)
            mTxtVehiclePermit!!.setTextColor(resources.getColor(R.color.colorPrimary))
        }
        if (ValidationUtils.isValidString(driverVehicleRegistration)) {
            mImgCheckVehicleRegistration!!.background =
                resources.getDrawable(R.drawable.ic_check_thumb)
            mTxtVehicleRegistration!!.setTextColor(resources.getColor(R.color.colorPrimary))
        }

        //check all document uploaded, then change button text to Getting Started
        if (ValidationUtils.isValidString(driverLicence) && ValidationUtils.isValidString(
                driverVehicleInsurance
            ) && ValidationUtils.isValidString(driverVehiclePermit) && ValidationUtils.isValidString(
                driverVehicleRegistration
            )
        ) {
            mTxtBtnContinue!!.setText(R.string.getting_started)
        }
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
        if (mApiGetVehicleDetailsCall != null && !mApiGetVehicleDetailsCall!!.isCanceled) {
            mApiGetVehicleDetailsCall!!.cancel()
        }
    }
}