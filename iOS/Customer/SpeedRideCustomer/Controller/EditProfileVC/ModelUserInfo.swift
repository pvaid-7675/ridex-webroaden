//
//  ModelUserInfo.swift
//  RidexCustomer
//
//

import UIKit
import SwiftyJSON

class ModelUserInfo {
    
    var data : ModelUserInfoData!
    var msg : String!
    var status : Int!
    
    init() {
        
    }
    
    /**
     * Instantiate the instance using the passed json values to set the properties values
     */
    init(fromJson json: JSON!){
        if json.isEmpty{
            return
        }
        let dataJson = json["data"]
        if !dataJson.isEmpty{
            data = ModelUserInfoData(fromJson: dataJson)
        }
        msg = json["msg"].stringValue
        status = json["status"].intValue
    }
}

class ModelUserInfoData{
    
    var email : String!
    var firstName : String!
    var id : Int!
    var image : String!
    var lastName : String!
    var lat : String!
    var lng : String!
    var loginType : String!
    var mobile : String!
    var onDuty : String!
    var phoneVerified : String!
    var role : String!
    var socialId : String!
    var status : String!
    var updatedAt : String!
    
    init() {
        
    }
    /**
     * Instantiate the instance using the passed json values to set the properties values
     */
    init(fromJson json: JSON!){
        if json.isEmpty{
            return
        }
       
        email = json["email"].stringValue
        firstName = json["first_name"].stringValue
        id = json["id"].intValue
        image = json["image"].stringValue
        lastName = json["last_name"].stringValue
        lat = json["lat"].stringValue
        lng = json["lng"].stringValue
        loginType = json["login_type"].stringValue
        mobile = json["mobile"].stringValue
        onDuty = json["on_duty"].stringValue
        phoneVerified = json["phone_verified"].stringValue
        role = json["role"].stringValue
        socialId = json["social_id"].stringValue
        status = json["status"].stringValue
        updatedAt = json["updated_at"].stringValue
    }
}

