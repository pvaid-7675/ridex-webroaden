package com.speedride.driver.modules.userManagement.ui.fragment

import android.content.Context

import com.speedride.driver.base.BaseFragment
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.speedride.driver.modules.earning.ui.adapter.EarningAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.modules.earning.dataModel.EarningDetail
import com.speedride.driver.modules.earning.dataModel.Earning
import android.widget.ProgressBar
import com.speedride.driver.R
import android.os.Bundle
import com.speedride.driver.utils.ValidationUtils
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.Spannable
import androidx.recyclerview.widget.DefaultItemAnimator
import android.text.SpannableString
import android.util.Log
import android.view.View
import org.json.JSONObject
import android.widget.Toast
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class TodayEarningFragment : BaseFragment() {
    private var mTxtMyEarning: TextView? = null
    private var mTxtSpendTime: TextView? = null
    private var mTxtCompletedTrip: TextView? = null
    private var mRecyclerView: RecyclerView? = null
    private var mEarningAdapter: EarningAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mApiGetEarningListCall: Call<ServerResponse<EarningDetail>>? = null
    private var mStrDriverId: String? = null
    private var mScrollPagePos = 0
    private var isLoading = true
    private var isFirstTime = false
    private var mRatingListMain: MutableList<Earning>? = null
    private var mProgressBar: ProgressBar? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_earning

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun initView(view: View?) {
        if (view != null) {
            init(view)
        }
        withPagination

        //get first page json data on first time
        if (mScrollPagePos == 0) {
            if (ValidationUtils.isInternetAvailable(activityContext!!)) {
                //showProgress();
                isFirstTime = true
                mProgressBar!!.visibility = View.VISIBLE
                apiGetEarningTypeList()
            } else {
                showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
            }
        }
        val s = "DIFFERENT"
        val spanTxt = SpannableStringBuilder(s)
        spanTxt.setSpan(
            RelativeSizeSpan(0.2f),
            s.length - 2,
            s.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        Log.e(TAG, "initView: $spanTxt")
    }

    private fun init(view: View) {
        mProgressBar = view.findViewById(R.id.progress_bar)
        mPreferenceUtils = PreferenceUtils.getInstance(requireActivity())
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mRecyclerView = view.findViewById(R.id.recyclerViewEarnings)
        mTxtMyEarning = view.findViewById(R.id.txtMyEarning)
        mTxtSpendTime = view.findViewById(R.id.txtSpendTime)
        mTxtCompletedTrip = view.findViewById(R.id.txtCompletedTrip)
        mRatingListMain = ArrayList()
        mEarningAdapter = EarningAdapter(activityContext!!, mRatingListMain!!, javaClass.simpleName)
        mLinearLayoutManager = LinearLayoutManager(activityContext)
        mRecyclerView!!.setLayoutManager(mLinearLayoutManager)
        mRecyclerView!!.setItemAnimator(DefaultItemAnimator())
        //    mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView!!.setAdapter(mEarningAdapter)
    }//check for scroll down

    // Pagination and lazy loading
    private val withPagination: Unit
        private get() {
            // Pagination and lazy loading
            mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) { //check for scroll down
                        val visibleItemCount = mLinearLayoutManager!!.childCount
                        val totalItemCount = mLinearLayoutManager!!.itemCount
                        val firstVisibleItemPosition =
                            mLinearLayoutManager!!.findFirstVisibleItemPosition()
                        val lastVisibleItemPosition =
                            mLinearLayoutManager!!.findLastVisibleItemPosition()
                        Log.d("visibleItemCount", "onScrolled: $visibleItemCount")
                        Log.d("totalItemCount", "onScrolled: $totalItemCount")
                        Log.d("firstVisibleItemPos", "onScrolled: $firstVisibleItemPosition")
                        Log.d("lastVisibleItemPosition", "onScrolled: $lastVisibleItemPosition")
                        if (isLoading) {
                            if (firstVisibleItemPosition > 0 && lastVisibleItemPosition == totalItemCount - 1) {
                                Log.d("MainAct", "onScrolledPosition: $mScrollPagePos")
                                if (ValidationUtils.isInternetAvailable(activityContext!!)) {
                                    isLoading = false
                                    isFirstTime = false
                                    mProgressBar!!.visibility = View.VISIBLE
                                    apiGetEarningTypeList()
                                } else {
                                    showToastMessage(
                                        activity!!,
                                        activity!!.resources.getString(R.string.network_error)
                                    )
                                }
                            }
                        }
                    }
                }
            })
        }

    //api call for get vehicle type list
    private fun apiGetEarningTypeList() {
        mApiGetEarningListCall = Singleton.restClient
            .getEarningList(mScrollPagePos.toString(), Common.EARNING_TYPE_TODAY)
        mApiGetEarningListCall!!.enqueue(object : Callback<ServerResponse<EarningDetail>> {
            override fun onResponse(
                call: Call<ServerResponse<EarningDetail>>,
                response: Response<ServerResponse<EarningDetail>>
            ) {
                dismissProgress()
                mProgressBar!!.visibility = View.GONE
                val resp: ServerResponse<*>? = response.body()
                if (resp != null && resp.status == 200) {
                    isLoading = true

                    //AppLog.d(TAG, "onResponse:" + new Gson().toJson(response));
                    val earningList = response.body()!!.data?.earning_list
                    if (ValidationUtils.isValidObject(response.body()!!.data)) {
                        if (ValidationUtils.isValidList(earningList)) {
                            val total_earning = response.body()!!.data?.all_cost
                            val ss1 = SpannableString("Rs.")
                            ss1.setSpan(RelativeSizeSpan(0.6f), 0, 3, 0) // set size
                            //ss1.setSpan(new ForegroundColorSpan(Color.RED), 0, 3, 0);// set color
                            val formatDbl = java.lang.Double.valueOf(total_earning)
                            val formatStr = String.format("%.2f", formatDbl)
                            //mTxtMyEarning!!.text = TextUtils.concat(formatStr, ss1)
                            mTxtMyEarning!!.text = "$$formatStr"
                            mPreferenceUtils!!.save(
                                PreferenceUtils.PREF_KEY_TODAY_TOTAL_EARNINGS,
                                formatStr
                            )
                            val spend_time = response.body()!!.data?.total_times
                            mTxtSpendTime!!.text = spend_time
                            mPreferenceUtils!!.save(
                                PreferenceUtils.PREF_KEY_TODAY_TOTAL_SPEND_TIME,
                                spend_time
                            )
                            val total_count = response.body()!!.data?.total_count
                            mTxtCompletedTrip!!.text = total_count
                            mPreferenceUtils!!.save(
                                PreferenceUtils.PREF_KEY_TODAY_TOTAL_TRIPS,
                                total_count
                            )
                            if (isFirstTime) if (!mRatingListMain!!.isEmpty()) {
                                mRatingListMain!!.clear()
                            }
                            mRatingListMain!!.addAll(earningList!!)
                            mEarningAdapter!!.notifyDataSetChanged()

                            //increase item size
                            mScrollPagePos = mScrollPagePos + 10
                        }
                    } else {
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_TODAY_TOTAL_EARNINGS, "")
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_TODAY_TOTAL_SPEND_TIME, "")
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_TODAY_TOTAL_TRIPS, "")
                    }
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
            }

            override fun onFailure(call: Call<ServerResponse<EarningDetail>>, t: Throwable) {
                dismissProgress()
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                Log.e(TAG, "onFailure: $t")
            }
        })
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (!isFirstTime) {
            mScrollPagePos = 0
            if (ValidationUtils.isInternetAvailable(activityContext!!)) {
                isFirstTime = true
                mProgressBar!!.visibility = View.VISIBLE
                apiGetEarningTypeList()
            } else {
                showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
                mTxtMyEarning!!.text = "0"
                mTxtSpendTime!!.text = "0"
                mTxtCompletedTrip!!.text = "0"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isFirstTime = false
        dismissProgress()
        onCancelApiCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancelApiCall()
        dismissProgress()
    }

    private fun onCancelApiCall() {
        if (mApiGetEarningListCall != null && !mApiGetEarningListCall!!.isCanceled) {
            mApiGetEarningListCall!!.cancel()
        }
    }

    companion object {
        private val TAG = TodayEarningFragment::class.java.simpleName
    }
}