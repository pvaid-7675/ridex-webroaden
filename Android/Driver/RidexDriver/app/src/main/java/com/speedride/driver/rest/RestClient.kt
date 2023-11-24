package com.speedride.driver.rest

import com.speedride.customer.model.ChatResponse
import com.speedride.driver.model.*
import com.speedride.driver.modules.earning.dataModel.EarningDetail
import com.speedride.driver.modules.earning.dataModel.TodayEarningResponse
import com.speedride.driver.modules.ride.dataModel.HistoryDetail
import com.speedride.driver.modules.userManagement.dataModel.RatingDetail
import com.speedride.driver.modules.userManagement.dataModel.ShiftTimeGetModel
import com.speedride.driver.modules.userManagement.dataModel.VehicleType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RestClient {
    @Multipart
    @POST("register")
    fun registerDriver(
        @Part("first_name") first_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("mobile") mobile: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("device_type") device_type: RequestBody?,
        @Part("device_unique_id") mdevice_unique_id: RequestBody?,
        @Part("fcm_token") mfcm_token: RequestBody?
    ): Call<ServerResponse<Data>>

    @FormUrlEncoded
    @POST("verify-otp")
    fun verifyOtp(
        @Field("user_id") user_id: String?,
        @Field("otp") otp: String?
    ): Call<ServerResponse<Object>>

    @FormUrlEncoded
    @POST("resend-otp")
    fun resendOtp(
        @Field("user_id") user_id: String? /*,
                                   @Field("email") String email*/
    ): Call<ServerResponse<Object>>

    @FormUrlEncoded
    @POST("login")
    fun loginDriver(
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("role") role: String?,
        @Field("device_type") device_type: String?,
        @Field("device_unique_id") device_unique_id: String?,
        @Field("fcm_token") fcm_token: String?
    ): Call<ServerResponse<Data>>

    @FormUrlEncoded
    @POST("user-device")
    fun deviceDetails(
        @Field("user_id") user_id: String?,
        @Field("unique_id") unique_id: String?,
        @Field("device_token") device_token: String?,
        @Field("device_type") device_type: String?
    ): Call<ServerResponse<*>>

    @POST("logout")
    fun logOut( /*@Field("user_id") String user_id,
                                @Field("device_token") String device_token,
                                @Field("device_type") String device_type*/
    ): Call<ServerResponse<Object>>

    @FormUrlEncoded
    @POST("driver-onduty")
    fun driverStatus( /*@Field("user_id") String user_id,*/
        @Field("on_duty") on_duty: String?
    ): Call<ServerResponse<Data>>

    @FormUrlEncoded
    @POST("social-login")
    fun socialLogin(
        @Field("social_id") social_id: String?,
        @Field("email") email: String?,
        @Field("first_name") first_name: String?,
        @Field("last_name") last_name: String?,
        @Field("login_type") login_type: String?,
        @Field("image") image: String?

    ): Call<ServerResponse<Data>>?

    @FormUrlEncoded
    @POST("social-login")
    fun socialLogin1(
        @Field("social_id") social_id: String?,
        @Field("email") email: String?,
        @Field("mobile") mobile: String?,
        @Field("image") image: String?,
        @Field("first_name") first_name: String?,
        @Field("last_name") last_name: String?,
        @Field("login_type") login_type: String?,
        @Field("role") role: String?

    ): Call<ServerResponse<Data>>?

    @FormUrlEncoded
    @POST("forgot-password")
    fun forgotPassword(@Field("email") email: String): Call<ServerResponse<Any>>

    @get:GET("vehicle-type-list")
    val vehicleTypeList: Call<ServerResponse<List<VehicleType>>>

    @get:GET("country-code-list")
    val countryCodeList: Call<ServerResponse<List<CountryCodelist>>>

    @FormUrlEncoded
    @POST("add-driver-vehicle-type")
    fun sendVehicleType(
        @Field("user_id") user_id: String,
        @Field("vt_id") vehicle_type_id: String
    ): Call<ServerResponse<Any>>

    @FormUrlEncoded
    @POST("add-vehicle-detail")
    fun sendVehicleDetails(
        @Field("user_id") user_id: String,
        @Field("vt_id") vehicle_type_id: String,
        @Field("brand") vehicle_brand: String,
        @Field("model") vehicle_model: String,
        @Field("year") vehicle_year: String,
        @Field("color") vehicle_color: String,
        @Field("icolor") vehicle_interior_color: String,
        @Field("number") vehicle_number: String
    ): Call<ServerResponse<Any>>

    @Multipart
    @POST("upload-vehicle-document")
    fun uploadDriverVehicleDocumentsAll(
        @Part("user_id") user_id: RequestBody?,
        @Part("type") type: RequestBody?,
        @Part document: MultipartBody.Part?
    ): Call<ServerResponse<Data>>/*@Field("user_id") String user_id*/
    @Multipart
    @POST("upload-vehicle-document")
    fun uploadDriverVehicleDocumentsAllTEST(
        @Part("user_id") user_id: RequestBody?,
        @Part("type") type: RequestBody?,
        @Part document: MultipartBody.Part?
    ): Call<ServerResponse<Ddetail>>/*@Field("user_id") String user_id*/
    /*@FormUrlEncoded*/ /*@POST("get-vehicle-detail")*/
    @get:GET("get-vehicle-detail")
    val vehicleDetails: Call<ServerResponse<Data>>

    /*@FormUrlEncoded*/
    @get:GET("get-profile")
    val driverProfile: Call<ServerResponse<Data>>

    @Multipart
    @POST("update-profile")
    fun updateDriverProfile(
        @Part("first_name") first_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("mobile") mobile: RequestBody?,
        @Part("country_phone_code") country_phone_code: RequestBody?,
        @Part image: MultipartBody.Part? /*,
                                                   @Part("user_id") RequestBody user_id*/
    ): Call<ServerResponse<Data>>

    @FormUrlEncoded
    @POST("change-password")
    fun changePassword( /*@Field("user_id") String user_id,*/
        @Field("current_password") current_password: String?,
        @Field("new_password") new_password: String? /*,
                                       @Field("confirm_password") String confirm_password*/
    ): Call<Object>

    @FormUrlEncoded
    @POST("history")
    fun getHistoryList( /*@Field("user_id") String user_id,
                                                             @Field("role") String role,*/
        @Field("offset") offset: String?
    ): Call<ServerResponse<List<HistoryDetail>>>

    @FormUrlEncoded
    @POST("earning-list")
    fun getEarningList( /*@Field("user_id") String driver_id,*/
        @Field("offset") offset: String?,
        @Field("option") option: String?
    ): Call<ServerResponse<EarningDetail>>

    @FormUrlEncoded
    @POST("rating-list")
    fun getRatingList(
        @Field("driver_id") driver_id: String?,
        @Field("offset") offset: String?
    ): Call<ServerResponse<RatingDetail>>

    @FormUrlEncoded
    @POST("create-rating")
    fun reviewCommentRateTrip(
        @Field("user_id") user_id: String ,
        @Field("driver_id") driver_id: String?,
        @Field("point") point: String?,
        @Field("bookid") mbookid: String?,
        @Field("comments") comments: String?
    ): Call<ServerResponse<Object>>

    @FormUrlEncoded
    @POST("conf-ride")
    fun confirmPassCodeRideTrip(
        @Field("bookid") bookid: String?,
        @Field("ref_code") ref_code: String?
    ): Call<ServerResponse<Object>>

    @FormUrlEncoded
    @POST("start-trip")
    fun startRideWithCustomer(
        @Field("bookid") bookid: String?,
        @Field("driver_id") driver_id: Int?,
        @Field("customer_id") customer_id: String?
    ): Call<ServerResponse<Object>>

    @FormUrlEncoded
    @POST("complete-trip")
    fun completeRideWithCustomer(
        @Field("bookid") bookid: String?,
        @Field("driver_id") driver_id: Int?,
        @Field("customer_id") customer_id: String?
    ): Call<ServerResponse<Object>>

    //@FormUrlEncoded
    @GET("get-drivertrip-list")
    fun todayEarningList(): Call<TodayEarningResponse>

    @FormUrlEncoded
    @POST("driver-assign-schedule-ride")
    fun acceptRideSchedule(
        @Field("driver_id") driver_id : Int?,
        @Field("book_id") book_id: String?
    ): Call<Object>

    @FormUrlEncoded
    @POST("get-shareride-data")
    fun getSharedRide(
        @Field("driver_id") driver_id: Int?
    ): Call<SharedRideRequestData>

    @FormUrlEncoded
    @POST("get-ride-data")
    fun getRideStatus(
        @Field("ride_id") ride_id: String?,
    ):  Call<SoketResponse?>?


    @FormUrlEncoded
    @POST("get-userride-data")
    fun getTripStatus(
        @Field("user_id") user_id: String?,
    ): Call<RideStatusResponse>
    @Multipart
    @POST("upload-chat-image")
    fun getChatImageUpload(
//        @Field("image") image: String?,
        @Part image: MultipartBody.Part?,
    ): Call<Object>

    @FormUrlEncoded
    @POST("get-chat-messages")
    fun getChatMessage(
        @Field("ride_id") bookid: String?
    ): Call<ServerResponse<List<ChatResponse.ChatResponseItem>>>


    @FormUrlEncoded
    @POST("get-admindriver-chat-messages")
    fun getAdminChatMessage(
        @Field("driver_id") driver_id: String?
    ): Call<ServerResponse<List<AdminChatResponse.AdminChatResponseItem>>>

    /*@FormUrlEncoded*/
    @GET("get-user")
    fun driverShiftTime(@Query("user_id") driver_id : String?): Call<ShiftTimeGetModel>

    @FormUrlEncoded
    @POST("update-driver-time-slot")
    fun updateShiftTime( @Field("driver_id") driver_id : String?,
                         @Field("start_time") start_time: String?,
                         @Field("end_time") end_time: String?): Call<Object>
    @POST("get-agency-list")
    fun agencyList(): Call<AgencyModel>

    @FormUrlEncoded
    @POST("activeInactiveUser")
    fun accountDelete(
        @Field("user_id") user_id: String?
    ): Call<Object>
}