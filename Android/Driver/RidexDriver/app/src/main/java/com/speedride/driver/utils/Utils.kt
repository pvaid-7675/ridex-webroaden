package com.speedride.driver.utils

import android.app.ActivityOptions
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.speedride.driver.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun makeTextViewResizable(tv: TextView, maxLine: Int, expandText: String, viewMore: Boolean) {
        if (tv.tag == null) {
            tv.tag = tv.text
        }
        val vto = tv.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val text: String
                val lineEndIndex: Int
                val obs = tv.viewTreeObserver
                obs.removeGlobalOnLayoutListener(this)
                if (maxLine == 0) {
                    lineEndIndex = tv.layout.getLineEnd(0)
                    text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                } else if (maxLine > 0 && tv.lineCount >= maxLine) {
                    lineEndIndex = tv.layout.getLineEnd(maxLine - 1)
                    text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                } else {
                    lineEndIndex = tv.layout.getLineEnd(tv.layout.lineCount - 1)
                    text = tv.text.subSequence(0, lineEndIndex).toString() + " " + expandText
                }
                tv.text = text
                tv.movementMethod = LinkMovementMethod.getInstance()
                tv.setText(
                    addClickablePartTextViewResizable(
                        Html.fromHtml(tv.text.toString()), tv, lineEndIndex, expandText,
                        viewMore
                    ), TextView.BufferType.SPANNABLE
                )
            }
        })
    }

    private fun addClickablePartTextViewResizable(
        strSpanned: Spanned, tv: TextView,
        maxLine: Int, spanableText: String, viewMore: Boolean
    ): SpannableStringBuilder {
        val str = strSpanned.toString()
        val ssb = SpannableStringBuilder(strSpanned)
        if (str.contains(spanableText)) {
            ssb.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    tv.layoutParams = tv.layoutParams
                    tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                    tv.invalidate()
                    if (viewMore) {
                        makeTextViewResizable(tv, -1, "View Less", false)
                    } else {
                        makeTextViewResizable(tv, 3, "View More", true)
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length, 0)
        }
        return ssb
    }

    fun convertStringDateToDateFormat(inputDate: String?): Date? {
        if (inputDate == null) return null
        var theDateFormat = SimpleDateFormat("HH:mm a")
        var date: Date? = null
        try {
            date = theDateFormat.parse(inputDate)
        } catch (parseException: ParseException) {
            // Date is invalid. Do what you want.
        } catch (exception: Exception) {
            // Generic catch. Do what you want.
        }
        theDateFormat = SimpleDateFormat("HH:mm a")
        theDateFormat.format(date)
        return date
    }

    fun stringDateToFormatDate(dtStart: String?): Date? {
        if (dtStart == null) return null
        var date: Date? = null
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            date = format.parse(dtStart)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    fun stringDateToFormatDateShared(dtStart :String?):String{
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputFormat = SimpleDateFormat("hh:mm a")
        val parsedDate = inputFormat.parse(dtStart.toString())
        val formattedDate = outputFormat.format(parsedDate)
        println(formattedDate)
        return formattedDate
    }

    fun dateFormatToStringDateForEarning(dtStart: Date?): String {
        if (dtStart == null) return ""
        val dateTime: String
        val dateFormat = SimpleDateFormat("HH:mm aa")
        dateTime = dateFormat.format(dtStart)
        return dateTime
    }

    fun dateFormatToStringDateForRating(dtStart: Date?): String {
        if (dtStart == null) return ""
        val dateTime: String
        val dateFormat = SimpleDateFormat("dd MMM yyyy")
        dateTime = dateFormat.format(dtStart)
        return dateTime
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

    //add vehicle images
    fun checkVehicleImages(id: String): Int {
        if (id == "1") {
            return R.mipmap.auto
        } else if (id == "2") {
            return R.mipmap.car_micro
        } else if (id == "3") {
            return R.mipmap.car_mini
        } else if (id == "4") {
            return R.mipmap.car_prime
        } else if (id == "5") {
            return R.mipmap.car_rental
        } else if (id == "6") {
            return R.mipmap.car_share
        }
        return 0
    }

    //show selected vehicle images
    fun checkVehicleImagesSelected(id: String): Int {
        if (id == "1") {
            return R.mipmap.auto_active
        } else if (id == "2") {
            return R.mipmap.car_micro_active
        } else if (id == "3") {
            return R.mipmap.car_mini_active
        } else if (id == "4") {
            return R.mipmap.car_prime_active
        } else if (id == "5") {
            return R.mipmap.car_rental_active
        } else if (id == "6") {
            return R.mipmap.car_share_active
        }
        return R.drawable.ic_car_marker
    }

    //show driver vehicle images
    fun checkVehicleImagesTopView(id: String): Int {
        if (id == "1") {
            return R.drawable.ic_car_micro_top_view
        } else if (id == "2") {
            return R.drawable.ic_car_micro_top_view
        } else if (id == "3") {
            return R.drawable.ic_car_mini_top_view
        } else if (id == "4") {
            return R.drawable.ic_car_prime_top_view
        } else if (id == "5") {
            return R.drawable.ic_car_rental_top_view
        } else if (id == "6") {
            return R.drawable.ic_car_share_top_view
        }
        return R.drawable.ic_car_marker
    }

    fun formateDateFromstring(
        inputFormat: String?,
        outputFormat: String?,
        inputDate: String?
    ): String? {
        var parsed: Date? = null
        var outputDate = ""
        val df_input = SimpleDateFormat(inputFormat, Locale.getDefault())
        val df_output = SimpleDateFormat(outputFormat, Locale.getDefault())
        try {
            parsed = df_input.parse(inputDate)
            outputDate = df_output.format(parsed)
        } catch (e: ParseException) {
            Log.d(TAG, "ParseException - dateFormat")
        }
        return outputDate
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getCurrentDate():String{
        val d = Date(Date().time + 28800000)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = sdf.format(Date())
        System.out.println(" C DATE is  "+currentDate)
        return currentDate
    }

}