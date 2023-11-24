package com.speedride.customer.modules.login.model

data class RequestGetToken(
    val grant_type: String,
    val username: String,
    val password: String
)

//grant_type:password
//username:1001070
//password:BDWBVFQGY6