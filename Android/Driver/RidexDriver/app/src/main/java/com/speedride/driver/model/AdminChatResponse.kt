package com.speedride.driver.model

class AdminChatResponse : ArrayList<AdminChatResponse.AdminChatResponseItem>(){
     class AdminChatResponseItem(){
        var sender_id: String?=null
        var details: String?=null
        var message_type: String?=null
        var date_time: String?=null
         var receiver_id: String?=""
    }


}