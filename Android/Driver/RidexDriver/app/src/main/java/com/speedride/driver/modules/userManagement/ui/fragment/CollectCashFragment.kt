package com.speedride.driver.modules.userManagement.ui.fragment

import android.content.Context

import com.speedride.driver.base.BaseFragment
import com.speedride.driver.modules.ride.ui.activity.OnRideActivity
import android.widget.TextView
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.modules.ride.dataModel.CustomerRequestModel
import com.speedride.driver.R
import android.os.Bundle
import android.util.Log
import android.view.View
import com.speedride.driver.utils.ValidationUtils

/**
 * A simple [Fragment] subclass.
 */
class CollectCashFragment : BaseFragment(), View.OnClickListener {
    private var onRideActivity: OnRideActivity? = null
    private var mTxtBtnCashCollect: TextView? = null
    private var mTxtTotalFare: TextView? = null
    private var mTxtTotalDistance: TextView? = null
    private var mTxtPickUpAddress: TextView? = null
    private var mTxtDropAddress: TextView? = null
    private var mTxtTripFare: TextView? = null
    private var preferenceUtils: PreferenceUtils? = null
    private var customerRequestModel: CustomerRequestModel? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_collect_cash

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        view?.let { initView(it) }
        onRideActivity = activity as OnRideActivity?
        onRideActivity!!.setTitle(resources.getString(R.string.collect_cash))
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun initView(view: View?) {
        preferenceUtils = activityContext?.let { PreferenceUtils.getInstance(it) }
        customerRequestModel = preferenceUtils!!.customerRideData
        mTxtPickUpAddress = view?.findViewById(R.id.txtPickUpAddres)
        mTxtDropAddress = view?.findViewById(R.id.txtDropAddres)
        mTxtTotalFare = view?.findViewById(R.id.txtTotalFarec)
        mTxtTotalDistance = view?.findViewById(R.id.txtTotalDistance)
        mTxtTripFare = view?.findViewById(R.id.txtRideFarec)
        if (ValidationUtils.isValidString(customerRequestModel!!.pickup)) mTxtPickUpAddress!!.setText(
            customerRequestModel!!.pickup
        )
        if (ValidationUtils.isValidString(customerRequestModel!!.departure)) mTxtDropAddress!!.setText(
            customerRequestModel!!.departure
        )

        if (ValidationUtils.isValidObject(customerRequestModel)) {
            customerRequestModel?.charge?.let { Log.e("customerRequestModel?.charge Collect cash", it) }

            if (ValidationUtils.isValidString(customerRequestModel!!.charge)) {
                val formatDbl = java.lang.Double.valueOf(customerRequestModel!!.charge)
                val formatStr = String.format("%.2f", formatDbl)
                Log.d(TAG, "initView:dbl getCharge" + customerRequestModel!!.charge)
                Log.d(TAG, "initView: getCharge$formatStr")
                mTxtTotalFare!!.setText("$$formatStr")
                mTxtTripFare!!.setText("$$formatStr")
            }
            //Charge it give some time 0 thats why i set est price
            /*if (ValidationUtils.isValidString(customerRequestModel!!.estprice)) {
                val formatDbl = java.lang.Double.valueOf(customerRequestModel!!.estprice)
                val formatStr = String.format("%.2f", formatDbl)
                Log.d(TAG, "initView:dbl getCharge" + customerRequestModel!!.estprice)
                Log.d(TAG, "initView: getCharge$formatStr")
                mTxtTotalFare!!.setText("$$formatStr")
                mTxtTripFare!!.setText("$$formatStr")
            }*/
            if (ValidationUtils.isValidString(customerRequestModel!!.km)) {
                val formatDbl = java.lang.Double.valueOf(customerRequestModel!!.km)
                val formatStr = String.format("%.2f", formatDbl)

                mTxtTotalDistance!!.text = "$formatStr miles."
            }
        }
        mTxtBtnCashCollect = view?.findViewById(R.id.btnCashCollect)
        mTxtBtnCashCollect!!.setOnClickListener(this)
    }

    override fun showToastMessage(context: Context?, message: String?) {}
    override fun onClick(v: View) {
        if (v.id == R.id.btnCashCollect) {
            preferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, true)
            requireActivity().finish()
            animationIntent(requireActivity(), requireActivity().intent)
        }
    }

    companion object {
        private val TAG = CollectCashFragment::class.java.simpleName
    }
}