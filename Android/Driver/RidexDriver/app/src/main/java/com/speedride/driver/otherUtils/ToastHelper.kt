package com.speedride.driver.otherUtils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.speedride.driver.R
import com.speedride.driver.databinding.ToastHelperLayoutBinding

class ToastHelper {

    companion object {
        private const val SUCCESS = 200
        private const val INFO = 300
        private const val WARNING = 400
        private const val ERROR = 500
        const val LENGTH_LONG = Toast.LENGTH_LONG
        const val LENGTH_SHORT = Toast.LENGTH_SHORT

        fun success(mCtx: Context, msg: String, length: Int): Toast {
            return create(mCtx, msg, length, SUCCESS)
        }

        fun info(mCtx: Context, msg: String, length: Int): Toast {
            return create(mCtx, msg, length, INFO)
        }

        fun warning(mCtx: Context, msg: String, length: Int): Toast {
            return create(mCtx, msg, length, WARNING)
        }

        fun error(mCtx: Context, msg: String, length: Int): Toast {
            return create(mCtx, msg, length, ERROR)
        }

        private fun create(mCtx: Context, msg: String, length: Int, type: Int): Toast {

            val toast = Toast(mCtx)
            toast.view?.setBackgroundColor(Color.TRANSPARENT)

            when (type) {
                SUCCESS -> {
                    val binding = ToastHelperLayoutBinding.inflate(LayoutInflater.from(mCtx))
                    binding.messageView.text = msg
                    binding.cardParent.setCardBackgroundColor(
                        ContextCompat.getColor(
                            mCtx,
                            R.color.success
                        )
                    )
                    binding.icon.setImageResource(R.drawable.ic_success)
                    toast.view = binding.root
                }
                INFO -> {


                    val binding = ToastHelperLayoutBinding.inflate(LayoutInflater.from(mCtx))
                    binding.messageView.text = msg
                    binding.cardParent.setCardBackgroundColor(
                        ContextCompat.getColor(
                            mCtx,
                            R.color.info
                        )
                    )
                    binding.icon.setImageResource(R.drawable.ic_info)
                    toast.view = binding.root


                }
                WARNING -> {
                    val binding = ToastHelperLayoutBinding.inflate(LayoutInflater.from(mCtx))
                    binding.messageView.text = msg
                    binding.cardParent.setCardBackgroundColor(
                        ContextCompat.getColor(
                            mCtx,
                            R.color.warning
                        )
                    )
                    binding.icon.setImageResource(R.drawable.ic_warning)
                    toast.view = binding.root

                }
                ERROR -> {
                    val binding = ToastHelperLayoutBinding.inflate(LayoutInflater.from(mCtx))
                    binding.messageView.text = msg
                    binding.cardParent.setCardBackgroundColor(
                        ContextCompat.getColor(
                            mCtx,
                            R.color.error
                        )
                    )
                    binding.icon.setImageResource(R.drawable.ic_error)
                    toast.view = binding.root
                }

            }
            return toast
        }
    }

}