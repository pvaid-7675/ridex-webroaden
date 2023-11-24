package com.speedride.driver.modules.home.ui.adapter

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.speedride.driver.R
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.engine.GlideException
import android.widget.RelativeLayout
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.speedride.driver.utils.Common

class ViewImagesPagerAdapter(
    private val context: Context,
    private val images: Array<String>,
    onImageClickListener: OnImageClickListener?
) : PagerAdapter() {
    private val layoutInflater: LayoutInflater
    private val onImageClickListener: OnImageClickListener?

    interface OnImageClickListener {
        fun onImageClick()
    }

    init {
        layoutInflater = LayoutInflater.from(context)
        this.onImageClickListener = onImageClickListener
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView: ImageView
        val itemView = layoutInflater.inflate(R.layout.item_viewpager_viewimage, container, false)
        imageView = itemView.findViewById(R.id.imageViewUserPhotos)
        val progressBar = itemView.findViewById<ProgressBar>(R.id.progress)
        Glide.with(context).load(Common.UPLOAD_URL + images[position])
            .apply(
                RequestOptions().placeholder(R.drawable.ic_progress_bar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            )
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }
            }).into(imageView)
        imageView.setOnClickListener { onImageClickListener?.onImageClick() }
        container.addView(itemView)
        return itemView
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }
}