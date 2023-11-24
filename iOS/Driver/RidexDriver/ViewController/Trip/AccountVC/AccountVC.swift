//
//  AccountVC.swift
//  TaxiDriver
//
//  Created by Dilip Manek on 31/07/18.
//  Copyright © 2018 Dilip Manek. All rights reserved.
//

import UIKit

//MARK: - CellAccountData Class
class CellAccountData: UITableViewCell {
    
    //TODO: - IBOutlet Declaration
    @IBOutlet var lblType: UILabel!
    @IBOutlet var imgType: UIImageView!
    
}

//MARK: - ModelAccount
class ModelAccount {
    var imgType =  UIImage()
    var strType = String()
    
    init(imgType:UIImage, strType: String) {
        self.imgType = imgType
        self.strType = strType
    }
}

//MARK: - AccountVC
class AccountVC: MTViewController {

    //TODO: - IBOutlet Declaration
    @IBOutlet var lblName: UILabel!
    @IBOutlet var imgProfile: UIImageView!
    @IBOutlet var tblAccountData: UITableView!
    @IBOutlet var tabAccount: UITabBarItem!
    @IBOutlet var imgSwitch: UIImageView!
    @IBOutlet weak var imgVehicle: UIImageView!
    
    //TODO: - Variable Declaration
    var arrAccount = [ModelAccount]()
    var isSwitch: Bool = false
    var isContarctor = 1
    var userData : getUserData!
    
    //TODO: - Override Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        //Initialization
        
        self.initialization()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        //Set Switch
        self.setSwitch()
        self.lblName.text = appDelegate.objDriverInfo?.name ?? ""
        if let imageUrl = appDelegate.objDriverInfo?.image, imageUrl != ""{
            let url = WebURL.uploadURL + imageUrl
            self.imgProfile.imageFromURL(url, placeHolder: #imageLiteral(resourceName: "imgProfile"))
        }else{
            self.imgProfile.image = #imageLiteral(resourceName: "imgProfile")
        }
        if let imageUrl = appDelegate.objDriverInfo?.vehicleDetail?.typevehicle?.vImage{
            let url = WebURL.vehicleImageURL + imageUrl
            self.imgVehicle.imageFromURL(url, placeHolder: #imageLiteral(resourceName: "imgProfile"))
        }
        
        getDriverDetails()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
       
    }
}

//MARK: - IBAction Methods
extension AccountVC {
    
    //TODO: - Tapped On EditProfile
    @IBAction func tappedOnEditProfile(_ sender: Any) {
        self.redirectToEditProfile(isAnimated: true)
    }
    
    //TODO: - Tapped On EditCarDetails
    @IBAction func tappedOnEditCarDetails(_ sender: Any) {
        self.redirectToSelectVehicleType(isAnimated: true)
    }
    
    //TODO: - Tapped On EditVehicleDetails
    @IBAction func tappedOnEditVehicleDetails(_ sender: Any) {
        self.redirectToSelectVehicleType(isAnimated: true)
    }
    
    //TODO: - Tapped On Switch
    @IBAction func tappedOnSwitch(_ sender: Any) {
        if isSwitch == true {
            isSwitch = false
            imgSwitch.image = UIImage(named: "ImgSwitchOff")
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "Offline") , object: nil, userInfo: nil)
        } else {
            isSwitch = true
            imgSwitch.image = UIImage(named: "imgSwitchOn")
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "Online") , object: nil, userInfo: nil)
        }
    }
}

//MARK: - Other Methods
extension AccountVC {
   
