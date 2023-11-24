//
//  DocumentDetailsVC.swift
//  TaxiDriver
//
//  Created by Dilip Manek on 14/09/18.
//  Copyright © 2018 Dilip Manek. All rights reserved.
//

import UIKit
import SDWebImage

//MARK: - CellDocumentDetails Class
class CellDocumentDetails: UITableViewCell {
    
    //TODO: - IBOutlet Declaration
    @IBOutlet var widthImgDocConstraint: NSLayoutConstraint!
    @IBOutlet var imgDoc: UIImageView!
    @IBOutlet var lblTitle: UILabel!
}

//MARK: - ModelDocumentDetails Class
class ModelDocumentDetails{
    
    var strTitle: String = ""
    var strImage: String = ""
    
    init(strTitle: String,strImage: String) {
        self.strTitle = strTitle
        self.strImage = strImage
    }
}
//MARK: - DocumentDetailsVC
class DocumentDetailsVC: MTViewController {

    //TODO: - IBOutelet Declaration
    @IBOutlet var tblDocumentDetails: UITableView!
    
    //TODO: - Variable Declaration
    var arrModelDocumentDetails = [ModelDocumentDetails]()
    
    //TODO: - Override Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        //Initialization
        self.initialization()
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}
//MARK: - IBAction Methods
extension DocumentDetailsVC {
    
    //TODO: - Tapped On Back
    @IBAction func tappedOnBack(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
}

//MARK: - Other Methods
extension DocumentDetailsVC {
    
    //TODO: - Initialization Methods
    func initialization() {
      self.getVehicleDetailApiCall(isShowHud: true)
    }
}

//MARK: - UITableViewDataSource & UITableViewDelegate Methods
extension DocumentDetailsVC: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if arrModelDocumentDetails.count != 0 {
            return arrModelDocumentDetails.count
        } else {
            return 0
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "CellDocumentDetails") as! CellDocumentDetails
        cell.lblTitle.text = arrModelDocumentDetails[indexPath.row].strTitle
        if indexPath.row == 0{
            cell.widthImgDocConstraint.constant = 260
        } else {
             cell.widthImgDocConstraint.constant = 128
        }
        
        let url = WebURL.uploadURL + arrModelDocumentDetails[indexPath.row].strImage
        cell.imgDoc.imageFromURL(url, placeHolder: #imageLiteral(resourceName: "imgProfile"))
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //tableView.deselectRow(at: indexPath, animated: true)
        let url = WebURL.uploadURL + arrModelDocumentDetails[indexPath.row].strImage
        let storyboard = UIStoryboard(name: "Trip", bundle: nil)
        let documentDetails = storyboard.instantiateViewController(withIdentifier: "DocumentImageVC") as! DocumentImageVC
        documentDetails.strImgUrl = url
        self.navigationController?.pushViewController(documentDetails, animated: false)
        
    }
    
}

//MARK: - Api Call
extension DocumentDetailsVC {
    
    //TODO: - Get Vehicle Detail Api Call
    func getVehicleDetailApiCall(isShowHud: Bool = false){
        WebService.call.withPath(WebURL.getVehicleDetail, parameter: [:], methods: .get) { (responseDic) in
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<ModelDriverInfo>?) in
                if APIResponse?.status == 200 {
                    if let objUserInfo = APIResponse?.data {
                        appDelegate.objDriverInfo = objUserInfo
                        if let driverDetail = objUserInfo.driverDetail{
                            self.arrModelDocumentDetails.append(ModelDocumentDetails.init(strTitle: "Driving Licence", strImage: driverDetail.dLicence ?? ""))
//                            self.arrModelDocumentDetails.append(ModelDocumentDetails.init(strTitle: "Document", strImage: driverDetail.vDocument ?? ""))
                            self.arrModelDocumentDetails.append(ModelDocumentDetails.init(strTitle: "Vehicle Insurance", strImage: driverDetail.vInsurance ?? ""))
                            self.arrModelDocumentDetails.append(ModelDocumentDetails.init(strTitle: "Vehicle Permit", strImage: driverDetail.vPermit ?? ""))
                            self.arrModelDocumentDetails.append(ModelDocumentDetails.init(strTitle: "Vehicle Registration", strImage: driverDetail.vRegistration ?? ""))
                        }
                        self.tblDocumentDetails.reloadData()
                    }
                }else{
                    //Show Alert
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
            }
        }
    }
}



class DocumentImageVC: MTViewController {

    @IBOutlet weak var img_full: UIImageView!
    
    var strImgUrl = ""
    
    //TODO: - Override Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        //Initialization
        
        img_full.imageFromURL(strImgUrl, placeHolder: #imageLiteral(resourceName: "document_placehodler"))
    }
    
    @IBAction func tappedOnBack(_ sender: Any) {
        self.navigationController?.popViewController(animated: false)
    }
    
}
