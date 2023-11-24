package com.speedride.driver.modules.userManagement.dataModel

data class ShiftTimeGetModel(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val contract: String,
        val country_phone_code: Any,
        val created_at: String,
        val document_verify: String,
        val email: String,
        val email_verified_at: Any,
        val end_time: String,
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
        val social_id: String,
        val start_time: String,
        val status: String,
        val stripe_cust_id: Any,
        val updated_at: String
    )
}