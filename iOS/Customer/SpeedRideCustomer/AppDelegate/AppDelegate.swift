//
//  AppDelegate.swift
//  RidexCustomer
//
//  Created by Devubha Manek on 26/07/18.
//  Copyright © 2018 Devubha Manek. All rights reserved.
//

import UIKit
import GoogleMaps
import GooglePlaces
import SocketIO
import GoogleSignIn
import NVActivityIndicatorView
import FirebaseCore
import SVProgressHUD
import StripePaymentsUI
import FBSDKCoreKit

//MARK: - AppDelagate Object
var appDelegate = UIApplication.shared.delegate as! AppDelegate

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    var loadingIndicator:NVActivityIndicatorView!
    var isLoadingIndicatorOpen:Bool = false
    let signInConfig = GIDConfiguration(clientID: WebURL.googleClientID)
    var objUserInfo : Model_UserInfo? {
        get {
            return UserInfo.sharedInstance.getUserInfo()
        }
        set(newValue){
            if let userModel = newValue{
                print("New data saved")
                UserInfo.sharedInstance.setUserInfo(objUserInfo: userModel)
            }
        }
    }
    var platformFees: Int = 0

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // Firebase configuration
        FirebaseApp.configure()
        
        // Google configuration
        GMSServices.provideAPIKey(WebURL.googleMapkey)
        GMSPlacesClient.provideAPIKey(WebURL.googleMapkey)
        
        // Facebook configuration
        ApplicationDelegate.shared.application( application, didFinishLaunchingWithOptions: launchOptions )
        
        // Stripe configuration
        //StripeAPI.defaultPublishableKey = "pk_test_51M4U1TBVTjOyMg4rh6rC9xawCbJT18jiyxOkMe9KM62QP9dFRCdCI8eUX3jnBak1KzCw74VdYXHdUJlyiQszIl7i001woDAFam"
        StripeAPI.defaultPublishableKey = "pk_live_51M4U1TBVTjOyMg4rKMyXSrnAW4rS2Rp8jSnybiSPQDNcBz6u2D9fBNrVVMUGkD6ehLSEiG4BXSExemckNdyYa5rC00XLgajbhI"
        
        
        // Push notification configuration
        /*UIApplication.shared.applicationIconBadgeNumber = 0
         UNUserNotificationCenter.current().delegate = self
         self.registerForPushNotifications()*/
        
        // Configure barear token if user is logged in
        if let bearerToken = UserInfo.sharedInstance.getMyUserDefaults(key: MyUserDefaults.bearerToken) as? String {
            WebURL.bearerToken = bearerToken
        }
        
        // Set Initial screen
        setInitialScreen()
        return true
    }
    
    func application(_ application: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        let facebookDidHandle: Bool = ApplicationDelegate.shared.application(application, open: url, sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String, annotation: options[UIApplication.OpenURLOptionsKey.annotation])
        let googleDidHandle: Bool = GIDSignIn.sharedInstance.handle(url)
        return facebookDidHandle || googleDidHandle
    }
    
    
    func applicationWillResignActive(_ application: UIApplication) {
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        //SocketHelper.shared.disconnectSocket()
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        SocketIOManager.shared.establishConnection()

        /*SocketHelper.shared.connectSocket { (isConnected) in
            if isConnected{
                print("Socket connected")
            }else{
                print("Socket dis-connected")
            }
        }*/
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }
}

