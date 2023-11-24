//
//  AppDelegate.swift
//  TaxiDriver
//
//  Created by Dilip Manek on 26/07/18.
//  Copyright © 2018 Dilip Manek. All rights reserved.
//

import UIKit
import GoogleMaps
import GooglePlaces
import NVActivityIndicatorView
import SocketIO
import GoogleSignIn
import SVProgressHUD
import FirebaseCore
import FBSDKCoreKit

//MARK: - AppDelagate Object
var appDelegate = UIApplication.shared.delegate as! AppDelegate

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    var loadingIndicator:NVActivityIndicatorView!
    var isLoadingIndicatorOpen:Bool = false
    let signInConfig = GIDConfiguration(clientID: WebURL.googleClientID)
    var isDriverVerifed: Bool = false // true = user has verified Mobile number and added all required details with document
    var objDriverInfo: ModelDriverInfo? {
        get {
            return UserInfo.sharedInstance.getUserInfo()
        }
        set(newValue){
            if let userModel = newValue{
                UserInfo.sharedInstance.setUserInfo(objUserInfo: userModel)
            }
        }
    }
    
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
//        UITabBar.appearance().unselectedItemTintColor = UIColor(rgb: 0xFFC600)
        UITabBar.appearance().unselectedItemTintColor = hexStringToUIColor(hex: "#363F45")

        // Firebase configuration
        FirebaseApp.configure()
        
        // Google configuration
        GMSServices.provideAPIKey(WebURL.googleKey)
        GMSPlacesClient.provideAPIKey(WebURL.googleKey)
        
        // Facebook configuration
        ApplicationDelegate.shared.application( application, didFinishLaunchingWithOptions: launchOptions )
        
        if let bearerToken = UserInfo.sharedInstance.getMyUserDefaults(key: MyUserDefaults.bearerToken) as? String {
            WebURL.bearerToken = bearerToken
        }
      
        ApplicationDelegate.shared.application(application, didFinishLaunchingWithOptions: launchOptions)
        if UserInfo.sharedInstance.isUserLogin() && UserInfo.sharedInstance.getUserInfo() != nil {
            self.objDriverInfo = UserInfo.sharedInstance.getUserInfo()!
            print("Current UserId: \(self.objDriverInfo?.id)")
            self.redirectToNext()
        }else{
            self.redirectToMainScreen(isAnimated: false)
        }
        return true
    }
    
    func application(_ application: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        
        let facebookDidHandle: Bool = ApplicationDelegate.shared.application(application, open: url, sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String, annotation: options[UIApplication.OpenURLOptionsKey.annotation])
        var googleDidhandled: Bool = false
        googleDidhandled = GIDSignIn.sharedInstance.handle(url)
        if googleDidhandled || facebookDidHandle {
            return true
        }else{
            return false
        }
    }

    func hexStringToUIColor (hex:String) -> UIColor {
        var cString:String = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()

        if (cString.hasPrefix("#")) {
            cString.remove(at: cString.startIndex)
        }

        if ((cString.count) != 6) {
            return UIColor.gray
        }

        var rgbValue:UInt64 = 0
        Scanner(string: cString).scanHexInt64(&rgbValue)

        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        SocketIOManager.instance.closeConnection()

    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
        if isDriverVerifed{
            SocketIOManager.instance.establishConnection()
        }
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }
    
    //MARK: - Redirection Function
    func redirectToNext(){
        let phoneVerified = self.objDriverInfo?.phoneVerified ?? "0"
        let socialId = self.objDriverInfo?.socialId ?? ""
        
        if phoneVerified != "1" && socialId == ""{
            self.redirectToVerifyMobile(isAnimated: true)
            
        }else if self.objDriverInfo?.driverDetail?.vtId == nil{
            self.redirectToSelectVehicleType(isAnimated: true)
            
        } else if self.objDriverInfo?.vehicleDetail?.vtId == nil {
            self.redirectToAddVehicleDetails(isAnimated: true)

        } else if !isAllDocumentUploaded(){
            self.redirectToUploadDocuments(isAnimated: true)
            
        } else if phoneVerified == "1"{
            isDriverVerifed = true
            SocketIOManager.instance.establishConnection()
            self.redirectToMenu(isAnimated: true)
            
        } else {
            self.redirectToMainScreen(isAnimated: false)
        }
    }
    
    func isAllDocumentUploaded() -> Bool {
        if let userInfo = self.objDriverInfo?.driverDetail{
            if userInfo.dLicence == "" || userInfo.dLicence == nil || userInfo.vInsurance == "" ||  userInfo.vInsurance == nil || userInfo.vPermit == "" || userInfo.vPermit == nil || userInfo.vRegistration == "" || userInfo.vRegistration == nil{
                return false
            }else{
                return true
            }
        }
        return false
    }
    
    //TODO: - Redirect To Main screen
    func redirectToMainScreen(isAnimated: Bool){
        let storyBoard = UIStoryboard(name: "Main", bundle: nil)
        let vc = storyBoard.instantiateViewController(withIdentifier: "MainVC") as! MainVC
        self.window?.rootViewController = UINavigationController.init(rootViewController: vc)
        vc.navigationController?.setNavigationBarHidden(true, animated: isAnimated)
        self.window?.makeKeyAndVisible()
    }
    
    
    //TODO: - Redirect To Verify Mobile
    func redirectToVerifyMobile(isAnimated: Bool){
        let storyBoard = UIStoryboard(name: "Main", bundle: nil)
        let vc = storyBoard.instantiateViewController(withIdentifier: "VerifyMobileVC") as! VerifyMobileVC
        let login = storyBoard.instantiateViewController(withIdentifier: "MainVC") as! MainVC
        let navVC = UINavigationController(rootViewController: login)
        navVC.addChild(vc)
        self.window?.rootViewController = navVC
        vc.navigationController?.setNavigationBarHidden(true, animated: false)
        self.window?.makeKeyAndVisible()
    }
    
    //TODO: - Redirect To Add Vehicle Details
    func redirectToAddVehicleDetails(isAnimated: Bool){
        let storyBoard = UIStoryboard(name: "Main", bundle: nil)
        let vc = storyBoard.instantiateViewController(withIdentifier: "AddVehicleDetailsVC") as! AddVehicleDetailsVC
        let login = storyBoard.instantiateViewController(withIdentifier: "MainVC") as! MainVC
        let navVC = UINavigationController(rootViewController: login)
        navVC.addChild(vc)
        self.window?.rootViewController = navVC
        vc.navigationController?.setNavigationBarHidden(true, animated: false)
        self.window?.makeKeyAndVisible()
    }
    
    //TODO: - Redirect To Upload Documents
    func redirectToUploadDocuments(isAnimated: Bool){
        let storyBoard = UIStoryboard(name: "Main", bundle: nil)
        let vc = storyBoard.instantiateViewController(withIdentifier: "UploadDocumentsVC") as! UploadDocumentsVC
        let login = storyBoard.instantiateViewController(withIdentifier: "MainVC") as! MainVC
        let navVC = UINavigationController(rootViewController: login)
        navVC.addChild(vc)
        self.window?.rootViewController = navVC
        vc.navigationController?.setNavigationBarHidden(true, animated: false)
        self.window?.makeKeyAndVisible()
    }
    
    //TODO: - Redirect To Select Vehicle Type
    func redirectToSelectVehicleType(isAnimated: Bool){
        let storyBoard = UIStoryboard(name: "Main", bundle: nil)
        let vc = storyBoard.instantiateViewController(withIdentifier: "SelectVehicleTypeVC") as! SelectVehicleTypeVC
        let login = storyBoard.instantiateViewController(withIdentifier: "MainVC") as! MainVC
        let navVC = UINavigationController(rootViewController: login)
        navVC.addChild(vc)
        self.window?.rootViewController = navVC
        vc.navigationController?.setNavigationBarHidden(true, animated: false)
        self.window?.makeKeyAndVisible()
    }
    
    //TODO: - Redirect To Menu
    func redirectToMenu(isAnimated: Bool){
        let tabBarController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "MainTabbar")
        self.window?.rootViewController = UINavigationController.init(rootViewController: tabBarController)
        tabBarController.navigationController?.setNavigationBarHidden(true, animated: false)
        self.window?.makeKeyAndVisible()
    }
}

