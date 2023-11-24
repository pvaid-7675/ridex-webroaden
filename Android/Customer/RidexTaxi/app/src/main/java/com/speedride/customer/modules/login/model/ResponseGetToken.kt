package com.speedride.customer.modules.login.model

data class ResponseGetToken(
    //val .expires: String,
    //val .issued: String,
    val EmpNo: String,
    val Name: String,
    val Role: String,
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String,
    val token_type: String
)