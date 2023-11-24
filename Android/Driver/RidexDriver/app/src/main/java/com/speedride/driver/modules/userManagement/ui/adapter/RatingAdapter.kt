package com.speedride.driver.modules.userManagement.ui.adapter

import android.content.Context
import com.speedride.driver.modules.userManagement.dataModel.reviewdata
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.RatingBar
import com.speedride.driver.R
import android.os.Build
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.speedride.driver.utils.ValidationUtils
import com.bumptech.glide.Glide
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.Utils
import java.lang.Exception

/**
 * Created by MTPC-153 on 3/7/2018.
 */
class RatingAdapter(private val mContext: Context, private val ratingList: List<reviewdata>) :
    RecyclerView.Adapter<RatingAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtUserName: TextView
        var txtRateDate: TextView
        var txtComment: TextView
        var imgUser: ImageView
        var ratingBar: RatingBar

        init {
            txtUserName = itemView.findViewById(R.id.txtUserName)
            txtRateDate = itemView.findViewById(R.id.txtRateDate)
            txtComment = itemView.findViewById(R.id.txtUserComment)
            imgUser = itemView.findViewById(R.id.imgUserPhoto)
            ratingBar = itemView.findViewById(R.id.ratingBar)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                try {
                    val progressDrawable = ratingBar.progressDrawable
                    if (progressDrawable != null) {
                        DrawableCompat.setTint(
                            progressDrawable,
                            ContextCompat.getColor(mContext, R.color.colorOrange)
                        )
                    }
                } catch (e: Exception) {
                    //
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_ratings, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val reviewdatamodel = ratingList[position]
        if (reviewdatamodel != null) {
            holder.txtUserName.text = reviewdatamodel.name
            holder.txtComment.visibility = View.GONE
            if (ValidationUtils.isValidString(reviewdatamodel.comments)) {
                holder.txtComment.visibility = View.VISIBLE
                holder.txtComment.text = reviewdatamodel.comments
                if (reviewdatamodel.comments?.length!! > 100) {
                    //add View More to expand view comment
                    Utils.makeTextViewResizable(holder.txtComment, 3, "View More", true)
                }
            }
//            if (ValidationUtils.isValidString(reviewdatamodel.created_at)) {
//                val convertedDate = Utils.stringDateToFormatDate(reviewdatamodel.created_at)
//                val strDate = Utils.dateFormatToStringDateForRating(convertedDate)
//                holder.txtRateDate.text = Utils.formateDateFromstring("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" , "dd-MMM-yyyy HH:mm",reviewdatamodel.created_at)
//            }

            holder.txtRateDate.text = Utils.formateDateFromstring("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" , "dd-MMM-yyyy HH:mm",reviewdatamodel.created_at)

            if (ValidationUtils.isValidString(reviewdatamodel.point)) {
                val rate = reviewdatamodel.point?.toFloat()
                if (rate != null) {
                    holder.ratingBar.rating = rate
                }
            }
            if (ValidationUtils.isValidString(reviewdatamodel.image)) Glide.with(mContext).load(
                Common.UPLOAD_URL + reviewdatamodel.image
            ).placeholder(R.drawable.ic_driver_profile).into(holder.imgUser)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return ratingList.size
    }
}