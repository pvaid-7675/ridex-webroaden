package com.speedride.driver.modules.userManagement.ui.activity


import com.speedride.driver.base.BaseActivity
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.model.ServerResponse
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.utils.ValidationUtils
import org.json.JSONObject
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import com.speedride.driver.model.Data
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*

class AddVehicleDetailsActivity : BaseActivity(), View.OnClickListener {
    private var mSpnYear: Spinner? = null
    private var mSpnColor: Spinner? = null
    private var mSpnInteriorColor: Spinner? = null
    private var mTxtBtnContinue: TextView? = null
    private var mEdtBrand: EditText? = null
    private var mEdtModel: EditText? = null
    private var mEdtVehicleNum: EditText? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mApiSendVehicleDetailsCall: Call<ServerResponse<Any>>? = null
    private var mApiGetVehicleDetailsCall: Call<ServerResponse<Data>>? = null
    private var mStrDriverId: String? = null
    private var mStrVehicleId: String? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mImgVehicle: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_vehicle_details)
        initView(null)
    }

    override val activity: AppCompatActivity?
        get() = this@AddVehicleDetailsActivity
    override val actionTitle: String?
        get() = resources.getString(R.string.add_vehicle_details)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun initView(view: View?) {
        init()
    }

    override fun showToastMessage(context: Context?, message: String?) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        setToolbar(findViewById<View>(R.id.rlToolbar) as RelativeLayout)
        hideKeyboard(this)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))
        mPreferenceUtils = PreferenceUtils.getInstance(this@AddVehicleDetailsActivity)
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mStrVehicleId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_ID, "")
        Log.d(TAG, "init: $mStrDriverId $mStrVehicleId")
        mImgVehicle = findViewById(R.id.imgVehicle)
        if (ValidationUtils.isValidString(
                mPreferenceUtils!!.get(
                    PreferenceUtils.PREF_KEY_VEHICLE_ID,
                    ""
                )
            )
        ) {
            mImgVehicle!!.setImageResource(
                Utils.checkVehicleImagesSelected(
                    mPreferenceUtils!!.get(
                        PreferenceUtils.PREF_KEY_VEHICLE_ID,
                        ""
                    )
                )
            )
        }
        mEdtBrand = findViewById(R.id.edtBrand)
        mEdtModel = findViewById(R.id.edtModel)
        mEdtVehicleNum = findViewById(R.id.edtVehicleNum)
        mTxtBtnContinue = findViewById(R.id.btn_Continue)
        mTxtBtnContinue!!.setOnClickListener(this)
        mSpnYear = findViewById(R.id.spnYear)
        mSpnColor = findViewById(R.id.spnColor)
        mSpnInteriorColor = findViewById(R.id.spnInteriorColor)
        setSpinner()
        if (ValidationUtils.isInternetAvailable(this)) {
            showProgress()
            apiGetVehicleDetails()
        } else {
            activity?.let { showToastMessage(it, activity!!.resources.getString(R.string.network_error)) }
        }
    }

    private val prefData: Unit
        private get() {
            if (ValidationUtils.isValidObject(mPreferenceUtils!!.driverData)) if (ValidationUtils.isValidString(
                    mPreferenceUtils!!.driverData?.vehicle_detail?.vt_id
                )
            ) {
                mEdtBrand!!.setText(mPreferenceUtils!!.driverData?.vehicle_detail?.brand)
                mEdtModel!!.setText(mPreferenceUtils!!.driverData?.vehicle_detail?.model)
                mEdtVehicleNum!!.setText(mPreferenceUtils!!.driverData?.vehicle_detail?.number)
                setSpinner()
            }
        }

    private fun setSpinner() {

        //year spinner adapter
        val years = ArrayList<String>()
        val thisYear = Calendar.getInstance()[Calendar.YEAR]
        val previousYear = thisYear - 16
        var isFirstTime = true
        for (i in previousYear..thisYear) {
            if (isFirstTime) {
                isFirstTime = false
                years.add("Select")
            } else {
                years.add(Integer.toString(i))
            }
        }
        val yearSpnAdapter = ArrayAdapter(this, R.layout.layout_spinner_header, years)
        yearSpnAdapter.setDropDownViewResource(R.layout.layout_spinner_item_drop_down)
        mSpnYear!!.adapter = yearSpnAdapter
        //check for already selected
        if (ValidationUtils.isValidObject(mPreferenceUtils!!.driverData)
            && mPreferenceUtils!!.driverData?.vehicle_detail != null
        ) {
            if (ValidationUtils.isValidString(mPreferenceUtils!!.driverData?.vehicle_detail?.year)) {
                val year_pos =
                    yearSpnAdapter.getPosition(mPreferenceUtils!!.driverData?.vehicle_detail?.year)
                mSpnYear!!.setSelection(year_pos)
            }
        }

        //year spinner adapter
        val colorSpnAdapter = ArrayAdapter<CharSequence>(
            this,
            R.layout.layout_spinner_header,
            resources.getStringArray(R.array.color_list)
        )
        colorSpnAdapter.setDropDownViewResource(R.layout.layout_spinner_item_drop_down)
        mSpnColor!!.adapter = colorSpnAdapter
        //check for already selected
        if (ValidationUtils.isValidObject(mPreferenceUtils!!.driverData)) if (ValidationUtils.isValidObject(
                mPreferenceUtils!!.driverData?.vehicle_detail
            )
        ) if (ValidationUtils.isValidString(
                mPreferenceUtils!!.driverData?.vehicle_detail?.color
            )
        ) {
            val color_pos =
                colorSpnAdapter.getPosition(mPreferenceUtils!!.driverData?.vehicle_detail?.color)
            mSpnColor!!.setSelection(color_pos)
        }

        //year spinner adapter
        val intColorSpnAdapter = ArrayAdapter<CharSequence>(
            this,
            R.layout.layout_spinner_header,
            resources.getStringArray(R.array.color_list)
        )
        intColorSpnAdapter.setDropDownViewResource(R.layout.layout_spinner_item_drop_down)
        mSpnInteriorColor!!.adapter = intColorSpnAdapter
        //check for already selected
        if (ValidationUtils.isValidObject(mPreferenceUtils!!.driverData)
            && mPreferenceUtils!!.driverData?.vehicle_detail != null
        ) if (ValidationUtils.isValidString(
                mPreferenceUtils!!.driverData?.vehicle_detail?.icolor
            )
        ) {
            val interior_color_pos =
                intColorSpnAdapter.getPosition(mPreferenceUtils!!.driverData?.vehicle_detail?.icolor)
            mSpnInteriorColor!!.setSelection(interior_color_pos)
        }
    }

    private val yearSelect: Boolean
        private get() = mSpnYear!!.selectedItem.toString() == "Select"
    private val colorSelect: Boolean
        private get() = mSpnColor!!.selectedItem.toString() == "Select"
    private val interiorColorSelect: Boolean
        private get() = mSpnInteriorColor!!.selectedItem.toString() == "Select"
    private val isValidate: Boolean
        private get() {
            if (!ValidationUtils.isValidString(mEdtBrand!!)) {
                mEdtBrand!!.error = getString(R.string.enter_vehicle_brand)
                mEdtBrand!!.requestFocus()
                return false
            } else if (mEdtBrand!!.text.toString().length < 2) {
                mEdtBrand!!.error = getString(R.string.enter_minimum_2_character)
                mEdtBrand!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidString(mEdtModel!!)) {
                mEdtModel!!.error = getString(R.string.enter_vehicle_model)
                mEdtModel!!.requestFocus()
                return false
            } else if (mEdtModel!!.text.toString().length < 2) {
                mEdtModel!!.error = getString(R.string.enter_minimum_2_character)
                mEdtModel!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidString(mEdtVehicleNum!!)) {
                mEdtVehicleNum!!.error = getString(R.string.enter_vehicle_number)
                mEdtVehicleNum!!.requestFocus()
                return false
            } else if (mEdtVehicleNum!!.text.toString().length < 2) {
                mEdtVehicleNum!!.error = getString(R.string.enter_minimum_2_character)
                mEdtVehicleNum!!.requestFocus()
                return false
            } else if (yearSelect) {
                activity?.let {
                    showToastMessage(
                        it,
                        activity!!.resources.getString(R.string.select_vehicle_year)
                    )
                }
                return false
            } else if (colorSelect) {
                activity?.let {
                    showToastMessage(
                        it,
                        activity!!.resources.getString(R.string.select_vehicle_color)
                    )
                }
                return false
            } else if (interiorColorSelect) {
                activity?.let {
                    showToastMessage(
                        it,
                        activity!!.resources.getString(R.string.select_vehicle_interior_color)
                    )
                }
                return false
            }
            return true
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
                    val resp  = response.body()
                    if (resp != null && resp.status == 200) {
                        mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                        prefData
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@AddVehicleDetailsActivity,
                                    jObjError.getString("message")
                                )
                            } catch (e: Exception) {
                                showToastMessage(this@AddVehicleDetailsActivity, e.message!!)
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

    //api call for send selected vehicle details
    private fun apiSendVehicleDetails() {
        hideKeyboard(this)
        if (ValidationUtils.isValidString(mStrDriverId) && ValidationUtils.isValidString(
                mStrVehicleId
            )
        ) {
            mApiSendVehicleDetailsCall = Singleton.restClient.sendVehicleDetails(
                mStrDriverId!!,
                mStrVehicleId!!,
                mEdtBrand!!.text.toString(),
                mEdtModel!!.text.toString(),
                mSpnYear!!.selectedItem.toString(),
                mSpnColor!!.selectedItem.toString(),
                mSpnInteriorColor!!.selectedItem.toString(),
                mEdtVehicleNum!!.text.toString()
            )
            mApiSendVehicleDetailsCall!!.enqueue(object : Callback<ServerResponse<Any>> {
                override fun onResponse(
                    call: Call<ServerResponse<Any>>,
                    response: Response<ServerResponse<Any>>
                ) {
                    dismissProgress()
                    val resp = response.body()
                    if (resp != null && resp.status == 200) {
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_VEHICLE_DETAILS, true)
                        activity?.let { showToastMessage(it, response.body()!!.message) }
                        val documentActivity =
                            Intent(this@AddVehicleDetailsActivity, DocumentActivity::class.java)
                        animationIntent(this@AddVehicleDetailsActivity, documentActivity)
                      //  finish()
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@AddVehicleDetailsActivity,
                                    jObjError.getString("message")
                                )
                            } catch (e: Exception) {
                                showToastMessage(this@AddVehicleDetailsActivity, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Any>>, t: Throwable) {
                    dismissProgress()
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                }
            })
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
        if (mApiSendVehicleDetailsCall != null && !mApiSendVehicleDetailsCall!!.isCanceled) {
            mApiSendVehicleDetailsCall!!.cancel()
        }
        if (mApiGetVehicleDetailsCall != null && !mApiGetVehicleDetailsCall!!.isCanceled) {
            mApiGetVehicleDetailsCall!!.cancel()
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_Continue) {
            if (ValidationUtils.isInternetAvailable(this)) {
                if (isValidate) {
                    showProgress()
                    apiSendVehicleDetails()
                }
            } else {
                activity?.let { showToastMessage(it, activity!!.resources.getString(R.string.network_error)) }
            }
        }
    }

    companion object {
        private val TAG = AddVehicleDetailsActivity::class.java.simpleName
    }
}