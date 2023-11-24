//
//  DropOffTableVC.swift
//  TaxiMan
//
//

import UIKit
import GooglePlaces
import GoogleMaps

//MARK: - Custome Delegate
protocol DropOffTableVCDelegate {
    func passDropData(data: String, placeId: String)
}

//MARK: - DropOffTable View Controller
class DropOffTableVC: MTViewController{
    
    //TODO: - IBOutlet Declaration
    @IBOutlet var tblDropOffLocation: UITableView!
    @IBOutlet var txtDropOffLocation: UITextField!
    @IBOutlet var lblPickUpLocation: UILabel!
    @IBOutlet var lblRequest: UILabel!
    @IBOutlet var cntManuBtn: UIControl!
    
    
    //TODO: - Varriable Declaration
    var arrLocation = [ModelLocationList]()
    var placeIdOfDropOffLocation = String()
    var obj_ModelGooglePlace = [Model_Location]()
  //  var strPickUpLocation = String()
    var strDropOffLocation = String()
    var coordinateOfPickUpLocation = CLLocationCoordinate2D()
    var coordinateOfDropOffLocation :CLLocationCoordinate2D? = CLLocationCoordinate2D()
    var placeIdOfPickUpLocation = String()
     var delegate: DropOffTableVCDelegate?
    
    //TODO: - Override Methods
    override func viewDidLoad() {
        
        super.viewDidLoad()
        
        self.initialization()
    }
}
//MARK: - Extra Method
extension DropOffTableVC{
    
    //initialization
    func initialization(){
        
        self.navigationController?.isNavigationBarHidden = true
       // lblPickUpLocation.text = strPickUpLocation
        
        if strDropOffLocation != "Dropoff Address" {
            txtDropOffLocation.text = strDropOffLocation
        }
    }
}
//MARK: - Textfield Delegate
extension DropOffTableVC:UITextFieldDelegate{
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
}
//MARK: - TableViewDelegate,TableViewDataSource
extension DropOffTableVC:UITableViewDelegate,UITableViewDataSource{
    
    //numberOfSections
    func numberOfSections(in tableView: UITableView) -> Int {
        
            return 1
    }
    //numberOfRowsInSection
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return obj_ModelGooglePlace.count
    }
    
    //cellForRowAtindexPath
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath)
        cell.textLabel?.text = obj_ModelGooglePlace[indexPath.row].longAddress ?? obj_ModelGooglePlace[indexPath.row].shortAddress ?? ""
        return cell
    }
    //didSelectRowAtindexPath
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        strDropOffLocation = obj_ModelGooglePlace[indexPath.row].longAddress ?? obj_ModelGooglePlace[indexPath.row].shortAddress ?? ""
        txtDropOffLocation.text = strDropOffLocation
        placeIdOfDropOffLocation = obj_ModelGooglePlace[indexPath.row].placeID ?? ""
        
        if (coordinateOfDropOffLocation != nil || placeIdOfDropOffLocation != "") && txtDropOffLocation.text != ""
        {

            if txtDropOffLocation.text != nil {
                self.delegate?.passDropData(data: txtDropOffLocation.text!, placeId: placeIdOfDropOffLocation)
                navigationController?.popViewController(animated: true)
                
            }

        }else {
        
            let alertView = UIAlertController(title: "SELECT PLACE", message: "Select place from table", preferredStyle: .alert)
            let action = UIAlertAction(title: "OK", style: .default){
                UIAlertAction in
            }
            alertView.addAction(action)
            self.present(alertView, animated: true, completion: nil)
        }
    }
}
//MARK: - Get GooglePlace Api Calling
extension DropOffTableVC{

    //dropOffLocationChange
    @IBAction func dropOffLocationChange(_ sender: Any) {
        
        getPlaces(searchString: txtDropOffLocation.text!)
//        obj_ModelGooglePlace?.predictions?.removeAll()
        if txtDropOffLocation.text == "" {
            self.tblDropOffLocation.reloadData()
        }
    }
//MARK:- GetPlaces
    
    func getPlaces(searchString: String) {
        
        var parameter = [String:Any]()
        parameter["input"] = searchString
        parameter["key"] = WebURL.googleMapkey
        parameter["components"] = "country:in"
        
        WebService.call.withPath(WebURL.googlePlaceApi,parameter: parameter, methods:.get) { (responseDic) in
                if let dicPredictions = responseDic["predictions"] as? [[String:Any]] {
                    self.obj_ModelGooglePlace.removeAll()
                    for dicPlace in dicPredictions {
                        if let placeID = dicPlace["place_id"] as? String,let place = dicPlace["description"] as? String{
                            let arrPlace = place.split(separator: ",")
                            self.obj_ModelGooglePlace.append(Model_Location(placeID: placeID, shortAddress: String(arrPlace[0]), longAddress: place))
                        }
                    }
                    self.tblDropOffLocation.delegate = self
                    self.tblDropOffLocation.dataSource = self
                    self.tblDropOffLocation.reloadData()
                }
        }
//        let url = WebURL.googlePlaceApi
//        WebService.call.withPath(url, parameter: parameter, methods: .get) { (json, error) in
//            print(json)
//            self.obj_ModelGooglePlace = ModelGooglePlace.init(fromJson: json)
//            self.tblDropOffLocation.reloadData()
//        }
    }
 
}
//MARK: - IBAction Event
extension DropOffTableVC{
    
    //tappedOnExit
    @IBAction func tappedOnExit(_ sender: UIControl) {
        
        self.navigationController?.popViewController(animated: true)
    }
    //tapToTxtDropOffLocation
    @IBAction func tapToTxtDropOffLocation(_ sender: Any) {
        
        coordinateOfDropOffLocation = nil
        txtDropOffLocation.text = ""
      //  lblRequest.backgroundColor = UIColor.white
        txtDropOffLocation.becomeFirstResponder()
    }
    //tapToGoBack
    @IBAction func tapToGoBack(_ sender: Any) {
        
        self.navigationController?.popViewController(animated: true)
    }
}
