package com.speedride.driver.modules.userManagement.ui.adapter

import android.content.Context
import com.speedride.driver.modules.userManagement.dataModel.VehicleType
import androidx.recyclerview.widget.RecyclerView
import com.speedride.driver.utils.PreferenceUtils
import android.widget.LinearLayout
import android.widget.TextView
import com.speedride.driver.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.speedride.driver.utils.Common
import com.speedride.driver.utils.ValidationUtils

/**
 * Created by MTPC-153 on 3/7/2018.
 */
class VehicleSelectTypeAdapter(
    private val mContext: Context,
    private val vehicleTypeList: List<VehicleType>,
    private val mItemClickListener: (Any) -> Int
) : RecyclerView.Adapter<VehicleSelectTypeAdapter.MyViewHolder>() {
    private var mPreferenceUtils: PreferenceUtils? = null
    public var isSelect = false
    public var vehicleID = ""
    public var currentSelectedPosition = -1

    lateinit var selectedValue: (Any) -> Unit

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var viewVehicle: View
        var llVehicleType: LinearLayout
        var llVehicleInfo: LinearLayout
        var tvVehicleName: TextView
        var tvVehicleInfo: TextView
        var tvPersonSize: TextView
        var imgVehicle: ImageView
        var imgCheckRight: ImageView
        var llImgChecked: LinearLayout

        init {
            llImgChecked = itemView.findViewById(R.id.llImgChecked)
            viewVehicle = itemView.findViewById(R.id.viewVehicleType)
            llVehicleType = itemView.findViewById(R.id.llVehicleType)
            llVehicleInfo = itemView.findViewById(R.id.llVehicleInfo)
            tvVehicleName = itemView.findViewById(R.id.txtVehicleName)
            tvVehicleInfo = itemView.findViewById(R.id.txtVehicleInfo)
            tvPersonSize = itemView.findViewById(R.id.txtPersonSize)
            imgVehicle = itemView.findViewById(R.id.imgVehicle)
            imgCheckRight = itemView.findViewById(R.id.imgCheckRight)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_vehicle_type, parent, false)
        mPreferenceUtils = PreferenceUtils.getInstance(mContext)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val vehicleType: VehicleType = vehicleTypeList[position]
        holder.tvVehicleName.text = vehicleType.v_name
        holder.tvPersonSize.text = vehicleType.max_person + " Max Person"
        holder.tvVehicleInfo.text = vehicleType.description
        Glide.with(mContext)
            .load(Common.OTHER_IMAGE_URL + vehicleType.v_image)
            .placeholder(R.drawable.ic_taxi).into(holder.imgVehicle);


        /* if (ValidationUtils.isValidString(vehicleType.getV_image())) {
          Glide.with(this).load(vehicleType.getV_image()).placeholder(R.drawable.ic_driver_profile).into(holder.imgVehicle);
     }*/

        /* //add vehicle images
     if (ValidationUtils.isValidString(vehicleType.getId())) {
         holder.imgVehicle.setImageResource(checkVehicleImages(vehicleType.getId()));
     }
*/
        //check for already selected
        if (!isSelect) {
            if (ValidationUtils.isValidObject(mPreferenceUtils?.driverData)) {
                if (ValidationUtils.isValidObject(mPreferenceUtils?.driverData?.driver_detail) && ValidationUtils.isValidString(
                        mPreferenceUtils?.driverData?.driver_detail?.vt_id
                    )
                ) {
                    if ((mPreferenceUtils?.driverData?.driver_detail?.vt_id == vehicleTypeList[holder.adapterPosition].id)) {
                        vehicleTypeList.get(holder.adapterPosition).selected = true
                        // mItemClickListener.onClick(vehicleTypeList[holder.adapterPosition].id.toInt())
                    }
                }
            }
        }
        if (vehicleType.selected) {
            holder.llVehicleInfo.visibility = View.VISIBLE
            holder.viewVehicle.visibility = View.VISIBLE
            holder.llImgChecked.visibility = View.VISIBLE
            holder.tvVehicleName.setTextColor(mContext.resources.getColor(R.color.colorText))
//            holder.imgVehicle.setImageResource(Utils.checkVehicleImagesSelected(vehicleType.id!!))
            for (i in vehicleTypeList.indices) {
                if (!vehicleTypeList[i].selected) {
                    vehicleTypeList.get(holder.adapterPosition).selected = false
                }
            }
        } else {
            holder.llVehicleInfo.visibility = View.GONE
            holder.viewVehicle.visibility = View.GONE
            holder.llImgChecked.visibility = View.GONE
            holder.tvVehicleName.setTextColor(mContext.resources.getColor(R.color.gray))
//            holder.imgVehicle.setImageResource(Utils.checkVehicleImages(vehicleType.id!!))
        }


        holder.llVehicleType.setOnClickListener(View.OnClickListener {
            isSelect = true
            vehicleID = vehicleType.id.toString()
            if (vehicleTypeList[holder.adapterPosition].selected) {
                vehicleTypeList.get(holder.adapterPosition).selected = false
                holder.llVehicleInfo.visibility = View.GONE
                holder.viewVehicle.visibility = View.GONE
                holder.llImgChecked.visibility = View.GONE
            } else {

                vehicleTypeList.get(holder.adapterPosition).selected = true
                holder.llVehicleInfo.visibility = View.VISIBLE
                holder.viewVehicle.visibility = View.VISIBLE
                holder.llImgChecked.visibility = View.VISIBLE
            }
            notifyDataSetChanged()
        })
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return vehicleTypeList.size
    }
}