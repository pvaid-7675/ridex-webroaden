package com.speedride.driver.modules.userManagement.ui.fragment

import android.content.Context
import com.speedride.driver.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import com.speedride.driver.R
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.speedride.driver.modules.home.ui.adapter.ViewPagerAdapter

class EarningsFragment : BaseFragment() {
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_earnings

    override fun onViewCreate(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            initView(view)
        }
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {}
    override fun printLog(message: String?) {}
    override fun initView(view: View?) {
        viewPager = view?.findViewById(R.id.viewpager)
        tabLayout = view?.findViewById(R.id.tabs_layout)

        //set viewpager adapters all fragment to viewpager
        setupViewPager(viewPager)
        tabLayout!!.setupWithViewPager(viewPager)
        //set selected tab text colour
        //tabLayout.setTabTextColors(getResources().getColor(R.color.gray), getResources().getColor(R.color.colorAccent));
    }

    override fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        if (requireActivity().supportFragmentManager != null) {
            Log.d(TAG, "setupViewPager: " + TAG)
            val adapter = ViewPagerAdapter(childFragmentManager)
            adapter.addFragment(TodayEarningFragment(), "Today")
            adapter.addFragment(WeeklyEarningFragment(), "Weekly")
            viewPager!!.adapter = adapter
        }
        //viewPager.setCurrentItem(1);
    }

    companion object {
        private val TAG = EarningsFragment::class.java.simpleName
    }
}