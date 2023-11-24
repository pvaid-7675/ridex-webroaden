//
//  ChangePasswordVC.swift
//  MtTaxiApp
//
//  Created by keyur on 06/09/18.
//  Copyright © 2018 Devubha Manek. All rights reserved.
//

import UIKit
//MARK: - ChangePasswordVC
class ChangePasswordVC: MTViewController {

    //TODO: - IBOutlet Declaration
    @IBOutlet weak var txtOldPassword: UITextField!
    @IBOutlet weak var txtNewPassword: UITextField!
    @IBOutlet weak var txtReEnterNewPassword: UITextField!
    @IBOutlet var imgNewCurrentPass: UIImageView!
    @IBOutlet var imgNewPass: UIImageView!
    @IBOutlet var imgCurrentPass: UIImageView!
    
    //TODO: - Variable Declaration
    var strMessage: String = ""
    var toolBar : CustomToolBar = CustomToolBar.init(frame: CGRect(x: 0, y: 0, width: ScreenSize.width, height: 40),isSegment: true)
    var isNewcurrentPass: Bool = true
    var isNewPass: Bool = true
    var isCurrentPass:  Bool = true
    //TODO: - Override Methods
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}
//MARK: - IBAction Methods
extension ChangePasswordVC {
    
    //TODO: tapped On Save Password
    @IBAction func tappedOnSavePassword(_ sender: Any) {
        
        if isValidForm() == false {
            self.alertPopup(strTitle: "Alert", strBtn: "OK", msg: strMessage)
        } else {
            //Change Password Api Call
            self.changePasswordApiCall(isShowHud: true)
        }
    }
    
    //TODO: - tapped On back
    @IBAction func tappedOnBack(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    
    //TODO: - Tapped On New Current Pass
    @IBAction func tappedOnNewCurrentPass(_ sender: Any) {
        if(isNewcurrentPass == true) {
            txtReEnterNewPassword.isSecureTextEntry = false
            isNewcurrentPass = false
            self.imgNewCurrentPass.image = UIImage(named: "imgEyeVisible")
        } else {
            txtReEnterNewPassword.isSecureTextEntry = true
            isNewcurrentPass = true
            self.imgNewCurrentPass.image = UIImage(named: "imgEyeHide")
        }
    }
    
    //TODO: - Tapped On New Pass
    @IBAction func tappedOnNewPass(_ sender: Any) {
        if(isNewPass == true) {
            txtNewPassword.isSecureTextEntry = false
            isNewPass = false
            self.imgNewPass.image = UIImage(named: "imgEyeVisible")
        } else {
            txtNewPassword.isSecureTextEntry = true
            isNewPass = true
            self.imgNewPass.image = UIImage(named: "imgEyeHide")
        }
    }
           
    //TODO: - Tapped On Current Pass
    @IBAction func tappedOnCurrentPass(_ sender: Any) {
        if(isCurrentPass == true) {
            txtOldPassword.isSecureTextEntry = false
            isCurrentPass = false
            self.imgCurrentPass.image = UIImage(named: "imgEyeVisible")
        } else {
            txtOldPassword.isSecureTextEntry = true
            isCurrentPass = true
            self.imgCurrentPass.image = UIImage(named: "imgEyeHide")
        }
    }
}

//MARK: - Other Methods
extension ChangePasswordVC{
    
    //TODO: - IsValidForm
    func isValidForm() -> Bool {
        if txtOldPassword.text?.removeWhiteSpace().count == 0 {
            strMessage = "Please enter password."
            return false
        } else if txtNewPassword.text?.removeWhiteSpace().count == 0 {
            strMessage = "Please enter new password."
            return false
        } else if txtNewPassword.text?.isValidPassword() == false {
            strMessage = "Please enter valid new password."
            return false
        }else if txtReEnterNewPassword.text?.removeWhiteSpace().count == 0 {
            strMessage = "Please enter confirm password."
            return false
        } else if txtReEnterNewPassword.text?.isValidPassword() == false {
            strMessage = "Please enter valid confirm Password."
            return false
        } else if txtReEnterNewPassword.text! != txtNewPassword.text! {
            strMessage = "New password and confirm password are not match."
            return false
        }
        return true
    }
    
    //TODO: - AlertPopup
    func alertPopup(strTitle: String, strBtn : String, msg : String){
        let alert = UIAlertController.init(title: strTitle, message:msg, preferredStyle: .alert)
        alert.addAction(UIAlertAction.init(title: strBtn, style: .default, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
}
//MARK: - UITextField Delegate
extension ChangePasswordVC : UITextFieldDelegate,CustomToolBarDelegate {
    
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        UIView.animate(withDuration: 0.2) {
            self.view.layoutIfNeeded()
        }
        textField.inputAccessoryView = toolbarInit(textField: textField)
        return true
    }
    
    func textFieldDidBeginEditing(_ textField: UITextField){
        
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        UIView.animate(withDuration: 0.2) {
            self.view.layoutIfNeeded()
        }
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder();
        return true;
    }
    
    //MARK: - textField character changed
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        return true
    }
    
    //MARK: - Keyboard
    func toolbarInit(textField: UITextField) -> UIToolbar {
        toolBar.delegate1 = self
        toolBar.txtField = textField
        return toolBar;
    }
    
    //MARK: - Custom ToolBar Delegates
    func getSegmentIndex(segmentIndex: Int,selectedTextField: UITextField) {
        
        if segmentIndex == 1 {
            if let nextField = self.view.viewWithTag(selectedTextField.tag + 1) as? UITextField {
                nextField.becomeFirstResponder()
            }
            else if let nextTextView = self.view.viewWithTag(selectedTextField.tag + 1) as? UITextView
            {
                nextTextView.becomeFirstResponder()
            }
            else {
                resignKeyboard()
            }
        }else{
            if let nextField = self.view.viewWithTag(selectedTextField.tag - 1) as? UITextField {
                nextField.becomeFirstResponder()
                
            }else {
                // Not found, so remove keyboard.
                resignKeyboard()
            }
        }
    }
    //Close keyboard
    func closeKeyBoard() {
        resignKeyboard()
    }
    //Resign keyboard
    func resignKeyboard() {
        self.view.endEditing(true)
    }
}
//MARK: - API Call
extension ChangePasswordVC{
    
    //TODO: -  Ride Book Api Call
    func changePasswordApiCall(isShowHud: Bool = false){
        var parameter = [String:Any]()
        parameter["current_password"] = self.txtOldPassword.text?.removeWhiteSpace()
        parameter["new_password"] = self.txtNewPassword.text?.removeWhiteSpace()
        WebService.call.withPath(WebURL.resetPassword, parameter: parameter) { (responseDic) in
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<Model_Dummy>?) in
                if let response = APIResponse {
                    if let status = response.status, status == 200{
                        if let strToken = response.accessToken {
                            UserInfo.sharedInstance.setMyUserDefaults(value: strToken, key: MyUserDefaults.bearerToken)
                            WebURL.bearerToken = strToken
                        }
                        self.showAlertWithCompletion(title: "Alert", message: response.message ?? "", options: "Ok") { indx in
                            self.navigationController?.popViewController(animated: true)
                        }
                    }else{
                        showAlert(msg: response.message ?? "Something went wrong. Please try again later!")
                    }
                }else{
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
}

