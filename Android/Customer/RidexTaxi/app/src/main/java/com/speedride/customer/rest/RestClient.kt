package com.speedride.customer.rest

import com.speedride.customer.model.ChatResponse
import com.speedride.customer.model.RideStatusResponse
import com.speedride.customer.model.ServerResponse
import com.speedride.customer.model.SoketResponse
import com.speedride.customer.modules.login.model.Data
import com.speedride.customer.modules.main.model.ScheduleRideList
import com.speedride.customer.modules.main.model.VehicleData
import com.speedride.customer.modules.payment.model.HistoryDetail
import com.speedride.customer.modules.register.view.activity.model.CountryCodeModel
import com.speedride.customer.modules.main.model.map_poliline.Result
import com.speedride.customer.modules.payment.model.PaymentClientToken
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.util.Objects

interface RestClient {

    @Multipart
    @POST("register")
    fun registerUser(
        @Part("first_name") first_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("mobile") mobile: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("device_type") device_type: RequestBody?,
        @Part("device_unique_id") mdevice_unique_id: RequestBody?,
        @Part("fcm_token") mfcm_token: RequestBody?,
        @Part("country_phone_code") country_phone_code: RequestBody?

    ): Call<com.speedride.customer.model.ServerResponse<Data>>?

    @FormUrlEncoded
    @POST("login")
    fun loginCustomer(
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("role") role: String?,
        @Field("device_type") device_type: String?,
        @Field("device_unique_id") device_unique_id: String?,
        @Field("fcm_token") fcm_token: String?
    ): Call<com.speedride.customer.model.ServerResponse<Data>>?

    @FormUrlEncoded
    @POST("user-device")
    fun deviceDetails(
        @Field("user_id") user_id: String?,
        @Field("unique_id") unique_id: String?,
        @Field("device_token") device_token: String?,
        @Field("device_type") device_type: String?
    ): Call<com.speedride.customer.model.ServerResponse<*>?>?

    @FormUrlEncoded
    @POST("verify-otp")
    fun verifyOtp(
        @Field("user_id") user_id: String?,
        @Field("otp") otp: String?
        /*,@Field("role") String role*/
    ): Call<com.speedride.customer.model.ServerResponse<Data?>>

    @FormUrlEncoded
    @POST("resend-otp")
    fun resendOtp(@Field("user_id") user_id: String?): Call<com.speedride.customer.model.ServerResponse<Object>>?

    @FormUrlEncoded
    @POST("forgot-password")
    fun forgotPassword(@Field("email") email: String?): Call<com.speedride.customer.model.ServerResponse<Object>>

    @FormUrlEncoded
    @POST("find_drivers")
    @JvmSuppressWildcards
    fun confirmBookFindDrivers(
        @Field("customer_id") user_id: String?,
        @Field("plat") pLat: String?,
        @Field("plong") pLong: String?,
        @Field("dlat") dLat: String?,
        @Field("dlong") dLong: String?,
        @Field("km") km: String?,
        @Field("vt_id") vehicleType: String?,
        @Field("paymode") paymentMode: String?,
        @Field("charge") charge: String?,
        @Field("departure") departure: String?,
        @Field("estimate_time") estimate_time: String?,
        @Field("pickup") pickup: String?,
        @Field("is_sharing") is_sharing: Int?,
        @Field("is_schedule") is_schedule: Int?

    ): Call<Object>

    @FormUrlEncoded
    @POST("book-ride")
    fun confirmBookRideFromDriver(
        @Field("customer_id") customer_id: String?,
        @Field("plat") pLat: String?,
        @Field("plong") pLong: String?,
        @Field("dlat") dLat: String?,
        @Field("dlong") dLong: String?,
        @Field("km") km: String?,
        @Field("vt_id") vehicleType: String?,
        @Field("paymode") paymentMode: String?,
        @Field("charge") charge: String?,
        @Field("driver_id") driver_id: String?,
        @Field("departure") departure: String?,
        @Field("pickup") pickup: String?,
        @Field("base_far") base_far: String?,
        @Field("fees_amount") fees_amount: String?,
       @Field("estimate_time") estimate_time: String?,
        @Field("fees_percentage") fees_percentage: String?,
        @Field("is_sharing") is_sharing: Int?,
        @Field("is_schedule") is_schedule: Int?

    ): Call<com.speedride.customer.model.SoketResponse?>?

    @FormUrlEncoded
    @POST("book-ride")
    fun confirmBookRideFromDriverSchedule(
        @Field("customer_id") customer_id: String?,
        @Field("plat") pLat: String?,
        @Field("plong") pLong: String?,
        @Field("dlat") dLat: String?,
        @Field("dlong") dLong: String?,
        @Field("km") km: String?,
        @Field("vt_id") vehicleType: String?,
        @Field("paymode") paymentMode: String?,
        @Field("charge") charge: String?,
        @Field("departure") departure: String?,
        @Field("pickup") pickup: String?,
        @Field("base_far") base_far: String?,
        @Field("fees_amount") fees_amount: String?,
        @Field("estimate_time") estimate_time: String?,
        @Field("fees_percentage") fees_percentage: String?,
        @Field("is_sharing") is_sharing: String?,
        @Field("is_schedule") is_schedule: String?,
        @Field("schedule_time") schedule_time : String?
    ): Call<com.speedride.customer.model.SoketResponse?>?

