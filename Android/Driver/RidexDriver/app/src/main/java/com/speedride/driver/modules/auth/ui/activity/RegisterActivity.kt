package com.speedride.driver.modules.auth.ui.activity

import android.app.Activity
import android.app.AlertDialog
import com.speedride.driver.base.BaseActivity
import com.speedride.driver.interfaces.ImagePickerListener
import com.google.android.gms.common.api.GoogleApiClient
import com.makeramen.roundedimageview.RoundedImageView
import com.speedride.driver.model.ServerResponse
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.content.pm.PackageManager
import okhttp3.RequestBody
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
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.*
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.tasks.OnCompleteListener
import com.speedride.driver.databinding.ActivityRegisterBinding
import com.speedride.driver.model.CountryCodelist
import com.speedride.driver.model.Data
import com.speedride.driver.modules.home.ui.activity.MainActivity
import com.speedride.driver.modules.userManagement.ui.activity.AddVehicleDetailsActivity
import com.speedride.driver.modules.userManagement.ui.activity.DocumentActivity
import com.speedride.driver.modules.userManagement.ui.activity.SelectVehicleTypeActivity
import com.speedride.driver.networkHelper.retrofit.RetrofitBuilder
import com.speedride.driver.rest.Singleton
import com.speedride.driver.rest.Singleton.Companion.restClient
import com.speedride.driver.utils.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import com.speedride.driver.model.AgencyModel
import com.speedride.driver.modules.auth.ui.activity.adapter.AgencySpinnerAdapter
import com.speedride.driver.modules.auth.ui.activity.adapter.CountrySpinnerAdapter
import com.speedride.driver.R
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity(), ImagePickerListener, View.OnClickListener,
    GoogleApiClient.OnConnectionFailedListener {

    private val binding by lazy {
       ActivityRegisterBinding.inflate(layoutInflater)
    }
    private lateinit var mCtx : Context
    lateinit var  imageUri : Uri

    private var mPreferenceUtils: PreferenceUtils? = null
    private var mImgUploadPhoto: RoundedImageView? = null
    private var mImagePickerDialog: ImagePickerDialog? = null
    private var mPermissionUtils: PermissionUtils? = null
    private var mEdtFirstName: EditText? = null
    private var mEdtLastName: EditText? = null
    private var mEdtEmail: EditText? = null
    private var mEdtMobile: EditText? = null
    private var mEdtPassword: EditText? = null
    private var mTxtNext: TextView? = null
    private var mTxtPassword: TextView? = null
    private var rbEmp: RadioButton? = null
    private var rbContractor: RadioButton? = null
    private var mViewPassword: View? = null
    private var mLlGoogle: LinearLayout? = null
    private var mLlFacebook: LinearLayout? = null
    private var mImageFile: File? = null
    private var mApiRegisterResponseCall: Call<ServerResponse<Data>>? = null
    private val mApiDeviceDetailCall: Call<ServerResponse<*>>? = null
    private var mApiSocialLogin: Call<ServerResponse<Data>>? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mCallbackManager: CallbackManager? = null
    private var facebookProfilePicture: Uri? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mCountryCODEListCall: Call<ServerResponse<List<CountryCodelist>>>? = null
    private var mAgencyList: Call<AgencyModel>? = null
    private var spinner:Spinner? = null
    private var agencySpinner:Spinner? = null
    private var mCustomArrayAdapter: CountrySpinnerAdapter? = null
    private var mAgencySpinnerAdapter: AgencySpinnerAdapter? = null
    private var countryList: ArrayList<CountryCodelist>? = null
    private var agencyList: ArrayList<AgencyModel.Data>? = null
    private var countryCode:String? = null
    private var agencyId:String? = null
    private var tvCountry: AppCompatTextView?= null
    private var tvAgency: AppCompatTextView?= null
    private var rgDriverType:RadioGroup?=null
    private var driverType:String?=null
    private var setRadioId:Int?=null
    private var mImgEditUser: RoundedImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mCtx = this
        initView(null)
        apiGETCountryCODEList()
        apiGetAgencyList()
    }

    override val activity: AppCompatActivity
        get() = this@RegisterActivity
    override val actionTitle: String
        get() = getString(R.string.register)
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
        mPreferenceUtils = PreferenceUtils.getInstance(this@RegisterActivity)
        mImgUploadPhoto = findViewById(R.id.imgUploadPhoto)
        mEdtFirstName = findViewById(R.id.edtFirstName)
        mEdtLastName = findViewById(R.id.edtLastName)
        mEdtEmail = findViewById(R.id.edtEmail)
        mEdtMobile = findViewById(R.id.edtMobile)
        mEdtPassword = findViewById(R.id.edtPassword)
        mTxtPassword = findViewById(R.id.txtPassword)
        rbEmp = findViewById(R.id.rbEmp)
        rbContractor = findViewById(R.id.rbContractor)
        mTxtNext = findViewById(R.id.txtNext)
        mLlGoogle = findViewById(R.id.llGoogle)
        mLlFacebook = findViewById(R.id.llFacebook)
        mViewPassword = findViewById(R.id.viewPassword)
        mImgEditUser = findViewById(R.id.imgEditUser)
        spinner =findViewById(R.id.spinner)
        agencySpinner =findViewById(R.id.spAgency)
        mTxtNext!!.setOnClickListener(this)
        mImgUploadPhoto!!.setOnClickListener(this)
        mLlGoogle!!.setOnClickListener(this)
        mLlFacebook!!.setOnClickListener(this)
        tvCountry = findViewById(R.id.tvCountry)
        tvAgency = findViewById(R.id.tvAgency)
        rgDriverType = findViewById(R.id.rgDriverType)
        mImgEditUser!!.setOnClickListener(this)
