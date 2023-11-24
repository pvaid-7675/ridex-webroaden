package com.speedride.customer.model

class ChatResponse : ArrayList<ChatResponse.ChatResponseItem>(){
     class ChatResponseItem(){
        var date_time: String?=null
        var details: String?=null
        var message_type: String?=null
        var ride_id: String?=null
        var user_id: String?=null
    }

}