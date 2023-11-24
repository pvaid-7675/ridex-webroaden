package com.speedride.driver.otherUtils

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.speedride.driver.R
import com.speedride.driver.databinding.SnackHelperLayoutBinding

class SnackHelper {

    companion object {
        private const val SUCCESS = 200
        private const val INFO = 300
        private const val WARNING = 400
        private const val ERROR = 500
        const val LENGTH_LONG = Snackbar.LENGTH_LONG
        const val LENGTH_SHORT = Snackbar.LENGTH_SHORT
        const val LENGTH_INDEFINITE = Snackbar.LENGTH_INDEFINITE

        fun success(view: View, msg:String, length: Int): Snackbar {
            return create(view, msg, length, SUCCESS)
        }
        fun info(view: View, msg:String, length: Int): Snackbar {
            return create(view, msg, length, INFO)
        }
        fun warning(view: View, msg:String, length: Int): Snackbar {
            return create(view, msg, length, WARNING)
        }
        fun error(view: View, msg:String, length: Int): Snackbar {
            return create(view, msg, length, ERROR)
        }

        private fun create(view: View, msg:String, length:Int, type:Int): Snackbar {

            val snackBar = Snackbar.make(view, "", length)
            snackBar.view.setBackgroundColor(Color.TRANSPARENT)
            val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout
            snackBarLayout.setPadding(0,0,0,0)

            when(type){
                SUCCESS -> {
                    val binding = SnackHelperLayoutBinding.inflate(LayoutInflater.from(view.context))
                    binding.messageView.text = msg
                    binding.cardParent.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.success))
                    binding.icon.setImageResource(R.drawable.ic_success)
                    snackBarLayout.addView(binding.root)
                }
                INFO -> {
                    val binding = SnackHelperLayoutBinding.inflate(LayoutInflater.from(view.context))
                    binding.messageView.text = msg
                    binding.cardParent.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.info))
                    binding.icon.setImageResource(R.drawable.ic_info)
                    snackBarLayout.addView(binding.root)
                }
                WARNING -> {
                    val binding = SnackHelperLayoutBinding.inflate(LayoutInflater.from(view.context))
                    binding.messageView.text = msg
                    binding.cardParent.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.warning))
                    binding.icon.setImageResource(R.drawable.ic_warning)
                    snackBarLayout.addView(binding.root)
                }
                ERROR -> {
                    val binding = SnackHelperLayoutBinding.inflate(LayoutInflater.from(view.context))
                    binding.messageView.text = msg
                    binding.cardParent.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.error))
                    binding.icon.setImageResource(R.drawable.ic_error)
                    snackBarLayout.addView(binding.root)
                }

            }
            return snackBar
        }
    }


}