package com.speedride.driver.modules.userManagement.ui.activity

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.google.gson.Gson
import com.speedride.driver.R
import com.speedride.driver.base.BaseActivity
import com.speedride.driver.base.BaseFragment.Companion.dismissProgress
import com.speedride.driver.modules.chat.ChatActivity
import com.speedride.driver.modules.userManagement.dataModel.ShiftTimeGetModel
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.AppLog
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.PreferenceUtils
import kotlinx.android.synthetic.main.activity_shift_time.*
import kotlinx.android.synthetic.main.fragment_shift.edtStartTime
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ShiftTimeActivity : BaseActivity(),View.OnClickListener {

    var mEdtStartTime: AppCompatEditText? = null
    var mEdtEndTime: AppCompatEditText? = null
    var mUpdateTime: AppCompatTextView? = null
    var mTimePicker: TimePickerDialog? = null
    var selectedStartTime:String?=null
    var selectedEndTime:String?=null
    var mPreferenceUtils:PreferenceUtils?= null
    private var mApiGetShiftTime: Call<ShiftTimeGetModel>? = null
    private var mApiUpdateShiftTime: Call<Object>? = null

    override val activity: AppCompatActivity?
        get() = this@ShiftTimeActivity
    override val actionTitle: String?
        get() = resources.getString(R.string.shift_time_title)
    override val isHomeButtonEnable: Boolean
        get() = true

    fun setTitle(title: String?) {
        setToolbarTitle(title)
    }
    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shift_time)
        init()
        apiGetShiftTime()

    }

    @SuppressLint("ResourceAsColor")
    fun init(){
        setToolbar(findViewById(R.id.appbar))
        mEdtStartTime =findViewById(R.id.edtStartTime)
        mEdtEndTime = findViewById(R.id.edtEndTime)
        mUpdateTime = findViewById(R.id.tvUpdateShiftTime)
        mPreferenceUtils = PreferenceUtils.getInstance(this@ShiftTimeActivity)
        if(mPreferenceUtils?.driverData?.driver_type.equals("Contract")){
            tvUpdateShiftTime.isEnabled = true
            tvUpdateShiftTime.setBackgroundResource(R.drawable.bg_button)
        }else{
            tvUpdateShiftTime.isEnabled = false
            tvUpdateShiftTime.setBackgroundResource(R.drawable.bg_button_disable)
        }
        mEdtStartTime?.setOnClickListener(this)
        mEdtEndTime?.setOnClickListener(this)
        mUpdateTime?.setOnClickListener(this)
    }

    override fun initView(view: View?) {

    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    fun showTimeDialogStartTime(){
        var currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)
        var second = currentTime.get(Calendar.SECOND)
        mTimePicker = TimePickerDialog(activity,
            { view, hourOfDay, minute ->
                edtStartTime.setText(String.format("%d:%d:%d", hourOfDay, minute, second))

                selectedStartTime = String.format("%d:%d:%d", hourOfDay, minute, second)
            }, hour, minute, true)
        mTimePicker!!.show()
    }

    fun showTimeDialogEndTime(){
        var currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)
        var second = currentTime.get(Calendar.SECOND)
        mTimePicker = TimePickerDialog(activity, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                edtEndTime.setText(String.format("%d:%d:%d", hourOfDay, minute, second))
                selectedEndTime = String.format("%d:%d:%d", hourOfDay, minute, second)
            }
        }, hour, minute, true)
        mTimePicker!!.show()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.edtStartTime ->{
                hideKeyboard(this)
                showTimeDialogStartTime()
            }
            R.id.edtEndTime ->{
                hideKeyboard(this)
                showTimeDialogEndTime()
            }
            R.id.tvUpdateShiftTime ->{
                if(selectedStartTime!=null && selectedEndTime!=null){
                     apiUpdateShiftTime()
                }else{
                    showToastMessage(activity,"Please select Shift time")
                }
            }
        }

    }

    private fun apiGetShiftTime() {
        hideKeyboard(this)
        mApiGetShiftTime = Singleton.restClient.driverShiftTime(mPreferenceUtils?.driverData?.id)
        mApiGetShiftTime!!.enqueue(object : Callback<ShiftTimeGetModel> {
            override fun onResponse(
                call: Call<ShiftTimeGetModel>,
                response: Response<ShiftTimeGetModel>
            ) {
                val resp: ShiftTimeGetModel? = response.body()
                dismissProgress()
                if (resp != null && resp.status == 200) {
                    edtStartTime.setText(resp.data.start_time)
                    edtEndTime.setText(resp.data.end_time)

                } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@ShiftTimeActivity,
                                    jObjError.getString("message")
                                )
                                AppLog.d(
                                    "ShiftTimeActivity.TAG",
                                    "onResponse:Login Msg " + jObjError.getString("message")
                                )
                            } catch (e: Exception) {
                                showToastMessage(this@ShiftTimeActivity, e.message!!)
                            }
                        }
                    }
            }
           override  fun onFailure(call: Call<ShiftTimeGetModel>, t: Throwable) {
                AppLog.d("TAG", "onFailure: $t")
                dismissProgress()
            }

        })
    }

    private fun apiUpdateShiftTime() {
        hideKeyboard(this)
        mApiUpdateShiftTime = Singleton.restClient.updateShiftTime(mPreferenceUtils?.driverData?.id,selectedStartTime,selectedEndTime)
        mApiUpdateShiftTime!!.enqueue(object : Callback<Object> {
            override fun onResponse(
                call: Call<Object>,
                response: Response<Object>
            ) {
             //   val resp: ShiftTimeGetModel? = response.body()
                dismissProgress()
                try {
                    val resp1: Any? = response.body()
                    val gson = Gson()
                    val json = gson.toJson(resp1)
                    val jObject = JSONObject(json)
                    if (jObject.getInt("status") == Common.STATUS_200) {
                        showToastMessage(activity,jObject.getString("message"))
                        apiGetShiftTime()
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(
                                    this@ShiftTimeActivity,
                                    jObjError.getString("message")
                                )
                                AppLog.d(
                                    "ShiftTimeActivity.TAG",
                                    "onResponse:Login Msg " + jObjError.getString("message")
                                )
                            } catch (e: Exception) {
                                showToastMessage(this@ShiftTimeActivity, e.message!!)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            override  fun onFailure(call: Call<Object>, t: Throwable) {
                AppLog.d("TAG", "onFailure: $t")
                dismissProgress()
            }

        })
    }
    companion object {
        fun setTitle(chatActivity: ChatActivity, title: String?) {
            chatActivity.setToolbarTitle(title)
        }
    }

}