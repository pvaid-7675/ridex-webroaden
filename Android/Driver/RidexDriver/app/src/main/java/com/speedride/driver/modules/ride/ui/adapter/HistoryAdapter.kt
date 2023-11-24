package com.speedride.driver.modules.ride.ui.adapter

import android.content.Context
import android.util.Log
import com.speedride.driver.modules.ride.dataModel.HistoryDetail
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.speedride.driver.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.speedride.driver.utils.ValidationUtils
import com.bumptech.glide.Glide
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.Utils

class HistoryAdapter(private val mContext: Context, private val mHistoryList: List<HistoryDetail>) :
    RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var mImgUser: ImageView
        var mTxtUserName: TextView
        var mTxtCarName: TextView
        var mTxtStatus: TextView
        var mTxtCarNumber: TextView
        var mTxtKm: TextView
        var mTxtDuration: TextView
        var mTxtPrice: TextView
        var mTxtTime: TextView
        var mTxtAddress: TextView
        var mTxtDestination: TextView

        init {
             mImgUser = itemView.findViewById(R.id.img_Driver)
            mTxtUserName = itemView.findViewById(R.id.txtUserName)
             mTxtCarName = itemView.findViewById(R.id.txtCar_Name)
            mTxtStatus = itemView.findViewById(R.id.txtStatus)
             mTxtCarNumber = itemView.findViewById(R.id.txtCarNumber)
            mTxtKm = itemView.findViewById(R.id.txtKm)
            mTxtDuration = itemView.findViewById(R.id.txtDuration)
            mTxtPrice = itemView.findViewById(R.id.txtPrice)
            mTxtTime = itemView.findViewById(R.id.txtTime)
            mTxtAddress = itemView.findViewById(R.id.txvSource)
            mTxtDestination = itemView.findViewById(R.id.tvDestination)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_history_new, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val history = mHistoryList[position]
        holder.mTxtUserName.text = history.cusers?.name
         holder.mTxtCarName.text = history.dvehicle?.brand
         holder.mTxtCarNumber.text = history.dvehicle?.number
        Log.d(TAG, "onBindViewHolder: getStatus" + history.status + " Address " + history.departure)
      //  Log.e("historyList", " historyList: " + mHistoryList!!.get(position).vehicle_detail?.model + " | " +  mHistoryList!!.get(position).km)

        //set trip status text view
        if (history.status == Common.TRIP_COMPLETED) {
            holder.mTxtStatus.text = Common.TRIP_COMPLETED
            holder.mTxtStatus.setBackgroundResource(R.drawable.bg_rounded_corner_green)
        } else {
            holder.mTxtStatus.setBackgroundResource(R.drawable.bg_rounded_corner_red)
        }
//        val formatDblKm = java.lang.Double.valueOf(history.km + "km")
//        val formatStrKm = String.format("%.2f", formatDblKm)
//        holder.mTxtKm.text = formatStrKm + "km"
        holder.mTxtKm.text = history.km
        holder.mTxtDuration.text = history.estimate_time
        val formatDbl = java.lang.Double.valueOf(history.charge)
        val formatStr = String.format("%.2f", formatDbl)
        holder.mTxtPrice.text = "$$formatStr"

        if (ValidationUtils.isValidString(history.created_at)) {
            val convertedDate = Utils.stringDateToFormatDate(history.created_at)
            val strDate = Utils.dateFormatToStringDateForRating(convertedDate)
            val strTime = Utils.dateFormatToStringDateForEarning(convertedDate)
            holder.mTxtTime.text = history.start_time
        }

        holder.mTxtAddress.text = history.pickup
        holder.mTxtDestination.text = history.departure
        Glide.with(mContext).load(Common.UPLOAD_URL + (history.cusers?.image))
            .placeholder(R.drawable.ic_driver_profile).into(holder.mImgUser)
    }

    override fun getItemCount(): Int {
        return mHistoryList.size
    }

    companion object {
        private val TAG = HistoryAdapter::class.java.simpleName
    }
}