//
//  ChatImageModel.swift
//  RidexCustomer


//   let chatImageModel = try? JSONDecoder().decode(ChatImageModel.self, from: jsonData)

import Foundation

// MARK: - ChatImageModel
struct ChatImageModel: Codable {
    let status: Int?
    let message: String?
    let data: String?
}
