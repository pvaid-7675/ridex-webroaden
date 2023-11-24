package com.speedride.customer.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.speedride.customer.interfaces.BaseView

import java.io.UnsupportedEncodingException

abstract class BaseFragment : Fragment(), BaseView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var mView: View? = null
        if (container != null) {
            mView = LayoutInflater.from(container.context).inflate(
                layoutResourceId, container, false
            )
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreate(view, savedInstanceState)
    }

    /*public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
       // NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onActivityCreate(savedInstanceState)
    }

    /* if (getActivity() != null)
            return getActivity();
        else
            return DrDCannabis.getInstance().getApplicationContext();*/
    val activityContext: Context?
        get() =/* if (getActivity() != null)
            return getActivity();
        else
            return DrDCannabis.getInstance().getApplicationContext();*/
            activity
    val parentActivity: Activity?
        get() = if (activity != null) activity else null

    fun decodeStringResponse(encodeString: String?): String? {
        try {
            val data = Base64.decode(encodeString, 0)
            return String(data, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }

    abstract val layoutResourceId: Int
    abstract fun onViewCreate(view: View?, savedInstanceState: Bundle?)
    abstract fun onActivityCreate(savedInstanceState: Bundle?)
    abstract fun printLog(message: String?)
}