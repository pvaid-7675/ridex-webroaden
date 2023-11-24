package com.speedride.driver.modules.userManagement.ui.activity

import android.Manifest
import android.app.Activity

import com.speedride.driver.base.BaseActivity
import com.speedride.driver.interfaces.ImagePickerListener
import com.makeramen.roundedimageview.RoundedImageView
import com.speedride.driver.model.ServerResponse
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import com.speedride.driver.R
import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.utils.PermissionUtils.OnPermissionGrantCallback
import android.content.Intent
import com.google.gson.Gson
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.speedride.driver.model.CountryCodelist
import com.speedride.driver.model.Data
import com.speedride.driver.modules.auth.ui.activity.adapter.CountrySpinnerAdapter
import com.speedride.driver.rest.Singleton
import com.speedride.driver.utils.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import org.json.JSONObject
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.ArrayList

class EditProfileActivity : BaseActivity(), View.OnClickListener, ImagePickerListener {
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mImgUploadPhoto: RoundedImageView? = null
    private var mImgEditUser: RoundedImageView? = null
    private var mTxtUpdate: TextView? = null
    private var mEdtFirstName: EditText? = null
    private var mEdtLastName: EditText? = null
    private var mEdtEmail: EditText? = null
    private var mEdtMobile: EditText? = null
    private var mImagePickerDialog: ImagePickerDialog? = null
    private var mPermissionUtils: PermissionUtils? = null
    private var mImage: File? = null
    private var mApiUpdateProfile: Call<ServerResponse<Data>>? = null
    private var mApiGetProfileData: Call<ServerResponse<Data>>? = null
    private var mStrDriverId: String? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mProgressBar: ProgressBar? = null
    lateinit var  imageUri : Uri
    private var mImageFile: File? = null
    private var mCountryCODEListCall: Call<ServerResponse<List<CountryCodelist>>>? = null
    private var spinner:Spinner? = null
    private var mCustomArrayAdapter: CountrySpinnerAdapter? = null
    private var countryList: ArrayList<CountryCodelist>? = null
    private var countryCode:String? = null
    private var tvCountry: AppCompatTextView?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        initView(null)
    }

    override val activity: AppCompatActivity
        get() = this@EditProfileActivity
    override val actionTitle: String
        get() = resources.getString(R.string.edit_profile)
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
        mImgEditUser = findViewById(R.id.imgEditUser)
        mImgUploadPhoto = findViewById(R.id.imgUploadPhoto)
        mEdtFirstName = findViewById(R.id.edtFirstName)
        mEdtLastName = findViewById(R.id.edtLastName)
        mEdtEmail = findViewById(R.id.edtEmail)
        mEdtMobile = findViewById(R.id.edtMobile)
        mTxtUpdate = findViewById(R.id.txtUpdate)
        mProgressBar = findViewById(R.id.progress_bar)
        spinner = findViewById(R.id.spinner)
        mImgEditUser!!.setOnClickListener(this)
        mTxtUpdate!!.setOnClickListener(this)
        mPreferenceUtils = PreferenceUtils.getInstance(this@EditProfileActivity)
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        tvCountry = findViewById(R.id.tvCountry)
        countryList = arrayListOf()
        if (ValidationUtils.isInternetAvailable(this)) {
            showProgress()
            apiGetProfileData()
        } else {
            showToastMessage(activity, activity.resources.getString(R.string.network_error))
        }
        tvCountry?.setOnClickListener {
            spinner?.performClick()
        }
        setCountrySelection()
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    fun setCountrySelection(){
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                // It returns the clicked item.
                try {
                    val clickedItem: CountryCodelist =
                        parent.getItemAtPosition(position) as CountryCodelist
                    countryCode = clickedItem.phone
                    tvCountry?.text = clickedItem.phone
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    //check permission and capture image
    private fun captureImage() {
        mPermissionUtils = PermissionUtils(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            object : OnPermissionGrantCallback {
                override fun onPermissionGranted() {
                    selectImage()
                }

                override fun onPermissionError(permission: String?) {}
            })
    }

    private fun selectImage() {
        if (mImagePickerDialog == null) {
            mImagePickerDialog = ImagePickerDialog(this, this)
            mImagePickerDialog!!.selectImage()
        } else {
            mImagePickerDialog!!.selectImage()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
        //    mImagePickerDialog!!.onActivityResult(requestCode, resultCode, data)
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
                mImage = File(filesDir, "image.jpg")
                val inputStream = contentResolver.openInputStream(imageUri)
                val outputStream = FileOutputStream(mImage)
                inputStream!!.copyTo(outputStream)
            }
        }


    //api for get driver profile details
    private fun apiGetProfileData() {
        hideKeyboard(this)
        mApiGetProfileData = Singleton.restClient.driverProfile
        mApiGetProfileData!!.enqueue(object : Callback<ServerResponse<Data>> {
            override fun onResponse(
                call: Call<ServerResponse<Data>>,
                response: Response<ServerResponse<Data>>
            ) {
                val resp: ServerResponse<*>? = response.body()
                dismissProgress()
                if (resp != null && resp.status == 200) {
                    Log.d(TAG, "onResponse: EditProfile: " + Gson().toJson(resp))
                    if (ValidationUtils.isValidString(response.body()!!.data?.id)) {

                        //save driver details in pref
                        mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                        if (ValidationUtils.isValidString(mPreferenceUtils!!.driverData?.image)) {
                            mProgressBar!!.visibility = View.VISIBLE
                            if (mPreferenceUtils!!.get(
                                    PreferenceUtils.PREF_KEY_SOCIAL_LOGIN,
                                    false
                                )
                            ) {
                                if (mPreferenceUtils!!.driverData?.image?.contains("users") == true) {
                                    val path =
                                        mPreferenceUtils!!.driverData?.image?.replace("'\'", "")
                                    Glide.with(activity)
                                        .load(Common.UPLOAD_URL + path)
                                        .placeholder(R.drawable.ic_driver_profile)
                                        .listener(object : RequestListener<Drawable?> {
                                            override fun onLoadFailed(
                                                e: GlideException?,
                                                model: Any,
                                                target: Target<Drawable?>,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                mProgressBar!!.visibility = View.GONE
                                                return false
                                            }

                                            override fun onResourceReady(
                                                resource: Drawable?,
                                                model: Any,
                                                target: Target<Drawable?>,
                                                dataSource: DataSource,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                mProgressBar!!.visibility = View.GONE
                                                return false
                                            }
                                        })
                                        .into(mImgUploadPhoto!!)
                                } else {
                                    val path =
                                        mPreferenceUtils!!.driverData?.image?.replace("'\'", "")
                                    Glide.with(activity)
                                        .load(path)
                                        .placeholder(R.drawable.ic_driver_profile)
                                        .listener(object : RequestListener<Drawable?> {
                                            override fun onLoadFailed(
                                                e: GlideException?,
                                                model: Any,
                                                target: Target<Drawable?>,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                mProgressBar!!.visibility = View.GONE
                                                return false
                                            }

                                            override fun onResourceReady(
                                                resource: Drawable?,
                                                model: Any,
                                                target: Target<Drawable?>,
                                                dataSource: DataSource,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                mProgressBar!!.visibility = View.GONE
                                                return false
                                            }
                                        })
                                        .into(mImgUploadPhoto!!)
                                }
                            } else {
                                val path = mPreferenceUtils!!.driverData?.image?.replace("'\'", "")
                                Glide.with(activity)
                                    .load(Common.UPLOAD_URL + path)
                                    .placeholder(R.drawable.ic_driver_profile)
                                    .listener(object : RequestListener<Drawable?> {
                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any,
                                            target: Target<Drawable?>,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            mProgressBar!!.visibility = View.GONE
                                            return false
                                        }

                                        override fun onResourceReady(
                                            resource: Drawable?,
                                            model: Any,
                                            target: Target<Drawable?>,
                                            dataSource: DataSource,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            mProgressBar!!.visibility = View.GONE
                                            return false
                                        }
                                    })
                                    .into(mImgUploadPhoto!!)
                            }
                            if (ValidationUtils.isValidString(mPreferenceUtils!!.driverData?.name)) {
                                val first_name: String
                                val last_name: String
                                if (mPreferenceUtils!!.driverData?.name?.contains(" ") == true) {
                                    val parts =
                                        mPreferenceUtils!!.driverData?.name!!.split(" ").toTypedArray() // escape .
                                    first_name = parts[0]
                                    last_name = parts[1]
                                } else {
                                    first_name = mPreferenceUtils!!.driverData?.name.toString()
                                    last_name = ".."
                                }
                                mEdtFirstName!!.setText(first_name)
                                mEdtLastName!!.setText(last_name)
                            }
                            mEdtEmail!!.setText(mPreferenceUtils!!.driverData?.email)
                            mEdtMobile!!.setText(mPreferenceUtils!!.driverData?.mobile)
                            apiGETCountryCODEList()
                        }
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(
                                this@EditProfileActivity,
                                jObjError.getString("message")
                            )
                            AppLog.d(TAG, "onResponse:Login Msg " + jObjError.getString("message"))
                        } catch (e: Exception) {
                            showToastMessage(this@EditProfileActivity, e.message!!)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                AppLog.d(TAG, "onFailure: $t")
                dismissProgress()
            }
        })
    }

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
                    val position = countryList?.indexOfFirst { it.phone.equals(mPreferenceUtils?.driverData?.country_phone_code) }
                    if (position != null) {
                        if(position>-1){
                            spinner?.setSelection(position)
                            tvCountry?.text = countryList?.get(position)?.phone
                        }
                    }

                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(
                                this@EditProfileActivity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            showToastMessage(this@EditProfileActivity, e.message!!)
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

    //api for update driver profile details
    private fun apiCallForUpdateProfile() {
        val firstNameBody : RequestBody = mEdtFirstName!!.text.toString().toRequestBody("text/plain".toMediaType())
        val lastNameBody : RequestBody = mEdtLastName!!.text.toString().toRequestBody("text/plain".toMediaType())
        val mobileBody : RequestBody = mEdtMobile!!.text.toString().toRequestBody("text/plain".toMediaType())
        val countryIdBody =
            countryCode?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
        var imageBody : MultipartBody.Part? = null
        if (mImage != null) {
            val mFile = mImage!!.asRequestBody("image/*".toMediaTypeOrNull())
            imageBody = MultipartBody.Part.createFormData("image", mImage!!.name, mFile)
        }
        mApiUpdateProfile = Singleton.restClient.updateDriverProfile(
            firstNameBody,
            lastNameBody,
            mobileBody,
            countryIdBody,
            imageBody /*, userIdBody*/
        )
        mApiUpdateProfile!!.enqueue(object : Callback<ServerResponse<Data>> {
            override fun onResponse(
                call: Call<ServerResponse<Data>>,
                response: Response<ServerResponse<Data>>
            ) {
                val resp = response.body()
                dismissProgress()
                if (resp != null && resp.status == 200) {
                    //AppLog.e(TAG, "onResponse: Response: " + new Gson().toJson(response));
                    mPreferenceUtils!!.saveDriverData(response.body()!!.data)
                    showToastMessage(activity, resp.message)
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showToastMessage(
                                this@EditProfileActivity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            showToastMessage(this@EditProfileActivity, e.message!!)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
                //showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                dismissProgress()
            }
        })
    }

    private val isValidate: Boolean
        private get() = if (!ValidationUtils.isValidString(mEdtFirstName!!)) {
            mEdtFirstName!!.error = getString(R.string.enter_first_name)
            mEdtFirstName!!.requestFocus()
            false
        } else if (mEdtFirstName!!.text.toString().length < 2) {
            mEdtFirstName!!.error = getString(R.string.enter_minimum_2_character)
            mEdtFirstName!!.requestFocus()
            false
        } else if (!ValidationUtils.isValidString(mEdtLastName!!)) {
            mEdtLastName!!.error = getString(R.string.enter_last_name)
            mEdtLastName!!.requestFocus()
            false
        } else if (mEdtLastName!!.text.toString().length < 2) {
            mEdtLastName!!.error = getString(R.string.enter_minimum_2_character)
            mEdtLastName!!.requestFocus()
            false
        } else if (!ValidationUtils.isValidString(mEdtMobile!!)) {
            mEdtMobile!!.error = getString(R.string.enter_mobile_number)
            mEdtMobile!!.requestFocus()
            false
        } else if (!ValidationUtils.isValidMobile(mEdtMobile!!)) {
            mEdtMobile!!.error = getString(R.string.enter_valid_mobile_number)
            mEdtMobile!!.requestFocus()
            false
        } else {
            true
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgEditUser -> callImagePicker_new()
            R.id.txtUpdate -> if (ValidationUtils.isInternetAvailable(this)) {
                if (isValidate) {
                    showProgress()
                    apiCallForUpdateProfile()
                }
            } else {
                showToastMessage(activity, activity.resources.getString(R.string.network_error))
            }
        }
    }

    override fun getImageFileFromPicker(image: File?) {
        if (image != null) {
            mImage = image
            mImgUploadPhoto!!.setImageBitmap(mImagePickerDialog!!.getBitmapFromFile(image))
        }
    }

    fun showProgress() {
        if (mProgressDialog != null) mProgressDialog!!.show()
    }

    fun dismissProgress() {
        if (mProgressDialog != null) mProgressDialog!!.dismiss()
    }

    private fun onCancelApiCall() {
        if (mApiUpdateProfile != null && !mApiUpdateProfile!!.isCanceled) {
            mApiUpdateProfile!!.cancel()
        }
        if (mApiGetProfileData != null && !mApiGetProfileData!!.isCanceled) {
            mApiGetProfileData!!.cancel()
        }
    }

    override fun onPause() {
        super.onPause()
        dismissProgress()
        onCancelApiCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancelApiCall()
        dismissProgress()
    }

    companion object {
        private val TAG = EditProfileActivity::class.java.simpleName
    }
}