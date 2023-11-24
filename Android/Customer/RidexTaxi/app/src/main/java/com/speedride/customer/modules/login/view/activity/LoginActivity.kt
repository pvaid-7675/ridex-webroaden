package com.speedride.customer.modules.login.view.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.facebook.*
import com.facebook.GraphRequest.GraphJSONObjectCallback
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.speedride.customer.R
import com.speedride.customer.base.BaseActivity
import com.speedride.customer.modules.login.model.Data
import com.speedride.customer.modules.main.view.activity.MainActivity
import com.speedride.customer.modules.register.view.activity.RegisterActivity
import com.speedride.customer.modules.utils.*
import com.speedride.customer.modules.utils.Singleton.Companion.restClient
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginActivity() : BaseActivity(), View.OnClickListener,
    GoogleApiClient.OnConnectionFailedListener {

    /*private var mEdtEmailOrMobile: AppCompatEditText? = null
    private var edtPassword: AppCompatEditText? = null
    private var mTxtLogin: AppCompatTextView? = null
    private var mTxtForget: AppCompatTextView? = null*/

    private var mPreference: PreferenceUtils? = null
    private var mLoginResponseCall: Call<com.speedride.customer.model.ServerResponse<Data>>? = null
    private var mApiSocialLogin: Call<com.speedride.customer.model.ServerResponse<Data>>? = null
    private var mProgressDialog: ProgressDialog? = null

    //google login
    //private var mLlGoogle: LinearLayout? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    //facebook login
    //private var mLlFacebook: LinearLayout? = null
    private var mCallbackManager: CallbackManager? = null
    private var facebookProfilePicture: Uri? = null
    var android_id: String? = null
    val activity: Activity = this@LoginActivity

    public override fun onStart() {
        super.onStart()
        /*OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            AppLog.Companion.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
//            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            if (!mPreference.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
                showProgress();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                        hideProgress();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        android_id = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        initView(null)
    }

    fun alertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Coming soon")
        alertDialogBuilder.setPositiveButton("ok") { arg0, arg1 -> }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override val actionTitle: String
        get() = getString(R.string.login_small)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back_white
    }

    override fun initView(view: View?) {
        setToolbar(findViewById(R.id.appbar))
        mPreference = PreferenceUtils.getInstance(this@LoginActivity)
        Log.e("fcm token", mPreference?.get(PreferenceUtils.PREF_KEY_FCM_TOKEN, "")!!)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setMessage(getString(R.string.please_wait_msg))
        mProgressDialog!!.hide()
        googleLoginInitialization()
        printKeyHashes()
        fbLoginInitialization()

        /*mEdtEmailOrMobile = findViewById(R.id.edtEmailOrMobile)
        edtPassword = findViewById(R.id.edtPassword)
        mTxtLogin = findVaqiewById(R.id.txtLogin)
        mTxtForget = findViewById(R.id.txtForget)
        mLlGoogle = findViewById(R.id.llGoogle)
        mLlFacebook = findViewById(R.id.llFacebook)*/

        txtLogin?.setOnClickListener(this)
        txtForget?.setOnClickListener(this)
        llGoogle?.setOnClickListener(this)
        llFacebook?.setOnClickListener(this)

        /*mEdtEmailOrMobile.setText("customer@mailinator.com");
        edtPassword.setText("customer@123");*/
    }

    override fun showToastMessage(message: String) {

        try {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        try {
//            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        activity.runOnUiThread {
//            Toast.makeText(
//                activity,
//                " $message",
//                Toast.LENGTH_LONG
//            ).show()
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.llGoogle -> if (ValidationUtils.isInternetAvailable(this)) {
                googleSignIn();
            } else {
                showToastMessage(getResources().getString(R.string.network_error));
                return;
            }
            // alertDialog()
            R.id.llFacebook -> if (ValidationUtils.isInternetAvailable(this)) {
                showProgress();
                facebookLogin();
            } else {
                showToastMessage(getResources().getString(R.string.network_error));
                return;
            }
            // alertDialog()
            R.id.txtForget -> {
                val forgetIntent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                animationIntent(this, forgetIntent)
            }
            R.id.txtLogin -> if (ValidationUtils.isInternetAvailable(this)) {
                if (isValidate) {
                    showProgress()
                    apiLoginData()
                }
            } else {
                showToastMessage(resources.getString(R.string.network_error))
                return
            }
        }
    }

    private fun apiLoginData() {
        mLoginResponseCall = restClient.loginCustomer(
            edtEmailOrMobile!!.text.toString(),
            edtPassword!!.text.toString(),
            Common.ROLE_CUSTOMER,
            Common.ANDROID,
            android_id,
            mPreference!!.get(PreferenceUtils.PREF_KEY_FCM_TOKEN)
        )

        Log.e("TaxiTest", "_____________ apiLoginData")
        Log.e("TaxiTest", "Email ________" + edtEmailOrMobile!!.text.toString())
        Log.e("TaxiTest", "Password _____" + edtPassword!!.text.toString())
        Log.e("TaxiTest", "Role _________" + Common.ROLE_CUSTOMER)
        Log.e("TaxiTest", "Android ______" + Common.ANDROID)
        Log.e("TaxiTest", "android_id ___" + android_id)
        Log.e("TaxiTest", "Token ________" + mPreference!!.get(PreferenceUtils.PREF_KEY_FCM_TOKEN))

        mLoginResponseCall?.enqueue(object :
            Callback<com.speedride.customer.model.ServerResponse<Data>> {
            override fun onResponse(
                call: Call<com.speedride.customer.model.ServerResponse<Data>>,
                response: Response<com.speedride.customer.model.ServerResponse<Data>>
            ) {
                Log.e("TaxiTest", "onResponse (apiLoginData) ________" + response.body())
                val resp: com.speedride.customer.model.ServerResponse<*>? = response.body()
                if (resp != null && resp.checkResponse_(response) == Common.STATUS_200) {
                    AppLog.Companion.e(TAG, "onResponse:MSG: " + response.body()!!.message)
                    //AppLog.Companion.e(TAG, "onResponse:Data: " + new Gson().toJson(response));
                    val userIdRegister: String
                    val userEmail: String
                    if (response.body()!!.data.id != null) {
                        userIdRegister = response.body()!!.data.id!!
                        userEmail = response.body()!!.data.email!!
                        //call device detail post api
                        mPreference!!.saveUserInfo(response.body()!!.data)
                        mPreference!!.save(
                            PreferenceUtils.PREF_KEY_BEARER_TOKEN,
                            "Bearer " + response.body()!!.accessToken
                        )
                        if (ValidationUtils.isValidString(
                                mPreference!!.get(
                                    PreferenceUtils.PREF_KEY_FCM_TOKEN, ""
                                )
                            )
                        ) {
                            // Utils.showToast(activity,response.body()!!.message)
                            showToastMessage(response.body()!!.message)
                            //call device detail post api
                            apiDeviceDetail(userIdRegister, userEmail)
                        } else {
                            FirebaseMessaging.getInstance().token.addOnCompleteListener(object :
                                    OnCompleteListener<String?> {
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
                                        mPreference!!.save(
                                            PreferenceUtils.PREF_KEY_FCM_TOKEN, token
                                        )
                                        apiDeviceDetail(userIdRegister, userEmail)
                                    }
                                })
                        }
                    }

                }
                /* else if (resp != null && resp.checkResponse(response) == Common.STATUS_400 && response.body().getData().getPhone_verified().equals("0")) {
                    AppLog.Companion.d(TAG, "onResponse Device detail:---- " + response.body().getMessage() + "   " + new Gson().toJson(response));
                    showToastMessage(resp.getMessage());
                    String userIdRegister, userEmail;
                    if (response.body().getData().getId() != null) {
                        userIdRegister = response.body().getData().getId();
                        userEmail = response.body().getData().getEmail();
                        mPreference.save(PreferenceUtils.PREF_KEY_USER_ID, userIdRegister);
                        mPreference.save(PreferenceUtils.PREF_KEY_USER_EMAIL, userEmail);
                        mPreference.saveUserInfo(response.body().getData());
                        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        mApiDeviceDetailCall = Singleton.getInstance().getRestClient().deviceDetails(userIdRegister, android_id, "token_abcd", Common.ANDROID);
                        mApiDeviceDetailCall.enqueue(new Callback<ServerResponse>() {
                            @Override
                            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                                Intent verifyIntent = new Intent(LoginActivity.this, VerifyMobileActivity.class);
                                verifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                animationIntent(LoginActivity.this, verifyIntent);
                                finish();
                            }

                            @Override
                            public void onFailure(Call<ServerResponse> call, Throwable t) {
                            }
                        });
                        AppLog.Companion.d(TAG, "onResponse: " + "Id " + userIdRegister + "Android Id " + android_id);
                    }
                }*/
                else {
                    hideProgress()
                    //showToastMessage(response.message())
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(
                                response.errorBody()!!.string()
                            )
                            showToastMessage(jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage((e.message)!!)
                        }
                    }else if(response.body() != null) {
                        showToastMessage(response.body()!!.message)
                    }else{
                        showToastMessage("Something go wrong")
                    }
                }
            }

            override fun onFailure(
                call: Call<com.speedride.customer.model.ServerResponse<Data>>, t: Throwable
            ) {
                AppLog.Companion.e(TAG, "onFailure: $t")
                hideProgress()
            }
        })
    }

    private fun apiDeviceDetail(registerId: String, userEmail: String) {
        mPreference!!.save(PreferenceUtils.PREF_KEY_USER_ID, registerId)
        mPreference!!.save(PreferenceUtils.PREF_KEY_USER_EMAIL, userEmail)
        connectSocket()
        if (mPreference!!.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
            mPreference!!.save(PreferenceUtils.PREF_KEY_LOGIN, true)
            val intentMainActivity = Intent(this@LoginActivity, MainActivity::class.java)
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@LoginActivity, intentMainActivity)
            finish()
        } else {
            mPreference!!.save(PreferenceUtils.PREF_KEY_REGISTER, true)
            checkStatus()
        }

        /*if (ValidationUtils.isInternetAvailable(this)) {

            AppLog.Companion.d(TAG, "onResponse: " + "Id " + registerId + "Android Id " + android_id);

            mApiDeviceDetailCall = Singleton.getInstance().getRestClient().deviceDetails(registerId, android_id, mPreference.get(PreferenceUtils.PREF_KEY_FCM_TOKEN, ""), Common.ANDROID);
            mApiDeviceDetailCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    ServerResponse resp = response.body();

                    hideProgress();

                    if (resp != null && resp.checkResponse(response) == Common.STATUS_200) {
                        AppLog.Companion.d(TAG, "onResponse Device detail:---- " + response.body().getMessage() + "   " + new Gson().toJson(response));

                        AppLog.Companion.d(TAG, "onResponse Device detail:---- " + response.body().getMessage() + "   " + new Gson().toJson(response));

                        mPreference.save(PreferenceUtils.PREF_KEY_USER_ID, registerId);
                        mPreference.save(PreferenceUtils.PREF_KEY_USER_EMAIL, userEmail);

                        connectSocket();

                        if (mPreference.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
                            mPreference.save(PreferenceUtils.PREF_KEY_LOGIN, true);

                            Intent intentMainActivity = new Intent(LoginActivity.this, MainActivity.class);
                            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            animationIntent(LoginActivity.this, intentMainActivity);
                            finish();

                        } else {

                            mPreference.save(PreferenceUtils.PREF_KEY_REGISTER, true);
                            checkStatus();
                        }
                    } else {
                        if (response.body().getMessage() != null) {
                            showToastMessage(response.body().getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    AppLog.Companion.d(TAG, "onResponse:---- " + t);
                    hideProgress();
                    showToastMessage(getResources().getString(R.string.something_went_wrong));
                }
            });
        } else {
            hideProgress();
            showToastMessage(getResources().getString(R.string.network_error));
        }*/
    }

    private fun connectSocket() {
        if (!mPreference!!.get(PreferenceUtils.PREF_KEY_SOCKET, false)) {
            Log.d(TAG, "connectSocket: not connect")
            val intent = Intent(this, com.speedride.customer.service.ServiceClass::class.java)
            intent.action = Common.CONNECT_SOCKET
            startService(intent)
        }
    }

    private fun checkStatus() {
        if (!mPreference!!.get(PreferenceUtils.PREF_KEY_VERIFY_OTP, false)) {
            val intentHomeActivity = Intent(this, VerifyMobileActivity::class.java)
            intentHomeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentHomeActivity)
            finish()
        } else {
            mPreference!!.save(PreferenceUtils.PREF_KEY_LOGIN, true)
            val intentHomeActivity = Intent(this, MainActivity::class.java)
            intentHomeActivity.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentHomeActivity)
            finish()
        }
    }

    /**
     * @return Validation
     */
    private val isValidate: Boolean
        private get() {
            if (!ValidationUtils.isValidString(edtEmailOrMobile)) {
                edtEmailOrMobile!!.error = getString(R.string.enter_email_or_mobile_number)
                edtEmailOrMobile!!.requestFocus()
                return false
            } else if (!ValidationUtils.isEmailValid(edtEmailOrMobile!!.text.toString()) && !ValidationUtils.isValidMobile(
                    edtEmailOrMobile
                )
            ) {
                edtEmailOrMobile!!.error = getString(R.string.enter_valid_email_or_mobile_number)
                edtEmailOrMobile!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidString(edtPassword)) {
                edtPassword!!.error = getString(R.string.enter_password)
                edtPassword!!.requestFocus()
                return false
            } else if (edtPassword!!.text.toString().length < 6) {
                edtPassword!!.error = getString(R.string.password_length_must_be_six_character)
                edtPassword!!.requestFocus()
                return false
            } else {
                return true
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        } else {
            mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
            AppLog.Companion.e(TAG, "onActivityResult: " + "mCallbackManager")
        }
    }

    /*
     * This all code for google login and onStart() is also for google login
     * */
    //google login
    private fun googleLoginInitialization() {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build()
    }

    private fun googleSignIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun googleSignOut() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) Auth.GoogleSignInApi.signOut(
            mGoogleApiClient
        ).setResultCallback(ResultCallback {
            mGoogleApiClient!!.disconnect()
            mGoogleApiClient!!.maybeSignOut()
        })
    }

    private fun handleSignInResult(result: GoogleSignInResult?) {
        AppLog.Companion.d(TAG, "handleSignInResult:" + result!!.isSuccess)
        if (result.isSuccess) {
            val data = mPreference!!.userInfo
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            AppLog.Companion.d(TAG, "iddddddddddd" + acct!!.id)
            val google_id: String = acct.id.toString()
            val firstName: String = acct.givenName.toString()
            val lastName: String = acct.familyName.toString()
            var personPhotoUrl = ""
            val email: String = acct.email.toString()
            var mobile = ""
            if (data != null && ValidationUtils.isValidString(data.mobile)) mobile = data.mobile!!
            val google = Common.LOGIN_TYPE_GOOGLE
            val user = Common.ROLE_CUSTOMER
            //  var user_id = ""
            // if (data != null && ValidationUtils.isValidString(data.id)) user_id = data.id!!
            if (acct.photoUrl != null) {
                personPhotoUrl = acct.photoUrl.toString()
            }
            AppLog.Companion.e(TAG, "google_id$google_id")
            AppLog.Companion.e(TAG, "firstName: $firstName,\n lastName: $lastName")
            AppLog.Companion.e(TAG, "email: $email,\n Image: $personPhotoUrl")
            showProgress()
            apiCallSocialLogin(
                google_id,
                email,
                mobile,
                personPhotoUrl,
                firstName,
                lastName,
                google,
                user,
                //  user_id
            )
        } else {
            // Signed out, show unauthenticated UI.
            hideProgress()
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(
            this@LoginActivity, Arrays.asList("email", "public_profile", "user_friends")
        )
        //  fbLoginInitialization()
    }

    private fun fbLoginInitialization() {
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    AppLog.Companion.e("onSuccess", "OnSuccess method")
                    val token = result.accessToken
                    AppLog.Companion.e("token", token.toString() + "")
                    val profile = Profile.getCurrentProfile()
                    if (profile != null) {
                        Log.e(
                            "Profile pic url ",
                            profile.getProfilePictureUri(900, 900).toString() + ""
                        )
                        facebookProfilePicture = profile.getProfilePictureUri(900, 900)
                    }
                    val graphRequest =
                        GraphRequest.newMeRequest(token, object : GraphJSONObjectCallback {
                            override fun onCompleted(user: JSONObject, response: GraphResponse) {
                                AppLog.Companion.e("JSonobject", user.toString() + "")
                                val email: String
                                try {
                                    val data = mPreference!!.userInfo
                                    val fb_id = user.optString("id")
                                    AppLog.Companion.e(TAG, "onCompleted: fb_id: $fb_id")
                                    val profilePicture =
                                        URL("https:graph.facebook.com/" + user.getString("id") + "/picture?large")
                                    email = user.optString("email")
                                    AppLog.Companion.e(TAG, "onCompleted: email: $email")
                                    AppLog.Companion.e(
                                        TAG, "onCompleted: profilePicture: $profilePicture"
                                    )
                                    val name = user.optString("name")
                                    val first_name: String
                                    val last_name: String
                                    if (name.contains(" ")) {
                                        //val parts = name.split(" ", 2.toBoolean()).toTypedArray() // escape .
                                        val parts = name.split(" ").toTypedArray() // escape .
                                        first_name = parts[0]
                                        last_name = parts[1]
                                    } else {
                                        first_name = name
                                        last_name = ".."
                                        AppLog.Companion.e(
                                            TAG, "onCompleted:else F$first_name L$last_name"
                                        )
                                    }
                                    AppLog.Companion.e(TAG, "onCompleted: first_name $first_name")
                                    AppLog.Companion.e(TAG, "onCompleted: last_name $last_name")
                                    val mobile = ""
                                    val social_login_type = Common.LOGIN_TYPE_FACEBOOK
                                    val role = Common.ROLE_CUSTOMER
                                    var user_id = ""
                                    if (data != null && ValidationUtils.isValidString(data.id)) user_id =
                                        data.id!!
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
                                }
                            }
                        })
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email,picture")
                    graphRequest.parameters = parameters
                    graphRequest.executeAsync()
                }

                override fun onCancel() {
                    dismissProgress()
                    AppLog.Companion.e("Cancel", "Cancel")
                    showToastMessage("Login Cancelled")
//                    hideProgress()
                }

                override fun onError(error: FacebookException) {
                    AppLog.Companion.e("Error", error.toString())
                    showToastMessage("Login Error")
                    hideProgress()
                }
            })
    }

    private fun printKeyHashes() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature: Signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                AppLog.Companion.i(TAG, "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            AppLog.Companion.e(TAG, "printHashKey()", e)
        } catch (e: Exception) {
            AppLog.Companion.e(TAG, "printHashKey()", e)
        }
    }

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
            socialId, email, mobile, personPhotoUrl, firstName, lastName, loginType, role
            //  user_id
        )

        mApiSocialLogin?.enqueue(object : Callback<com.speedride.customer.model.ServerResponse<Data>> {
            override fun onResponse(
                call: Call<com.speedride.customer.model.ServerResponse<Data>>,
                response: Response<com.speedride.customer.model.ServerResponse<Data>>
            ) {
                val resp: com.speedride.customer.model.ServerResponse<*>? = response.body()
                if (resp != null && resp.checkResponse_(response) == Common.STATUS_200) {

                    //AppLog.Companion.e(TAG, "onResponse:Login Successfully. " + new Gson().toJson(response));
                    mPreference!!.saveUserInfo(response.body()!!.data)
                    mPreference!!.save(
                        PreferenceUtils.PREF_KEY_BEARER_TOKEN,
                        "Bearer " + response.body()!!.accessToken
                    )
                    val data = mPreference!!.userInfo
                    AppLog.Companion.e(
                        TAG, "Data: " + data!!.phone_verified + data.email + data.mobile
                    )
                    hideProgress()
                    if ((response.body()!!.data.phone_verified == "0")) {
                        AppLog.Companion.e(TAG, "Open register Activity")
                        showToastMessage("Please fill required fields for registration.")
                        mPreference!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                        val registerIntent =
                            Intent(this@LoginActivity, RegisterActivity::class.java)
                        animationIntent(this@LoginActivity, registerIntent)
                        finish()
                    } else if ((response.body()!!.data.phone_verified == "1")) {
                        AppLog.Companion.e(TAG, "onResponse:Login ")
                        showToastMessage(resp.message)

                        //save driver details in pref
                        mPreference!!.saveUserInfo(response.body()!!.data)
                        mPreference!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                        AppLog.Companion.d(TAG, "onResponse: getLogin_type" + data.login_type)
                        if (ValidationUtils.isValidString(
                                mPreference!!.get(
                                    PreferenceUtils.PREF_KEY_FCM_TOKEN, ""
                                )
                            )
                        ) {
                            //call device detail post api
                            apiDeviceDetail(data.id!!, data.email!!)
                        } else {
                            //get FIREBASE INSTANCE ID
                            FirebaseMessaging.getInstance().token.addOnCompleteListener(object :
                                    OnCompleteListener<String?> {
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
                                        mPreference!!.save(
                                            PreferenceUtils.PREF_KEY_FCM_TOKEN, token
                                        )
                                        apiDeviceDetail(data.id!!, data.email!!)
                                    }
                                })
                        }
                    }
                } else {
                    hideProgress()
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(
                                response.errorBody()!!.string()
                            )
                            showToastMessage(jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage((e.message)!!)
                        }
                    }
                    hideProgress()
                }
            }

            override fun onFailure(
                call: Call<com.speedride.customer.model.ServerResponse<Data>>, t: Throwable
            ) {
                showToastMessage(resources.getString(R.string.something_went_wrong))
                hideProgress()
            }
        })
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        hideProgress()
    }

    private fun onCancelApiCall() {
        if (mLoginResponseCall != null && !mLoginResponseCall!!.isCanceled) {
            mLoginResponseCall!!.cancel()
        }
        if (mApiSocialLogin != null && !mApiSocialLogin!!.isCanceled) {
            mApiSocialLogin!!.cancel()
        }
    }

    fun showProgress() {
        if (mProgressDialog != null && !mProgressDialog!!.isShowing) mProgressDialog!!.show()
    }

    fun hideProgress() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) mProgressDialog!!.hide()
    }

    fun dismissProgress() {
        if (mProgressDialog != null) mProgressDialog!!.dismiss()
    }

    override fun onPause() {
        super.onPause()
        hideProgress()
        onCancelApiCall()
        //googleSignOut()
        facebookLogout()
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancelApiCall()
        //googleSignOut()
        dismissProgress()
        facebookLogout()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mPreference!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)
        //googleSignOut()
        facebookLogout()
    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
        private val RC_SIGN_IN = 7
        fun facebookLogout() {
            if (LoginManager.getInstance() != null) {
                LoginManager.getInstance().logOut()
            }
        }
    }
}