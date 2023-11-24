//
//  CityCabMainVC.swift
//  RidexCustomer
//
//

import UIKit
import GoogleMaps
import CoreLocation

class CityCabMainVC: UIViewController {
    
    @IBOutlet var menuMainView: UIView!
    @IBOutlet weak var conMenu: UIControl!
    @IBOutlet weak var mapView: GMSMapView!
    
    //TODO: - Varriable Declaration
    var isPickupLocationSelected = true
    var locationManager = CLLocationManager()
    var marker:GMSMarker!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Initialization
        self.initialization()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func tappedOnManu(_ sender: Any) {
        
    }
}
//MARK: - Other Methods
extension CityCabMainVC {
    
    //TODO: - Initialization
    func initialization() {
        
        //Left Side
        conMenu.addTarget(self.revealViewController(), action: #selector(SWRevealViewController.revealToggle(_:)), for: .touchUpInside)
        self.view.addGestureRecognizer(revealViewController().panGestureRecognizer())
        self .revealViewController().rearViewRevealWidth = UIScreen.main.bounds.width - 75
        
        
        //MAP Current location
        locationManager.delegate = self
        self.marker = GMSMarker()
        self.marker.title = "Current Location"
        locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
        locationManager.distanceFilter = 500
        locationManager.requestWhenInUseAuthorization()
        locationManager.requestAlwaysAuthorization()
        locationManager.startUpdatingLocation()
        
        // GOOGLE MAPS SDK: USER'S LOCATION
        mapView.isMyLocationEnabled = true
        mapView.settings.myLocationButton = true
    }
}

//MARK: - CLLocationManagerDelegate Methods
extension CityCabMainVC: CLLocationManagerDelegate {
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        if status == .authorizedWhenInUse {
            locationManager.startUpdatingLocation()
            mapView.isMyLocationEnabled = true
            mapView.settings.myLocationButton = true
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            mapView.camera = GMSCameraPosition(target: location.coordinate, zoom: 200, bearing: 0, viewingAngle: 0)
            
 //            marker.position = CLLocationCoordinate2DMake(location.coordinate.latitude, location.coordinate.longitude)//CLLocationCoordinate2DMake(location.coordinate.latitude, location.coordinate.longitude)
//            marker.map = self.mapView
            locationManager.stopUpdatingLocation()
        }
    }
}
