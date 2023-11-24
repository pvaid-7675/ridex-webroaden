package com.speedride.customer.interfaces

import java.io.File

interface ImagePickerListener {
    fun getImageFileFromPicker(image: File)
}