package com.speedride.driver.modules.userManagement.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Context

import com.speedride.driver.base.BaseFragment
import com.speedride.driver.interfaces.ImagePickerListener
import com.speedride.driver.modules.userManagement.ui.activity.DocumentUploadActivity
import android.widget.RelativeLayout
import com.makeramen.roundedimageview.RoundedImageView
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.R
import com.bumptech.glide.Glide
import android.widget.Toast
import android.os.Bundle
import com.speedride.driver.utils.PermissionUtils.OnPermissionGrantCallback
import okhttp3.RequestBody
import org.json.JSONObject
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.speedride.driver.model.Data
import com.speedride.driver.rest.Singleton
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

class DrivingLicenceFragment : BaseFragment(), ImagePickerListener, View.OnClickListener {
    private var mDocumentUploadActivity: DocumentUploadActivity? = null
    private var mRlTakePhoto: RelativeLayout? = null
    private var mImgUploadPhoto: RoundedImageView? = null
    private var mPermissionUtils: PermissionUtils? = null
    private var mImagePickerDialog: ImagePickerDialog? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mApiSendDrivingLicenceCall: Call<ServerResponse<Data>>? = null
    private var mStrDriverId: String? = null
    lateinit var  imageUri : Uri
    private var mImageFile: File? = null

    override fun initView(view: View?) {

        mPreferenceUtils = activity?.let { PreferenceUtils.getInstance(it) }
        mStrDriverId = mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DRIVER_ID, "")
        mRlTakePhoto = view?.findViewById(R.id.rlTakePhoto)
        mImgUploadPhoto = view?.findViewById(R.id.imgDrivingLicence)
        mRlTakePhoto!!.setOnClickListener(this)

        //check image already select
        val captureImage =
            mPreferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_DRIVER_LICENCE, "")
        if (captureImage != null) {
            Glide.with(this).load(Common.UPLOAD_URL + captureImage)
                .placeholder(R.drawable.ic_driver_licence).into(mImgUploadPhoto!!)
        }
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override val layoutResourceId: Int
        get() = R.layout.fragment_upload_driving_licence

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        view?.let { initView(it) }
        mDocumentUploadActivity = activity as DocumentUploadActivity?
        mDocumentUploadActivity!!.setTitle(resources.getString(R.string.driving_licence))
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}

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

    //select image from interface method
    private fun selectImage() {
        if (mImagePickerDialog == null) {
            mImagePickerDialog = ImagePickerDialog(this, this)
            mImagePickerDialog!!.selectImage()
        } else {
            mImagePickerDialog!!.selectImage()
        }
    }

    //api call for send driving licence image
    private fun apiSendDrivingLicence(imageFile: File?) {
        if (ValidationUtils.isValidString(mStrDriverId) && imageFile != null) {
            val idBody : RequestBody = mStrDriverId!!.toRequestBody("text/plain".toMediaType())
            val docTypeBody : RequestBody = Common.DOC_LICENCE.toRequestBody("text/plain".toMediaType())

            var licence_image : MultipartBody.Part? = null
            if (imageFile != null) {
                val mFile = imageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
                licence_image = MultipartBody.Part.createFormData("document", imageFile!!.name, mFile)
            }

            mApiSendDrivingLicenceCall = Singleton.restClient
                .uploadDriverVehicleDocumentsAll(idBody, docTypeBody, licence_image)
            mApiSendDrivingLicenceCall!!.enqueue(object : Callback<ServerResponse<Data>> {
                override fun onResponse(
                    call: Call<ServerResponse<Data>>,
                    response: Response<ServerResponse<Data>>
                ) {
                    dismissProgress()
                    val resp: ServerResponse<*>? = response.body()
                    if (resp != null && resp.status == 200) {
                        showToastMessage(activity!!, response.body()!!.message)
                        if (ValidationUtils.isValidString(response.body()!!.data?.d_licence)) //capture image save in preference
                            mPreferenceUtils!!.save(
                                PreferenceUtils.PREF_KEY_DOCUMENT_DRIVER_LICENCE, response.body()!!
                                    .data?.d_licence
                            )
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                showToastMessage(context!!, jObjError.getString("message"))
                            } catch (e: Exception) {
                                showToastMessage(context!!, e.message!!)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse<Data>>, t: Throwable) {
                    dismissProgress()
                    Log.d(TAG, "onFailure: $t")
                    //  showToastMessage(getActivity(), getActivity().getResources().getString(R.string.something_went_wrong));
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            mImagePickerDialog!!.onActivityResult(requestCode, resultCode, data)
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

    override fun onClick(v: View) {
        if (v.id == R.id.rlTakePhoto) {
            callImagePicker_new()
        }
    }

    // image picker call
    // sagar **********************************************************
    private fun callImagePicker_new() {
        Log.e("ImageCheck", " ImagePicker Open ")
        ImagePicker.with(requireActivity())
            .crop()
            .cropFreeStyle()
            .provider(ImageProvider.BOTH) //Or bothCameraGallery()
            .createIntentFromDialog { launcher.launch(it) }

        /*  mPermissionUtils = PermissionUtils(
              this,
              arrayOf(
                  Manifest.permission.CAMERA,
                  Manifest.permission.READ_EXTERNAL_STORAGE,
                  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  Manifest.permission.ACCESS_COARSE_LOCATION,
                  Manifest.permission.ACCESS_FINE_LOCATION
              ),
              object : OnPermissionGrantCallback {
                  override fun onPermissionGranted() {
                      selectImage()
                  }

                  override fun onPermissionError(permission: String?) {}
              })*/
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                // Use the uri to load the image
                imageUri = uri

                // getImageFileFromPicker(uri.toFile())
                mImgUploadPhoto!!.setImageURI(uri)
                showProgress()
                Log.e("ImageCheck", " imageUri::: " + imageUri)
                val filesDir = requireActivity().applicationContext.filesDir
                mImageFile = File(filesDir, "image.jpg")
                val inputStream = requireActivity().contentResolver.openInputStream(imageUri)
                val outputStream = FileOutputStream(mImageFile)
                inputStream!!.copyTo(outputStream)
                apiSendDrivingLicence(mImageFile)
            }
        }


    override fun getImageFileFromPicker(image: File?) {
        if (activityContext?.let { ValidationUtils.isInternetAvailable(it) } == true) {
            if (ValidationUtils.isValidObject(image)) {
                showProgress()
                /*     mImgUploadPhoto!!.setImageBitmap(image?.let {
                         mImagePickerDialog!!.getBitmapFromFile(
                             it
                         )
                     })*/
                apiSendDrivingLicence(image)
            }
        } else {
            showToastMessage(requireActivity(), requireActivity().resources.getString(R.string.network_error))
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


    private fun onCancelApiCall() {
        if (mApiSendDrivingLicenceCall != null && !mApiSendDrivingLicenceCall!!.isCanceled) {
            mApiSendDrivingLicenceCall!!.cancel()
        }
    }

    companion object {
        private val TAG = DrivingLicenceFragment::class.java.simpleName

        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): DrivingLicenceFragment {
            val fragment = DrivingLicenceFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }


}