//
//  ChatCell.swift
//  RidexCustomer
//
//

import UIKit

class ChatCell: UITableViewCell {

    //sender text
    @IBOutlet weak var lbl_sender_msg: UILabel!
    @IBOutlet weak var lbl_sender_name: UILabel!
    @IBOutlet weak var lbl_sender_time: UILabel!
    //sender Image
    @IBOutlet weak var img_sender: UIImageView!
    @IBOutlet weak var lbl_sender_name_img: UILabel!
    @IBOutlet weak var lbl_sender_time_img: UILabel!
    
    //User Text
    @IBOutlet weak var lbl_user_msg: UILabel!
    @IBOutlet weak var lbl_user_name: UILabel!
    @IBOutlet weak var lbl_user_time: UILabel!
    //User Image
    @IBOutlet weak var img_user: UIImageView!
    @IBOutlet weak var lbl_user_name_img: UILabel!
    @IBOutlet weak var lbl_user_time_img: UILabel!
    
    
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
