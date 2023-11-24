package com.speedride.driver.modules.earning.dataModel

data class TodayEarningResponse(
    var `data`: Data,
    var message: String,
    var status: Int
) {
    data class Data(
        var last_trip: List<LastTrip>,
        var today_trip: TodayTrip
    ) {
        data class LastTrip(
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
            var end_time: String,
            var estimate_time: String,
            var fees_amount: String,
            var fees_percentage: String,
            var id: Int,
            var km: String,
            var paymode: String,
            var pickup: String,
            var plat: String,
            var plong: String,
            var reason: String,
            var ref_code: String,
            var start_time: String,
            var status: String,
            var updated_at: String,
            var vt_id: Int
        ) {
            data class Cusers(
                var country_phone_code: String,
                var created_at: String,
                var document_verify: String,
                var email: String,
                var email_verified_at: String,
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
                var stripe_cust_id: String,
                var updated_at: String
            )
        }

        data class TodayTrip(
            var all_cost: Double,
            var total_count: Int,
            var total_times: String
        )
    }
}