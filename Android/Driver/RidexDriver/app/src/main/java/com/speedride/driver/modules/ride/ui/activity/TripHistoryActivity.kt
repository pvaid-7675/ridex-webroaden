package com.speedride.driver.modules.ride.ui.activity


import com.speedride.driver.base.BaseActivity
import android.app.ProgressDialog
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.speedride.driver.modules.ride.ui.adapter.HistoryAdapter
import com.speedride.driver.modules.ride.dataModel.HistoryDetail
import androidx.recyclerview.widget.LinearLayoutManager
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.model.ServerResponse
import android.widget.ProgressBar
import android.os.Bundle
import android.util.Log
import android.view.View
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.utils.ValidationUtils
import android.widget.Toast
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import org.json.JSONObject

import com.speedride.driver.rest.Singleton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.ArrayList

class TripHistoryActivity : BaseActivity() {
    private var mProgressDialog: ProgressDialog? = null
    private var mRecyclerView: RecyclerView? = null
    private var mHistoryAdapter: HistoryAdapter? = null
    private var mHistoryList: MutableList<HistoryDetail>? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mApiGetTripHistoryListCall: Call<ServerResponse<List<HistoryDetail>>>? = null
    private var mStrDriverId: String? = null
    private var mScrollPagePos = 0
    private var isLoading = true
    private var mProgressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_history)
        initView(null)
    }

    override val activity: AppCompatActivity
        get() = this@TripHistoryActivity
    override val actionTitle: String
        get() = resources.getString(R.string.history)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun initView(view: View?) {
        init()
        withPagination

        //get first page json data on first time
        if (mScrollPagePos == 0) {
            if (ValidationUtils.isInternetAvailable(this)) {
                showProgress()
                apiGetTripHistoryList()
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error))
            }
        }
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun init() {
        setToolbar((findViewById<View>(R.id.rlToolbar) as RelativeLayout))
        mProgressBar = findViewById(R.id.progress_bar)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))
        mPreferenceUtils = PreferenceUtils.getInstance(activity)
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mRecyclerView = findViewById(R.id.rvHistory)
        mHistoryList = ArrayList()
        mHistoryAdapter = HistoryAdapter(this, mHistoryList as ArrayList<HistoryDetail>)
        mLinearLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.setLayoutManager(mLinearLayoutManager)
        mRecyclerView!!.setItemAnimator(DefaultItemAnimator())
        //    mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView!!.setAdapter(mHistoryAdapter)

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
                                if (ValidationUtils.isInternetAvailable(this@TripHistoryActivity)) {
                                    isLoading = false
                                    showProgress()
                                    mProgressBar!!.visibility = View.VISIBLE
                                    apiGetTripHistoryList()
                                } else {
                                    showToastMessage(
                                        activity,
                                        activity.resources.getString(R.string.network_error)
                                    )
                                }
                            }
                        }
                    }
                }
            })
        }

    //api call for get vehicle type list
    private fun apiGetTripHistoryList() {
        mApiGetTripHistoryListCall =
            Singleton.restClient.getHistoryList(mScrollPagePos.toString())
        mApiGetTripHistoryListCall!!.enqueue(object : Callback<ServerResponse<List<HistoryDetail>>> {
            override fun onResponse(
                call: Call<ServerResponse<List<HistoryDetail>>>,
                response: Response<ServerResponse<List<HistoryDetail>>>
            ) {
                dismissProgress()
                mProgressBar!!.visibility = View.GONE
                val resp: ServerResponse<*>? = response.body()
                Log.d("historyList", " resp " + resp!!.data.toString())
                if (resp != null && resp.status == 200) {
                    isLoading = true
                    val historyList = response.body()!!.data


                    if (ValidationUtils.isValidList(historyList)) {
                        mHistoryList!!.addAll(historyList!!)
                        mHistoryAdapter!!.notifyDataSetChanged()
                        //increase item size
                        mScrollPagePos = mScrollPagePos + 10
                    } else {
                        if (mScrollPagePos == 0) showToastMessage(activity, "No history found.")
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(
                                this@TripHistoryActivity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            showToastMessage(this@TripHistoryActivity, e.message!!)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ServerResponse<List<HistoryDetail>>>, t: Throwable) {
                dismissProgress()
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                Log.e(TAG, "onFailure: $t")
            }
        })
    }



    fun showProgress() {
        if (mProgressDialog != null) mProgressDialog!!.show()
    }

    fun dismissProgress() {
        if (mProgressDialog != null) mProgressDialog!!.dismiss()
    }

    private fun onCancelApiCall() {
        if (mApiGetTripHistoryListCall != null && !mApiGetTripHistoryListCall!!.isCanceled) {
            mApiGetTripHistoryListCall!!.cancel()
        }
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

    companion object {
        private val TAG = TripHistoryActivity::class.java.simpleName
    }
}