package com.speedride.driver.model

data class AgencyModel(
    val `data`: List<Data>,
    val message: String,
    val status: Int
) {
    data class Data(
        val address: Any,
        val billing_address: Any,
        val card_exp_date: Any,
        val card_holder_name: Any,
        val card_number: Any,
        val company_name: String,
        val created_at: Any,
        val email: String,
        val id: Int,
        val mobile_number: String,
        val number_of_users: Any,
        val person_of_contact: Any,
        val total_cars_or_trip: Any,
        val updated_at: Any
    )
}