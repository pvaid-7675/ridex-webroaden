//
//  BookingconfirmVC.swift
//  RidexCustomer
//
//

import UIKit

protocol BookingconfirmDelegate {
    func okSelected()
}

class BookingconfirmVC: UIViewController {

     //TODO: - IBOutlet Declaration
    @IBOutlet weak var lblEstimateTime: UILabel!
    var delegate: BookingconfirmDelegate?

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .clear
        self.view.isOpaque = false
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
}

//MARK: - Tapped Action
extension BookingconfirmVC {
    @IBAction func tappedOnDone(_ sender: Any) {
        self.delegate?.okSelected()
        self.dismiss(animated: true)
    }
}
