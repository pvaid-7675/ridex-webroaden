package com.speedride.driver.utils

import android.app.NotificationManager
import android.content.Context
import android.widget.EditText
import android.net.ConnectivityManager
import java.lang.Exception
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by MTPC-133 on 11/22/2017.
 */
object ValidationUtils {
    fun cancelOffNotification(context: Context, Id: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager != null && isValidObject(Id)) {
            manager.cancel(Id)
        }
    }

    @JvmStatic
    fun <T> isValidObject(`object`: T?): Boolean {
        return if (`object` != null) {
            true
        } else {
            false
        }
    }

    fun <T> isValidList(list: List<T>?): Boolean {
        return if (list != null && list.size > 0) {
            true
        } else {
            false
        }
    }

    fun isValidString(string: String?): Boolean {
        return if (string != null && string.length > 0) {
            true
        } else {
            false
        }
    }

    fun isValidString(editText: EditText): Boolean {
        val text = getStringFromEditText(editText)
        return if (text != null && text.length > 0) {
            true
        } else {
            false
        }
    }

    fun isValidString(editText: EditText, error: String?): Boolean {
        return if (isValidString(getStringFromEditText(editText))) {
            editText.error = null
            true
        } else {
            editText.error = error
            false
        }
    }

    fun getStringFromEditText(editText: EditText): String {
        return editText.text.toString().trim { it <= ' ' }
    }

    fun isValidEmailAddress(argEditText: EditText): Boolean {
        return try {
            if (isValidString(getStringFromEditText(argEditText))) {
                argEditText.error = null
                val pattern =
                    Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
                val matcher = pattern.matcher(argEditText.text)
                if (matcher.matches()) {
                    argEditText.error = null
                    true
                } else {
//                    argEditText.setError("Please Enter valid email address.");
                    false
                }
            } else {
//                argEditText.setError("Please Enter email address.");
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isValidPhoneNumber(editText: EditText): Boolean {
        val text = editText.text.toString()
        return if (isValidString(text) && text.length >= 10 && text.length <= 16) true else false
    }

    /* public static boolean isValidMobile(EditText editText) {
        String phone = editText.getText().toString().trim();
        if (phone.length() != 10)
            return false;
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }*/
    fun isValidPassword(editText: EditText): Boolean {
        val password = editText.text.toString()
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

    @JvmStatic
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
            .isConnected
    }

    fun isValidMobile(editText: EditText): Boolean {
        val phone = editText.text.toString().trim { it <= ' ' }
        return if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length <10) {
                // if(phone.length() != 10) {
                false
            } else {
                true
            }
        } else {
            false
        }
    }

    // email validation
    fun isValidEmail(editText: EditText): Boolean {
        val email = editText.text.toString().trim { it <= ' ' }
        var isValid = false
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val inputStr: CharSequence = email
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(inputStr)
        if (matcher.matches()) {
            isValid = true
        }
        return isValid
    }
}