package com.speedride.driver.modules.home.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.R
import com.speedride.driver.base.BaseActivity

class HelpActivity : BaseActivity() {
    override val activity: AppCompatActivity?
        get() = this@HelpActivity

    override val actionTitle: String?
        get() = resources.getString(R.string.help)
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
        setContentView(R.layout.activity_help)
        init()

    }

    fun init(){
        setToolbar(findViewById(R.id.appbar))
    }
    override fun initView(view: View?) {

    }

    override fun showToastMessage(context: Context?, message: String?) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}