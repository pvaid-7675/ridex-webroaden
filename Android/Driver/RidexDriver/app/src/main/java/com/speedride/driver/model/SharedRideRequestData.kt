package com.speedride.driver.model

data class SharedRideRequestData(
    var `data`: List<Data>,
    var message: String,
    var status: Int
) {
    data class Data(
        var base_far: String,
        var charge: String,
        var coupon: String,
        var created_at: String,
        var cusers: Cusers,
        var customer_id: Int,
        var departure: String,
        var dlat: String,
        var dlong: String,
        var driver_id: Int,
        var dusers: Dusers,
        var end_time: Any,
        var estimate_time: String,
        var fees_amount: String,
        var fees_percentage: String,
        var id: Int,
        var is_schedule: Int,
        var is_sharing: Int,
        var km: String,
        var paymode: String,
        var pickup: String,
        var plat: String,
        var plong: String,
        var reason: Any,
        var ref_code: String,
        var schedule_time: Any,
        var share_parent_ride_id: Any,
        var start_time: String,
        var status: String,
        var updated_at: String,
        var vt_id: Int
    ) {
        data class Cusers(
            var country_phone_code: Any,
            var created_at: String,
            var document_verify: String,
            var email: String,
            var email_verified_at: Any,
            var id: Int,
            var image: String,
            var lat: Any,
            var lng: Any,
            var login_type: String,
            var mobile: String,
            var name: String,
            var on_duty: String,
            var phone_verified: String,
            var role: String,
            var social_id: String,
            var status: String,
            var stripe_cust_id: Any,
            var updated_at: String
        )

        data class Dusers(
            var country_phone_code: Any,
            var created_at: String,
            var document_verify: String,
            var email: String,
            var email_verified_at: Any,
            var id: Int,
            var image: String,
            var lat: String,
            var lng: String,
            var login_type: String,
            var mobile: String,
            var name: String,
            var on_duty: String,
            var phone_verified: String,
            var role: String,
            var social_id: String,
            var status: String,
            var stripe_cust_id: Any,
            var updated_at: String
        )
    }
}