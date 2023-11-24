//
//  Model_UserInfo.swift
//  RidexCustomer
//
//  Created by Krunal's MacBook Air on 14/07/21.
//  Copyright © 2021 Devubha Manek. All rights reserved.
//

import Foundation

struct Model_UserInfo : Codable {
    let countryPhoneCode : String?
    let createdAt : String?
    let documentVerify : String?
    let driverDetail : DriverDetailBlank?
    let email : String?
    let emailVerifiedAt : String?
    let id : Int?
    let image : String?
    let lat : String?
    let lng : String?
    let loginType : String?
    let mobile : String?
    let name : String?
    let onDuty : String?
    let phoneVerified : String?
    let role : String?
    let socialId : String?
    let status : String?
    let updatedAt : String?
    let vehicleDetail : VehicleDetailBlank?
}

struct DriverDetailBlank : Codable {
}

struct VehicleDetailBlank : Codable {
}
/*struct Model_UserInfo : Codable {
    let createdAt : String?
    let documentVerify : String?
    let driverDetail : DriverDetailModel?
    let email : String?
    let emailVerifiedAt : String?
    let id : Int?
    let image : String?
    let lat : String?
    let lng : String?
    let loginType : String?
    let mobile : String?
    let name : String?
    let onDuty : String?
    let phoneVerified : String?
    let role : String?
    let socialId : String?
    let status : String?
    let updatedAt : String?
    let vehicleDetail : VehicleDetailModel?


    enum CodingKeys: String, CodingKey {
        case createdAt = "created_at"
        case documentVerify = "document_verify"
        case driverDetail
        case email = "email"
        case emailVerifiedAt = "email_verified_at"
        case id = "id"
        case image = "image"
        case lat = "lat"
        case lng = "lng"
        case loginType = "login_type"
        case mobile = "mobile"
        case name = "name"
        case onDuty = "on_duty"
        case phoneVerified = "phone_verified"
        case role = "role"
        case socialId = "social_id"
        case status = "status"
        case updatedAt = "updated_at"
        case vehicleDetail
    }
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        createdAt = try values.decodeIfPresent(String.self, forKey: .createdAt)
        documentVerify = try values.decodeIfPresent(String.self, forKey: .documentVerify)
        driverDetail = try DriverDetailModel(from: decoder)
        email = try values.decodeIfPresent(String.self, forKey: .email)
        emailVerifiedAt = try values.decodeIfPresent(String.self, forKey: .emailVerifiedAt)
        id = try values.decodeIfPresent(Int.self, forKey: .id)
        image = try values.decodeIfPresent(String.self, forKey: .image)
        lat = try values.decodeIfPresent(String.self, forKey: .lat)
        lng = try values.decodeIfPresent(String.self, forKey: .lng)
        loginType = try values.decodeIfPresent(String.self, forKey: .loginType)
        mobile = try values.decodeIfPresent(String.self, forKey: .mobile)
        name = try values.decodeIfPresent(String.self, forKey: .name)
        onDuty = try values.decodeIfPresent(String.self, forKey: .onDuty)
        phoneVerified = try values.decodeIfPresent(String.self, forKey: .phoneVerified)
        role = try values.decodeIfPresent(String.self, forKey: .role)
        socialId = try values.decodeIfPresent(String.self, forKey: .socialId)
        status = try values.decodeIfPresent(String.self, forKey: .status)
        updatedAt = try values.decodeIfPresent(String.self, forKey: .updatedAt)
        vehicleDetail = try VehicleDetailModel(from: decoder)
    }

}
struct VehicleDetailModel : Codable {

    let brand : String?
    let color : String?
    let icolor : String?
    let id : Int?
    let model : String?
    let number : String?
    let userId : Int?
    let vtId : Int?
    let year : String?


    enum CodingKeys: String, CodingKey {
        case brand = "brand"
        case color = "color"
        case icolor = "icolor"
        case id = "id"
        case model = "model"
        case number = "number"
        case userId = "user_id"
        case vtId = "vt_id"
        case year = "year"
    }
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        brand = try values.decodeIfPresent(String.self, forKey: .brand)
        color = try values.decodeIfPresent(String.self, forKey: .color)
        icolor = try values.decodeIfPresent(String.self, forKey: .icolor)
        id = try values.decodeIfPresent(Int.self, forKey: .id)
        model = try values.decodeIfPresent(String.self, forKey: .model)
        number = try values.decodeIfPresent(String.self, forKey: .number)
        userId = try values.decodeIfPresent(Int.self, forKey: .userId)
        vtId = try values.decodeIfPresent(Int.self, forKey: .vtId)
        year = try values.decodeIfPresent(String.self, forKey: .year)
    }

}
struct DriverDetailModel : Codable {

    let createdAt : String?
    let dLicence : String?
    let id : Int?
    let type : String?
    let updatedAt : String?
    let userId : Int?
    let vDocument : String?
    let vInsurance : String?
    let vPermit : String?
    let vRegistration : String?
    let vtId : Int?


    enum CodingKeys: String, CodingKey {
        case createdAt = "created_at"
        case dLicence = "d_licence"
        case id = "id"
        case type = "type"
        case updatedAt = "updated_at"
        case userId = "user_id"
        case vDocument = "v_document"
        case vInsurance = "v_insurance"
        case vPermit = "v_permit"
        case vRegistration = "v_registration"
        case vtId = "vt_id"
    }
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        createdAt = try values.decodeIfPresent(String.self, forKey: .createdAt)
        dLicence = try values.decodeIfPresent(String.self, forKey: .dLicence)
        id = try values.decodeIfPresent(Int.self, forKey: .id)
        type = try values.decodeIfPresent(String.self, forKey: .type)
        updatedAt = try values.decodeIfPresent(String.self, forKey: .updatedAt)
        userId = try values.decodeIfPresent(Int.self, forKey: .userId)
        vDocument = try values.decodeIfPresent(String.self, forKey: .vDocument)
        vInsurance = try values.decodeIfPresent(String.self, forKey: .vInsurance)
        vPermit = try values.decodeIfPresent(String.self, forKey: .vPermit)
        vRegistration = try values.decodeIfPresent(String.self, forKey: .vRegistration)
        vtId = try values.decodeIfPresent(Int.self, forKey: .vtId)
    }

}
*/
