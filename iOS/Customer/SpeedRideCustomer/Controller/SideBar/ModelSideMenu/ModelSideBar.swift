//
//  ModelSideBar.swift
//  MTLogin
//
//

import Foundation
import UIKit

//MARK: - Model Class leftSidebar
class ModelSideBar {
    
    var title:String = ""
    var image:UIImage?
    
    init(strTitle:String,image:UIImage) {
        
        self.title = strTitle
        self.image = image
    }
}
//MARK: - Model Class for RightSideBar
class ModelRightSideBar {
    
    var title:String = ""
    
    init(strTitle:String) {
        
        self.title = strTitle
    }
    
}
//Model Class for userDetails
class ModelUserDetails {
    
    var userName:String = ""
    var image:UIImage?
    
    init(strTitle:String,image:UIImage) {
        
        self.userName = strTitle
        self.image = image
    }
}
//Title for left sideBar
enum Title : String {
    case home = "Home"
    case payment = "Payment"
    case history = "History"
    case scheduleRide = "Scheduled Rides"
    case notification = "Notification"
    case setting = "Setting"
    case help = "Help"
    case logout = "LogOut"
}

enum SettingTitle : String {
    case EditProfile = "Edit Profile"
    case ChangePassword = "Change Password"
    case EmergencyContacts = "Emergency Contacts"
    case DeleteAccount = "Delete Account"
    
}
