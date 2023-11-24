package com.speedride.driver.modules.userManagement.ui.fragment

import android.content.Context

import com.speedride.driver.base.BaseFragment
import com.speedride.driver.modules.ride.ui.activity.OnRideActivity
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.modules.ride.dataModel.CustomerRequestModel
import com.speedride.driver.R
import android.os.Bundle
import com.bumptech.glide.Glide
import android.os.Build
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.speedride.driver.modules.home.ui.activity.MainActivity
import com.speedride.driver.modules.ride.ui.fragment.OnTripFragment
import org.json.JSONObject
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class ReviewTripFragment : BaseFragment(), View.OnClickListener {
    private var mOnRideActivity: OnRideActivity? = null
    private var mTxtBtnRateNow: TextView? = null
    private var mTxtTotalFare: TextView? = null
    private var mTxtTotalDistance: TextView? = null
    private var mTxtUserName: TextView? = null
    private var mTxtRateTime: TextView? = null
    private var mTxtDistance: TextView? = null
    private var mImgUser: ImageView? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mRatingBar: RatingBar? = null
    var mEdtComment: EditText? = null
    private var mApiReviewRatingTripResponseCall: Call<ServerResponse<Object>>? = null
    private var mStrDriverId: String? = null
    private var mStrCustomerId: String? = null
    private var customerRequestModel: CustomerRequestModel? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_review_trip

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }
        mOnRideActivity = activity as OnRideActivity?
        mOnRideActivity!!.setTitle(resources.getString(R.string.review_your_trip))
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun initView(view: View?) {
        mPreferenceUtils = PreferenceUtils.getInstance(activityContext!!)
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        customerRequestModel = mPreferenceUtils!!.customerRideData
        mEdtComment = view?.findViewById(R.id.edtComment)
        mTxtBtnRateNow = view?.findViewById(R.id.txtRateNow)
        mTxtBtnRateNow!!.setOnClickListener(this)
        mTxtTotalFare = view?.findViewById(R.id.txtTotalFare)
        mTxtTotalDistance = view?.findViewById(R.id.txtTotalDistance)
        mTxtUserName = view?.findViewById(R.id.txtUserName)
        mTxtRateTime = view?.findViewById(R.id.txtRateTime)
        mTxtDistance = view?.findViewById(R.id.txtDistance)
        mImgUser = view?.findViewById(R.id.imgUserIcon)
        if (ValidationUtils.isValidObject(customerRequestModel)) {
            if (ValidationUtils.isValidString(customerRequestModel!!.customer_id)) {
                mStrCustomerId = customerRequestModel!!.customer_id
            }
            if (ValidationUtils.isValidString(customerRequestModel!!.charge)) {
                val formatDbl = customerRequestModel!!.charge?.let { java.lang.Double.valueOf(it) }
                val formatStr = String.format("%.2f", formatDbl)
                mTxtTotalFare!!.setText("$$formatStr")
            }
            if (ValidationUtils.isValidString(customerRequestModel!!.km)) {
                mTxtTotalDistance!!.setText(customerRequestModel!!.km + " miles.")
            }
            if (ValidationUtils.isValidString(customerRequestModel!!.km)) {
                mTxtDistance!!.setText(customerRequestModel!!.km + " miles.")
            }
            if (ValidationUtils.isValidString(customerRequestModel!!.name)) {
                mTxtUserName!!.setText(customerRequestModel!!.name)
            }
            if (ValidationUtils.isValidString(customerRequestModel!!.requestdatetime)) {
                val convertedDate =
                    Utils.stringDateToFormatDate(customerRequestModel!!.requestdatetime)
                val strDate = Utils.dateFormatToStringDateForEarning(convertedDate)
                mTxtRateTime!!.setText(strDate)
            }
            if (ValidationUtils.isValidString(customerRequestModel!!.image)) {
                Glide.with(this).load(Common.UPLOAD_URL + customerRequestModel!!.image)
                    .placeholder(R.drawable.ic_driver_profile).into(mImgUser!!)
            }
        }
        mRatingBar = view?.findViewById(R.id.ratingTaxi)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                val progressDrawable = mRatingBar!!.getProgressDrawable()
                if (progressDrawable != null) {
                    DrawableCompat.setTint(
                        progressDrawable,
                        ContextCompat.getColor(activityContext!!, R.color.colorAccent)
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    /* else if (!ValidationUtils.isValidString(mEdtComment)) {
            mEdtComment.setError(getString(R.string.enter_comment));
            mEdtComment.requestFocus();
            return false;
        } else if (mEdtComment.getText().toString().length() < 2) {
            mEdtComment.setError(getString(R.string.enter_minimum_2_character));
            mEdtComment.requestFocus();
            return false;
        }*/
    private val isValidate: Boolean
        private get() {
            if (mRatingBar!!.rating == 0f) {
                showToastMessage(requireActivity(), "Please rate ride")
                return false
            } /* else if (!ValidationUtils.isValidString(mEdtComment)) {
            mEdtComment.setError(getString(R.string.enter_comment));
            mEdtComment.requestFocus();
            return false;
        } else if (mEdtComment.getText().toString().length() < 2) {
            mEdtComment.setError(getString(R.string.enter_minimum_2_character));
            mEdtComment.requestFocus();
            return false;
        }*/
            return true
        }

    //api call for add review on trip finish
    private fun apiRatingReviewTrip() {
        mOnRideActivity!!.hideKeyboard(requireActivity())
        val comment: String
        comment = if (ValidationUtils.isValidString(
                mEdtComment!!.text.toString().trim { it <= ' ' })
        ) mEdtComment!!.text.toString().trim { it <= ' ' } else {
            ""
        }
        mApiReviewRatingTripResponseCall = mStrCustomerId?.let {
            Singleton.restClient
                .reviewCommentRateTrip(
                    it,mStrDriverId,
                    mRatingBar!!.rating.toString(),
                    mPreferenceUtils!!.customerRideData!!.bookid,
                    comment
                )
        }
        mApiReviewRatingTripResponseCall!!.enqueue(object : Callback<ServerResponse<Object>?> {
            override fun onResponse(
                call: Call<ServerResponse<Object>?>,
                response: Response<ServerResponse<Object>?>
            ) {
                try {
                    val resp = response.body()

                    val gson = Gson()
                    val json = gson.toJson(resp)
                    val jObject = JSONObject(json)
                    dismissProgress()
                    if (jObject.getInt("status") == Common.STATUS_200) {
                        showToastMessage(activity!!,jObject.getString("message"))
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, false)
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_ON_TRIP, false)
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, false)
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_REVIEW_TRIP, false)
                        val intentHome = Intent(activity, MainActivity::class.java)
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        animationIntent(activity!!, intentHome)
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(context!!, jObjError.getString("message"))
                            } catch (e: Exception) {
                                showToastMessage(context!!, e.message!!)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ServerResponse<Object>?>, t: Throwable) {
                AppLog.d(TAG, "onFailure: $t")
                dismissProgress()
            }
        })
    }

    override fun onClick(v: View) {
        if (v.id == R.id.txtRateNow) {
            if (isValidate) if (ValidationUtils.isInternetAvailable(requireContext())) {
                showProgress()
                apiRatingReviewTrip()
            } else {
                showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
                dismissProgress()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        dismissProgress()
        onCancelApiCall()
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, false)
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_ON_TRIP, false)
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COLLECT_CASH, false)
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_REVIEW_TRIP, false)
        val intentHome = Intent(activity, MainActivity::class.java)
        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        animationIntent(requireActivity(), intentHome)
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancelApiCall()
        dismissProgress()
    }

    private fun onCancelApiCall() {
        if (mApiReviewRatingTripResponseCall != null && !mApiReviewRatingTripResponseCall!!.isCanceled) {
            mApiReviewRatingTripResponseCall!!.cancel()
        }
    }

    companion object {
        private val TAG = OnTripFragment::class.java.simpleName
    }
}