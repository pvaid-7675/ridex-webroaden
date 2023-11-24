package com.speedride.driver.modules.auth.ui.activity

import android.Manifest
import android.app.AlertDialog
import com.speedride.driver.base.BaseActivity
import com.google.android.gms.common.api.GoogleApiClient
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import com.speedride.driver.model.ServerResponse
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.RelativeLayout
import android.widget.Toast
import android.content.pm.PackageManager
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import android.content.Intent
import com.speedride.driver.service.ServiceClass
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import com.facebook.*
import com.facebook.login.LoginBehavior
import com.speedride.driver.utils.PermissionUtils.OnPermissionGrantCallback
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.speedride.driver.JavaHelper
import com.speedride.driver.model.Data
import com.speedride.driver.model.RideStatusResponse
import com.speedride.driver.modules.home.ui.activity.MainActivity
import com.speedride.driver.modules.ride.dataModel.CustomerRequestModel
import com.speedride.driver.modules.ride.ui.activity.OnRideActivity
import com.speedride.driver.modules.userManagement.ui.activity.AddVehicleDetailsActivity
import com.speedride.driver.modules.userManagement.ui.activity.DocumentActivity
import com.speedride.driver.modules.userManagement.ui.activity.SelectVehicleTypeActivity
import com.speedride.driver.rest.Singleton
import com.speedride.driver.rest.Singleton.Companion.restClient
import com.speedride.driver.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import com.speedride.driver.R

