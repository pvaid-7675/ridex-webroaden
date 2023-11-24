package com.speedride.driver.utils

import android.app.ProgressDialog
import android.content.Context
import com.speedride.driver.R

object ProgressDialogHelper {
    private var progressDialog: ProgressDialog? = null

    fun showProgressDialog(context: Context): ProgressDialog? {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(context, R.style.StyleProgressDialog)
            progressDialog!!.setCancelable(false)
            progressDialog!!.setCanceledOnTouchOutside(false)
            progressDialog!!.setOnCancelListener { progressDialog = null }
            try {
                progressDialog!!.show()
            } catch (e: Exception) {
            }
        }
        return progressDialog
    }


    fun dismissProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            try {
                progressDialog!!.dismiss()
            } catch (e: Exception) {
            }
        }
        progressDialog = null
    }

}