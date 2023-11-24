package com.speedride.driver.modules.userManagement.dataModel

import java.io.Serializable


data class VehicleType(
    var id: String? = null,
    var max_person: String? = null,
    var v_image: String? = null,
    var description: String? = null,
    var v_name: String? = null,
    var selected: Boolean = false
): Serializable