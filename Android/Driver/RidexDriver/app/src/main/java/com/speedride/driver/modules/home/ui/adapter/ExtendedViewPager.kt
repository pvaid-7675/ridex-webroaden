package com.speedride.driver.modules.home.ui.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.speedride.driver.ui.TouchImageView

class ExtendedViewPager : ViewPager {


    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    override fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        return if (v is TouchImageView) {
            v.canScrollHorizontallyFroyo(-dx)
        } else {
            super.canScroll(v, checkV, dx, x, y)
        }
    }
}