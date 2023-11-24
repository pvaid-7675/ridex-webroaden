package com.speedride.customer.model

data class ChatHistotyResponse(
    val `data`: List<Data>,
    val message: String,
    val status: Int
) {
    data class Data(
        val date_time: String,
        val details: String,
        val message_type: String,
        val ride_id: Int,
        val user_id: Int
    )
}