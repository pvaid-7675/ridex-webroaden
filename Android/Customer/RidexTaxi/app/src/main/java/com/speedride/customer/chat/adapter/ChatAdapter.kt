package com.speedride.customer.chat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.UserInfo
import com.speedride.customer.R
import com.speedride.customer.model.ChatResponse
import com.speedride.customer.modules.login.model.Data
import com.speedride.customer.modules.utils.Common
import com.speedride.customer.modules.utils.PreferenceUtils
import com.speedride.customer.modules.utils.Utils
import com.squareup.picasso.Picasso
import java.util.*

class ChatAdapter(private val mPrefrence: PreferenceUtils, private val mHistoryList: ArrayList<ChatResponse.ChatResponseItem>, private val mContext: Context) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var userInfo: Data? =null
            init{
                userInfo = mPrefrence.userInfo
            }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_layout
            , parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = mHistoryList[position]
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter() // or centerCrop
            .override(5000, 800);
        if (userInfo?.id.equals(history.user_id)){
            holder.tvSenderUserName.text = userInfo?.name + "(Customer)"
            holder.llSendMsg.visibility = View.GONE
            holder.rlReceive.visibility = View.VISIBLE
            if(history.message_type.equals("image")){
                holder.ivSenderImage.visibility = View.VISIBLE
                holder.tvSenderMsg.visibility = View.GONE
             //   Picasso.get().load(history.details).into(holder.ivSenderImage)
                Glide.with(mContext).asBitmap().load(history.details)
                    .apply(requestOptions)
                    .into(
                        holder.ivSenderImage
                    )
            }else {
                holder.ivSenderImage.visibility = View.GONE
                holder.tvSenderMsg.visibility = View.VISIBLE
                holder.tvSenderMsg.text = history.details
            }
           holder.tvSenderTime.text = Utils.formateDateFromstring("yyyy-MM-dd HH:mm:ss" , " HH:mm",history.date_time)
        }else if(mPrefrence.customerRideConfirmData?.driver_id!!.equals(history.user_id)){
            holder.rlReceive.visibility = View.GONE
            holder.llSendMsg.visibility = View.VISIBLE
            holder.tvReceiveUserName.text = mPrefrence.customerDriverRideInfo!!.driver_details.name+ "(Driver)"
            if(history.message_type.equals("image")){
                holder.ivReceiverImage.visibility = View.VISIBLE
                holder.tvReceiveMsg.visibility = View.GONE
//                Picasso.get().load(Common.IMAGE_URL + history.details).into(holder.ivReceiverImage)
                Glide.with(mContext).asBitmap().load(history.details)
                    .apply(requestOptions)
                    .into(
                        holder.ivReceiverImage
                    )
            }else {
                holder.ivReceiverImage.visibility = View.GONE
                holder.tvReceiveMsg.visibility = View.VISIBLE
                holder.tvReceiveMsg.text = history.details
            }
            holder.tvReceiverTime.text = Utils.formateDateFromstring("yyyy-MM-dd HH:mm:ss" , " HH:mm",history.date_time)
        } else{
            holder.rlReceive.visibility = View.GONE
            holder.llSendMsg.visibility = View.VISIBLE
            holder.tvReceiveUserName.text ="Admin"
            if(history.message_type.equals("image")){
                holder.tvReceiveMsg.visibility = View.GONE
                holder.ivReceiverImage.visibility = View.VISIBLE
                // Picasso.get().load(history.details).into(holder.ivReceiverImage)
                Glide.with(mContext).asBitmap().load(history.details)
                    .apply(requestOptions)
                    .into(
                        holder.ivReceiverImage
                    )
            }else {
                holder.ivReceiverImage.visibility = View.GONE
                holder.tvReceiveMsg.visibility = View.VISIBLE
                holder.tvReceiveMsg.text = history.details
            }
            holder.tvReceiverTime.text = Utils.formateDateFromstring("yyyy-MM-dd HH:mm:ss" , " HH:mm",history.date_time)
        }
    }

    override fun getItemCount(): Int {
        return mHistoryList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSenderUserName: AppCompatTextView
        var tvSenderTime: AppCompatTextView
        var tvSenderMsg: AppCompatTextView
        var ivSenderImage: AppCompatImageView
        var tvReceiverTime: AppCompatTextView
        var tvReceiveUserName: AppCompatTextView
        var tvReceiveMsg: AppCompatTextView
        var ivReceiverImage: AppCompatImageView
        var llSendMsg:LinearLayout
        var rlReceive:LinearLayout
        init {
            tvSenderUserName = itemView.findViewById(R.id.tvSenderUserName)
            ivSenderImage = itemView.findViewById(R.id.ivSenderImage)
            tvSenderTime = itemView.findViewById(R.id.tvSenderTime)
            tvSenderMsg = itemView.findViewById(R.id.tvSenderMsg)
            tvReceiverTime = itemView.findViewById(R.id.tvReceiverTime)
            tvReceiveUserName = itemView.findViewById(R.id.tvReceiveUserName)
            tvReceiveMsg = itemView.findViewById(R.id.tvReceiveMsg)
            ivReceiverImage = itemView.findViewById(R.id.ivReceiverImage)
            llSendMsg = itemView.findViewById(R.id.llSendMsg)
            rlReceive = itemView.findViewById(R.id.rlReceive)
        }
    }
    fun addData(chatResponse: ChatResponse.ChatResponseItem){
        mHistoryList.add(chatResponse)
        notifyDataSetChanged()
    }

    fun addAll(mChatList: ArrayList<ChatResponse.ChatResponseItem>) {
//        mHistoryList.clear()
//        mHistoryList.addAll(mChatList)
        notifyDataSetChanged()
    }
}