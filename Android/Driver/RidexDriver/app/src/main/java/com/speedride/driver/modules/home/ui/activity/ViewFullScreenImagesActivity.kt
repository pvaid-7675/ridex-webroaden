package com.speedride.driver.modules.home.ui.activity

import androidx.appcompat.app.AppCompatActivity
import com.speedride.driver.modules.home.ui.adapter.ViewImagesPagerAdapter.OnImageClickListener
import android.widget.TextView
import android.widget.RelativeLayout
import com.speedride.driver.modules.home.ui.adapter.ViewImagesPagerAdapter
import android.os.Bundle
import com.speedride.driver.R
import android.util.Log
import android.view.View
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.speedride.driver.modules.home.ui.adapter.ExtendedViewPager

class ViewFullScreenImagesActivity : AppCompatActivity(), View.OnClickListener,
    OnImageClickListener {
    private var mExtendedViewPager: ExtendedViewPager? = null
    private var mTextViewImageCount: TextView? = null
    private var mTextViewDone: TextView? = null
    private var mTxtImageName: TextView? = null
    private val rlMain: RelativeLayout? = null
    private var mImageList: Array<String>? = null
    private var mPosition = 0
    private var mTotalImages = 0
    private var rlToolbar: RelativeLayout? = null
    private var isClicked = false
    private var mViewImagesPagerAdapter: ViewImagesPagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile_pictures)
        initViews()
    }

    /*
     *  Initialization views
     * */
    private fun initViews() {
        mExtendedViewPager = findViewById(R.id.viewPager)
        mTextViewDone = findViewById(R.id.textViewDone)
        mTxtImageName = findViewById(R.id.txtImageName)
        mTextViewImageCount = findViewById(R.id.textViewImageCount)
        rlToolbar = findViewById(R.id.rlToolbar)

        // on click listener
        mTextViewDone!!.setOnClickListener(this)

        //get mPosition and image list
        val intent = intent
        if (intent != null) {
            mImageList = intent.getStringArrayExtra("Document_List")
            mPosition = intent.getIntExtra("Document_List_Position", 0)
            mTxtImageName!!.setText(intent.getStringExtra("Document_Name"))
            mTotalImages = mImageList!!.size
        }
        mViewImagesPagerAdapter = mImageList?.let { ViewImagesPagerAdapter(this, it, this) }
        mExtendedViewPager!!.adapter = mViewImagesPagerAdapter
        mExtendedViewPager!!.currentItem = mPosition

        // set count
        if (mTotalImages > 1) {
            mTextViewImageCount!!.setVisibility(View.VISIBLE)
            mPosition++
            mTextViewImageCount!!.setText(mPosition.toString() + " " + getString(R.string.text_of) + " " + mTotalImages)
        } else {
            mTextViewImageCount!!.setVisibility(View.GONE)
        }
        setUpViewPager()
    }

    private fun setUpViewPager() {
        mExtendedViewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (isClicked) {
                    rlToolbar!!.visibility = View.GONE
                    isClicked = false
                }
            }

            override fun onPageSelected(position: Int) {
                var position = position
                position++
                mTextViewImageCount!!.text =
                    position.toString() + " " + getString(R.string.text_of) + " " + mTotalImages
                Log.d(TAG, "onPageSelected: $position")
                manageImageName(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun manageImageName(mPos: Int) {
        if (mPos == 1) {
            mTxtImageName!!.text = resources.getString(R.string.driving_licence)
        } else if (mPos == 2) {
            mTxtImageName!!.text = resources.getString(R.string.vehicle_insurance)
        } else if (mPos == 3) {
            mTxtImageName!!.text = resources.getString(R.string.vehicle_permit)
        } else if (mPos == 4) {
            mTxtImageName!!.text = resources.getString(R.string.vehicle_registration)
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.textViewDone) {
            finish()
        }
    }

    override fun onImageClick() {
        if (isClicked) {
            rlToolbar!!.visibility = View.GONE
            isClicked = false
        } else {
            rlToolbar!!.visibility = View.VISIBLE
            isClicked = true
        }
    }

    companion object {
        private val TAG = ViewFullScreenImagesActivity::class.java.simpleName
    }
}