extension AppDelegate{
    func setInitialScreen(){
        if UserInfo.sharedInstance.isUserLogin() && UserInfo.sharedInstance.getUserInfo() != nil {
            self.objUserInfo = UserInfo.sharedInstance.getUserInfo()!
            if self.objUserInfo?.phoneVerified != "1" && self.objUserInfo?.socialId == ""{
                self.gotoVerifyScreen()
            }else if self.objUserInfo?.socialId != ""{
                self.gotoMainScreen()
            }else{
                self.gotoHomeScreen()
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    self.getFees { _ in }
                }
            }
        }else{
            self.gotoMainScreen()
        }
    }
    
    func gotoVerifyScreen(){
        let storyBoard = UIStoryboard(name: "Main", bundle: nil)
        let vc = storyBoard.instantiateViewController(withIdentifier: "VerifyMobileVC") as! VerifyMobileVC
        self.window?.rootViewController = UINavigationController.init(rootViewController: vc)
        vc.navigationController?.setNavigationBarHidden(true, animated: false)
        self.window?.makeKeyAndVisible()
    }
    
    func gotoHomeScreen(){
        let storyBoard = UIStoryboard(name: "SideBar", bundle: nil)
        let vc = storyBoard.instantiateViewController(withIdentifier: "SWRevealViewController") as! SWRevealViewController
        self.window?.rootViewController = UINavigationController.init(rootViewController: vc)
        vc.navigationController?.setNavigationBarHidden(true, animated: false)
        self.window?.makeKeyAndVisible()
    }
    
    func gotoMainScreen(){
        let storyBoard = UIStoryboard(name: "Main", bundle: nil)
        let vc = storyBoard.instantiateViewController(withIdentifier: "HomeVC") as! HomeVC
        self.window?.rootViewController = UINavigationController.init(rootViewController: vc)
        vc.navigationController?.setNavigationBarHidden(true, animated: false)
        self.window?.makeKeyAndVisible()
    }
}

//MARK: - Show/Hide Loading Indicator
extension AppDelegate{
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
        SVProgressHUD.setForegroundColor(#colorLiteral(red: 0.9254902005, green: 0.2352941185, blue: 0.1019607857, alpha: 1))
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
}

extension AppDelegate{
    func getFees(completion: @escaping (Bool) -> Void) {
        WebService.call.withPath(WebURL.getFees, methods: .get) { (responseDic) in
            print("json: \(responseDic)")
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<getFees>?) in
                if APIResponse?.status == 200, let fees = APIResponse?.data?.fees {
                    print("Fees: \(fees)")
                    self.platformFees = fees
                    UserInfo.sharedInstance.setMyUserDefaults(value: fees, key: MyUserDefaults.feePercentage)
                    completion(true)
                }else{
                    //Show Alert
                    completion(false)
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
}

/*extension AppDelegate: UNUserNotificationCenterDelegate,MessagingDelegate{
    //didRegisterForRemoteNotificationsWithDeviceToken
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        var token = ""
        for i in 0..<deviceToken.count {
            token = token + String(format: "%02.2hhx", arguments: [deviceToken[i]])
        }
        //setMyUserDefaults(value: token, key: MyUserDefaults.DeviceToken)
        print("APNS Token: ", token)
        Messaging.messaging().apnsToken = deviceToken
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let token = fcmToken {
            UserInfo.sharedInstance.setMyUserDefaults(value: token, key: UserDefaultsKeys.DeviceToken)
            print("FCM Token:\(token)")
        }
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
         print("Notification Register error: ", error.localizedDescription)

    }

    func registerForPushNotifications() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) {
            ( granted, error) in
            if granted && error == nil{
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                    UNUserNotificationCenter.current().delegate = self
                }
            }else{
                DispatchQueue.main.async {
                    appDelegateShared.window?.makeKeyAndVisible()
                    appDelegateShared.window?.rootViewController?.showAlertWithCompletion(title: nil, message:"Allow notification access in your device settings." , options: "not allow","settings" , completion: { (indexAlert) in
                        if indexAlert == 1{
                            guard let settingsUrl = URL(string: UIApplicationOpenSettingsURLString) else {
                                return
                            }
                            if UIApplication.shared.canOpenURL(settingsUrl) {
                                UIApplication.shared.open(settingsUrl, options:[:], completionHandler: nil)
                            }
                        }
                    })
                }
            }
        }
        Messaging.messaging().delegate = self
    }
}*/
//
