package com.speedride.driver.model

class Data {
    var on_duty: String? = null
    var otp_expire: String? = null
    var otp: String? = null
    var v_document: String? = null
    var d_licence: String? = null
    var v_permit: String? = null
    var v_insurance: String? = null
    var v_registration: String? = null
    var driver_type:String?=null
    var driver_detail: Ddetail? = null
    var vehicle_detail: Dvehicle? = null
    fun setFirst_name(first_name: String?) {
        name = first_name
    }

    var id: String? = null
    var name: String? = null
    var phone_verified: String? = null
    var updated_at: String? = null
    var status: String? = null
    var email: String? = null
    var created_at: String? = null
    var role: String? = null
    var image: String? = null
    var mobile: String? = null
    var social_id: String? = null
    var login_type: String? = null
    var fees: String? = null
    var country_phone_code:String? = null
}