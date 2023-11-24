//
//  UpdatePhonenumberVC.swift
//  RidexCustomer
//

import UIKit

class UpdatePhonenumberVC: MTViewController {

    //TODO: - IBOutlet Declaration
    override func viewDidLoad() {
        super.viewDidLoad()

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    //MARK: - Tapped Action
    @IBAction func tappedOnSave(_ sender: Any) {
        
    }
    
    @IBAction func tappedOnBack(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
}
