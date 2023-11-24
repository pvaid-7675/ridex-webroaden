package com.speedride.driver.modules.chat.adminchat

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.speedride.customer.chat.adapter.AdminChatAdapter
import com.speedride.driver.R
import com.speedride.driver.base.BaseActivity
import com.speedride.driver.broadcast_receiver.ReceiverClass
import com.speedride.driver.model.AdminChatResponse
import com.speedride.driver.model.ServerResponse
import com.speedride.driver.modules.chat.ChatActivity
import com.speedride.driver.rest.Singleton
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
import kotlin.Comparator

class AdminChatActivity :  BaseActivity() {

    var rvChatList: RecyclerView? = null
    var ivCamera: AppCompatImageView? = null
    var edtMessage: AppCompatEditText? = null
    var ivSendMsg: AppCompatImageView? = null
    var chatAdapter: AdminChatAdapter? = null
    private var isLoading = true
    private var mApiImageUpload: Call<Object>? = null
    private var chatResponseItem: AdminChatResponse.AdminChatResponseItem? = null
    private var mPreferenceUtils: PreferenceUtils? = null
    private var mActivity: Activity = this
    private var mChatList: ArrayList<AdminChatResponse.AdminChatResponseItem>? = null
    private var mApiChatList: Call<ServerResponse<List<AdminChatResponse.AdminChatResponseItem>>>? = null
    lateinit var imageUri: Uri
    private var mImageFile: File? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_chat)
        val intentRequest = Intent(this, ReceiverClass::class.java)
        intentRequest.action = Common.RECIEVE_CHAT_MESSAGE_TO_ADMIN
        sendBroadcast(intentRequest)
        initView()
       // withPagination
    }

    fun setTitle(title: String?) {
        setToolbarTitle(title)
    }

    override val activity: AppCompatActivity
        get() = this@AdminChatActivity
    override val actionTitle: String
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
            if (event.equals(Common.RECIEVE_CHAT_MESSAGE_TO_ADMIN, ignoreCase = true)) {
                val obj = JSONObject(data)
                val jsonObject1 = obj//allData.getJSONObject(i)
                var chatResponseItem = AdminChatResponse.AdminChatResponseItem()
                chatResponseItem?.sender_id = jsonObject1.getString("sender_id");
                chatResponseItem?.details = jsonObject1.getString("details");
                chatResponseItem?.date_time = jsonObject1.getString("date_time");
                chatResponseItem?.message_type = jsonObject1.getString("message_type");
                chatResponseItem?.receiver_id = jsonObject1.getString("receiver_id");
                var count = mChatList?.count {
                    it.date_time.equals(chatResponseItem?.date_time, true)
                }
                mChatList?.let {
                    Collections.sort(it, Comparator<AdminChatResponse.AdminChatResponseItem> { obj1, obj2 ->
                        // ## Ascending order
                        obj1?.date_time!!.compareTo(obj2?.date_time.toString()) // To compare string values
                    })
                }
                if (count == 0) {
                    mChatList?.add(chatResponseItem!!)
                    if (chatAdapter == null) {
                        // Collecti          ons.sort(mChatList)
                        chatAdapter = AdminChatAdapter(mPreferenceUtils!!, mChatList!!, mActivity)
                        rvChatList?.adapter = chatAdapter
                    } else {
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
            IntentFilter(Common.RECIEVE_CHAT_MESSAGE_TO_ADMIN)
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

    override fun initView(view: View?) {

    }

    override fun showToastMessage(context: Context?, message: String?) {

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initView() {
        setToolbar(findViewById(R.id.appbar))
        // mAppSocketIO = AppSocketIO(this)
        rvChatList = findViewById(R.id.rvChatList)
        mLinearLayoutManager = LinearLayoutManager(activity)
        rvChatList!!.setLayoutManager(mLinearLayoutManager)
        rvChatList!!.setItemAnimator(DefaultItemAnimator())

        ivCamera = findViewById(R.id.ivCamera)
        edtMessage = findViewById(R.id.edtMessage)
        ivSendMsg = findViewById(R.id.ivSendMsg)
        mPreferenceUtils = PreferenceUtils.getInstance(this)
        mChatList = arrayListOf()
        chatResponseItem = AdminChatResponse.AdminChatResponseItem()
       chatHistory()
        onClick()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun onClick() {
        ivCamera?.setOnClickListener {
            callImagePicker_new()
        }
        ivSendMsg?.setOnClickListener {
           sendTextMessage()
//            if (chatResponseItem != null) {
//                edtMessage?.text?.clear()
//
//                val gson = GsonBuilder().setPrettyPrinting().create()
//                val amyJson = gson.toJson(chatResponseItem)
//                val intentRequest = Intent(this, ReceiverClass::class.java)
//                intentRequest.action = Common.SEND_MESSAGE_TO_ADMIN_CHAT
//                intentRequest.putExtra("chatItemAdmin", amyJson)
//                sendBroadcast(intentRequest)
//            }

        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun sendTextMessage(){

        if(edtMessage!!.text?.toString()!!.isNotEmpty()){
            if(chatResponseItem!=null){
                chatResponseItem?.message_type = Common.MESSAGE_TYPE_TEXT
                chatResponseItem?.details = edtMessage?.text.toString()
                chatResponseItem?.sender_id = mPreferenceUtils?.driverData?.id.toString()
                chatResponseItem?.receiver_id = ""
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
    fun apiCallUpload() {

        var body: MultipartBody.Part? = null
        if (mImageFile != null) {
            val mFile = mImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("image", mImageFile!!.name, mFile)
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
                        chatResponseItem?.sender_id = mPreferenceUtils?.driverData?.id.toString()
                        chatResponseItem?.receiver_id = ""
                        chatResponseItem?.date_time = Utils.getCurrentDate()
                        if (chatResponseItem != null) {
                            val gson = GsonBuilder().setPrettyPrinting().create()
                            val amyJson = gson.toJson(chatResponseItem)
                            val intentRequest = Intent(activity, ReceiverClass::class.java)
                            intentRequest.action = Common.SEND_MESSAGE_TO_ADMIN_CHAT
                            intentRequest.putExtra("chatItemAdmin", amyJson)
                            sendBroadcast(intentRequest)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }

            override fun onFailure(call: Call<Object>, t: Throwable) {
                Toast.makeText(
                    this@AdminChatActivity,
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
            .maxResultSize(1080,1080)
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
    fun chatHistory(){

      mApiChatList = Singleton.restClient.getAdminChatMessage(mPreferenceUtils?.driverData?.id)

        mApiChatList?.enqueue(object : Callback<ServerResponse<List<AdminChatResponse.AdminChatResponseItem>>> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<ServerResponse<List<AdminChatResponse.AdminChatResponseItem>>>,
                response: Response<ServerResponse<List<AdminChatResponse.AdminChatResponseItem>>>
            ) {
                try {
                    val resp = response.body()
                    //Response<ServerResponse>
                    if (resp != null && response.code() == 200) {
                      isLoading = true
                        val  chatResponseItem = response.body()!!.data
                        mChatList?.addAll(chatResponseItem)
                        setAdapter()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }

            override fun onFailure(call: Call<ServerResponse<List<AdminChatResponse.AdminChatResponseItem>>>,   t: Throwable) {
                Toast.makeText(
                    this@AdminChatActivity,
                    resources.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    fun setAdapter(){
        chatAdapter = AdminChatAdapter(mPreferenceUtils!!, mChatList!!, mActivity)
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

   /* private val withPagination: Unit
        private get() {
            // Pagination and lazy loading
            rvChatList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) { //check for scroll down
                        val visibleItemCount = mLinearLayoutManager!!.childCount
                        val totalItemCount = mLinearLayoutManager!!.itemCount
                        val firstVisibleItemPosition =
                            mLinearLayoutManager!!.findFirstVisibleItemPosition()
                        val lastVisibleItemPosition =
                            mLinearLayoutManager!!.findLastVisibleItemPosition()
                        Log.d("visibleItemCount", "onScrolled: $visibleItemCount")
                        Log.d("totalItemCount", "onScrolled: $totalItemCount")
                        Log.d("firstVisibleItemPos", "onScrolled: $firstVisibleItemPosition")
                        Log.d("lastVisibleItemPosition", "onScrolled: $lastVisibleItemPosition")
                        if (isLoading) {
                            if (firstVisibleItemPosition > 0 && lastVisibleItemPosition == totalItemCount - 1) {
                                //Log.d(RatingsFragment.TAG, "onScrolledPosition: $mScrollPagePos")
                                if (ValidationUtils.isInternetAvailable(activity!!)) {
                                    isLoading = false
                                  //  mProgressBar!!.visibility = View.VISIBLE
                                    chatHistory()
                                } else {
                                    showToastMessage(
                                        activity!!,
                                        activity!!.resources.getString(R.string.network_error)
                                    )
                                }
                            }
                        }aaaaajt
                    }
                }
            })
        }*/
}