class LoginActivity : BaseActivity(), View.OnClickListener,
    GoogleApiClient.OnConnectionFailedListener {
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mPermissionUtils: PermissionUtils? = null
    private var mEdtEmailOrMobile: EditText? = null
    private var mEdtPassword: EditText? = null
    private var mTxtLogin: TextView? = null
    private var mTxtForget: TextView? = null
    private var mLlGoogle: LinearLayout? = null
    private var mLlFacebook: LinearLayout? = null
    private var login_button: FacebookButtonBase? = null
    private var mApiLoginResponseCall: Call<ServerResponse<Data>>? = null
    private var mApiSocialLogin: Call<ServerResponse<Data>>? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mCallbackManager: CallbackManager? = null
    private var mCallbackManager2: CallbackManager? = null
    private var facebookProfilePicture: Uri? = null
    private var mProgressDialog: ProgressDialog? = null
    var android_id: String? = null
    private var mApiGetRideStatus: Call<RideStatusResponse>? = null
    private var commonDialogUtils: CommonDialogUtils? = null

    private val EMAIL = "email"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        JavaHelper.printHashKey(this);
        android_id = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        initView(null)

        /* login_fb.setOnClickListener {

             login_fb.setReadPermissions(listOf(EMAIL))
             mCallbackManager2 = CallbackManager.Factory.create()

             LoginManager.getInstance().registerCallback(mCallbackManager2, object: FacebookCallback<LoginResult>{
                 override fun onSuccess(result: LoginResult?) {
                     TODO("Not yet implemented")
                 }

                 override fun onCancel() {
                     TODO("Not yet implemented")
                 }

                 override fun onError(error: FacebookException?) {
                     TODO("Not yet implemented")
                 }

             })
         }*/
    }


    override val activity: AppCompatActivity
        get() = this@LoginActivity
    override val actionTitle: String
        get() = resources.getString(R.string.login_small)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun initView(view: View?) {
        setToolbar((findViewById<View>(R.id.rlToolbar) as RelativeLayout))
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(resources.getString(R.string.please_wait_msg))
        googleLoginInitialization()
        FbLoginInitialization()
        printKeyHashes()
        mPreferenceUtils = PreferenceUtils.getInstance(this@LoginActivity)
        mEdtEmailOrMobile = findViewById(R.id.edtEmailOrMobile)
        mEdtPassword = findViewById(R.id.edtPassword)
        mTxtLogin = findViewById(R.id.txtLogin)
        mTxtForget = findViewById(R.id.txtForget)
        mTxtLogin!!.setOnClickListener(this)
        mTxtForget!!.setOnClickListener(this)
        mLlGoogle = findViewById(R.id.llGoogle)
        login_button = findViewById(R.id.login_fb)
        mLlGoogle!!.setOnClickListener(this)
        mLlFacebook = findViewById(R.id.llFacebook)
        mLlFacebook!!.setOnClickListener(this)
        login_button!!.setOnClickListener(this)


        /* mEdtEmailOrMobile.setText("driver@mailinator.com");
        mEdtPassword.setText("123123");*/

        checkPermission()
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//        var mToast = Toast(this)
//        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG)
//        try {
//            if (mToast.view!!.isShown) {llll
//                                                   mToast.cancel()
//            }
//            mToast.show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    private fun printKeyHashes() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i(TAG, "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "printHashKey()", e)
        } catch (e: Exception) {
            Log.e(TAG, "printHashKey()", e)
        }
    }

    private fun apiLoginDriver() {
        hideKeyboard(this)
        mApiLoginResponseCall = Singleton.restClient.loginDriver(
            mEdtEmailOrMobile!!.text.toString(), mEdtPassword!!.text.toString(), Common.ROLE_DRIVER,
            Common.ANDROID,
            android_id,
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_FCM_TOKEN)
        )
        mApiLoginResponseCall!!.enqueue(object : Callback<ServerResponse<Data>> {
            override fun onResponse(
                call: Call<ServerResponse<Data>>,
                response: Response<ServerResponse<Data>>
            ) {
                val resp: ServerResponse<*>? = response.body()
                if (resp != null && resp.status == 200) {
                    //   showToastMessage(activity, resp.msg)
                    AppLog.d(
                        TAG, "onResponse:Login Msg " + response.body()!!
                            .message
                    )
                    AppLog.d(
                        TAG, "onResponse:Login Id " + (response.body()!!
                            .data?.id)
                    )
                    val userIdRegister: String
                    val userEmail: String
                    if (response.body()!!.data?.id != null) {
                        userIdRegister = response.body()!!.data?.id.toString()
                        userEmail = response.body()!!.data?.email.toString()

                        //save driver details in pref
                        mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                        mPreferenceUtils!!.save(
                            PreferenceUtils.PREF_KEY_BEARER_TOKEN, "Bearer " + response.body()!!
                                .accessToken
                        )

                        if (ValidationUtils.isValidString(
                                mPreferenceUtils!!.get(
                                    PreferenceUtils.PREF_KEY_FCM_TOKEN,
                                    ""
                                )
                            )
                        ) {
                            //call device detail post api
                            apiCallDeviceDetail(userIdRegister, userEmail)
                        } else {

                            /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String newToken = instanceIdResult.getToken();
                                    Log.d(TAG, "onSuccess: newToken" + newToken);

                                    mPreferenceUtils.save(PreferenceUtils.PREF_KEY_FCM_TOKEN, newToken);
                                    apiCallDeviceDetail(userIdRegister, userEmail);
                                }
                            });*/
                            FirebaseMessaging.getInstance().token
                                .addOnCompleteListener(OnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        return@OnCompleteListener
                                    }
                                    // Get new FCM registration token
                                    val token = task.result
                                    mPreferenceUtils!!.save(
                                        PreferenceUtils.PREF_KEY_FCM_TOKEN,
                                        token
                                    )
                                    apiCallDeviceDetail(userIdRegister, userEmail)
                                })
                        }
                    }
                } else {
                    dismissProgress()
                    //showToastMessage(activity,response.message())
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(this@LoginActivity, jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage(this@LoginActivity, e.message!!)
                        }
                    }else if(response.body() != null) {
                        showToastMessage(activity,response.body()!!.message)
                    }else{
                        showToastMessage(activity,"Something go wrong")
                    }
                }
            }

            override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                AppLog.d(TAG, "onFailure: $t")
                dismissProgress()
            }
        })
    }

    /**
     * api call for Device app Detail
     */
    private fun apiCallDeviceDetail(registerId: String, userEmail: String) {
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SIGN_UP, true)
        connectSocket()
        //checkStatus()
        apiCallGetRideStatus();

        /*if (ValidationUtils.isInternetAvailable(this)) {
            String deviceOs = Build.VERSION.RELEASE;
            String model = Build.MODEL;
            String manufacture = Build.MANUFACTURER;

            AppLog.d(TAG, "onResponse: " + "Id " + registerId + "Android Id " + android_id);

            mApiDeviceDetailCall = Singleton.getInstance().getRestClient().deviceDetails(registerId, android_id, mPreferenceUtils.get(PreferenceUtils.PREF_KEY_FCM_TOKEN, ""), Common.ANDROID);
            mApiDeviceDetailCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                    ServerResponse resp = response.body();

                    dismissProgress();

                    if (resp != null && resp.checkResponse(response) == 200) {
                        //if account not verify with OTP, then send for verify


                    } else {
                        if (resp != null && resp.getMsg() != null)
                            showToastMessage(getActivity(), resp.getMsg());
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    AppLog.d(TAG, "onFailure:---- " + t);
                    //showToastMessage(getActivity(), getResources().getString(R.string.something_went_wrong));
                    dismissProgress();
                }
            });
        } else {
            showToastMessage(getActivity(), getResources().getString(R.string.network_error));
            dismissProgress();
        }*/
    }

    private fun connectSocket() {
        if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_SOCKET, false)) {
            Log.d(TAG, "connectSocket: not connect")
            val intent = Intent(this, ServiceClass::class.java)
            intent.action = Common.CONNECT_SOCKET
            startService(intent)
        }
    }

    //google login
    private fun googleLoginInitialization() {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
            .build()
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleApiClient?.let { Auth.GoogleSignInApi.getSignInIntent(it) }
        if (signInIntent != null) {
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult?) {
        Log.d(TAG, "handleSignInResult:" + result!!.isSuccess)
        Log.e("googleSignIn", "handleSignInResult:" + result!!.isSuccess)

        if (result.isSuccess) {
            showProgress()
            val data = mPreferenceUtils!!.driverData

            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount

            var user_id = "5648335"
            if (data != null && ValidationUtils.isValidString(data.id)) user_id = data.id.toString()

            val googleId: String = acct!!.id.toString()
            val firstName: String = acct!!.givenName.toString()
            val lastName: String = acct.familyName.toString()
            var personPhotoUrl = ""
            val email: String = acct.email.toString()
            var mobile = ""
            if (data != null && ValidationUtils.isValidString(data.mobile)) mobile =
                data.mobile.toString()
            val social_login_type = Common.LOGIN_TYPE_GOOGLE
            if (data != null && ValidationUtils.isValidString(data.mobile)) mobile = data.mobile!!
            val google = Common.LOGIN_TYPE_GOOGLE
            val user = Common.ROLE_DRIVER
            // if (data != null && ValidationUtils.isValidString(data.id)) user_id = data.id!!
            if (acct.photoUrl != null) {
                personPhotoUrl = acct.photoUrl.toString()
            }



            Log.e(
                TAG, """Google Id: ${googleId}firstName: $firstName,
 lastName: $lastName,
 email: $email,
 Image: $personPhotoUrl"""
            )


            //call google api for save user data
            if (googleId != null) {
                if (firstName != null) {
                    if (lastName != null) {
                        apiCallSocialLogin(
                            googleId,
                            email,
                            mobile,
                            personPhotoUrl,
                            firstName,
                            lastName,
                            google,
                            user,
                        )
                    }
                }
            }
        } else {
            // Signed out, show unauthenticated UI.
            dismissProgress()
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(
            this@LoginActivity, Arrays.asList("email", "public_profile")
        )
        LoginManager.getInstance().setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK)
       // LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK
       // LoginBehavior.NATIVE_WITH_FALLBACK
    }

    //facebook login
    private fun FbLoginInitialization() {
        //FacebookSdk.sdkInitialize(MainActivity.this);
        Log.e("fbold", "FbLoginInitialization")
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val token = result.accessToken
                    Log.e("fbold", "token" + token.toString() + "")
                    val profile = Profile.getCurrentProfile()
                    if (profile != null) {
                        Log.e(
                            "Profile pic url ",
                            profile.getProfilePictureUri(512, 512).toString() + ""
                        )
                        facebookProfilePicture = profile.getProfilePictureUri(512, 512)
                    }
                    val graphRequest = GraphRequest.newMeRequest(token) { user, graphResponse ->
                        showProgress()
                        Log.e("JsonObject", user.toString() + "")
                        var email: String? = null
                        try {
                            val data = mPreferenceUtils!!.driverData
                            val profilePicture =
                                URL("https:graph.facebook.com/" + user!!.getString("id") + "/picture?large")

//                                    String profilePicture = user.optString("picture");
                            Log.e(TAG, "onCompleted: profilePicture: $profilePicture")
                            val name = user.optString("name")
                            val first_name: String
                            val last_name: String
                            if (name.contains(" ")) {
                                val parts =
                                    name.split(" ").toTypedArray() // escape .
                                first_name = parts[0]
                                last_name = parts[1]
                            } else {
                                first_name = name
                                last_name = ".."
                                Log.e(TAG, "onCompleted:else F$first_name L$last_name")
                            }
                            Log.e(TAG, "onCompleted: F$first_name L$last_name")
                            val fb_id = user.optString("id")
                            Log.e(TAG, "onCompleted: fb_id: $fb_id")
                            email = user.optString("email")
                            Log.e(TAG, "onCompleted: email: $email")
                            val mobile = ""

//                                    String user_birthday = user.optString("birthday");
//                                    Log.e(TAG, "onCompleted: user_birthday: " + user_birthday);
                            val social_login_type = Common.LOGIN_TYPE_FACEBOOK
                            val role = Common.ROLE_DRIVER
                            var user_id = ""
                            if (data != null && ValidationUtils.isValidString(data.id)) user_id =
                                data.id.toString()
                            if (profilePicture.path != null) {
                                Log.e(TAG, "onCompleted: $facebookProfilePicture")
                                apiCallSocialLogin(
                                    fb_id,
                                    email,
                                    mobile,
                                    facebookProfilePicture.toString(),
                                    first_name,
                                    last_name,
                                    social_login_type,
                                    role
                                    // user_id
                                )
                            } else {
                                if (facebookProfilePicture != null) {
                                    Log.d(TAG, "onCompleted: $profilePicture")
                                    apiCallSocialLogin(
                                        fb_id,
                                        email,
                                        mobile,
                                        profilePicture.toString(),
                                        first_name,
                                        last_name,
                                        social_login_type,
                                        role
                                        //user_id
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            dismissProgress()
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email,picture")
                    graphRequest.parameters = parameters
                    graphRequest.executeAsync()
                }

                override fun onError(error: FacebookException) {
                    Log.e("fbold", " Error " + error.toString())
                    showToastMessage(activity, "Login Error")
                    dismissProgress()
                }

                override fun onCancel() {
                    Log.e("fbold", "Cancel")
                    showToastMessage(activity, "Login Cancelled")
                    dismissProgress()
                }
            })
    }

    /*
     @SuppressLint("SuspiciousIndentation")
      private fun apiCallSocialLogin(
          googleId: String,
          email: String,
          mobile: String,
          personPhotoUrl: String?,
          firstName: String,
          lastName: String,
          loginType: String,
          role: String
      ) {
          Log.e("googleSignIn", "apiCallSocialLogin INNNNNNNNN ")

          mApiSocialLogin = restClient.socialLogin(
              "12345",
              "mt_099@outlook.com",
              firstName,
              lastName,
              loginType,
              personPhotoUrl
              //  user_id
          )
              mApiSocialLogin?.enqueue(object : Callback<ServerResponse<Data>> {
                  override fun onResponse(
                      call: Call<ServerResponse<Data>>,
                      response: Response<ServerResponse<Data>>
                  ) {
                      Log.e("googleSignIn", "apiCallSocialLogin INNNNNNNNN 2")

                      Log.e("googleSignIn", """Google Id: ${googleId}firstName: $firstName,
   lastName: $lastName,
   email: $email,
   Image: $personPhotoUrl""")

                      val resp: ServerResponse<*>? = response.body()
                      if (resp != null && resp.checkResponse_(response) == Common.STATUS_200) {
                         // showToastMessage(activity, resp.message)
                          Log.e("googleSignIn", "apiCallSocialLogin INNNNNNNNN 3")

                      Log.e("googleSignIn", "onResponse:Signup Successfully. phone_verified: " + response.body()!!.data?.phone_verified)
                      if (response.body()!!.data?.phone_verified == "0") {
                          dismissProgress()
                          showToastMessage(activity, "Please fill required fields for registration.")
                          mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                          //save driver details in pref
                          mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                          val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                          animationIntent(this@LoginActivity, intent)
                      } else if (response.body()!!.data?.phone_verified == "1") {
                          showToastMessage(activity, resp.message)
                          mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                          //save driver details in pref
                          mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                          val data = mPreferenceUtils!!.driverData
                          if (ValidationUtils.isValidString(
                                  mPreferenceUtils!!.get(
                                      PreferenceUtils.PREF_KEY_FCM_TOKEN,
                                      ""
                                  )
                              )
                          ) {
                              //call device detail post api
                              if (data != null) {
                                  apiCallDeviceDetail(data.id!!, data.email!!)
                              }
                          } else {
                              //get FIREBASE INSTANCE ID
                              *//*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String newToken = instanceIdResult.getToken();
                                    Log.d(TAG, "onSuccess: newToken" + newToken);

                                    mPreferenceUtils.save(PreferenceUtils.PREF_KEY_FCM_TOKEN, newToken);
                                    apiCallDeviceDetail(data.getId(), data.getEmail());
                                }
                            });*//*
                            FirebaseMessaging.getInstance().token
                                .addOnCompleteListener(OnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        return@OnCompleteListener
                                    }

                                    // Get new FCM registration token
                                    val token = task.result
                                    mPreferenceUtils!!.save(
                                        PreferenceUtils.PREF_KEY_FCM_TOKEN,
                                        token
                                    )
                                    if (data != null) {
                                        apiCallDeviceDetail(data.id!!, data.email!!)
                                    }
                                })
                        }
                    }
                } else {
                    dismissProgress()
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(this@LoginActivity, jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage(this@LoginActivity, e.message!!)
                        }
                    }
                }
            }

              override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                dismissProgress()
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                AppLog.d(TAG, "onFailure:---- $t")
            }
        })
    }
*/


    private fun apiCallSocialLogin(
        socialId: String,
        email: String,
        mobile: String,
        personPhotoUrl: String,
        firstName: String,
        lastName: String,
        loginType: String,
        role: String
        //  user_id: String
    ) {
        mApiSocialLogin = restClient.socialLogin1(
            socialId,
            email,
            mobile,
            personPhotoUrl,
            firstName,
            lastName,
            loginType,
            role
            //  user_id
        )

        mApiSocialLogin?.enqueue(object : Callback<ServerResponse<Data>> {
            override fun onResponse(
                call: Call<ServerResponse<Data>>,
                response: Response<ServerResponse<Data>>
            ) {
                val resp: ServerResponse<*>? = response.body()
                if (resp != null && resp.checkResponse_(response) == Common.STATUS_200) {

                    Log.e(TAG, "onResponse:Login Successfully. ")
                    mPreferenceUtils!!.save(
                        PreferenceUtils.PREF_KEY_BEARER_TOKEN, "Bearer " + response.body()!!
                            .accessToken
                    )
                    mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                    //save driver details in pref
                    mPreferenceUtils!!.saveDriverData(response.body()!!.data)

                    val data = mPreferenceUtils!!.driverData
                    Log.e(TAG, "Data: " + data!!.phone_verified + data.email + data.mobile)

                    if ((response.body()!!.data.phone_verified == "0")) {
                        Log.e(TAG, "Open register Activity")
                        showToastMessage(activity, "Please fill required fields for registration.")
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                        val registerIntent =
                            Intent(this@LoginActivity, RegisterActivity::class.java)
                        animationIntent(this@LoginActivity, registerIntent)
                        finish()
                    } else if ((response.body()!!.data.phone_verified == "1")) {
                        Log.e(TAG, "onResponse:Login ")
                        showToastMessage(activity, resp.message)

                        //save driver details in pref
                        mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                        Log.e(TAG, "onResponse: getLogin_type" + data.login_type)
                        if (ValidationUtils.isValidString(
                                mPreferenceUtils!!.get(
                                    PreferenceUtils.PREF_KEY_FCM_TOKEN,
                                    ""
                                )
                            )
                        ) {
                            //call device detail post api
                            apiCallDeviceDetail(data.id!!, data.email!!)
                        } else {
                            //get FIREBASE INSTANCE ID
                            FirebaseMessaging.getInstance().token
                                .addOnCompleteListener(object : OnCompleteListener<String?> {
                                    override fun onComplete(task: Task<String?>) {
                                        if (!task.isSuccessful) {
                                            Log.w(
                                                "cvcbvcbv",
                                                "Fetching FCM registration token failed",
                                                task.exception
                                            )
                                            return
                                        }

                                        // Get new FCM registration token
                                        val token = task.result
                                        mPreferenceUtils!!.save(
                                            PreferenceUtils.PREF_KEY_FCM_TOKEN,
                                            token
                                        )
                                        apiCallDeviceDetail(data.id!!, data.email!!)
                                    }
                                })
                        }
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(
                                response.errorBody()!!.string()
                            )
                            showToastMessage(activity, jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage(activity, (e.message)!!)
                        }
                    }
                    dismissProgress()
                }
            }

            override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                showToastMessage(activity, resources.getString(R.string.something_went_wrong))
                dismissProgress()
            }
        })
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            handleSignInResult(result)
        } else {
            mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
            Log.e(TAG, "onActivityResult: " + "mCallbackManager")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermissionUtils!!.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    private fun googleSignOut() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) Auth.GoogleSignInApi.signOut(
            mGoogleApiClient!!
        ).setResultCallback {
            mGoogleApiClient!!.disconnect()
            mGoogleApiClient!!.maybeSignOut()
        }
    }

    /**
     * @return Validation
     */
    private val isValidate: Boolean
        private get() {
            if (!ValidationUtils.isValidString(mEdtEmailOrMobile!!)) {
                mEdtEmailOrMobile!!.error = getString(R.string.enter_email_or_mobile_number)
                mEdtEmailOrMobile!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidEmail(mEdtEmailOrMobile!!) && !ValidationUtils.isValidMobile(
                    mEdtEmailOrMobile!!
                )
            ) {
                mEdtEmailOrMobile!!.error = getString(R.string.enter_valid_email_or_mobile_number)
                mEdtEmailOrMobile!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidString(mEdtPassword!!)) {
                mEdtPassword!!.error = getString(R.string.enter_password)
                mEdtPassword!!.requestFocus()
                return false
            } else if (mEdtPassword!!.text.toString().length < 6) {
                mEdtPassword!!.error = getString(R.string.password_length_must_be_six_character)
                mEdtPassword!!.requestFocus()
                return false
            }
            return true
        }

    fun alertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Coming soon")
        alertDialogBuilder.setPositiveButton(
            "ok"
        ) { arg0, arg1 -> }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.llFacebook -> if (ValidationUtils.isInternetAvailable(this)) {
                showProgress();
                facebookLogin();
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error));
                return;
            }  // alertDialog()
            R.id.llGoogle -> if (ValidationUtils.isInternetAvailable(this)) {
                showProgress();
                googleSignIn()
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error));
                return;
            }
            R.id.txtForget -> {
                val forgetIntent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                animationIntent(this@LoginActivity, forgetIntent)
            }
            R.id.txtLogin -> if (ValidationUtils.isInternetAvailable(this)) {
                if (isValidate) {
                    showProgress()
                    apiLoginDriver()
                }
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error))
                return
            }
        }
    }

    //check driver completed
    private fun checkStatus() {
        if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VERIFY_OTP, false)) {
            val intentVerifyActivity = Intent(this@LoginActivity, VerifyMobileActivity::class.java)
            intentVerifyActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@LoginActivity, intentVerifyActivity)
        } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_TYPE, false)) {
            val intentVehicleTypeActivity = Intent(this, SelectVehicleTypeActivity::class.java)
            intentVehicleTypeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@LoginActivity, intentVehicleTypeActivity)
        } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_DETAILS, false)) {
            val intentVehicleDetailActivity = Intent(this, AddVehicleDetailsActivity::class.java)
            intentVehicleDetailActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@LoginActivity, intentVehicleDetailActivity)
        } else if (!mPreferenceUtils!!.get(
                PreferenceUtils.PREF_KEY_DOCUMENT_UPLOAD_SUCCESSFULLY,
                false
            )
        ) {
            val intentDocumentActivity = Intent(this, DocumentActivity::class.java)
            intentDocumentActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@LoginActivity, intentDocumentActivity)
        }else if (mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_RIDE_ON, false)){
            val intent = Intent(this, OnRideActivity::class.java)
            animationIntent(this@LoginActivity,intent)
            finish()
        }else {
            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_LOGIN, true)
            val intentMainActivity = Intent(this, MainActivity::class.java)
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@LoginActivity, intentMainActivity)
            finish()
        }
    }

    private fun onCancelApiCall() {
        if (mApiLoginResponseCall != null && !mApiLoginResponseCall!!.isCanceled) {
            mApiLoginResponseCall!!.cancel()
        }
        if (mApiSocialLogin != null && !mApiSocialLogin!!.isCanceled) {
            mApiSocialLogin!!.cancel()
        }
    }

    //check permission and capture image
    private fun checkPermission() {
        mPermissionUtils = PermissionUtils(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ),
            object : OnPermissionGrantCallback {
                override fun onPermissionGranted() {}
                override fun onPermissionError(permission: String?) {}
            })
    }

    fun showProgress() {
        if (mProgressDialog != null) mProgressDialog!!.show()
    }

    fun dismissProgress() {
        if (mProgressDialog != null) mProgressDialog!!.dismiss()
    }

    override fun onPause() {
        super.onPause()
        dismissProgress()
        onCancelApiCall()
        googleSignOut()
        //if facebook login ,so logout with this
        facebookLogout()
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancelApiCall()
        googleSignOut()
        dismissProgress()
        //if facebook login ,so logout with this
        facebookLogout()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        dismissProgress()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)
        googleSignOut()
        //if facebook login ,so logout with this
        facebookLogout()
    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
        private const val RC_SIGN_IN = 7

        /**
         * Logout From Facebook
         */
        fun facebookLogout() {
            LoginManager.getInstance().logOut()
        }
    }

    private fun apiCallGetRideStatus() {

        mApiGetRideStatus = Singleton.restClient.getTripStatus(mPreferenceUtils?.driverData?.id)

        mApiGetRideStatus?.enqueue(object : Callback<RideStatusResponse> {
            override fun onResponse(
                call: Call<RideStatusResponse>,
                response: Response<RideStatusResponse>
            ) {
                try {
                    val response1 = response.body()?.data
                    val resp1: Any? = response.body()
                    val gson = Gson()
                    val json = gson.toJson(resp1)
                    val jObject = JSONObject(json)
                    //Response<ServerResponse>
                    if (jObject.getInt("status") == Common.STATUS_200) {
                        val status = response1?.status
                        if (response1 != null) {
                            saveCustomer(response1)
                        }
                        if(status.equals("Riding")){
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, true)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, true)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, true)
                            //preferenceUtils[PreferenceUtils.PREF_KEY_RIDE_ON, false]
                        }else if(status.equals("Completed")){
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, false)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, false)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_COMPLETE_TRIP, false)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_ON_TRIP, true)
                            mPreferenceUtils?.save(PreferenceUtils.PREF_KEY_IS_SHARED_RIDE_ON,false)
                        }else if(status.equals("Pending")){
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, true)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, true)
                        }else if(status.equals("Confirmed")){
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_RIDE_ON, true)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_PASS_CODE_CONFIRM, true)
                            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_START_TRIP, false)
                        }
                    }
                    checkStatus()
                } catch (e: Exception) {
                    e.printStackTrace()
                    checkStatus()
                }
            }

            override fun onFailure(call: Call<RideStatusResponse>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    resources.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
                checkStatus()
            }
        })
    }
    public fun saveCustomer(rideStatusResponse: RideStatusResponse.Data){
        var customer: CustomerRequestModel = CustomerRequestModel()
        customer.customer_id = rideStatusResponse.customer_id.toString()
        customer.pickup =  rideStatusResponse.pickup
        customer.bookid = rideStatusResponse.id.toString()
        customer.paymode =  rideStatusResponse.paymode
        customer.dlong =  rideStatusResponse.dlong
        customer.dlat =  rideStatusResponse.dlat
        customer.vt_id = rideStatusResponse.vt_id.toString()
        customer.departure =  rideStatusResponse.departure
        customer.image =  rideStatusResponse.cusers.image
        customer.km =  rideStatusResponse.km
        customer.name =  rideStatusResponse.cusers.name
        customer.plat =  rideStatusResponse.plat
        customer.charge =  rideStatusResponse.charge
        // customer.requestdatetime =  rideStatusResponse.requestdatetime
        customer.esttime = rideStatusResponse.esttime.toString()
        customer.estprice =  rideStatusResponse.estprice
        customer.driver_id =  rideStatusResponse.driver_id
        customer.plong =  rideStatusResponse.plong
         customer.mobile =  rideStatusResponse.cusers.mobile
        customer.estimate_time =  rideStatusResponse.estimate_time
        customer.is_schedule = rideStatusResponse.is_schedule.toString()
        customer.book_id = rideStatusResponse.id.toString()
        customer.is_admin_created =  rideStatusResponse.is_admin_created
        customer.is_sharing =  rideStatusResponse.is_sharing
        mPreferenceUtils?.saveCustomerRideData(customer)
    }
}


