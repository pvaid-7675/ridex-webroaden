package com.speedride.customer.chat

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.speedride.customer.R
import com.speedride.customer.base.BaseActivity
import com.speedride.customer.chat.adapter.ChatAdapter
import com.speedride.customer.interfaces.ImagePickerListener
import com.speedride.customer.model.ChatHistotyResponse
import com.speedride.customer.model.ChatResponse
import com.speedride.customer.model.ServerResponse
import com.speedride.customer.modules.payment.model.HistoryDetail
import com.speedride.customer.modules.utils.*
import com.speedride.customer.soketIO.AppSocketIO
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class ChatActivity : BaseActivity(), ImagePickerListener {

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
    private var mPreferenceUtils:PreferenceUtils?=null
    private var mActivity:Activity = this
    private var mChatList: ArrayList<ChatResponse.ChatResponseItem>?=null
    private var mApiChatList: Call<com.speedride.customer.model.ServerResponse<List<ChatResponse.ChatResponseItem>>>? = null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        Utils.startService(mActivity, Common.RECIEVE_CHAT_MESSAGE)
        initView()
    }
    override val actionTitle: String?
        get() = getString(R.string.chat)

    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back_white
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
            val event = intent.getStringExtra(com.speedride.customer.soketIO.AppSocketIO.BROADCAST_EVENT_NAME)
            val data = intent.getStringExtra(com.speedride.customer.soketIO.AppSocketIO.BROADCAST_EVENT_DATA)
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
        rvChatList = findViewById(R.id.rvChatList)
        ivCamera = findViewById(R.id.ivCamera)
        edtMessage = findViewById(R.id.edtMessage)
        ivSendMsg = findViewById(R.id.ivSendMsg)
        mPreferenceUtils = PreferenceUtils.getInstance(this)
        mChatList = arrayListOf()
        chatResponseItem = ChatResponse.ChatResponseItem()
        chatHistory()
        onClick()
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun sendTextMessage(){
        if(edtMessage!!.text?.toString()!!.isNotEmpty()){
            if(chatResponseItem!=null){
                chatResponseItem?.message_type = Common.MESSAGE_TYPE_TEXT
                chatResponseItem?.details = edtMessage?.text.toString()
                chatResponseItem?.user_id = mPreferenceUtils?.userInfo?.id.toString()
                chatResponseItem?.ride_id = mPreferenceUtils?.customerDriverRideInfo?.id
                chatResponseItem?.date_time = Utils.getCurrentDate()

                edtMessage?.text?.clear()
                AppSocketIO.sendMessage(chatResponseItem)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun onClick(){
        ivCamera?.setOnClickListener {
            captureImage()
        }
        ivSendMsg?.setOnClickListener {
            sendTextMessage()
//            if(chatResponseItem!=null){
//                edtMessage?.text?.clear()
//                com.speedride.customer.soketIO.AppSocketIO.sendMessage(chatResponseItem)
//            }

        }
    }

    override fun setToolbar(frameLayout: RelativeLayout) {
        val toolbar = frameLayout.findViewById<Toolbar>(R.id.toolbar)
        mTxtTitle = frameLayout.findViewById(R.id.txtToolbarTitle)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(isHomeButtonEnable)
            actionbar.setHomeAsUpIndicator(setHomeButtonIcon())
            actionbar.setTitle("")
            mTxtTitle?.setText(actionTitle)
            actionbar.setHomeButtonEnabled(true)
        }
    }
    fun apiCallUpload(){
        var body: MultipartBody.Part? = null
        if (fileImage != null) {
            val requestFile = RequestBody.create("image/jpg".toMediaTypeOrNull(), fileImage!!)
            body = MultipartBody.Part.createFormData("image", fileImage!!.name, requestFile)
        }
        mApiImageUpload = Singleton.restClient.getChatImageUpload(body)

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
                        chatResponseItem?.user_id = mPreferenceUtils?.userInfo?.id.toString()
                        chatResponseItem?.date_time = Utils.getCurrentDate()
                        chatResponseItem?.ride_id = mPreferenceUtils?.customerDriverRideInfo?.id
                        if(chatResponseItem!=null){
                            com.speedride.customer.soketIO.AppSocketIO.sendMessage(chatResponseItem)
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
    private fun captureImage() {
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
    }
    private fun selectImage() {
        if (mImagePickerDialog == null) {
            mImagePickerDialog = ImagePickerDialog(this, this)
            mImagePickerDialog!!.selectImage()
        } else {
            mImagePickerDialog!!.selectImage()
        }
    }

    override fun getImageFileFromPicker(image: File) {
        if (image != null) {
            fileImage = image
            chatResponseItem?.message_type = "image"
             apiCallUpload()
            //mImgUploadPhoto!!.setImageBitmap(mImagePickerDialog!!.getBitmapFromFile(image))
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermission!!.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                AppLog.Companion.e("Tag", "onActivityResult: " + "mCallbackManager")
            }
        }
    }

    override fun initView(view: View?) {
        TODO("Not yet implemented")
    }

    override fun showToastMessage(message: String) {
        TODO("Not yet implemented")
    }

    fun chatHistory(){

        mApiChatList = Singleton.restClient.getChatMessage(mPreferenceUtils?.customerDriverRideInfo?.id)

        mApiChatList?.enqueue(object : Callback<com.speedride.customer.model.ServerResponse<List<ChatResponse.ChatResponseItem>>> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<com.speedride.customer.model.ServerResponse<List<ChatResponse.ChatResponseItem>>>,
                response: Response<com.speedride.customer.model.ServerResponse<List<ChatResponse.ChatResponseItem>>>
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

            override fun onFailure(call: Call<com.speedride.customer.model.ServerResponse<List<ChatResponse.ChatResponseItem>>>, t: Throwable) {
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
}