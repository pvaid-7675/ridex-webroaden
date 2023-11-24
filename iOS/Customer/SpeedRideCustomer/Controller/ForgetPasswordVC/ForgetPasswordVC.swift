//
//  ForgetPasswordVC.swift
//  RidexCustomer
//
//

import UIKit

//MARK: - ForgetPasswordVC
class ForgetPasswordVC: MTViewController {
    
    //TODO: - IBOutlet Declaration
    @IBOutlet var txtEmail: UITextField!
    
   
    //TODO: - Variable Declaration
    var strMessage: String = ""
    
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
extension ForgetPasswordVC {
    
    //TODO: - Tapped On Back
    @IBAction func tappedOnBack(_ sender: Any) {
        
        self.navigationController?.popViewController(animated: true)
    }
    
    //TODO: - Tapped On ResetPassword
    @IBAction func tappedOnResetPassword(_ sender: Any) {
        if isValidForm() == false {
            
            self.alertPopup(strTitle: "Alert", strBtn: "OK", msg: strMessage)
        } else {
            
            //Forgot Password Api Call
            self.forgotPasswordApiCall()
        }
    }
    
    @IBAction func tappedOnLogin(_ sender: Any) {
        self.navigationController?.popToRootViewController(animated: true)
    }
    
}

//MARK: - Other Methods
extension ForgetPasswordVC {
    
    //TODO: -  IsValidForm
    func isValidForm() -> Bool {
        
        if txtEmail.text?.removeWhiteSpace().count == 0 {
           strMessage = "Please enter email."
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

//MARK: - Redirect TO Next
extension ForgetPasswordVC {
    
    //TODO: - Redirect To Login
    func redirectToLogin(isAnimated: Bool){
        
        let login = storyboard?.instantiateViewController(withIdentifier: "LoginVC") as! LoginVC
        self.navigationController?.pushViewController(login, animated: isAnimated)
    }
}

//MARK: - Custom Toolbar Methods
extension ForgetPasswordVC {
    
    //TODO: - Toolbar methods
    func toolbarInit() -> UIToolbar {
        let toolBar = UIToolbar()
        toolBar.frame = CGRect(x: 0, y: 0, width: self.view.frame.size.width, height: 40)
        toolBar.barStyle = UIBarStyle.default
        toolBar.isTranslucent = false
        toolBar.barTintColor = UIColor.init(red: 52.0/255.0, green: 63.0/255.0, blue: 70.0/255.0, alpha: 1.0)
        toolBar.tintColor = UIColor.white
        let doneButton = UIBarButtonItem(title: "Done", style: .done, target: self, action: #selector(closeKeyBoard))
        let spaceButton = UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
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
extension ForgetPasswordVC {
    
    //TODO: - Initialization Methods
    func initialization(){
         txtEmail.inputAccessoryView = toolbarInit()
    }
}

//MARK: - API Calling
extension ForgetPasswordVC {
    
    //TODO: - Forgot Password Api Call
    func forgotPasswordApiCall(){
        var parameter = [String:Any]()
        parameter["email"] = txtEmail.text?.removeWhiteSpace()
        WebService.call.withPath(WebURL.forgotPassword, parameter: parameter) { [weak self] (responseDic) in
            guard let self = self else { return }
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<Model_Default>?) in
                if let response = APIResponse {
                    if let status = response.status, status == 200{
                        self.showAlertWithCompletion(title: "Alert", message: response.message ?? "", options: "Ok") { indx in
                            self.navigationController?.popToRootViewController(animated: true)
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
