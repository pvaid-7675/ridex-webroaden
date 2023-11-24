package com.speedride.driver.modules.home.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.R
import com.speedride.driver.base.BaseActivity


class AboutFragment : BaseActivity() {
    override val activity: AppCompatActivity?
        get() = this@AboutFragment
    override val actionTitle: String?
        get() = resources.getString(R.string.about)
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }
    fun setTitle(title: String?) {
        setToolbarTitle(title)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_about)
        init()

    }

    fun init(){
        setToolbar(findViewById(R.id.appbar))
    }
    override fun initView(view: View?) {
        TODO("Not yet implemented")
    }

    override fun showToastMessage(context: Context?, message: String?) {
        TODO("Not yet implemented")
    }


}