package com.speedride.customer.base

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import com.speedride.customer.R
import com.speedride.customer.interfaces.BaseView

abstract class  BaseActivity : AppCompatActivity(), BaseView {

    var mTxtTitle: AppCompatTextView? = null
    abstract val actionTitle: String?
    abstract val isHomeButtonEnable: Boolean
    abstract fun setHomeButtonIcon(): Int

    open fun setToolbar(frameLayout: RelativeLayout) {
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

    /* public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/
    fun setToolbarTitle(title: String?) {
        mTxtTitle!!.text = title
    }

    fun setIsHomeEnable(isHomeEnable: Boolean) {
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(isHomeEnable)
    }

    fun hideKeyboard(activity: Activity): Boolean {
        // Check if no view has focus:
        var keyboardClosed = false
        try {
            val view = activity.currentFocus
            if (view != null) {
                val imm =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
                keyboardClosed = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return keyboardClosed
    }

    fun animationIntent(context: Context, intent: Intent?) {
        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition
            val options = ActivityOptions.makeCustomAnimation(context, R.anim.enter, R.anim.exit)
            context.startActivity(intent, options.toBundle())
        } else {
            // Swap without transition
            context.startActivity(intent)
        }
    }
}