package com.speedride.driver.modules.userManagement.ui.activity


import com.speedride.driver.base.BaseActivity
import android.widget.ProgressBar
import com.speedride.driver.utils.PreferenceUtils
import android.app.ProgressDialog
import android.content.Context
import com.speedride.driver.model.ServerResponse
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import android.widget.RelativeLayout
import com.speedride.driver.utils.ValidationUtils
import android.widget.Toast
import org.json.JSONObject
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.engine.GlideException
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.speedride.driver.modules.home.ui.activity.ViewFullScreenImagesActivity
import com.speedride.driver.model.Data
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class DocumentDisplayActivity : BaseActivity(), View.OnClickListener {
    private var mImgDrivingLicence: ImageView? = null
    private var mImgVehicleInsurance: ImageView? = null
    private var mImgVehiclePermit: ImageView? = null
    private var mImgVehicleRegistration: ImageView? = null
    private var mProgressBar1: ProgressBar? = null
    private var mProgressBar2: ProgressBar? = null
    private var mProgressBar3: ProgressBar? = null
    private var mProgressBar4: ProgressBar? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mStrDriverId: String? = null
    private var mStrVehicleId: String? = null
    private var mApiGetVehicleDetailsCall: Call<ServerResponse<Data>>? = null
    private lateinit var mDocumentList: Array<String?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_display)
        initView(null)
    }

    override val activity: AppCompatActivity
        get() = this@DocumentDisplayActivity
    override val actionTitle: String
        get() = resources.getString(R.string.documents)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun initView(view: View?) {
        setToolbar((findViewById<View>(R.id.rlToolbar) as RelativeLayout))
        init()
        if (ValidationUtils.isInternetAvailable(this)) {
            showProgress()
            apiGetVehicleDetails()
        } else {
            showToastMessage(activity, activity.resources.getString(R.string.network_error))
        }
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
                        if (ValidationUtils.isValidObject(
                                response.body()!!.data
                            )
                        ) {
                            mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                            response.body()!!.data?.let { loadDocumentsData(it) }

                            /* new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                }
                            }, 2000);*/
                        }
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@DocumentDisplayActivity,
                                    jObjError.getString("message")
                                )
                            } catch (e: Exception) {
                                showToastMessage(this@DocumentDisplayActivity, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                    dismissProgress()
                    Log.d(TAG, "onFailure: $t")
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                }
            })
        }
    }

    private fun init() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))
        mPreferenceUtils = PreferenceUtils.getInstance(this@DocumentDisplayActivity)
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mStrVehicleId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_ID, "")
        mImgDrivingLicence = findViewById(R.id.imgDrivingLicence)
        mImgVehicleInsurance = findViewById(R.id.imgVehicleInsurance)
        mImgVehiclePermit = findViewById(R.id.imgVehiclePermit)
        mImgVehicleRegistration = findViewById(R.id.imgVehicleRegistration)
        mProgressBar1 = findViewById(R.id.progress_bar1)
        mProgressBar2 = findViewById(R.id.progress_bar2)
        mProgressBar3 = findViewById(R.id.progress_bar3)
        mProgressBar4 = findViewById(R.id.progress_bar4)
        mImgDrivingLicence!!.setOnClickListener(this)
        mImgVehicleInsurance!!.setOnClickListener(this)
        mImgVehiclePermit!!.setOnClickListener(this)
        mImgVehicleRegistration!!.setOnClickListener(this)
    }

    private fun loadDocumentsData(data: Data) {

        //add all doc in list
        mDocumentList = arrayOfNulls(4)
        if (ValidationUtils.isValidString(data.driver_detail?.d_licence)) {
            Glide.with(this)
                .load(Common.UPLOAD_URL + (data.driver_detail?.d_licence))
                .placeholder(R.drawable.ic_driver_licence)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        mProgressBar1!!.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        mProgressBar1!!.visibility = View.GONE
                        return false
                    }
                })
                .into(mImgDrivingLicence!!)
            mDocumentList[0] = data.driver_detail?.d_licence
        }
        if (ValidationUtils.isValidString(data.driver_detail?.v_insurance)) {
            Glide.with(this)
                .load(Common.UPLOAD_URL + (data.driver_detail?.v_insurance))
                .placeholder(R.drawable.ic_driver_document)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        mProgressBar2!!.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        mProgressBar2!!.visibility = View.GONE
                        return false
                    }
                })
                .into(mImgVehicleInsurance!!)
            mDocumentList[1] = data.driver_detail?.v_insurance
        }
        if (ValidationUtils.isValidString(data.driver_detail?.v_permit)) {
            Glide.with(this)
                .load(Common.UPLOAD_URL + (data.driver_detail?.v_permit))
                .placeholder(R.drawable.ic_driver_document)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        mProgressBar3!!.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        mProgressBar3!!.visibility = View.GONE
                        return false
                    }
                })
                .into(mImgVehiclePermit!!)
            mDocumentList[2] = data.driver_detail?.v_permit
        }
        if (ValidationUtils.isValidString(data.driver_detail?.v_registration)) {
            Glide.with(this)
                .load(Common.UPLOAD_URL + (data.driver_detail?.v_registration))
                .placeholder(R.drawable.ic_driver_document)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        mProgressBar4!!.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        mProgressBar4!!.visibility = View.GONE
                        return false
                    }
                })
                .into(mImgVehicleRegistration!!)
            mDocumentList[3] = data.driver_detail?.v_registration
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

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.imgDrivingLicence -> if (ValidationUtils.isValidObject(mDocumentList) && mDocumentList.size == 4) {
                val intent = Intent(this, ViewFullScreenImagesActivity::class.java)
                intent.putExtra("Document_List", mDocumentList)
                intent.putExtra("Document_List_Position", 0)
                intent.putExtra("Document_Name", resources.getString(R.string.driving_licence))
                animationIntent(this@DocumentDisplayActivity, intent)
            }
            R.id.imgVehicleInsurance -> if (ValidationUtils.isValidObject(mDocumentList) && mDocumentList.size == 4) {
                val intent = Intent(this, ViewFullScreenImagesActivity::class.java)
                intent.putExtra("Document_List", mDocumentList)
                intent.putExtra("Document_List_Position", 1)
                intent.putExtra("Document_Name", resources.getString(R.string.vehicle_insurance))
                animationIntent(this@DocumentDisplayActivity, intent)
            }
            R.id.imgVehiclePermit -> if (ValidationUtils.isValidObject(mDocumentList) && mDocumentList.size == 4) {
                val intent = Intent(this, ViewFullScreenImagesActivity::class.java)
                intent.putExtra("Document_List", mDocumentList)
                intent.putExtra("Document_List_Position", 2)
                intent.putExtra("Document_Name", resources.getString(R.string.vehicle_permit))
                animationIntent(this@DocumentDisplayActivity, intent)
            }
            R.id.imgVehicleRegistration -> if (ValidationUtils.isValidObject(mDocumentList) && mDocumentList.size == 4) {
                val intent = Intent(this, ViewFullScreenImagesActivity::class.java)
                intent.putExtra("Document_List", mDocumentList)
                intent.putExtra("Document_List_Position", 3)
                intent.putExtra("Document_Name", resources.getString(R.string.vehicle_registration))
                animationIntent(this@DocumentDisplayActivity, intent)
            }
        }
    }

    companion object {
        private val TAG = DocumentDisplayActivity::class.java.simpleName
    }
}