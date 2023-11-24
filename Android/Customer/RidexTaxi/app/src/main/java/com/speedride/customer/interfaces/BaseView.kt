package com.speedride.customer.interfaces

import android.view.View

interface BaseView {
    fun initView(view: View?)
    fun showToastMessage(message: String)
}