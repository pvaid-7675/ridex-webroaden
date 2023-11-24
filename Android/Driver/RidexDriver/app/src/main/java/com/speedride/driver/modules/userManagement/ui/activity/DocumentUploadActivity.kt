package com.speedride.driver.modules.userManagement.ui.activity


import com.speedride.driver.base.BaseActivity
import android.os.Bundle
import com.speedride.driver.R
import com.speedride.driver.modules.userManagement.ui.fragment.DrivingLicenceFragment
import com.speedride.driver.modules.userManagement.ui.fragment.VehicleInsuranceFragment
import com.speedride.driver.modules.userManagement.ui.fragment.VehiclePermitFragment
import com.speedride.driver.modules.userManagement.ui.fragment.VehicleRegistrationFragment
import android.content.Context
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.speedride.driver.utils.Common

class DocumentUploadActivity : BaseActivity() {
    private var mFragmentPosition: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_upload)
        initView(null)
        mFragmentPosition = intent.getStringExtra(Common.FRAGMENT_POSITION)
        Log.d(TAG, "onCreate: $mFragmentPosition")
        when (mFragmentPosition) {
            "0" -> pushFragment(DrivingLicenceFragment())
            "1" -> pushFragment(VehicleInsuranceFragment())
            "2" -> pushFragment(VehiclePermitFragment())
            "3" -> pushFragment(VehicleRegistrationFragment())
        }
    }

    fun pushFragment(fragment: Fragment?) {
        /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_contain, fragment)
                        .commit();
            }
        });*/
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_contain, fragment!!)
            .commit()
    }

    //handle fragments in drawer
    fun pushFragment(
        fragment: Fragment?,
        parentFragment: Fragment?,
        isAddToBackStack: Boolean,
        isJustAdd: Boolean,
        shouldAnimate: Boolean,
        ignorIfCurrent: Boolean
    ) {
        if (fragment == null || parentFragment == null) return

        // Add the fragment to the 'fragment_container' FrameLayout
        val fragmentManager: FragmentManager?

//        if (isFirstFragment)
        fragmentManager = parentFragment.fragmentManager
        //        else
//            fragmentManager = parentFragment.getChildFragmentManager();

        // Find current visible fragment
        val fragmentCurrent = fragmentManager!!.findFragmentById(R.id.frame_contain)
        if (ignorIfCurrent && fragmentCurrent != null) {
            if (fragment.javaClass.canonicalName.equals(fragmentCurrent.tag, ignoreCase = true)) {
                return
            }
        }
        val fragmentTransaction = fragmentManager.beginTransaction()
        if (shouldAnimate) {
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        } else {
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }
        if (fragmentCurrent != null) {
            fragmentTransaction.hide(fragmentCurrent)
        }
        if (isAddToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.canonicalName)
        }
        if (isJustAdd) {
            fragmentTransaction.add(R.id.frame_contain, fragment, fragment.javaClass.canonicalName)
        } else {
            fragmentTransaction.replace(
                R.id.frame_contain,
                fragment,
                fragment.javaClass.canonicalName
            )
        }
        fragmentTransaction.commitAllowingStateLoss()
    }

    //hide keypad
    private fun hideKeyboard() {
        val inputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        this.currentFocus!!.clearFocus()
    }

    val visibleFragment: Fragment?
        get() {
            val fragmentManager = supportFragmentManager
            val fragments = fragmentManager.fragments
            if (fragments != null) {
                for (fragment in fragments) {
                    if (fragment != null && fragment.isVisible) return fragment
                }
            }
            return null
        }

    //for set title
    fun setTitle(title: String?) {
        setToolbarTitle(title)
    }

    override val activity: AppCompatActivity
        get() = this@DocumentUploadActivity
    override val actionTitle: String?
        get() = null
    override val isHomeButtonEnable: Boolean
        get() = true

    override fun setHomeButtonIcon(): Int {
        return R.drawable.ic_back
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initView(view: View?) {
        setToolbar((findViewById<View>(R.id.rlToolbar) as RelativeLayout))
    }

    override fun showToastMessage(context: Context?, message: String?) {}

    companion object {
        private val TAG = DocumentUploadActivity::class.java.simpleName
    }
}