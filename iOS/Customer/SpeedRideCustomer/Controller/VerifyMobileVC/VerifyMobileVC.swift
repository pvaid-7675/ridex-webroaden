//
//  VerifyMobileVC.swift
//  RidexCustomer
//

import UIKit
import KWVerificationCodeView
//MARK: - VerifyMobileVC
class VerifyMobileVC: MTViewController {
    
    //TODO: - IBOutlet Declaration

    @IBOutlet weak var vcOtp: KWVerificationCodeView!
    
    //TODO: - Variable Declaration
    
    //TODO: - Override Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Call Initialization
        self.initialization()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        
    }
}

//MARK: - IBAction Methods
extension VerifyMobileVC {
    //TODO: - Tapped On Back
    @IBAction func tappedOnBack(_ sender: Any) {
        
        self.navigationController?.popViewController(animated: true)
    }
     //TODO: - Tapped On Submit
    @IBAction func tappedOnSubmit(_ sender: Any) {
   
        self.verifyOTPApiCall()
    }
    //TODO: - Tapped On ResendOTP
    @IBAction func tappedOnResendOTP(_ sender: Any) {
        
        self.resendOtpApiCall(isShowHud: true)
    }
    //TODO: - Tapped On Change Number
    @IBAction func tappedOnChangeNumber(_ sender: Any) {
        
        //Redirect To Update Phone Number
        self.redirectToUpdatePhoneNumber(isAnimated: true)
    }
}

//MARK: - Custom Toolbar Methods
extension VerifyMobileVC {
    
    //TODO: - Toolbar methods
    func toolbarInit() -> UIToolbar {
        let toolBar = UIToolbar()
        toolBar.frame = CGRect(x: 0, y: 0, width: self.view.frame.size.width, height: 40)
        toolBar.barStyle = UIBarStyle.default
        toolBar.isTranslucent = false
        toolBar.barTintColor = UIColor.init(red: 52.0/255.0, green: 63.0/255.0, blue: 70.0/255.0, alpha: 1.0)
        toolBar.tintColor = UIColor.white
        let doneButton = UIBarButtonItem(title: "Done", style: UIBarButtonItem.Style.done, target: self, action: #selector(closeKeyBoard))
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        toolBar.setItems([spaceButton, doneButton], animated: false)
        toolBar.isUserInteractionEnabled = true
        toolBar.sizeToFit()
        return toolBar;
    }
    
    //Close keyboard
    @objc func closeKeyBoard() {
        resignKeyboard()
    }
    
    //Resign keyboard
    func resignKeyboard() {
        self.view.endEditing(true)
    }
}

//MARK: - Other Methods
extension VerifyMobileVC {
    
    //TODO: - Initialization Methods
    func initialization(){
        vcOtp.keyboardType = UIKeyboardType.numberPad
    }
    
    //TODO: - Redirect To Update Phone Number
    func redirectToUpdatePhoneNumber(isAnimated: Bool){
        let updatePhoneNumber = storyboard?.instantiateViewController(withIdentifier: "UpdatePhonenumberVC") as! UpdatePhonenumberVC
        self.navigationController?.pushViewController(updatePhoneNumber, animated: isAnimated)
    }
}
//MARK: - Redirect To Next
extension VerifyMobileVC {
    
    //TODO: - Redirect To PaymentTypeVC
    func redirectToPaymentType(isAnimated: Bool){
       
        let AddPaymentDetail = storyboard?.instantiateViewController(withIdentifier: "PaymentTypeVC") as! PaymentTypeVC
        self.navigationController?.pushViewController(AddPaymentDetail, animated: true)
    }
    //TODO: - redirect To Pickup
    func redirectToPickup(isAnimated: Bool){
        
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        let vc:SWRevealViewController = StoryBoard.SideBar.instantiateViewController(withIdentifier: "SWRevealViewController") as! SWRevealViewController
        appDelegate.window?.rootViewController = vc
       
    }
}

//MARK: - API Calling
extension VerifyMobileVC {
    
    // Verify OTP Api Call
    func verifyOTPApiCall(){
        var parameter = [String:Any]()
        parameter["user_id"] = appDelegate.objUserInfo?.id ?? 0
       parameter["otp"] = vcOtp.getVerificationCode()
        
        WebService.call.withPath(WebURL.verifyOTP, parameter: parameter) { (responseDic) in
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<Model_UserInfo>?) in
                if APIResponse?.status == 200, let objUserInfo = APIResponse?.data  {
                        appDelegate.objUserInfo = objUserInfo
                    
                    if let strToken = APIResponse?.accessToken {
                        UserInfo.sharedInstance.setMyUserDefaults(value: strToken, key: MyUserDefaults.bearerToken)
                        WebURL.bearerToken = strToken
                    }

                    //Redirect To SelectVehicleType
                    self.redirectToPickup(isAnimated: true)
                }else{
                    //Show Alert
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
    
    // Resend Otp Api Call
    func resendOtpApiCall(isShowHud: Bool = false){
        resignKeyboard()
        var parameter = [String:Any]()
        parameter["user_id"] = appDelegate.objUserInfo?.id ?? 0
        
        WebService.call.withPath(WebURL.resendOtp, parameter: parameter) { (responseDic) in
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<Model_Default>?) in
                if APIResponse?.status == 200 {
                    showAlert(msg: APIResponse?.message ?? "OTP sent successfully")
                }else{
                    //Show Alert
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
}
