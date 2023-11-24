package com.speedride.customer.interfaces

import android.content.SharedPreferences
import com.speedride.customer.modules.utils.BackgroundManager

interface Clearable {
    fun clear()
}

interface ICoreApp {
    var preferences: SharedPreferences
    var backgroundManager: BackgroundManager
}