//MARK: - Show/Hide Loading Indicator
extension AppDelegate{
    func showLoadingIndicator(view : UIView) {
        if (isLoadingIndicatorOpen == false) {
            view.isUserInteractionEnabled = false
            DispatchQueue.global(qos: .background).async {
                DispatchQueue.main.async {
                    //Frame
                    let indicatorFrame = CGRect(x: (view.center.x - 40) , y: (view.center.y - 40), width: 80, height: 80)
                    //Color
                    let indicatorColor = UIColor(red: 52.0 / 255.0, green: 62.0 / 255.0, blue: 70.0 / 255.0, alpha: 1.0)
                    //Init
                    self.loadingIndicator = NVActivityIndicatorView(frame: indicatorFrame , type: NVActivityIndicatorView.DEFAULT_TYPE, color: indicatorColor, padding: 10)
                    //Add to superView
                    view.addSubview(self.loadingIndicator)
                    //Start Animating
                    self.loadingIndicator.startAnimating()
                    
                    self.isLoadingIndicatorOpen = true
                }
            }
        }
    }
    
    //MARK: - HideLoadingIndicator
    func hideLoadingIndicator() {
        
        if self.loadingIndicator != nil && isLoadingIndicatorOpen == true {
            DispatchQueue.main.async {
                self.loadingIndicator.superview?.isUserInteractionEnabled = true
                self.loadingIndicator.stopAnimating()
                self.loadingIndicator.removeFromSuperview()
                self.isLoadingIndicatorOpen = false
            }
            
        }
    }
    