//        setRadioId =  rgDriverType!!.checkedRadioButtonId


        countryList  = arrayListOf()
        agencyList  = arrayListOf()

//        checkLoginStatusAndFillAllFields();

        tvCountry?.setOnClickListener {
            spinner?.performClick()
        }

        tvAgency?.setOnClickListener {
            spAgency.performClick()
        }
        setCountrySelection()
        setAgencySelection()
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun setCountrySelection(){
        spinner?.setOnItemSelectedListener(
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?, position: Int, id: Long
                ) {
                    // It returns the clicked item.
                    try {
                        val clickedItem: CountryCodelist =
                            parent.getItemAtPosition(position) as CountryCodelist
                        countryCode = clickedItem.phone
                        tvCountry?.setText(countryList?.get(position)?.phone)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            })
    }

    fun setAgencySelection(){
        agencySpinner?.setOnItemSelectedListener(
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?, position: Int, id: Long
                ) {
                    // It returns the clicked item.
                    try {
                        val clickedItem: AgencyModel.Data =
                            parent.getItemAtPosition(position) as AgencyModel.Data
                        agencyId = clickedItem.id.toString()
                        tvAgency?.setText(agencyList?.get(position)?.company_name)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            })
    }

    //check first time social login with register activity
    private fun checkLoginStatusAndFillAllFields() {
        val data = mPreferenceUtils!!.driverData
        if (ValidationUtils.isValidObject(data)) {
            if (data!!.phone_verified == "0" && mPreferenceUtils!!.get(
                    PreferenceUtils.PREF_KEY_SOCIAL_LOGIN,
                    false
                )
            ) {
                if (ValidationUtils.isValidString(data.name)) {
                    var first_name: String? = null
                    var last_name: String? = null
                    if (data.name?.contains(" ") == true) {
                        val parts = data.name?.split(" ",)?.toTypedArray() // escape .
                        parts?.let {
                            first_name = parts[0]
                            last_name = parts[1]
                        }

                    } else {
                        first_name = data.name.toString()
                        last_name = ".."
                    }
                    mEdtFirstName!!.setText(first_name)
                    mEdtFirstName!!.isFocusable = false
                    mEdtLastName!!.setText(last_name)
                    mEdtLastName!!.isFocusable = false
                }
                if (ValidationUtils.isValidString(data.email)) {
                    mEdtEmail!!.setText(data.email)
                    mEdtEmail!!.isFocusable = false
                } else {
                    mEdtEmail!!.setText(data.email)
                }
                if (ValidationUtils.isValidString(data.mobile)) {
                    mEdtMobile!!.setText(data.mobile)
                    mEdtMobile!!.isFocusable = false
                } else {
                    mEdtMobile!!.setText(data.mobile)
                }
                if (ValidationUtils.isValidString(data.image)) {
                    if (mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
                        if (data.name?.contains("users") == true) {
                            val path = data.image?.replace("'\'", "")
                            Glide.with(this).load(Common.UPLOAD_URL + path)
                                .placeholder(R.drawable.ic_driver_profile).into(
                                mImgUploadPhoto!!
                            )
                        } else {
                            val path = data.name?.replace("'\'", "")
                            Glide.with(this).load(path).placeholder(R.drawable.ic_driver_profile)
                                .into(
                                    mImgUploadPhoto!!
                                )
                        }
                    } else {
                        val path = data.image?.replace("'\'", "")
                        Glide.with(this).load(Common.UPLOAD_URL + path)
                            .placeholder(R.drawable.ic_driver_profile).into(
                            mImgUploadPhoto!!
                        )
                    }
                    mImgUploadPhoto!!.isEnabled = false
                }
                mTxtPassword!!.visibility = View.GONE
                mEdtPassword!!.visibility = View.GONE
                mViewPassword!!.visibility = View.GONE
            }
        }
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


    //api call for get vehicle type list
    private fun apiGETCountryCODEList() {
        mCountryCODEListCall = Singleton.restClient.countryCodeList
        mCountryCODEListCall!!.enqueue(object : Callback<ServerResponse<List<CountryCodelist>>> {
            override fun onResponse(
                call: Call<ServerResponse<List<CountryCodelist>>>,
                response: Response<ServerResponse<List<CountryCodelist>>>
            ) {
                dismissProgress()
                val resp = response.body()
                resp?.data?.let { countryList?.addAll(it) }
                if (resp != null && resp.status == 200) {
                    val vehicleTypeList = response.body()!!.data
                    mCustomArrayAdapter =
                       CountrySpinnerAdapter(
                            applicationContext,
                            R.layout.country_list_layout,
                           countryList!!
                        )
                    spinner?.adapter = mCustomArrayAdapter
                    val position = countryList?.indexOfFirst { it.phone.equals("+91") }
                    if (position != null) {
                        if(position>-1){
                            spinner?.setSelection(position!!)
                        }
                    }

                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(
                                this@RegisterActivity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            showToastMessage(this@RegisterActivity, e.message!!)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ServerResponse<List<CountryCodelist>>>, t: Throwable) {
                dismissProgress()
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                // AppLog.d(SelectVehicleTypeActivity.TAG, "onFailure:---- $t")
            }
        })

    }

    private fun apiGetAgencyList() {
        mAgencyList = Singleton.restClient.agencyList()
        mAgencyList!!.enqueue(object : Callback<AgencyModel> {
            override fun onResponse(
                call: Call<AgencyModel>,
                response: Response<AgencyModel>
            ) {
                dismissProgress()
                val resp = response?.body()

                Log.d(ContentValues.TAG, response!!.message())
                resp?.data?.let { agencyList?.addAll(it) }
                if(resp?.status== Common.STATUS_200) {
                   // val vehicleTypeList = response.body()!!.data
                    mAgencySpinnerAdapter =
                        AgencySpinnerAdapter(
                            applicationContext,
                            R.layout.agency_list_layout,
                            agencyList!!
                        )
                    agencySpinner?.adapter = mAgencySpinnerAdapter
//                    val position = agencyList?.indexOfFirst { it.phone.equals("+91") }
//                    if (position != null) {
//                        if(position>-1){
//                            spinner?.setSelection(position!!)
//                        }
//                    }

                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(
                                this@RegisterActivity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            showToastMessage(this@RegisterActivity, e.message!!)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<AgencyModel>, t: Throwable) {
                dismissProgress()
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                // AppLog.d(SelectVehicleTypeActivity.TAG, "onFailure:---- $t")
            }
        })

    }

    // sagar ********************************************************** uploads
    private fun apiCallRegisterData() {
        hideKeyboard(this)
        if(rgDriverType!!.checkedRadioButtonId == rbEmp?.id){
            driverType = "Employee"
        }else{
            driverType = "Contract"
        }
        val firstNameBody : RequestBody = mEdtFirstName!!.text.toString().toRequestBody("text/plain".toMediaType())
        val lastNameBody : RequestBody = mEdtLastName!!.text.toString().toRequestBody("text/plain".toMediaType())
        val emailBody : RequestBody = mEdtEmail!!.text.toString().toRequestBody("text/plain".toMediaType())
        val phoneBody : RequestBody = mEdtMobile!!.text.toString().toRequestBody("text/plain".toMediaType())
        val passwordBody : RequestBody = mEdtPassword!!.text.toString().toRequestBody("text/plain".toMediaType())
        val devicetypeBody : RequestBody = "Android".toRequestBody("text/plain".toMediaType())
        val android_id = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        val uniqueIdBody : RequestBody = android_id.toRequestBody("text/plain".toMediaType())
        val tockenBody : RequestBody = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_FCM_TOKEN, "").toRequestBody("text/plain".toMediaType())
        val roleBody : RequestBody = Common.ROLE_DRIVER.toRequestBody("text/plain".toMediaType())
        val countryIdBody =
            countryCode?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

        val contractBody =
            driverType?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

        val agencyBody =
            agencyId?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

        var body : MultipartBody.Part? = null
        if (mImageFile != null) {
            val mFile = mImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("image", mImageFile!!.name, mFile)
        }

        val retrofit = RetrofitBuilder.buildService(mCtx = mCtx)


        val call = retrofit.registerDriver(
            firstNameBody, lastNameBody, emailBody, phoneBody, passwordBody, body, devicetypeBody, uniqueIdBody, tockenBody,contractBody,agencyBody, countryIdBody
        )


        call.enqueue(object : Callback<ServerResponse<Data>> {
            override fun onResponse(
                call: Call<ServerResponse<Data>>,
                response: Response<ServerResponse<Data>>
            ) {
                val resp = response.body()
                if (resp != null && resp.status == 200) {
                    AppLog.d(
                        TAG, "onResponse:Register Msg " + response.body()!!
                            .message
                    )
                    AppLog.d(
                        TAG, "onResponse:Register Id " + (response.body()!!
                            .data?.id)
                    )
                    Log.e("ImageCheck", " response:: " + resp.message + " || status: " + resp.status + " || " + response.body()!!.data.image)
                    showToastMessage(activity, response.body()!!.message)
                    val userIdRegister: String
                    val userEmail: String
                    if (ValidationUtils.isValidString(response.body()!!.data?.id)) {
                        userIdRegister = response.body()!!.data?.id.toString()
                        userEmail = response.body()!!.data?.email.toString()

                        //save driver details in pref
                        mPreferenceUtils!!.saveDriverDataSignup(response.body()!!.data)
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
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            Log.e("error",jObjError.getString("message"))
                            showToastMessage(this@RegisterActivity, jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage(this@RegisterActivity, e.message!!)
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
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
            }
        })
    }

    /**
     * api call for Device app Detail
     */
    private fun apiCallDeviceDetail(registerId: String, userEmail: String) {
        connectSocket()
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_OTP_SEND, true)
        if (mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
            checkStatus()
        } else {
            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SIGN_UP, true)
            val verifyIntent = Intent(this@RegisterActivity, VerifyMobileActivity::class.java)
            verifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@RegisterActivity, verifyIntent)
            finish()
        }

        /* if (ValidationUtils.isInternetAvailable(this)) {
            String deviceOs = Build.VERSION.RELEASE;
            String model = Build.MODEL;
            String manufacture = Build.MANUFACTURER;
            String android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            AppLog.d(TAG, "onResponse: " + "Id " + registerId + "Android Id " + android_id + "Token: " + mPreferenceUtils.get(PreferenceUtils.PREF_KEY_FCM_TOKEN, ""));

            mApiDeviceDetailCall = Singleton.getInstance().getRestClient().deviceDetails(registerId, android_id, mPreferenceUtils.get(PreferenceUtils.PREF_KEY_FCM_TOKEN, ""), Common.ANDROID);
            mApiDeviceDetailCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    ServerResponse resp = response.body();

                    dismissProgress();

                    if (resp != null && resp.checkResponse(response) == 200) {
                        AppLog.d(TAG, "onResponse Device detail:---- " + response.body().getMsg() + "   " + new Gson().toJson(response));

                        connectSocket();
                        mPreferenceUtils.save(PreferenceUtils.PREF_KEY_OTP_SEND, true);

                        if (mPreferenceUtils.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
                            checkStatus();
                        } else {
                            mPreferenceUtils.save(PreferenceUtils.PREF_KEY_SIGN_UP, true);

                            showToastMessage(getActivity(), response.body().getMsg());
                            Intent verifyIntent = new Intent(RegisterActivity.this, VerifyMobileActivity.class);
                            verifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            animationIntent(RegisterActivity.this, verifyIntent);

                            finish();
                        }
                    } else {
                        if (response.body() != null && response.body().getMsg() != null)
                            showToastMessage(getActivity(), response.body().getMsg());
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    AppLog.d(TAG, "onFailure:---- " + t);
                    dismissProgress();
                    //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                }
            });
        } else {
            dismissProgress();
            showToastMessage(getActivity(), getActivity().getResources().getString(R.string.network_error));
        }*/
    }

    //check driver completed
    private fun checkStatus() {
        if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VERIFY_OTP, false)) {
            val intentVerifyActivity = Intent(this, VerifyMobileActivity::class.java)
            intentVerifyActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@RegisterActivity, intentVerifyActivity)
        } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_TYPE, false)) {
            val intentVehicleTypeActivity = Intent(this, SelectVehicleTypeActivity::class.java)
            intentVehicleTypeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@RegisterActivity, intentVehicleTypeActivity)
        } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_VEHICLE_DETAILS, false)) {
            val intentVehicleDetailActivity = Intent(this, AddVehicleDetailsActivity::class.java)
            intentVehicleDetailActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@RegisterActivity, intentVehicleDetailActivity)
        } else if (!mPreferenceUtils!!.get(
                PreferenceUtils.PREF_KEY_DOCUMENT_UPLOAD_SUCCESSFULLY,
                false
            )
        ) {
            val intentDocumentActivity = Intent(this, DocumentActivity::class.java)
            intentDocumentActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@RegisterActivity, intentDocumentActivity)
        } else {
            mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_LOGIN, true)
            val intentMainActivity = Intent(this, MainActivity::class.java)
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            animationIntent(this@RegisterActivity, intentMainActivity)
            finish()
        }
    }

    private fun connectSocket() {
        if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_SOCKET, false)) {
            Log.d(TAG, "connectSocket: not connect")
            val intent = Intent(this, ServiceClass::class.java)
            intent.action = Common.CONNECT_SOCKET
            startService(intent)
        }
    }

    // image picker call
    // sagar **********************************************************
    private fun callImagePicker_new() {
        Log.e("ImageCheck", " ImagePicker Open ")
        ImagePicker.with(this)
            .crop()
            .cropFreeStyle()
            .provider(ImageProvider.BOTH) //Or bothCameraGallery()
            .createIntentFromDialog { launcher.launch(it) }

    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                // Use the uri to load the image
                imageUri = uri

               // getImageFileFromPicker(uri.toFile())
                mImgUploadPhoto!!.setImageURI(uri)

                Log.e("ImageCheck", " imageUri::: " + imageUri)
                val filesDir = applicationContext.filesDir
                mImageFile = File(filesDir, "image.jpg")
                val inputStream = contentResolver.openInputStream(imageUri)
                val outputStream = FileOutputStream(mImageFile)
                inputStream!!.copyTo(outputStream)
            }
        }

    //select image from interface method
    private fun selectImage() {
        if (mImagePickerDialog == null) {
            mImagePickerDialog = ImagePickerDialog(this, this)
            mImagePickerDialog!!.selectImage()
        } else {
            mImagePickerDialog!!.selectImage()
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
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient!!)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleSignInResult(result: GoogleSignInResult?) {
        Log.d(TAG, "handleSignInResult:" + result!!.isSuccess)
        if (result.isSuccess) {
            showProgress()
            val data = mPreferenceUtils!!.driverData

            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            val firstName = acct!!.givenName
            val lastName = acct.familyName
            val email = acct.email
            val googleId = acct.id
            var mobile = ""
            if (ValidationUtils.isValidObject(data) && ValidationUtils.isValidString(
                    data!!.mobile
                )
            ) mobile = data.mobile.toString()
            val social_login_type = Common.LOGIN_TYPE_GOOGLE
            val user = Common.ROLE_DRIVER
            var user_id = ""
            if (data != null && ValidationUtils.isValidString(data.id)) user_id = data.id.toString()
            var personPhotoUrl: String? = ""
            if (acct.photoUrl != null) {
                personPhotoUrl = acct.photoUrl!!.path
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
                            social_login_type,
                            user,
                            user_id
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
            this@RegisterActivity, Arrays.asList("email", "public_profile")
        )
    }

    //facebook login
    private fun FbLoginInitialization() {
        //FacebookSdk.sdkInitialize(MainActivity.this);
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val token = result.accessToken
                    Log.e("token", token.toString() + "")
                    val profile = Profile.getCurrentProfile()
                    if (profile != null) {
                        Log.e(
                            "Profile pic url ",
                            profile.getProfilePictureUri(900, 900).toString() + ""
                        )
                        facebookProfilePicture = profile.getProfilePictureUri(900, 900)
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
                                    name.split(" ",).toTypedArray() // escape .
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
                                    role,
                                    user_id
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
                                        role,
                                        user_id
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // TODO: handle exception
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
                    Log.e("Error", error.toString())
                    Toast.makeText(this@RegisterActivity, "Login Error", Toast.LENGTH_SHORT).show()
                    dismissProgress()
                }

                override fun onCancel() {
                    Log.e("Cancel", "Cancel")
                    Toast.makeText(this@RegisterActivity, "Login Cancelled", Toast.LENGTH_SHORT)
                        .show()
                    dismissProgress()
                }
            })
    }

    private fun apiCallSocialLogin(
        googleId: String,
        email: String?,
        mobile: String,
        personPhotoUrl: String?,
        firstName: String,
        lastName: String,
        social_login_type: String,
        user: String,
        user_id: String
    ) {
        mApiSocialLogin = restClient.socialLogin(
            googleId,
            email,
            mobile,
            personPhotoUrl,
            firstName,
            lastName
        )
        mApiSocialLogin!!.enqueue(object : Callback<ServerResponse<Data>> {
            override fun onResponse(
                call: Call<ServerResponse<Data>>,
                response: Response<ServerResponse<Data>>
            ) {
                val resp: ServerResponse<*>? = response.body()
                if (resp != null && resp.status == 200) {
                    //Log.e(TAG, "onResponse:Signup Successfully. " + "Last nm" + lastName + new Gson().toJson(response));
                    if (response.body()!!.data?.phone_verified == "0") {
                        dismissProgress()
                        showToastMessage(activity, "Please fill required fields for registration.")

                        //save driver details in pref
                        mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                        checkLoginStatusAndFillAllFields()
                    } else if (response.body()!!.data?.phone_verified == "1") {
                        showToastMessage(activity, resp.message)
                        //save driver details in pref
                        mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, true)
                        val data = mPreferenceUtils!!.driverData
                        if (ValidationUtils.isValidString(
                                mPreferenceUtils!!.get(
                                    PreferenceUtils.PREF_KEY_FCM_TOKEN,
                                    ""
                                )
                            )
                        ) {
                            //call device detail post api
                            apiCallDeviceDetail(data?.id!!, data?.email!!)
                        } else {
                            /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(RegisterActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String newToken = instanceIdResult.getToken();
                                    Log.d(TAG, "onSuccess: newToken" + newToken);

                                    mPreferenceUtils.save(PreferenceUtils.PREF_KEY_FCM_TOKEN, newToken);
                                    apiCallDeviceDetail(data.getId(), data.getEmail());
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
                                    if (data != null) {
                                        data.id?.let { data.email?.let { it1 ->
                                            apiCallDeviceDetail(it,
                                                it1
                                            )
                                        } }
                                    }
                                })
                        }
                    }
                } else {
                    dismissProgress()
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(this@RegisterActivity, jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage(this@RegisterActivity, e.message!!)
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

    private val isValidate: Boolean
        private get() {
            if (!ValidationUtils.isValidString(mEdtFirstName!!)) {
                mEdtFirstName!!.error = getString(R.string.enter_first_name)
                mEdtFirstName!!.requestFocus()
                return false
            } else if (mEdtFirstName!!.text.toString().length < 2) {
                mEdtFirstName!!.error = getString(R.string.enter_minimum_2_character)
                mEdtFirstName!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidString(mEdtLastName!!)) {
                mEdtLastName!!.error = getString(R.string.enter_last_name)
                mEdtLastName!!.requestFocus()
                return false
            } else if (mEdtLastName!!.text.toString().length < 2) {
                mEdtLastName!!.error = getString(R.string.enter_minimum_2_character)
                mEdtLastName!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidString(mEdtEmail!!)) {
                mEdtEmail!!.error = getString(R.string.enter_email_id)
                mEdtEmail!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidEmail(mEdtEmail!!)) {
                mEdtEmail!!.error = getString(R.string.enter_valid_email_id)
                mEdtEmail!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidString(mEdtMobile!!)) {
                mEdtMobile!!.error = getString(R.string.enter_mobile_number)
                mEdtMobile!!.requestFocus()
                return false
            } else if (!ValidationUtils.isValidMobile(mEdtMobile!!)) {
                mEdtMobile!!.error = getString(R.string.enter_valid_mobile_number)
                mEdtMobile!!.requestFocus()
                return false
            } else if (!mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
                if (!ValidationUtils.isValidString(mEdtPassword!!)) {
                    mEdtPassword!!.error = getString(R.string.enter_password)
                    mEdtPassword!!.requestFocus()
                    return false
                } else if (mEdtPassword!!.text.toString().length < 6) {
                    mEdtPassword!!.error = getString(R.string.password_length_must_be_six_character)
                    mEdtPassword!!.requestFocus()
                    return false
                } else if (mImageFile == null) {
                    showToastMessage(
                        activity,
                        activity.resources.getString(R.string.please_select_profile_picture)
                    )
                    return false
                }
            }
            if(rgDriverType!!.checkedRadioButtonId == -1){
                showToastMessage(
                    activity,
                    activity.resources.getString(R.string.please_select_driver_type)
                )
                return false
            }
            return true
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermissionUtils!!.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            handleSignInResult(result)
        } else if (requestCode == ImagePickerDialog.REQUEST_CAMERA || requestCode == ImagePickerDialog.SELECT_FILE) {
            mImagePickerDialog!!.onActivityResult(requestCode, resultCode, data!!)
        } else {
            mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
            Log.e(TAG, "onActivityResult: " + "mCallbackManager")
        }
    }

     override fun getImageFileFromPicker(image: File?) {
        if (image != null) {
            mImageFile = image
            mImgUploadPhoto!!.setImageBitmap(mImagePickerDialog!!.getBitmapFromFile(image))
        }
    }

    private fun onCancelApiCall() {
        if (mApiRegisterResponseCall != null && !mApiRegisterResponseCall!!.isCanceled) {
            mApiRegisterResponseCall!!.cancel()
        }
        if (mApiDeviceDetailCall != null && !mApiDeviceDetailCall.isCanceled) {
            mApiDeviceDetailCall.cancel()
        }
        if (mApiSocialLogin != null && !mApiSocialLogin!!.isCanceled) {
            mApiSocialLogin!!.cancel()
        }
    }

    private fun googleSignOut() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) Auth.GoogleSignInApi.signOut(
            mGoogleApiClient!!
        ).setResultCallback {
            mGoogleApiClient!!.disconnect()
            mGoogleApiClient!!.maybeSignOut()
        }
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
            R.id.imgEditUser -> callImagePicker_new()
            R.id.llFacebook ->                /* if (ValidationUtils.isInternetAvailable(this)) {
                    showProgress();
                    facebookLogin();
                } else {
                    showToastMessage(getActivity(), getActivity().getResources().getString(R.string.network_error));
                    return;
                }*/alertDialog()
            R.id.llGoogle -> //                if (ValidationUtils.isInternetAvailable(this)) {
//                    showProgress();
//                    googleSignIn();
//                } else {
//                    showToastMessage(getActivity(), getActivity().getResources().getString(R.string.network_error));
//                    return;
//                }
                alertDialog()
            R.id.txtNext -> if (ValidationUtils.isInternetAvailable(this)) {
                if (mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)) {
                    if (isValidate) {
                        val data = mPreferenceUtils!!.driverData
                        if (ValidationUtils.isValidObject(data)) {
                            showProgress()
                            var first_name = "Ride"
                            var last_name = "Driver"
                            if (ValidationUtils.isValidString(mPreferenceUtils!!.driverData?.name)) {
                                if (mPreferenceUtils!!.driverData?.name?.contains(" ") == true) {
                                    val parts =
                                        mPreferenceUtils!!.driverData?.name?.split(" ",)
                                            ?.toTypedArray() // escape .
                                    parts?.let {
                                        first_name = parts[0]
                                        last_name = parts[1]
                                    }

                                } else {
                                    first_name = mPreferenceUtils!!.driverData?.name.toString()
                                    last_name = ".."
                                }
                            }
                            apiCallSocialLogin(data?.social_id!!,
                                mEdtEmail!!.text.toString().trim { it <= ' ' },
                                mEdtMobile!!.text.toString().trim { it <= ' ' },
                                data.image,
                                first_name,
                                last_name,
                                data.login_type!!,
                                data.role!!,
                                data.id!!
                            )
                        }
                    }
                } else {
                    if (isValidate) {
                      //  Log.e("","")
                       showProgress()
                      apiCallRegisterData()
                    }
                }
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error))
                return
            }
            R.id.imgUploadPhoto -> callImagePicker_new()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mPreferenceUtils!!.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false)
        googleSignOut()
        //if facebook login ,so logout with this
        facebookLogout()
    }

    fun showProgress() {
        if (mProgressDialog != null) mProgressDialog!!.show()
    }

    fun dismissProgress() {
        if (mProgressDialog != null) mProgressDialog!!.dismiss()
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
        dismissProgress()
        onCancelApiCall()
        googleSignOut()
        //if facebook login ,so logout with this
        facebookLogout()
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")
        //mPreferenceUtils.save(PreferenceUtils.PREF_KEY_SOCIAL_LOGIN, false);
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

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
        private const val RC_SIGN_IN = 7

        /**
         * Logout From Facebook
         */
        fun facebookLogout() {
            LoginManager.getInstance().logOut()
        }
    }
}