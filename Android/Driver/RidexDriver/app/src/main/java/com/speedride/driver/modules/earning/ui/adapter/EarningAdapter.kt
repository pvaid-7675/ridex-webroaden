package com.speedride.driver.modules.earning.ui.adapter

import android.content.Context
import com.speedride.driver.modules.earning.dataModel.Earning
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.speedride.driver.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.speedride.driver.utils.ValidationUtils
import com.speedride.driver.modules.userManagement.ui.fragment.TodayEarningFragment
import com.bumptech.glide.Glide
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.Utils

/**
 * Created by MTPC-153 on 3/7/2018.
 */
class EarningAdapter(
    private val mContext: Context,
    private val earningList: List<Earning>,
    private val earningClassName: String
) : RecyclerView.Adapter<EarningAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtUserName: TextView
        var txtRateTime: TextView
        var txtEarning: TextView
        var txtPaymentMethodEarning: TextView
        var imgUser: ImageView

        init {
            txtUserName = itemView.findViewById(R.id.txtUserName)
            txtRateTime = itemView.findViewById(R.id.txtRateTime)
            txtEarning = itemView.findViewById(R.id.txtEarning)
            txtPaymentMethodEarning = itemView.findViewById(R.id.txtPaymentMethodEarning)
            imgUser = itemView.findViewById(R.id.imgUserIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_earnings, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val earning = earningList[position]
        if (earning != null) {
            holder.txtUserName.text = earning.cusers?.name
            holder.txtPaymentMethodEarning.text = earning.paymode
            val formatDbl = java.lang.Double.valueOf(earning.charge)
            val formatStr = String.format("%.2f", formatDbl)
            holder.txtEarning.text = "$$formatStr"
            if (ValidationUtils.isValidString(earning.end_time)) {
                val convertedDate = Utils.formateDateFromstring("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" , "dd-MMM-yyyy HH:mm",earning.created_at)
               // val convertedDate = Utils.stringDateToFormatDate(earning.end_time)
                if (earningClassName.equals(
                        TodayEarningFragment::class.java.simpleName,
                        ignoreCase = true
                    )
                ) {
                    holder.txtRateTime.text = Utils.formateDateFromstring("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" , "dd-MMM-yyyy HH:mm",earning.created_at)
                   // holder.txtRateTime.text = strDate
                } else {
                    holder.txtRateTime.text = Utils.formateDateFromstring("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" , "dd-MMM-yyyy HH:mm",earning.created_at)
                   // holder.txtRateTime.text =strDate
                }
            }
            if (earning.cusers?.image != null) {
                Glide.with(mContext).load(Common.UPLOAD_URL + (earning.cusers?.image))
                    .placeholder(R.drawable.ic_driver_profile).into(holder.imgUser)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return earningList.size
    }
}