    func showHud(){
        SVProgressHUD.setDefaultStyle(.custom)
        SVProgressHUD.setBackgroundColor(UIColor.clear)
        SVProgressHUD.setBackgroundLayerColor(UIColor.clear)
        SVProgressHUD.setForegroundColor(#colorLiteral(red: 0.9254902005, green: 0.2352941185, blue: 0.1019607857, alpha: 1))
        SVProgressHUD.setRingNoTextRadius(25.0)
        SVProgressHUD.show()
        
        if !UIApplication.shared.isIgnoringInteractionEvents {
            UIApplication.shared.beginIgnoringInteractionEvents()
        }
    }
    
    func showProgressHud(processDone : Float){
        SVProgressHUD.setDefaultStyle(.custom)
        SVProgressHUD.setBackgroundColor(UIColor.black.withAlphaComponent(0.35))
        SVProgressHUD.setBackgroundLayerColor(UIColor.clear)
        SVProgressHUD.setForegroundColor(#colorLiteral(red: 1, green: 1, blue: 1, alpha: 1))
        SVProgressHUD.setRingNoTextRadius(50.0)
        SVProgressHUD.showProgress(processDone, status: "Uploading...")
        
        if !UIApplication.shared.isIgnoringInteractionEvents {
            UIApplication.shared.beginIgnoringInteractionEvents()
        }
    }
    
    func hideHud(){
        DispatchQueue.main.async {
            SVProgressHUD.dismiss()
            UIApplication.shared.endIgnoringInteractionEvents()
        }
    }
}



extension Date {
    
    func dateString(_ format: String = "dd-MM-YYYY, hh:mm a") -> String {
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = format
        
        return dateFormatter.string(from: self)
    }
    
    func dateByAddingYears(_ dYears: Int) -> Date {
        
        var dateComponents = DateComponents()
        dateComponents.year = dYears
        
        return Calendar.current.date(byAdding: dateComponents, to: self)!
    }
    
    func minuteByAddingTimes(_ dMins: Int) -> Date {
        
        var dateComponents = DateComponents()
        dateComponents.minute = dMins
        
        return Calendar.current.date(byAdding: dateComponents, to: self)!
    }
    
    func getCurrentDateWithFormat() -> String {
        
        let dateFormatter = DateFormatter()
        
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        return dateFormatter.string(from: Date())
        
    }
    
}


extension UINavigationController {

    func backToViewController(viewController: Swift.AnyClass) {

            for element in viewControllers as Array {
                if element.isKind(of: viewController) {
                    self.popToViewController(element, animated: true)
                break
            }
        }
    }
}
