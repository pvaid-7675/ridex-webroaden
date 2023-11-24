//
//  EditProfileVC.swift
//  RidexCustomer
//

import UIKit

class EditProfileVC: MTViewController {

    // IBOutlet Declaration
    @IBOutlet weak var txtCountryCode: UITextField!
    @IBOutlet var txtMobile: UITextField!
    @IBOutlet var txtEmail: UITextField!
    @IBOutlet var txtLastName: UITextField!
    @IBOutlet var txtFirstName: UITextField!
    @IBOutlet var imgProfile: UIImageView!
    @IBOutlet weak var vcEdit: UIView!
   
    var arrCountryCode = [CountryCodeData]()
    
    // Variable Declaration
    var strMessage: String = ""
    var obj_ModelUserInfo = ModelUserInfo.init()
    var isMenuOpen:Bool = false
    var gesture : UITapGestureRecognizer!
    
    let imagePickerController = UIImagePickerController()
    var isImageSelected: Bool = false
    
    var pickerCountry: UIPickerView = UIPickerView()
    
    //PickerView Variable
    var toolBar : CustomToolBar = CustomToolBar.init(frame: CGRect(x: 0, y: 0, width: ScreenSize.width, height: 40),isSegment: true)
    
    //MARK: - View LifeCycle
    override func viewDidLoad() {
        super.viewDidLoad()
        self.initialization()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // initialization
    func initialization(){
        
        txtCountryCode.isUserInteractionEnabled = true
        txtMobile.isUserInteractionEnabled = true
        txtEmail.isUserInteractionEnabled = false

        txtCountryCode.text = "+91"
        txtCountryCode.delegate = self
        
        pickerCountry.delegate = self
        pickerCountry.dataSource = self
        txtCountryCode.inputView = pickerCountry
        
        
        
        setUserData()
        
        getCountryCodeAPI()
        
        //Set ImagePickerController Delegate
        imagePickerController.delegate = self
        
        //Call Get Profile Api Call
        self.getProfileApiCall(isShowHud: true)
        
        if (gesture != nil) {
            self.vcEdit.removeGestureRecognizer(gesture)
        }
    }
    
    func setUserData(){
        if let user = appDelegate.objUserInfo{
            let name = user.name?.components(separatedBy: " ")
            self.txtFirstName.text = name?.first
            self.txtLastName.text = name?.last
            self.txtEmail.isUserInteractionEnabled = false
            self.txtEmail.text = user.email
            self.txtMobile.text = user.mobile
            self.txtCountryCode.text = user.countryPhoneCode
            
            if let image = user.image{
                let url = WebURL.uploadURL + image
                imgProfile.imageFromURL(url, placeHolder: #imageLiteral(resourceName: "ic_driver_profile"))
            } else {
                self.imgProfile.image = #imageLiteral(resourceName: "ic_driver_profile")
            }
        }
    }
}

//MARK: - Extra Method
extension EditProfileVC{
    
    //tappedOnGesture
    @objc func tappedOnGesture(_ sender:UITapGestureRecognizer){
        if isMenuOpen == true {
            self.revealViewController().revealToggle(vcEdit)
            isMenuOpen = false
            
            if (gesture != nil) {
                self.vcEdit.removeGestureRecognizer(gesture)
            }
        }
    }
}

//MARK: - Other Methods
extension EditProfileVC {
    
    func isValidForm() -> Bool {
        if imgProfile.image == #imageLiteral(resourceName: "ic_driver_profile") {
            strMessage = "Please upload profile image."
            return false
        } else if txtFirstName.text?.removeWhiteSpace().count == 0{
            strMessage = "Please enter first name."
            return false
        }else if txtFirstName.text?.isValidName(testStr: txtFirstName.text!) == false{
            strMessage = "Please enter valid first name."
            return false
        }else if txtLastName.text?.removeWhiteSpace().count == 0{
            strMessage = "Please enter last name."
            return false
        } else if txtLastName.text?.isValidName(testStr: txtLastName.text!) == false{
            strMessage = "Please enter valid last name."
            return false
        } else if txtEmail.text?.removeWhiteSpace().count == 0 {
            strMessage = "Please enter email."
            return false
        } else if txtEmail.text?.isValidEmail() == false{
            strMessage = "Please enter valid email."
            return false
        } else if txtMobile.text?.removeWhiteSpace().count == 0 {
            strMessage = "Please enter mobile."
            return false
        } else if txtMobile.text?.isValidPhoneNumber() == false {
            strMessage = "Please enter valid mobile."
            return false
        }
        return true
    }
    // AlertPopup
    func alertPopup(strTitle: String, strBtn : String, msg : String){
        
        let alert = UIAlertController.init(title: strTitle, message:msg, preferredStyle: .alert)
        alert.addAction(UIAlertAction.init(title: strBtn, style: .default, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
    // Get country codes API
    func getCountryCodeAPI(){
        WebService.call.withPath(WebURL.getCountryCode, parameter: [:],methods: .get) { (responseDic) in
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<[CountryCodeData]>?) in
                if APIResponse?.status == 200 {
                    if let objResponse = APIResponse?.data {
                        print(objResponse)
                        self.arrCountryCode = (APIResponse?.data)!
                        
                        if self.arrCountryCode.count > 0 {
                            let indexOfDefaultCountry = self.arrCountryCode.firstIndex(where: {$0.phone == "+91"})
                            self.pickerCountry.selectRow(indexOfDefaultCountry!, inComponent: 0, animated: true)
                        }
                        
                    }
                }else{
                    //Show Alert
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
}

//MARK: - Tapped Acton

extension EditProfileVC {
    
    @IBAction func tappedOnBack(_ sender: Any) {
       /*  self.view.endEditing(true)
        if isMenuOpen == true {
            isMenuOpen = false
            if (gesture != nil) {
                self.vcEdit.removeGestureRecognizer(gesture)
            }
        } else if isMenuOpen == false {
            isMenuOpen = true
            gesture = UITapGestureRecognizer(target: self, action:  #selector(self.tappedOnGesture(_:)))
            self.vcEdit.addGestureRecognizer(gesture)
        }
        self.revealViewController().revealToggle(sender)*/
        
        self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func tappedOnSave(_ sender: Any) {
        self.view.endEditing(true)
        
        if isValidForm() == false {
            self.alertPopup(strTitle: "Alert", strBtn: "OK", msg: strMessage)
        } else {
            self.updateprofileApiCall()
        }
    }
    
    //MARK: - Tapped On Upload ImgProfile
    @IBAction func tappedOnUploadImgProfile(_ sender: Any) {
        
        let actionSheet = UIAlertController(title: "Photos Source", message: "Choose a Source", preferredStyle: UIAlertController.Style.actionSheet)
        actionSheet.popoverPresentationController?.sourceView = sender as! UIControl
        actionSheet.popoverPresentationController?.sourceRect =  (sender as! UIControl).bounds
        actionSheet.addAction(UIAlertAction(title: "Camera", style: .default, handler: { (action:UIAlertAction) in
            if UIImagePickerController.isSourceTypeAvailable(.camera){
                AttachmentHandler.shared.showAttachmentActionSheet(vc: self)
                AttachmentHandler.shared.imagePickedBlock = { (image) in
                    self.imgProfile.image = image
                    self.isImageSelected = true
                }
            }
            else {
                showToast(text: "Camera Not avilable")
            }
            
        }))
        
        actionSheet.addAction(UIAlertAction(title: "Photo Library", style: .default, handler: { (action:UIAlertAction) in
            self.imagePickerController.sourceType = .photoLibrary
            self.present(self.imagePickerController, animated: true, completion: nil)
        }))
        
        actionSheet.addAction(UIAlertAction(title: "Cancel", style: .default, handler: nil))
        
        if let popoverController = actionSheet.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        self.present(actionSheet, animated: true, completion: nil)
    }
    
//    @IBAction func tappedOnUploadImgProfile(_ sender: UIButton) {
//        
//    }
    
}
//MARK: - UIImagePickerControllerDelegate and UINavigationControllerDelegate Methods
extension EditProfileVC:UIImagePickerControllerDelegate,UINavigationControllerDelegate{
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        let image = info[.originalImage] as! UIImage
        imgProfile.image = image
        self.isImageSelected = true
        picker.dismiss(animated: true, completion: nil)
    }

    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
}

//MARK: - API calling
extension EditProfileVC {
    
    //  update profile Api Call
    func updateprofileApiCall(){
        
        var parameter = [String:Any]()
        parameter["first_name"] = txtFirstName.text?.removeWhiteSpace()
        parameter["last_name"] = txtLastName.text?.removeWhiteSpace()
        parameter["mobile"] = txtMobile.text?.removeWhiteSpace()
        parameter["country_phone_code"] = self.txtCountryCode.text
        var arrImage = [ModelAPIImg]()
        if self.isImageSelected {
            arrImage = [ModelAPIImg.init(image: imgProfile.image!, key: "image")]
        }
        
        WebService.call.withPath(WebURL.updateprofile, parameter: parameter, images: arrImage) { (responseDic) in
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<Model_UserInfo>?) in
                if let status = APIResponse?.status, status == 200, let message = APIResponse?.message, let user = APIResponse?.data{
                    appDelegate.objUserInfo = user
                    //showAlert(msg: message)
                    self.showAlertWithCompletion(title: "", message: message, options: "OK", completion: { kOk in
                        if kOk == 0{
                            self.navigationController?.popViewController(animated: true)
                        }
                    })
                }else{
                    //Show Alert
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
    
    // Get Profile Api Call
    func getProfileApiCall(isShowHud: Bool = false){
        WebService.call.withPath(WebURL.getProfile, methods: .get) { (responseDic) in
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<Model_UserInfo>?) in
                if let status = APIResponse?.status, status == 200, let user = APIResponse?.data{
                    appDelegate.objUserInfo = user
                }else{
                    //Show Alert
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
}

//MARK: - UITextField Delegate
extension EditProfileVC : UITextFieldDelegate,CustomToolBarDelegate {
    
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        if textField == txtCountryCode || textField == txtMobile{
            UIView.animate(withDuration: 0.2) {
                self.view.layoutIfNeeded()
            }
            textField.inputAccessoryView = toolbarInit(textField: textField)
            
        }
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
    
    // MARK: - Keyboard
    func toolbarInit(textField: UITextField) -> UIToolbar
    {
        toolBar.delegate1 = self
        toolBar.txtField = textField
        return toolBar;
    }
    
    // MARK: - Custom ToolBar Delegates
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
    func resignKeyboard()
    {
        self.view.endEditing(true)
    }
}

//MARK: - UIPickerViewDataSource & UIPickerViewDelegate Method
extension EditProfileVC: UIPickerViewDataSource, UIPickerViewDelegate {
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return arrCountryCode.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return arrCountryCode[row].name! + "   " + arrCountryCode[row].phone!
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if arrCountryCode.count != 0 {
            txtCountryCode.text = arrCountryCode[row].phone
        }
        
    }
}
