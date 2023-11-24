package com.speedride.customer.base

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.speedride.customer.interfaces.ICoreApp
import com.speedride.customer.modules.utils.BackgroundManager

abstract class CoreApp : Application() {

    companion object : ICoreApp {
        override lateinit var preferences: SharedPreferences
        override lateinit var backgroundManager: BackgroundManager
    }

    abstract fun localizedContext(): Context

    fun localeAwareContext(base: Context): Context {
        //return LocaleHelper.onAttach(base)
        return base
    }
}
