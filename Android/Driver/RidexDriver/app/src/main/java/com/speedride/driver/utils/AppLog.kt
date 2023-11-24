package com.speedride.driver.utils

import android.util.Log
import java.lang.Exception

/**
 * Created by MTPC-133 on 2/13/2018.
 */
object AppLog {
    var DEBUG = true
    fun e(tag: String?, message: String?) {
        if (DEBUG && ValidationUtils.isValidString(message)) {
            Log.e(tag, message!!)
        }
    }

    fun e(tag: String?, message: String?, e: Exception?) {
        if (DEBUG && ValidationUtils.isValidString(message)) {
            Log.e(tag, message, e)
        }
    }

    fun w(tag: String?, message: String?) {
        if (DEBUG && ValidationUtils.isValidString(message)) {
            Log.w(tag, message!!)
        }
    }

    fun d(tag: String?, message: String?) {
        if (DEBUG && ValidationUtils.isValidString(message)) {
            Log.d(tag, message!!)
        }
    }

    fun i(tag: String?, message: String?) {
        if (DEBUG && ValidationUtils.isValidString(message)) {
            Log.i(tag, message!!)
        }
    }

    fun wtf(tag: String?, message: String?) {
        if (DEBUG && ValidationUtils.isValidString(message)) {
            Log.wtf(tag, message)
        }
    }

    fun wtf(tag: String?, message: String?, e: Exception?) {
        if (DEBUG && ValidationUtils.isValidString(message)) {
            Log.wtf(tag, message, e)
        }
    }

    fun e(tag: String?, value: Int) {
        e(tag, value.toString())
    }

    fun e(tag: String?, value: Float) {
        e(tag, value.toString())
    }

    fun e(tag: String?, value: Double) {
        e(tag, value.toString())
    }

    fun e(tag: String?, value: Boolean) {
        e(tag, value.toString())
    }

    fun w(tag: String?, value: Int) {
        w(tag, value.toString())
    }

    fun w(tag: String?, value: Float) {
        w(tag, value.toString())
    }

    fun w(tag: String?, value: Double) {
        w(tag, value.toString())
    }

    fun w(tag: String?, value: Boolean) {
        w(tag, value.toString())
    }

    fun d(tag: String?, value: Int) {
        d(tag, value.toString())
    }

    fun d(tag: String?, value: Float) {
        d(tag, value.toString())
    }

    fun d(tag: String?, value: Double) {
        d(tag, value.toString())
    }

    fun d(tag: String?, value: Boolean) {
        d(tag, value.toString())
    }

    fun i(tag: String?, value: Int) {
        i(tag, value.toString())
    }

    fun i(tag: String?, value: Float) {
        i(tag, value.toString())
    }

    fun i(tag: String?, value: Double) {
        i(tag, value.toString())
    }

    fun i(tag: String?, value: Boolean) {
        i(tag, value.toString())
    }

    fun wtf(tag: String?, value: Int) {
        wtf(tag, value.toString())
    }

    fun wtf(tag: String?, value: Float) {
        wtf(tag, value.toString())
    }

    fun wtf(tag: String?, value: Double) {
        wtf(tag, value.toString())
    }

    fun wtf(tag: String?, value: Boolean) {
        wtf(tag, value.toString())
    }

    fun enableLogging() {
        DEBUG = true
    }

    fun disableLogging() {
        DEBUG = true
    }
}