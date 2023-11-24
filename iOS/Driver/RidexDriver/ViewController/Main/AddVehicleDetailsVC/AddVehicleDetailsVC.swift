//
//  AddVehicleDetailsVC.swift
//  TaxiDriver
//
//  Created by Dilip Manek on 26/07/18.
//  Copyright © 2018 Dilip Manek. All rights reserved.
//

import UIKit

//MARK: - AddVehicleDetailsVC
class AddVehicleDetailsVC: MTViewController {
    
    //TODO: - IBOutlet Declaration
    @IBOutlet var txtInteriorColor: UITextField!
    @IBOutlet var txtColor: UITextField!
    @IBOutlet var txtYear: UITextField!
    @IBOutlet var txtModel: UITextField!
    @IBOutlet var txtBrand: UITextField!
    @IBOutlet var txtVehicleNumber: UITextField!
    @IBOutlet weak var imgVehicle: UIImageView!
    
    //TODO: - Variable Declaration
    var arrColor = [String]()
    var arrInteriorColor = [String]()
    var arrYear = [String]()
    var strMessage: String = ""
    
    var pickerInteriorColor: UIPickerView = UIPickerView()
    var pickerColor: UIPickerView = UIPickerView()
    var pickerYear: UIPickerView = UIPickerView()
    
    var toolBar : CustomToolBar = CustomToolBar.init(frame: CGRect(x: 0, y: 0, width: ScreenSize.width, height: 40),isSegment: true)
    var isEditData: Bool = false
    var selectedVehicleImage: String = ""
    
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

//MARK: - Other Methods
extension AddVehicleDetailsVC {
    
    //TODO: - Initialization
    func initialization() {
        
        self.arrColor = ["Red","Black","Brown","Gray"]
        self.arrInteriorColor = ["Red","Black","Brown","Gray"]
        
        var dates = Date()
        dates = Calendar.current.date(byAdding: .month, value: 0, to: Date())!
        let formatter = DateFormatter()
        
        //Current Year
        formatter.dateFormat = "yyyy"
        let year = formatter.string(from: dates)
        
        let currentYear = Int(year)!
        //let endYear = (currentYear + 35)
        //self.arrYear = (currentYear...endYear).map { String($0)}
        self.arrYear = (1995...currentYear).map { String($0)}
        
        pickerYear.delegate = self
        pickerYear.dataSource = self
        pickerColor.delegate = self
        pickerColor.dataSource = self
        pickerInteriorColor.delegate = self
        pickerInteriorColor.dataSource = self
        
        txtYear.inputView = pickerYear
        txtColor.inputView = pickerColor
        txtInteriorColor.inputView = pickerInteriorColor

        let url = WebURL.vehicleImageURL + selectedVehicleImage
        self.imgVehicle.imageFromURL(url, placeHolder: #imageLiteral(resourceName: "imgProfile"))

        if isEditData == true {
            //Get Vehicle Detail Api Call // Not used as of now. no edit vehicle funcitonality in app
            //self.getVehicleDetailApiCall(isShowHud: true)
            self.getVehicleDetailApi()
        }
    }
    
    //TODO: - Is Valid Form
    func isValidForm() -> Bool {
        if txtBrand.text?.removeWhiteSpace().count == 0 {
            strMessage = "Please enter car brand."
            return false
        } else if txtModel.text?.removeWhiteSpace().count == 0{
            strMessage = "Please enter car model."
            return false
        } else if txtYear.text?.removeWhiteSpace().count == 0{
            strMessage = "Please enter year."
            return false
        } else if txtColor.text?.removeWhiteSpace().count == 0 {
            strMessage = "Please enter color."
            return false
        } else if txtInteriorColor.text?.removeWhiteSpace().count == 0{
            strMessage = "Please enter interior color."
            return false
        } else if txtVehicleNumber.text?.removeWhiteSpace().count == 0 {
            strMessage = "Please enter vehicle number."
            return false
        }
        return true
    }
    
}
//MARK: - IBAction Methods
extension AddVehicleDetailsVC {
    
    @IBAction func tappedOnContinue(_ sender: Any) {
        if isValidForm() == false {
            alertController(title: "Alert", msg: strMessage)
        } else {
            //Call Add Vehicle Detail Api Call
            self.addVehicleDetailApiCall(isShowHud: true)
        }
    }
        
    @IBAction func tappedOnBack(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
}

//MARK: - Redirect To Next
extension AddVehicleDetailsVC {
    
