package com.speedride.driver.modules.userManagement.ui.fragment

import android.Manifest
import android.content.Context
import com.speedride.driver.base.BaseFragment
import com.speedride.driver.interfaces.ImagePickerListener
import com.makeramen.roundedimageview.RoundedImageView
import android.widget.RelativeLayout
import com.speedride.driver.modules.userManagement.ui.activity.DocumentUploadActivity
import com.speedride.driver.utils.ImagePickerDialog
import com.speedride.driver.utils.PermissionUtils
import com.speedride.driver.utils.PreferenceUtils
import com.speedride.driver.R
import com.speedride.driver.utils.ValidationUtils
import android.os.Bundle
import com.speedride.driver.utils.PermissionUtils.OnPermissionGrantCallback
import com.speedride.driver.utils.AppLog
import android.content.Intent
import android.view.View
import java.io.File

class ProfilePhotoFragment : BaseFragment(), ImagePickerListener, View.OnClickListener {
    private var mImgUploadPhoto: RoundedImageView? = null
    private var mRlTakePhoto: RelativeLayout? = null
    private var mDocumentUploadActivity: DocumentUploadActivity? = null
    private var mImagePickerDialog: ImagePickerDialog? = null
    private var mPermissionUtils: PermissionUtils? = null
    private var preferenceUtils: PreferenceUtils? = null
    private var mDestination: File? = null
    override fun initView(view: View?) {
        preferenceUtils = PreferenceUtils.getInstance(requireActivity())
        mImgUploadPhoto = view?.findViewById(R.id.imgUploadPhoto)
        mRlTakePhoto = view?.findViewById(R.id.rlTakePhoto)
        mRlTakePhoto!!.setOnClickListener(this)

        //check image already select
        val captureImage = preferenceUtils!!.get(PreferenceUtils.PREF_KEY_DOCUMENT_PROFILE_PHOTO, "")
        if (ValidationUtils.isValidString(captureImage)) {
            if (mImagePickerDialog == null) {
                mImagePickerDialog = ImagePickerDialog(this, this)
                mImgUploadPhoto!!.setImageBitmap(
                    mImagePickerDialog!!.getBitmapFromFile(
                        File(
                            captureImage
                        )
                    )
                )
            } else {
                mImgUploadPhoto!!.setImageBitmap(
                    mImagePickerDialog!!.getBitmapFromFile(
                        File(
                            captureImage
                        )
                    )
                )
            }
        }
    }

    override val layoutResourceId: Int
        get() = R.layout.fragment_upload_profile_photo

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }
        mDocumentUploadActivity = activity as DocumentUploadActivity?
        mDocumentUploadActivity!!.setTitle(resources.getString(R.string.profile_photo))
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun showToastMessage(context: Context?, message: String?) {}

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
                    AppLog.d(TAG, "onPermissionGranted: ")
                }

                override fun onPermissionError(permission: String?) {}
            })
    }

    //select image from interface method
    private fun selectImage() {
        if (mImagePickerDialog == null) {
            mImagePickerDialog = ImagePickerDialog(this, this)
            mImagePickerDialog!!.selectImage()
            AppLog.d(TAG, "onPermissionGranted: ++")
        } else {
            mImagePickerDialog!!.selectImage()
            AppLog.d(TAG, "onPermissionGranted:-- ")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            mImagePickerDialog!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.rlTakePhoto) {
            captureImage()
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

    override fun getImageFileFromPicker(image: File?) {
        if (image != null) {
            mDestination = image
            mImgUploadPhoto!!.setImageBitmap(mImagePickerDialog!!.getBitmapFromFile(image))
            //capture image save in preference
            preferenceUtils!!.save(PreferenceUtils.PREF_KEY_DOCUMENT_PROFILE_PHOTO, image.path)
        }
    }

    companion object {
        private val TAG = ProfilePhotoFragment::class.java.simpleName
    }
}