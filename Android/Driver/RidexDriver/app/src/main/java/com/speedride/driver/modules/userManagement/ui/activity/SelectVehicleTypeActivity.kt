package com.speedride.driver.modules.userManagement.ui.activity


import com.speedride.driver.base.BaseActivity
import androidx.recyclerview.widget.RecyclerView
import com.speedride.driver.modules.userManagement.ui.adapter.VehicleSelectTypeAdapter
import android.widget.TextView
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.modules.userManagement.dataModel.VehicleType
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.widget.RelativeLayout
import com.speedride.driver.utils.ValidationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DefaultItemAnimator
import org.json.JSONObject
import com.speedride.driver.utils.AppLog
import android.content.Intent
import android.util.Log
import android.view.View
import com.speedride.driver.rest.Singleton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class SelectVehicleTypeActivity : BaseActivity(), View.OnClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mVehicleSelectTypeAdapter: VehicleSelectTypeAdapter? = null
    private var mTxtBtnContinue: TextView? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mApiGetVehicleTypeListCall: Call<ServerResponse<List<VehicleType>>>? = null
    private var mApiSendVehicleTypeCall: Call<ServerResponse<Any>>? = null
    private var mStrDriverId: String? = null
    private var mStrVehicleId: String? = null
    private var isSelect = false
    private var mProgressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_type_select)
        initView(null)
    }

    override val activity: AppCompatActivity
        get() = this@SelectVehicleTypeActivity
    override val actionTitle: String
        get() = resources.getString(R.string.select_vehicle_type)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun initView(view: View?) {
        init()
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun init() {
        setToolbar((findViewById<View>(R.id.rlToolbar) as RelativeLayout))
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))
        mPreferenceUtils = PreferenceUtils.getInstance(this@SelectVehicleTypeActivity)
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mTxtBtnContinue = findViewById(R.id.btn_Continue)
        mTxtBtnContinue!!.setOnClickListener(this)
        mRecyclerView = findViewById(R.id.recyclerViewVehicleType)
        if (ValidationUtils.isInternetAvailable(this)) {
            dismissProgress()
            apiGetVehicleTypeList()
        } else {
            showToastMessage(activity, activity.resources.getString(R.string.network_error))
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_Continue) {
            isSelect = mVehicleSelectTypeAdapter!!.isSelect
            mStrVehicleId = mVehicleSelectTypeAdapter!!.vehicleID

            if (ValidationUtils.isInternetAvailable(this)) {
                if (isSelect) {
                    dismissProgress()
                    apiSendVehicleType()
                } else {
                    showToastMessage(
                        activity,
                        activity.resources.getString(R.string.please_select_vehicle_type)
                    )
                }
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error))
            }
        }
    }

    //api call for get vehicle type list
    private fun apiGetVehicleTypeList() {
        mApiGetVehicleTypeListCall = Singleton.restClient.vehicleTypeList
        mApiGetVehicleTypeListCall!!.enqueue(object : Callback<ServerResponse<List<VehicleType>>> {
            override fun onResponse(
                call: Call<ServerResponse<List<VehicleType>>>,
                response: Response<ServerResponse<List<VehicleType>>>
            ) {
                dismissProgress()
                val resp = response.body()
                if (resp != null && resp.status == 200) {
                    val vehicleTypeList = response.body()!!
                        .data
                    if (ValidationUtils.isValidList(vehicleTypeList)) {
                        mVehicleSelectTypeAdapter = vehicleTypeList?.let {
                            VehicleSelectTypeAdapter(
                                this@SelectVehicleTypeActivity,
                                it
                            ) { selectedVehicleId ->
                                mStrVehicleId = selectedVehicleId.toString()
                                isSelect = true
                                Log.d(TAG, "onClick: selectedVehicleId: $selectedVehicleId")
                            }
                        }
                        mRecyclerView!!.layoutManager =
                            LinearLayoutManager(this@SelectVehicleTypeActivity)
                        mRecyclerView!!.itemAnimator = DefaultItemAnimator()
                        //    mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
                        mRecyclerView!!.adapter = mVehicleSelectTypeAdapter
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(
                                this@SelectVehicleTypeActivity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            showToastMessage(this@SelectVehicleTypeActivity, e.message!!)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ServerResponse<List<VehicleType>>>, t: Throwable) {
                dismissProgress()
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                AppLog.d(TAG, "onFailure:---- $t")
            }
        })

    }

    //api call for send selected vehicle type
    private fun apiSendVehicleType() {
        Log.e(TAG, "apiSendVehicleType: $mStrDriverId $mStrVehicleId")
        if (ValidationUtils.isValidString(mStrDriverId) && ValidationUtils.isValidString(
                mStrVehicleId
            )
        ) {
            mApiSendVehicleTypeCall =
                Singleton.restClient.sendVehicleType(mStrDriverId!!, mStrVehicleId!!)
            mApiSendVehicleTypeCall!!.enqueue(object : Callback<ServerResponse<Any>> {
                override fun onResponse(
                    call: Call<ServerResponse<Any>>,
                    response: Response<ServerResponse<Any>>
                ) {
                    dismissProgress()
                    val resp = response.body()
                    if (resp != null && resp.status == 200) {
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_VEHICLE_TYPE, true)
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_VEHICLE_ID, mStrVehicleId)

                        //for save after select on already selected on Edit details from account
                        if (ValidationUtils.isValidObject(
                                mPreferenceUtils!!.driverData
                            )
                            && mPreferenceUtils!!.driverData?.driver_detail != null
                        ) {
                            mPreferenceUtils!!.driverData?.driver_detail?.vt_id = mStrVehicleId
                        }
                        if (ValidationUtils.isValidObject(
                                mPreferenceUtils!!.driverData
                            )
                            && mPreferenceUtils!!.driverData?.vehicle_detail != null
                        ) {
                            mPreferenceUtils!!.driverData?.vehicle_detail?.vt_id = mStrVehicleId
                        }
                        showToastMessage(activity, response.body()!!.message)

                   /*     var selectedValue: VehicleType? = null
                        mVehicleSelectTypeAdapter?.selectedValue = {
                            selectedValue = (it as VehicleType)
                        }
                        if (selectedValue != null) {*/
                            val intent = Intent(
                                this@SelectVehicleTypeActivity,
                                AddVehicleDetailsActivity::class.java
                            )
                                /*.putExtra(
                                "data" , selectedValue
                            )*/
                            animationIntent(this@SelectVehicleTypeActivity, intent)
                         //   finish()

                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@SelectVehicleTypeActivity,
                                    jObjError.getString("message")
                                )
                            } catch (e: Exception) {
                                showToastMessage(this@SelectVehicleTypeActivity, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Any>>, t: Throwable) {
                    AppLog.d(TAG, "onFailure:+++++$t")
                    dismissProgress()
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                }
            })
        }
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
        if (mApiGetVehicleTypeListCall != null && !mApiGetVehicleTypeListCall!!.isCanceled) {
            mApiGetVehicleTypeListCall!!.cancel()
        }
        if (mApiSendVehicleTypeCall != null && !mApiSendVehicleTypeCall!!.isCanceled) {
            mApiSendVehicleTypeCall!!.cancel()
        }
    }

    companion object {
        private val TAG = SelectVehicleTypeActivity::class.java.simpleName
    }
}