    @FormUrlEncoded
    @POST("cancel-trip")
    fun cancelBook(
        @Field("bookid") id: String?,
        @Field("reason") reason: String?,
        @Field("driver_id") driver_id: String?
    ): Call<com.speedride.customer.model.ServerResponse<Object>>

    /*@FormUrlEncoded*/
    @POST("logout")
    @JvmSuppressWildcards
    fun logOut( /*@Field("user_id") String user_id,
                                @Field("device_token") String device_token,
                                @Field("device_type") String device_type*/
    ): Call<com.speedride.customer.model.ServerResponse<Object>>?

    @Multipart
    @POST("update-profile")
    fun updateUser(
        @Part("first_name") first_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("mobile") mobile: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("country_phone_code") country_phone_code: RequestBody?
        /*,
                                          @Part("user_id") RequestBody user_id*/
    ): Call<com.speedride.customer.model.ServerResponse<Data>>?

    @Multipart
    @POST("social-login")
    fun socialLogin(
        @Part("social_id") social_id: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("mobile") mobile: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("first_name") first_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("login_type") login_type: RequestBody?,
        @Part("role") role: RequestBody?

    ): Call<com.speedride.customer.model.ServerResponse<Data>>?

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

    ): Call<com.speedride.customer.model.ServerResponse<Data>>?



    @FormUrlEncoded
    @POST("current-drivers")
    fun getCarList(
        @Field("user_id") user_id: String?,
        @Field("lat") lat: String?,
        @Field("lng") lng: String?
    ): Call<com.speedride.customer.model.ServerResponse<Data?>?>?

    @GET("avilable-vehicle-ride")
    fun availableVehicle(): Call<com.speedride.customer.model.ServerResponse<List<VehicleData?>?>?>?

    @GET("country-code-list")
    fun countryCode(): Call<CountryCodeModel>?

    @FormUrlEncoded
    @JvmSuppressWildcards
    @POST("change-password")
    fun changePassword( /*@Field("user_id") String user_id,*/
        @Field("current_password") current_password: String?,
        @Field("new_password") new_password: String? /*,
                                       @Field("confirm_password") String confirm_password*/
    ): Call<com.speedride.customer.model.ServerResponse<Object>>?

    @FormUrlEncoded
    @POST("create-rating")
    fun review(
        @Field("user_id")user_id: String ,
        @Field("driver_id") driver_id: String?,
        @Field("point") point: String?,
        @Field("comments") comments: String?,
        @Field("bookid") bookid: String?
    ): Call<com.speedride.customer.model.ServerResponse<Object>>?/*@Field("user_id") String user_id*/

    /*@FormUrlEncoded*/ /*@POST("get-profile")*/
    @get:GET("get-profile")
    val profile: Call<com.speedride.customer.model.ServerResponse<Data>>?

    @FormUrlEncoded
    @POST("history")
    fun getHistory( /*@Field("user_id") String user_id,
                                                         @Field("role") String role,*/
        @Field("offset") offset: Int
    ): Call<com.speedride.customer.model.ServerResponse<List<HistoryDetail>>>

    @FormUrlEncoded
    @POST("send_emergency_contact")
    fun sendEmergencyContact(@Field("contact") contact: String?): Call<com.speedride.customer.model.ServerResponse<*>>?

    @get:GET("get-fees")
    val fees: Call<com.speedride.customer.model.ServerResponse<Data>>?

    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("mode") mmode: String?,
        @Query("transit_routing_preference") mtransit_routing_preference: String?,
        @Query("origin") morigin: String?,
        @Query("destination") mdestination: String?,
        @Query("key") mkey: String?
    ): Call<Result?>?


   @FormUrlEncoded
   @POST("store-stripe-payment-detail")
    fun send_Payment_Detail(
       @Field("amount") amount:String?,
       @Field("user_id") user_id:String?,
       @Field("stripe_id") stripe_id:String?,
       @Field("ride_id") ride_id:String,
       @Field("payment_method_id") payment_method_id:String,
       @Field("card_exp_month") card_exp_month:String,
       @Field("card_exp_year") card_exp_year:String,
       @Field("card_last_digits") card_last_digits:String,
       @Field("status") status:String
   ): Call<Object>?

    @FormUrlEncoded
    @POST("stripe-payment-client-secret")
    fun sendPayment_Token(@Field("amount") amount: String?
    ): Call<PaymentClientToken>?

    @FormUrlEncoded
    @POST("get-schedule-rides")
    fun getScheduleUser(@Field("user_id") user_id: String?
    ): Call<ScheduleRideList>?

    @FormUrlEncoded
    @POST("cancel-trip")
    fun cancelBookScheduleRide(
        @Field("bookid") id: String?,
        @Field("reason") reason: String?,
        @Field("driver_id") driver_id: String?,
        @Field("is_schedule") is_schedule: String?
    ): Call<com.speedride.customer.model.ServerResponse<Any>>

    @FormUrlEncoded
    @POST("get-ride-data")
    fun bookAdminRideData(
        @Field("ride_id") ride_id: String?,
    ):  Call<com.speedride.customer.model.SoketResponse?>?

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
    ): Call<com.speedride.customer.model.ServerResponse<List<ChatResponse.ChatResponseItem>>>



}