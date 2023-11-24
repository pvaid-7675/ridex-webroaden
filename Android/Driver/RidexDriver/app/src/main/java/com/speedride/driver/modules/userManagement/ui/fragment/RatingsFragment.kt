package com.speedride.driver.modules.userManagement.ui.fragment

import android.content.Context

import com.speedride.driver.base.BaseFragment
import android.widget.TextView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.speedride.driver.modules.userManagement.ui.adapter.RatingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.modules.userManagement.dataModel.RatingDetail
import com.speedride.driver.modules.userManagement.dataModel.reviewdata
import android.widget.RatingBar
import com.speedride.driver.R
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.speedride.driver.utils.ValidationUtils
import androidx.recyclerview.widget.DefaultItemAnimator
import com.speedride.driver.rest.Singleton
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class RatingsFragment : BaseFragment() {
    private var mTxtRateAvarage: TextView? = null
    private var mTxtOneRating: TextView? = null
    private var mTxtTwoRating: TextView? = null
    private var mTxtThreeRating: TextView? = null
    private var mTxtFourRating: TextView? = null
    private var mTxtFiveRating: TextView? = null
    private var mProgressBarTotal: ProgressBar? = null
    private var mProgressBar1Star: ProgressBar? = null
    private var mProgressBar2Star: ProgressBar? = null
    private var mProgressBar3Star: ProgressBar? = null
    private var mProgressBar4Star: ProgressBar? = null
    private var mProgressBar5Star: ProgressBar? = null
    private var mRecyclerView: RecyclerView? = null
    private var mRatingAdapter: RatingAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mApiGetRatingListCall: Call<ServerResponse<RatingDetail>>? = null
    private var mStrDriverId: String? = null
    private var totalRate = 0f
    private var mScrollPagePos = 0
    private var isLoading = true
    private var mRatingListMain: MutableList<reviewdata>? = null
    private var mProgressBar: ProgressBar? = null
    private var mRatingBarTotal: RatingBar? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_ratings

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun initView(view: View?) {
        if (view != null) {
            init(view)
        }
        withPagination

        //get first page json data on first time
        if (mScrollPagePos == 0) {
            if (ValidationUtils.isInternetAvailable(activityContext!!)) {
                mProgressBar!!.visibility = View.VISIBLE
                apiGetRatingTypeList()
            } else {
                showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
            }
        }
    }

    private fun init(view: View) {
        mProgressBar = view.findViewById(R.id.progress_bar)
        mPreferenceUtils = PreferenceUtils.getInstance(requireActivity())
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mRatingListMain = ArrayList()
        mRecyclerView = view.findViewById(R.id.recyclerViewRatings)
        mTxtRateAvarage = view.findViewById(R.id.txtRate)
        mTxtOneRating = view.findViewById(R.id.txtTotalRate1)
        mTxtTwoRating = view.findViewById(R.id.txtTotalRate2)
        mTxtThreeRating = view.findViewById(R.id.txtTotalRate3)
        mTxtFourRating = view.findViewById(R.id.txtTotalRate4)
        mTxtFiveRating = view.findViewById(R.id.txtTotalRate5)
        mRatingBarTotal = view.findViewById(R.id.ratingBar)
        mProgressBarTotal = view.findViewById(R.id.progressBarTotal)
        mProgressBarTotal!!.setProgress(0)
        mProgressBar1Star = view.findViewById(R.id.progressBar1Star)
        mProgressBar1Star!!.setProgress(0)
        mProgressBar2Star = view.findViewById(R.id.progressBar2Star)
        mProgressBar2Star!!.setProgress(0)
        mProgressBar3Star = view.findViewById(R.id.progressBar3Star)
        mProgressBar3Star!!.setProgress(0)
        mProgressBar4Star = view.findViewById(R.id.progressBar4Star)
        mProgressBar4Star!!.setProgress(0)
        mProgressBar5Star = view.findViewById(R.id.progressBar5Star)
        mProgressBar5Star!!.setProgress(0)
        mRatingAdapter = RatingAdapter(activityContext!!, mRatingListMain!!)
        mLinearLayoutManager = LinearLayoutManager(activityContext)
        mRecyclerView!!.setLayoutManager(mLinearLayoutManager)
        mRecyclerView!!.setItemAnimator(DefaultItemAnimator())
        //    mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView!!.setAdapter(mRatingAdapter)

        /*  mProgressBar1Star.setProgress(manageRatingProgress(7));
        mProgressBar2Star.setProgress(manageRatingProgress(200));
        mProgressBar3Star.setProgress(manageRatingProgress(500));
        mProgressBar4Star.setProgress(manageRatingProgress(1111));
        mProgressBar5Star.setProgress(manageRatingProgress(330));

        mTxtOneRating.setText("7");
        mTxtTwoRating.setText("200");
        mTxtThreeRating.setText("500");
        mTxtFourRating.setText("1111");
        mTxtFiveRating.setText("330");

        int one = Integer.parseInt(mTxtOneRating.getText().toString());
        int two = Integer.parseInt(mTxtTwoRating.getText().toString());
        int three = Integer.parseInt(mTxtThreeRating.getText().toString());
        int four = Integer.parseInt(mTxtFourRating.getText().toString());
        int five = Integer.parseInt(mTxtFiveRating.getText().toString());

        int max[] = {one, two, three, four, five};
        String max_value = String.valueOf(getMaxValue(max));
        mTxtTotalRatingPersons.setText(max_value);

        if (totalRate < 6) {
            mRatingBarTotal.setRating(totalRate);

            String totRate = String.valueOf(totalRate);
            mTxtRateAvarage.setText(totRate);
        }*/
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
                                Log.d(TAG, "onScrolledPosition: $mScrollPagePos")
                                if (ValidationUtils.isInternetAvailable(activityContext!!)) {
                                    isLoading = false
                                    mProgressBar!!.visibility = View.VISIBLE
                                    apiGetRatingTypeList()
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

    // getting the maximum value
    fun getMaxValue(array: IntArray): Int {
        var maxValue = array[0]
        for (i in 1 until array.size) {
            if (array[i] > maxValue) {
                maxValue = array[i]
                totalRate = (i + 1).toFloat()
            }
        }
        return maxValue
    }

    // getting the miniumum value
    fun getMinValue(array: IntArray): Int {
        var minValue = array[0]
        for (i in 1 until array.size) {
            if (array[i] < minValue) {
                minValue = array[i]
            }
        }
        return minValue
    }

    private fun manageRatingProgress(rate: Int): Int {
        return if (rate > 0 && rate <= 10) {
            1
        } else if (rate > 10 && rate <= 20) {
            2
        } else if (rate > 20 && rate <= 30) {
            3
        } else if (rate > 30 && rate <= 40) {
            4
        } else if (rate > 40 && rate <= 50) {
            5
        } else if (rate > 50 && rate <= 100) {
            10
        } else if (rate > 100 && rate <= 200) {
            20
        } else if (rate > 200 && rate <= 300) {
            30
        } else if (rate > 300 && rate <= 400) {
            40
        } else if (rate > 400 && rate <= 500) {
            50
        } else if (rate > 500 && rate <= 600) {
            60
        } else if (rate > 600 && rate <= 700) {
            70
        } else if (rate > 700 && rate <= 800) {
            80
        } else if (rate > 800 && rate <= 900) {
            90
        } else if (rate > 900 && rate < 1000) {
            95
        } else if (rate > 1000) {
            100
        } else {
            0
        }
    }

    //api call for get vehicle type list
    private fun apiGetRatingTypeList() {
        mApiGetRatingListCall =
            Singleton.restClient.getRatingList(mStrDriverId, mScrollPagePos.toString())
        mApiGetRatingListCall!!.enqueue(object : Callback<ServerResponse<RatingDetail>> {
            override fun onResponse(
                call: Call<ServerResponse<RatingDetail>>,
                response: Response<ServerResponse<RatingDetail>>
            ) {
                dismissProgress()
                mProgressBar!!.visibility = View.GONE
                val resp: ServerResponse<*>? = response.body()
                Log.e("Rating",resp.toString())
                if (resp != null && resp.status == 200) {
                    isLoading = true
                    val ratingList = response.body()!!.data?.reviewdata
                    if (ValidationUtils.isValidList(ratingList)) {
                        val rating = response.body()!!.data?.average_rating
                        val formatRatings = String.format("%.1f", java.lang.Double.valueOf(rating))
                        mTxtRateAvarage!!.text = formatRatings
                        mRatingBarTotal!!.rating = formatRatings.toFloat()
                        val intRatings = formatRatings.toDouble().toInt()
                        mProgressBarTotal!!.progress = intRatings
                        val starcountList = response.body()!!
                            .data?.starcount
                        var reviewTotal = response.body()?.data?.total_rating_counts
                        mTxtOneRating?.text = reviewTotal?.rating_count_1
                        mTxtTwoRating?.text = reviewTotal?.rating_count_2
                        mTxtThreeRating?.text = reviewTotal?.rating_count_3
                        mTxtFourRating?.text = reviewTotal?.rating_count_4
                        mTxtFiveRating?.text = reviewTotal?.rating_count_5
                        if (ValidationUtils.isValidList(starcountList)) {
                            for (i in starcountList!!.indices) {
                                when (starcountList[i].star) {
                                    "1" -> {
                                        mProgressBar1Star!!.progress = manageRatingProgress(
                                            starcountList[i].count!!.toInt()
                                        )
                                        mTxtOneRating!!.text = starcountList[i].count
                                    }
                                    "2" -> {
                                        mProgressBar2Star!!.progress = manageRatingProgress(
                                            starcountList[i].count!!.toInt()
                                        )
                                        mTxtTwoRating!!.text = starcountList[i].count
                                    }
                                    "3" -> {
                                        mProgressBar3Star!!.progress = manageRatingProgress(
                                            starcountList[i].count!!.toInt()
                                        )
                                        mTxtThreeRating!!.text = starcountList[i].count
                                    }
                                    "4" -> {
                                        mProgressBar4Star!!.progress = manageRatingProgress(
                                            starcountList[i].count!!.toInt()
                                        )
                                        mTxtFourRating!!.text = starcountList[i].count
                                    }
                                    "5" -> {
                                        mProgressBar5Star!!.progress = manageRatingProgress(
                                            starcountList[i].count!!.toInt()
                                        )
                                        mTxtFiveRating!!.text = starcountList[i].count
                                    }
                                }
                            }
                        }
                        mRatingListMain!!.addAll(ratingList!!)
                        mRatingAdapter!!.notifyDataSetChanged()

                        //increase item size
                        mScrollPagePos = mScrollPagePos + 10
                    } else {
                        mRatingAdapter!!.notifyDataSetChanged()
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

            override fun onFailure(call: Call<ServerResponse<RatingDetail>>, t: Throwable) {
                dismissProgress()
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                Log.e(TAG, "onFailure: $t")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (mRatingListMain!!.size == 0) if (mScrollPagePos != 0) {
            mScrollPagePos = 0
            if (ValidationUtils.isInternetAvailable(activityContext!!)) {
                mProgressBar!!.visibility = View.VISIBLE
                apiGetRatingTypeList()
            } else {
                showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
            }
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

    private fun onCancelApiCall() {
        if (mApiGetRatingListCall != null && !mApiGetRatingListCall!!.isCanceled) {
            mApiGetRatingListCall!!.cancel()
        }
    }

    companion object {
        private val TAG = RatingsFragment::class.java.simpleName
    }
}