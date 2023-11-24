package com.speedride.driver.modules.ride.dataModel

class CustomerRequestModel {
    var isCanceled = false
    var unique_ref: String? = ""
    var pickup: String? = ""
    var bookid: String? = ""
    var paymode: String? = ""
    var dlong: String? = ""
    var dlat: String? = ""
    var vt_id: String? = ""
    var departure: String? = ""
    var image: String? = ""
    var km: String? = ""
    var name: String? = ""
    var plat: String? = ""
    var charge: String? = ""
    var requestdatetime: String? = ""
    var esttime: String? = ""
    var estprice: String? = ""
    var driver_id: Int? = 0
    var plong: String? = ""
    var customer_id: String? = ""
    var mobile: String? = ""
    var estimate_time: String? = ""
    var is_schedule:String = "0"
    var book_id:String = ""
    var is_admin_created:Int = 0
    var is_sharing:Int=0 //1= shared and 0=not shared
//    //This estimate_time veriable is setting Payal and change esttime na badle me estimate_time rakhyu che
//    var estimate_time: String? = ""
}