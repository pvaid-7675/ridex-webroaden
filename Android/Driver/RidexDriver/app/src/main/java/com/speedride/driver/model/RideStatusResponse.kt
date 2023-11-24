package com.speedride.driver.model

data class RideStatusResponse(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val base_far: String,
        val charge: String,
        val coupon: String,
        val created_at: String,
        val cusers: Cusers,
        val customer_id: Int,
        val departure: String,
        val dlat: String,
        val dlong: String,
        val driver_id: Int,
        val endTime: String,
        val end_time: Any,
        val estimate_time: String,
        val estprice: String,
        val esttime: Int,
        val fees_amount: String,
        val fees_percentage: String,
        val id: Int,
        val is_admin_created: Int,
        val is_schedule: Int,
        val is_sharing: Int,
        val km: String,
        val paymode: String,
        val pickup: String,
        val plat: String,
        val plong: String,
        val reason: String,
        val ref_code: Int,
        val scheduleTime: String,
        val schedule_time: Any,
        val shareParentRideId: String,
        val share_parent_ride_id: Any,
        val startTime: String,
        val start_time: Any,
        val status: String,
        val updated_at: String,
        val vt_id: Int
    ) {
        data class Cusers(
            val country_phone_code: String,
            val created_at: String,
            val document_verify: String,
            val email: String,
            val email_verified_at: Any,
            val id: Int,
            val image: String,
            val lat: String,
            val lng: String,
            val login_type: String,
            val mobile: String,
            val name: String,
            val on_duty: String,
            val phone_verified: String,
            val role: String,
            val social_id: Any,
            val status: String,
            val stripe_cust_id: Any,
            val updated_at: String
        )
    }
}