    //TODO: - Initialization Methods
    func initialization(){
        self.setTabData()
        arrAccount.removeAll()
        
       
        arrAccount.append(ModelAccount.init(imgType: #imageLiteral(resourceName: "documents_menu"), strType: "Documents"))
        arrAccount.append(ModelAccount.init(imgType: UIImage(named: "history_menu")!, strType: "History"))
        if isContarctor == 1{
            arrAccount.append(ModelAccount.init(imgType: UIImage(named: "shift_time_menu")!, strType: "Shift Time"))
        }
        arrAccount.append(ModelAccount.init(imgType: #imageLiteral(resourceName: "chat_home"), strType: "Chat"))
        arrAccount.append(ModelAccount.init(imgType: #imageLiteral(resourceName: "settings_menu"), strType: "Settings"))
        arrAccount.append(ModelAccount.init(imgType: #imageLiteral(resourceName: "about_menu"), strType: "About"))
        arrAccount.append(ModelAccount.init(imgType: #imageLiteral(resourceName: "help_menu"), strType: "Help"))
        arrAccount.append(ModelAccount.init(imgType: #imageLiteral(resourceName: "logout_menu"), strType: "Logout"))
        let fName = UserInfo.sharedInstance.getMyUserDefaults(key: MyUserDefaults.userFName) as? String
        let lName = UserInfo.sharedInstance.getMyUserDefaults(key: MyUserDefaults.userLName) as? String
        self.lblName.text = fName! + " " + lName!
        
        self.tblAccountData.reloadData()
    }
    
   
    
    //TODO: - Set Switch
    func setSwitch() {
        if let onDuty = appDelegate.objDriverInfo?.onDuty, onDuty == "on"{
            isSwitch = true
            imgSwitch.image = UIImage(named: "imgSwitchOn")
        }else{
            isSwitch = false
            imgSwitch.image = UIImage(named: "ImgSwitchOff")
        }
    }
                              
    //TODO: - SetTabData
    func setTabData()  {
       // UITabBarItem.appearance().setTitleTextAttributes([NSAttributedString.Key.font: UIFont(name: "Montserrat-Regular", size:10)!, NSAttributedString.Key.foregroundColor: UIColor(red: 255.0 / 255.0, green: 71.0 / 255.0, blue: 0.0 / 255.0, alpha: 1.0)], for: .selected)
    }
    
    //TODO: - Clear User Default
    func clearUserDefault(){
        UserInfo.sharedInstance.removeMyUserDefaults(key: MyUserDefaults.userId)
        UserInfo.sharedInstance.removeMyUserDefaults(key: MyUserDefaults.userEmail)
        UserInfo.sharedInstance.removeMyUserDefaults(key: MyUserDefaults.vehicleID)
        
        UserInfo.sharedInstance.clearMyUserDefaluts()
    }
}

//MARK: - Redirect To Next
extension AccountVC {
    
    //TODO: - Redirect To Main
    func redirectToMain(isAnimated: Bool){
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let mainVC = storyboard.instantiateViewController(withIdentifier: "MainVC") as! MainVC
        self.navigationController?.pushViewController(mainVC, animated: isAnimated)
    }
    
    //TODO: - Redirect To EditProfile
    func redirectToEditProfile(isAnimated: Bool){
        let storyboard = UIStoryboard(name: "Trip", bundle: nil)
        let editProfile = storyboard.instantiateViewController(withIdentifier: "EditProfileVC") as! EditProfileVC
        self.navigationController?.pushViewController(editProfile, animated: isAnimated)
    }
    
    //TODO: - Redirect To Setting
    func redirectToSetting(isAnimated: Bool){
        let storyboard = UIStoryboard(name: "Trip", bundle: nil)
        let setting = storyboard.instantiateViewController(withIdentifier: "SettingVC") as! SettingVC
        self.navigationController?.pushViewController(setting, animated: isAnimated)
    }
    
    func redirectToChat(isAnimated: Bool){
        let storyboard = UIStoryboard(name: "Trip", bundle: nil)
        let vc = storyboard.instantiateViewController(withIdentifier: "AdminChatVC") as! AdminChatVC
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    //TODO: - Redirect To History
    func redirectToHistory(isAnimated: Bool){
        let storyboard = UIStoryboard(name: "Trip", bundle: nil)
        let history = storyboard.instantiateViewController(withIdentifier: "HistoryVC") as! HistoryVC
        self.navigationController?.pushViewController(history, animated: isAnimated)
    }
    
    //TODO: - Redirect To History
    func redirectToShiftTime(isAnimated: Bool){
        let storyboard = UIStoryboard(name: "Trip", bundle: nil)
        let history = storyboard.instantiateViewController(withIdentifier: "ShiftTimeVC") as! ShiftTimeVC
        history.userData = self.userData
        self.navigationController?.pushViewController(history, animated: isAnimated)
    }
    
    //TODO: - Redirect To Document Details
    func redirectToDocumentDetails(isAnimated: Bool){
        let storyboard = UIStoryboard(name: "Trip", bundle: nil)
        let documentDetails = storyboard.instantiateViewController(withIdentifier: "DocumentDetailsVC") as! DocumentDetailsVC
        self.navigationController?.pushViewController(documentDetails, animated: isAnimated)
    }
    
    //TODO: - Redirect To Help
    func redirectToHelp(isAnimated: Bool){
        /*let storyboard = UIStoryboard(name: "Trip", bundle: nil)
        let help = storyboard.instantiateViewController(withIdentifier: "HelpVC") as! HelpVC
        self.navigationController?.pushViewController(help, animated: isAnimated)*/
    }
    
    //TODO: - Redirect To Select Vehicle Type
    func redirectToSelectVehicleType(isAnimated: Bool){
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let selectVehicleType = storyboard.instantiateViewController(withIdentifier: "SelectVehicleTypeVC") as! SelectVehicleTypeVC
        selectVehicleType.isEditVehicle = true
        self.navigationController?.pushViewController(selectVehicleType, animated: isAnimated)
    }
}

//MARK: - UITableViewDataSource & UITableViewDelegate Methods
extension AccountVC: UITableViewDataSource, UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return arrAccount.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "CellAccountData") as! CellAccountData
        cell.imgType.image = arrAccount[indexPath.row].imgType
        cell.lblType.text = arrAccount[indexPath.row].strType
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print(indexPath.row)
        
        if isContarctor == 1{
            switch indexPath.row {
            case 0:
                self.redirectToDocumentDetails(isAnimated: true)
                break
            case 1:
                self.redirectToHistory(isAnimated: true)
                break
            case 2:
                self.redirectToShiftTime(isAnimated: true)
                break
            case 3:
                self.redirectToChat(isAnimated: true)
                break
            case 4:
                self.redirectToSetting(isAnimated: true)
                break
            case 5:
                print("hey")
                break
            case 6:
                self.redirectToHelp(isAnimated: true)
                break
            case 7:
                self.logoutApiCall()
                break
            default:
                break
            }
        }else{
            switch indexPath.row {
            case 0:
                self.redirectToDocumentDetails(isAnimated: true)
                break
            case 1:
                self.redirectToHistory(isAnimated: true)
                break
            case 2:
                self.redirectToSetting(isAnimated: true)
                break
            case 3:
                print("hey")
                break
            case 4:
                self.redirectToHelp(isAnimated: true)
                break
            case 5:
                self.logoutApiCall()
                break
            default:
                break
            }
        }
        
        
    }
}

//MARK: - API Calling
extension AccountVC {
    //updateDriverTimeSlot
    func getDriverDetails(){
        if let driverId = appDelegate.objDriverInfo?.id{
            var parameter = [String:Any]()
            parameter["user_id"] = driverId
            
            WebService.call.withPath(WebURL.getUser, parameter: parameter,methods: .get) { (responseDic) in
                print("====responseDic====\(responseDic)")
                WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<getUserData>?) in
                    if APIResponse?.status == 200, let tempData = APIResponse?.data {
                        self.userData = tempData
//                        if tempData.contract == "0"{
//                            self.isContarctor = 0
//                            self.initialization()
//                        }
                    }else{
                        //Show Alert
                        showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                    }
                }
            }
        }
    }
    
    //TODO: - Logout Api Call
    func logoutApiCall(){
        WebService.call.withPath(WebURL.logout) { [weak self] (responseDic) in
            guard let self = self else { return }
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<Model_Dummy>?) in
                if let response = APIResponse {
                    self.showAlertWithCompletion(title: "Alert", message: response.message ?? "Logout Successfully", options: "Ok") { indx in
                        UserInfo.sharedInstance.clearMyUserDefaluts()
                        self.redirectToMain(isAnimated: true)
                    }
                }else{
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
}