    //TODO: - Redirect To UploadDocuments
    func redirectToUploadDocuments(isAnimated: Bool){
        let uploadDocuments = storyboard?.instantiateViewController(withIdentifier: "UploadDocumentsVC") as! UploadDocumentsVC
        uploadDocuments.isUploadData = isEditData
        self.navigationController?.pushViewController(uploadDocuments, animated: isAnimated)
    }
}

//MARK: - UIPickerViewDataSource & UIPickerViewDelegate Method
extension AddVehicleDetailsVC: UIPickerViewDataSource, UIPickerViewDelegate {
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView == pickerYear{
            return arrYear.count
        }else if pickerView == pickerColor {
            return arrColor.count
        }else if pickerView == pickerInteriorColor {
            return arrInteriorColor.count
        }else {
            return 0
        }
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView == pickerYear{
            return arrYear[row]
        } else if pickerView == pickerColor {
            return arrColor[row]
        }else if pickerView == pickerInteriorColor {
            return arrInteriorColor[row]
        }else{
            return "0"
        }
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if pickerView == pickerYear {
            if arrYear.count != 0 {
                txtYear.text = arrYear[row]
            }
        }else if pickerView == pickerColor {
            if arrColor.count != 0{
                txtColor.text = arrColor[row]
            }
        }else if pickerView == pickerInteriorColor {
            if arrInteriorColor.count != 0{
                txtInteriorColor.text = arrInteriorColor[row]
            }
        }
    }
}

//MARK: - UITextField Delegate
extension AddVehicleDetailsVC : UITextFieldDelegate,CustomToolBarDelegate {
    
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
            else if let nextTextView = self.view.viewWithTag(selectedTextField.tag + 1) as? UITextView {
                nextTextView.becomeFirstResponder()
            }else {
                resignKeyboard()
            }
        }else{
            if let nextField = self.view.viewWithTag(selectedTextField.tag - 1) as? UITextField {
                nextField.becomeFirstResponder()
            }else {
                //Not found, so remove keyboard.
                resignKeyboard()
            }
        }
    }
    //Close keyboard
    func closeKeyBoard() {
        resignKeyboard()
    }
    //Resign keyboard
    func resignKeyboard(){
        self.view.endEditing(true)
    }
}

//MARK: - API Call
extension AddVehicleDetailsVC {
    
    //TODO: - AddVehicleDetailApiCall
    func addVehicleDetailApiCall(isShowHud: Bool = false) {
        
        if let userId = appDelegate.objDriverInfo?.id as? Int, let vehicleId = appDelegate.objDriverInfo?.driverDetail?.vtId as? Int{
            //parameter
            var parameter = [String:Any]()
            parameter["user_id"] = userId
            parameter["vt_id"] = vehicleId
            parameter["brand"] = txtBrand.text?.removeWhiteSpace()
            parameter["model"] = txtModel.text?.removeWhiteSpace()
            parameter["year"] = txtYear.text?.removeWhiteSpace()
            parameter["color"] = txtColor.text?.removeWhiteSpace()
            parameter["icolor"] = txtInteriorColor.text?.removeWhiteSpace()
            parameter["number"] = txtVehicleNumber.text?.removeWhiteSpace()
            
            WebService.call.withPath(WebURL.addVehicleDetail, parameter: parameter) { (responseDic) in
                WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<ModelDriverVehicle>?) in
                    if APIResponse?.status == 200, let driverVehicle = APIResponse?.data {
                        if var userInfo = UserInfo.sharedInstance.getUserInfo(){
                            userInfo.vehicleDetail = driverVehicle
                            appDelegate.objDriverInfo = userInfo
                        }

                        //Redirect To UploadDocuments
                        self.redirectToUploadDocuments(isAnimated: true)
                    }else{
                        //Show Alert
                        showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                    }
                }
            }
        }
    }
    
    //TODO: - Get Vehicle Detail Api Call // Not used as of now, no edit functionality avbl in app
//    func getVehicleDetailApiCall(isShowHud: Bool = false){
//
//        if CheckConnection.isConnectedToNetwork() {
//            if (isShowHud == true) {
//                appDelegate.showLoadingIndicator(view: self.view)
//            }
//            let id: String = UserInfo.sharedInstance.getMyUserDefaults(key: MyUserDefaults.userId) as! String
//            //parameter
//            var parameter = [String:Any]()
//            parameter["user_id"] = id
//
//            let url = WebURL.getVehicleDetail
//            WebService.call.withPath(url, parameter: [:],methods: .get) { (json, error) in
//                appDelegate.hideLoadingIndicator()
//                print(json)
//                let status = json["status"].intValue
//                let message = json["msg"].stringValue
//                let data = json["data"].dictionaryValue
//                let dvehicle = data["vehicle_detail"]?.dictionaryValue
//                if (status == 200) {
//                    //Set Data
//                    self.txtBrand.text = dvehicle!["brand"]!.stringValue
//                    self.txtModel.text = dvehicle!["model"]!.stringValue
//                    self.txtYear.text = dvehicle!["year"]!.stringValue
//                    self.txtColor.text = dvehicle!["color"]!.stringValue
//                    self.txtInteriorColor.text = dvehicle!["icolor"]!.stringValue
//                    self.txtVehicleNumber.text = dvehicle!["number"]!.stringValue
//                }else {
//                    self.alertController(title: "Alert", msg: message)
//                }
//            }
//        }else {
//            alertController(title: "Alert", msg: "No internet connection available.")
//        }
//    }
    
    func getVehicleDetailApi(){
        
        let id: String = UserInfo.sharedInstance.getMyUserDefaults(key: MyUserDefaults.userId) as! String
        //parameter
        var parameter = [String:Any]()
        parameter["user_id"] = id
        
        WebService.call.withPath(WebURL.getVehicleDetail,parameter: parameter, methods: .get) { (responseDic) in
            appDelegate.hideLoadingIndicator()
            
            print(responseDic)
            let data = responseDic["data"] as! [String:Any]
            let dvehicle = data["vehicle_detail"] as! [String:Any]
            
            self.txtBrand.text = dvehicle["brand"] as? String
            self.txtModel.text = dvehicle["model"] as? String
            self.txtYear.text = dvehicle["year"] as? String
            self.txtColor.text = dvehicle["color"] as? String
            self.txtInteriorColor.text = dvehicle["icolor"] as? String
            self.txtVehicleNumber.text = dvehicle["number"] as? String
            
        }
    }
    
}
