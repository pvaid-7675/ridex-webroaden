package com.speedride.customer.interfaces

import com.speedride.customer.modules.main.model.Reason

interface ReasonListener {
    fun onItemClick(item: Reason?, reason: String?)
}