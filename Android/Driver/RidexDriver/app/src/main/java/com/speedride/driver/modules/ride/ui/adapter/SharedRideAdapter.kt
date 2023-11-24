package com.speedride.driver.modules.ride.ui.adapter

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.speedride.driver.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.speedride.driver.utils.ValidationUtils
import com.bumptech.glide.Glide
import com.speedride.driver.model.SharedRideRequestData
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.Utils

class SharedRideAdapter(private val mContext: Context, private val historyList: ArrayList<SharedRideRequestData.Data>, private val mListener:onCancelClick) :
    RecyclerView.Adapter<SharedRideAdapter.MyViewHolder>() {
    private var mHistoryList: ArrayList<SharedRideRequestData.Data>? = null

    init {
        mHistoryList = historyList
    }

    public fun replaceAll(historyList: List<SharedRideRequestData.Data>){
        mHistoryList!!.clear()
        mHistoryList!!.addAll(historyList)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var mImgUser: ImageView
        var mTxtUserName: TextView
        var mTxtDistance: TextView
        var mTxtTime: TextView
        var mBtnStartTrip: TextView
        var imgChat:ImageView

        init {
             mImgUser = itemView.findViewById(R.id.imgUserIcon)
             mTxtUserName = itemView.findViewById(R.id.txtUserName)
             mTxtTime = itemView.findViewById(R.id.txtTimes)
             mTxtDistance = itemView.findViewById(R.id.txtDistance)
             mBtnStartTrip = itemView.findViewById(R.id.btnStartTrip)
             imgChat = itemView.findViewById(R.id.imgChat)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_shared_ride_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val history = mHistoryList?.get(position)
        holder.mTxtUserName.text = history?.cusers?.name
        Log.d(TAG, "onBindViewHolder: getStatus" + history?.status + " Address " + history?.departure)
      //  Log.e("historyList", " historyList: " + mHistoryList!!.get(position).vehicle_detail?.model + " | " +  mHistoryList!!.get(position).km)

        //set trip status text view

//        val formatDblKm = java.lang.Double.valueOf(history.km + "km")
//        val formatStrKm = String.format("%.2f", formatDblKm)
//        holder.mTxtDistance.text = formatStrKm + "km"
        holder.mTxtDistance.text = history?.km+ " miles"
        if (ValidationUtils.isValidString(history?.created_at)) {
            val convertedDate =
                Utils.stringDateToFormatDateShared(history?.created_at)
            holder.mTxtTime.text = convertedDate.toString()
        }
        Glide.with(mContext).load(Common.UPLOAD_URL + (history?.cusers?.image))
            .placeholder(R.drawable.ic_driver_profile).into(holder.mImgUser)


        if (history?.status.equals("Pending")) {
            holder. mBtnStartTrip.setText(R.string.enter_passcode)

        } else if (history?.status.equals("Confirmed")) {
            holder. mBtnStartTrip.setText(R.string.start_trip)
        }else if (history?.status.equals("Riding")){
            holder. mBtnStartTrip.setText(R.string.complete_trip)
        }
        holder.mBtnStartTrip.setOnClickListener {
            mListener.onClicked(history!!)
        }
        holder.imgChat.setOnClickListener {
            mListener.onChatClick(history?.id.toString())
        }

    }


    override fun getItemCount(): Int {
        return mHistoryList?.size!!
    }

    companion object {
        private val TAG = SharedRideAdapter::class.java.simpleName
    }

    interface onCancelClick{
        fun onClicked(rideData:SharedRideRequestData.Data)
        fun onChatClick(rideId:String)
    }
}