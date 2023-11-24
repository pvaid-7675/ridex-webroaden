package com.speedride.driver.modules.chat

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.speedride.customer.chat.adapter.ChatAdapter
import com.speedride.customer.model.ChatResponse
import com.speedride.driver.R
import com.speedride.driver.base.BaseActivity
import com.speedride.driver.broadcast_receiver.ReceiverClass
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.rest.Singleton.Companion.restClient
import com.speedride.driver.socketIO.AppSocketIO
import com.speedride.driver.utils.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*


class ChatActivity : BaseActivity(){

    var rvChatList:RecyclerView?=null
    var ivCamera: AppCompatImageView?=null
    var edtMessage: AppCompatEditText?=null
    var ivSendMsg:AppCompatImageView?=null
    var chatAdapter: ChatAdapter?= null
    private var fileImage: File? = null
    private var mApiImageUpload: Call<Object>? = null
    private var mPermission: PermissionUtils? = null
    private var mImagePickerDialog: ImagePickerDialog? = null
    private var chatResponseItem:ChatResponse.ChatResponseItem?=null
    private var mPreferenceUtils: PreferenceUtils?=null
    private var mActivity:Activity = this
    private var mChatList: ArrayList<ChatResponse.ChatResponseItem>?=null
    private var mApiChatList: Call<ServerResponse<List<ChatResponse.ChatResponseItem>>>? = null
    private var mAppSocketIO: AppSocketIO? = null
    lateinit var  imageUri : Uri
    private var mImageFile: File? = null
    private var bookId:String?=null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
      //  Utils.startService(mActivity, Common.RECIEVE_CHAT_MESSAGE)
        val intentRequest = Intent(this, ReceiverClass::class.java)
        intentRequest.action = Common.RECIEVE_CHAT_MESSAGE
        sendBroadcast(intentRequest)
        initView()
    }
    fun setTitle(title: String?) {
        setToolbarTitle(title)
    }
    override val activity: AppCompatActivity?
        get() = this@ChatActivity
    override val actionTitle: String?
        get() = getString(R.string.chat)

    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

  
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }



    private val receiveMessage: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("ChatActivity- BroadcastReceiver", "receiveMessage: ")
            // Get extra data included in the Intent
            val event = intent.getStringExtra(AppSocketIO.BROADCAST_EVENT_NAME)
            val data = intent.getStringExtra(AppSocketIO.BROADCAST_EVENT_DATA)
            if (event.equals(Common.RECIEVE_CHAT_MESSAGE, ignoreCase = true)) {
                val obj = JSONObject(data)
//                if(!obj.has("data")) return
//                val allData = obj.getJSONArray("data")
//                for (i in 0 until allData.length()) {
                val jsonObject1 = obj//allData.getJSONObject(i)
                var chatResponseItem = ChatResponse.ChatResponseItem()
                chatResponseItem?.user_id =jsonObject1.getString("user_id");
                chatResponseItem?.details =jsonObject1.getString("details");
                chatResponseItem?.date_time =jsonObject1.getString("date_time");
                chatResponseItem?.message_type =jsonObject1.getString("message_type");
                chatResponseItem?.ride_id =jsonObject1.getString("ride_id");
                //  mChatList?.add(chatResponseItem!!)
                var count =   mChatList?.count{
                    it.date_time.equals(chatResponseItem?.date_time,true)
                }
                mChatList?.let {
                    Collections.sort(it, Comparator<ChatResponse.ChatResponseItem?> { obj1, obj2 ->
                        // ## Ascending order
                        obj1.date_time!!.compareTo(obj2.date_time.toString()) // To compare string values
                    })
                }
                if(count==0){
                    mChatList?.add(chatResponseItem!!)
                    if(chatAdapter==null) {
                        // Collecti          ons.sort(mChatList)
                        chatAdapter = ChatAdapter(mPreferenceUtils!!, mChatList!!, mActivity)
                        rvChatList?.adapter = chatAdapter
                    }else{
                        chatAdapter?.addAll(mChatList!!)

                    }
                }

                scrollLast()

            }

        }
    }

    override fun onResume() {
        super.onResume()
        //        mMapFragment.onResume();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(
            receiveMessage,
            IntentFilter(Common.RECIEVE_CHAT_MESSAGE)
        )
    }
    override fun onStop() {
        super.onStop()
        if (receiveMessage != null) {
            Log.d("TAG", "onStop: ")
            LocalBroadcastManager.getInstance(mActivity)
                .unregisterReceiver(receiveMessage)
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun initView(){
        setToolbar(findViewById(R.id.appbar))
       // mAppSocketIO = AppSocketIO(this)
        rvChatList = findViewById(R.id.rvChatList)
        ivCamera = findViewById(R.id.ivCamera)
        edtMessage = findViewById(R.id.edtMessage)
        ivSendMsg = findViewById(R.id.ivSendMsg)
        mPreferenceUtils = PreferenceUtils.getInstance(this)
        mChatList = arrayListOf()
        chatResponseItem = ChatResponse.ChatResponseItem()
        bookId = intent.getStringExtra("book_id")
        chatHistory()
        onClick()
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun sendTextMessage(){
        if(edtMessage!!.text?.toString()!!.isNotEmpty()){
            if(chatResponseItem!=null){
            chatResponseItem?.message_type = Common.MESSAGE_TYPE_TEXT
            chatResponseItem?.details = edtMessage?.text.toString()
            chatResponseItem?.user_id = mPreferenceUtils?.customerRideData?.driver_id.toString()
            chatResponseItem?.ride_id = bookId
            chatResponseItem?.date_time = Utils.getCurrentDate()

                edtMessage?.text?.clear()

                val gson = GsonBuilder().setPrettyPrinting().create()
                val amyJson = gson.toJson(chatResponseItem)
                val intentRequest = Intent(this, ReceiverClass::class.java)
                intentRequest.action = Common.SEND_MESSAGE_CHAT
                intentRequest.putExtra("chatItem",amyJson)
                sendBroadcast(intentRequest)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun onClick(){
        ivCamera?.setOnClickListener {
            callImagePicker_new()
        }
        ivSendMsg?.setOnClickListener {
            sendTextMessage()
        }
    }
    fun apiCallUpload(){

        var body : MultipartBody.Part? = null
        if (mImageFile != null) {
            val mFile = mImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("image", mImageFile!!.name, mFile)
        }
        mApiImageUpload = restClient.getChatImageUpload(body)

        mApiImageUpload?.enqueue(object : Callback<Object> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<Object>,
                response: Response<Object>
            ) {
                try {
                    val resp1: Any? = response.body()
                    val gson = Gson()
                    val json = gson.toJson(resp1)
                    val jObject = JSONObject(json)
                    //Response<ServerResponse>
                    if (jObject.getInt("status") == Common.STATUS_200) {
                        chatResponseItem?.message_type = Common.MESSAGE_TYPE_IMAGE
                        chatResponseItem?.details = jObject.getString("data")
                        chatResponseItem?.user_id = mPreferenceUtils?.customerRideData?.driver_id.toString()
                        chatResponseItem?.date_time = Utils.getCurrentDate()
                        chatResponseItem?.ride_id = bookId
                        if(chatResponseItem!=null){
                            val gson = GsonBuilder().setPrettyPrinting().create()
                            val amyJson = gson.toJson(chatResponseItem)
                            val intentRequest = Intent(activity, ReceiverClass::class.java)
                            intentRequest.action = Common.SEND_MESSAGE_CHAT
                            intentRequest.putExtra("chatItem",amyJson)
                            sendBroadcast(intentRequest)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }

            override fun onFailure(call: Call<Object>, t: Throwable) {
                Toast.makeText(
                    this@ChatActivity,
                    resources.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

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
         //   mImgUploadPhoto!!.setImageURI(uri)

                Log.e("ImageCheck", " imageUri::: " + imageUri)
                val filesDir = applicationContext.filesDir
                mImageFile = File(filesDir, "image.jpg")
                val inputStream = contentResolver.openInputStream(imageUri)
                val outputStream = FileOutputStream(mImageFile)
                inputStream!!.copyTo(outputStream)
                apiCallUpload()
            }
        }
    /*private fun captureImage() {
        mPermission = PermissionUtils(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            object : PermissionUtils.OnPermissionGrantCallback {
                override fun onPermissionGranted() {
                    selectImage()
                }

                override fun onPermissionError(permission: String?) {

                }
            })
    }*/
//    private fun selectImage() {
//        if (mImagePickerDialog == null) {
//            mImagePickerDialog = ImagePickerDialog(this, this)
//            mImagePickerDialog!!.selectImage()
//        } else {
//            mImagePickerDialog!!.selectImage()
//        }
//    }

   /*  fun getImageFileFromPicker(image: File) {
        if (image != null) {
            fileImage = image
            chatResponseItem?.message_type = "image"
            apiCallUpload()
            //mImgUploadPhoto!!.setImageBitmap(mImagePickerDialog!!.getBitmapFromFile(image))
        }
    }*/
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        mPermission!!.onRequestPermissionResult(requestCode, permissions, grantResults)
//    }

    /*public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ImagePickerDialog.REQUEST_CAMERA || requestCode == ImagePickerDialog.SELECT_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                mImagePickerDialog!!.onActivityResult(requestCode, resultCode, data!!)
            }else{
                ivCamera?.performClick()
            }
        }else if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }

            }

            else {
                //  mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
                AppLog.e("Tag", "onActivityResult: " + "mCallbackManager")
            }
        }
    }*/

    override fun initView(view: View?) {

    }

    override fun showToastMessage(context: Context?, message: String?) {
    }


    fun chatHistory(){

        mApiChatList = restClient.getChatMessage(bookId)

        mApiChatList?.enqueue(object : Callback<ServerResponse<List<ChatResponse.ChatResponseItem>>> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<ServerResponse<List<ChatResponse.ChatResponseItem>>>,
                response: Response<ServerResponse<List<ChatResponse.ChatResponseItem>>>
            ) {
                try {
                    val resp = response.body()
                    //Response<ServerResponse>
                    if (resp != null && response.code() == 200) {
                        val  chatResponseItem = response.body()!!.data
                        mChatList?.addAll(chatResponseItem)
                        setAdapter()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }

            override fun onFailure(call: Call<ServerResponse<List<ChatResponse.ChatResponseItem>>>,   t: Throwable) {
                                  Toast.makeText(
                    this@ChatActivity,
                    resources.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    fun setAdapter(){
        chatAdapter = ChatAdapter(mPreferenceUtils!!, mChatList!!, mActivity)
        rvChatList?.adapter = chatAdapter

        scrollLast()
        //rvChatList?.post(Runnable { rvChatList!!.smoothScrollToPosition(chatAdapter?.getItemCount()!!.minus(1)) })
    }
    fun scrollLast(){
        try {
            if(chatAdapter?.itemCount!! >0){
                rvChatList?.post(Runnable { rvChatList!!.smoothScrollToPosition(chatAdapter?.getItemCount()!!.minus(1)) })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun setTitle(chatActivity: ChatActivity, title: String?) {
            chatActivity.setToolbarTitle(title)
        }
    }


}