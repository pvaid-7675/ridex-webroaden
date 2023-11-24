package com.speedride.driver.modules.userManagement.ui.fragment

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.speedride.driver.R
import com.speedride.driver.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_shift.*
import java.util.Calendar

class ShiftFragment : BaseFragment() {

    var mEdtStartTime:AppCompatEditText? = null
    var mEdtEndTime:AppCompatEditText? = null
    var mUpdateTime: AppCompatTextView? = null
    var mTimePicker: TimePickerDialog ? = null
    var selectedTime:String?=null
    override val layoutResourceId: Int
         get() = R.layout.fragment_shift

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }


    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun printLog(message: String?) {
        TODO("Not yet implemented")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shift, container, false)
    }

    override fun initView(view: View?) {
        mEdtStartTime = view?.findViewById(R.id.edtStartTime)
        mEdtEndTime = view?.findViewById(R.id.edtEndTime)
        mUpdateTime = view?.findViewById(R.id.tvUpdateShiftTime)
        edtStartTime.setOnClickListener {
            showTimeDialog();
        }
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
 fun showTimeDialog(){
     var currentTime = Calendar.getInstance()
     var hour = currentTime.get(Calendar.HOUR_OF_DAY)
     var minute = currentTime.get(Calendar.MINUTE)
     mTimePicker = TimePickerDialog(requireContext(), object : TimePickerDialog.OnTimeSetListener {
         override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
             selectedTime = String.format("%d : %d", hourOfDay, minute)
         }
     }, hour, minute, true)
     mTimePicker!!.show()
 }


}