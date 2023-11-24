//
//  Model_APIResponse.swift
//  RidexCustomer
//
//  Created by Krunal's MacBook Air on 14/07/21.
//  Copyright © 2021 Devubha Manek. All rights reserved.
//

import Foundation

struct Model_APIResponse<M:Codable>: Codable{
    var data: M?
    var message: String?
    var status: Int?
    var lastPage: Int?
    var accessToken: String?
}
struct Model_Dummy: Codable{

}
struct Model_Default: Codable{

}
