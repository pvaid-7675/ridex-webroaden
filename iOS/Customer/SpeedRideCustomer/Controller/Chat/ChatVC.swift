//
//  ChatVC.swift
//  RidexCustomer
//

import UIKit

class ChatVC: UIViewController , UITextViewDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate{

    @IBOutlet weak var tbl_chat: UITableView!
    @IBOutlet weak var txt_msg: UITextView!
    
    var imagePicker = UIImagePickerController()
    //Variable
    var objRideInfo: ModelRideInfo?
    var arrChat = [ChatReceiveModel]()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        //tbl_chat.scrollToBottom()
        tbl_chat.keyboardDismissMode = .onDrag
        
        txt_msg.delegate = self
        txt_msg.text = "Message..."
        txt_msg.textColor = UIColor.lightGray
        
        self.getChatMessage()
        self.getPreviousChatMessages()
    }
    
    func textViewDidBeginEditing(_ textView: UITextView) {

        if txt_msg.textColor == UIColor.lightGray {
            txt_msg.text = ""
            txt_msg.textColor = UIColor.black
        }
    }
    
    func textViewDidEndEditing(_ textView: UITextView) {

        if txt_msg.text == "" {
            txt_msg.text = "Message..."
            txt_msg.textColor = UIColor.lightGray
        }
    }

    @IBAction func btn_BACK(_ sender: UIButton) {
        self.navigationController?.popViewController(animated: true)
    }

    @IBAction func btn_SEND_Message(_ sender: UIButton) {
        if txt_msg.text != "Message..." &&  txt_msg.text != ""{
            if let txm = txt_msg.text{
               self.sendMsg(txtmsg: txm)
               txt_msg.text = ""
               txt_msg.textColor = UIColor.lightGray
               txt_msg.resignFirstResponder()
           }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillDisappear), name: UIWindow.keyboardWillHideNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillAppear), name: UIWindow.keyboardWillShowNotification, object: nil)
    }
    
    
    override func viewWillDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc func keyboardWillAppear(_ notification: NSNotification) {
        
        if let _ = (notification.userInfo?[UIResponder.keyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue {
            let userInfo = notification.userInfo as! [String: NSObject] as NSDictionary
            let keyboardFrame = userInfo.value(forKey: UIResponder.keyboardFrameEndUserInfoKey) as! CGRect
            if self.view.frame.origin.y == 0{
                self.view.frame.origin.y -= keyboardFrame.height
            }
        }
    }
    
    @objc func keyboardWillDisappear(_ notification: NSNotification) {
        
        if let _ = (notification.userInfo?[UIResponder.keyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue {
            if self.view.frame.origin.y != 0{
                self.view.frame.origin.y = 0
            }
        }
    }
    
    // Schedule Ride Emit ( continuously )
    @objc func sendMsg(txtmsg:String,msgType:String = "text"){
        if !SocketIOManager.shared.isConnect {
            print("****** Socket is not connected. ******")
            return
        }
        
        if let userId = appDelegate.objUserInfo?.id{
            var parameter = [String:Any]()
            parameter["message_type"] = msgType
            parameter["details"] = txtmsg
            parameter["user_id"] = "\(userId)"
            parameter["ride_id"] = "\(self.objRideInfo?.id ?? 0)"
            parameter["date_time"] = Date().getCurrentDateWithFormat()
            print("****** Chcking for schedule rides ******")
            SocketIOManager.shared.socket.emit(SocketEvents.sendMsg, with: [parameter], completion: nil)
            
        }
    }
    //receive_message_
    @objc func getChatMessage(){
        if let userId = appDelegate.objUserInfo?.id{
            let request = SocketEvents.receiveMessage + "\(self.objRideInfo?.id ?? 0)"
            if SocketIOManager.shared.isConnect{
                SocketIOManager.shared.socket.on(request) { (result, emit) in
                    print("****** Driver assigned to your ride now you can move to Pickup Arriving screen: \(result) ********")
                    WebService.objectFrom(dic: result) { (APIResponse: [ChatReceiveModel]?) in
                        //self.objRideConfirmInfo = APIResponse?[0]
                        if let tempObj = APIResponse?[0]{
                            print("Ride ID ===\(tempObj)")
                            self.arrChat.append(tempObj)
                            self.tbl_chat.reloadData()
                        }
                        else{
                            print("Ride ID not found")
                        }
                    }
                }
            }else{
                print("****** Socket is not connected. ******")
            }
        }
    }
    
    //get-chat-messages
    func getPreviousChatMessages(){
        if let rideId = self.objRideInfo?.id{
            var parameter = [String:Any]()
            parameter["ride_id"] = rideId
            
            WebService.call.withPath(WebURL.getChatMessages, parameter: parameter) { (responseDic) in
                WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<[ChatReceiveModel]>?) in
                    if APIResponse?.status == 200, let histories = APIResponse?.data {

                        self.arrChat = histories
                        self.tbl_chat.reloadData()
                        
                    }else{
                        //Show Alert
                        showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                    }
                }
            }
        }
    }
    
    @IBAction func imagePickerBtnAction(selectedButton: UIButton)
    {
        
        let alert = UIAlertController(title: "Choose Image", message: nil, preferredStyle: .actionSheet)
        alert.addAction(UIAlertAction(title: "Camera", style: .default, handler: { _ in
            self.openCamera()
        }))
        
        alert.addAction(UIAlertAction(title: "Gallery", style: .default, handler: { _ in
            self.openGallery()
        }))
        
        alert.addAction(UIAlertAction.init(title: "Cancel", style: .cancel, handler: nil))
        
        self.present(alert, animated: true, completion: nil)
    }
    
    func openCamera()
    {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerController.SourceType.camera) {
            let imagePicker = UIImagePickerController()
            imagePicker.delegate = self
            imagePicker.sourceType = UIImagePickerController.SourceType.camera
            imagePicker.allowsEditing = false
            self.present(imagePicker, animated: true, completion: nil)
        }
        else
        {
            let alert  = UIAlertController(title: "Warning", message: "You don't have camera", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            self.present(alert, animated: true, completion: nil)
        }
    }
    func openGallery()
    {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerController.SourceType.photoLibrary){
            let imagePicker = UIImagePickerController()
            imagePicker.delegate = self
            imagePicker.allowsEditing = true
            imagePicker.sourceType = UIImagePickerController.SourceType.photoLibrary
            self.present(imagePicker, animated: true, completion: nil)
        }
        else
        {
            let alert  = UIAlertController(title: "Warning", message: "You don't have permission to access gallery.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    //MARK:-- ImagePicker delegate
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        if let pickedImage = info[.originalImage] as? UIImage {
            // imageViewPic.contentMode = .scaleToFill
            uploadChatImage(tempImg: pickedImage)
        }
        picker.dismiss(animated: true, completion: nil)
    }
    
    
    func uploadChatImage(tempImg:UIImage){
        var parameter:[String:Any] = [:]
        var mai = [ModelAPIImg(image: tempImg, key: "image")]
        //parameter["ride_id"] = ride_id
        WebService.call.withPath(WebURL.uploadChatImage, parameter: parameter,images: mai) { (responseDic) in
            print("json: \(responseDic)")
            
            do {
                let data = try JSONSerialization.data(withJSONObject: responseDic, options: .prettyPrinted)

                let decoder = JSONDecoder()
                do {
                    let customer = try decoder.decode(ChatImageModel.self, from: data)
                    print(customer)
                    self.sendMsg(txtmsg: customer.data ?? "",msgType: "image")
                } catch {
                    print(error.localizedDescription)
                }
            } catch {
                print(error.localizedDescription)
            }
        }
    }
    
}

extension ChatVC : UITableViewDelegate, UITableViewDataSource
{
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.arrChat.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        if self.arrChat[indexPath.row].userId ?? 0 == appDelegate.objUserInfo?.id ?? 0{
            if self.arrChat[indexPath.row].messageType! == "image"{
                let cell = tableView.dequeueReusableCell(withIdentifier: "CellUserImg", for: indexPath) as! ChatCell
                cell.img_user.imageFromURL(self.arrChat[indexPath.row].details ?? "", placeHolder: #imageLiteral(resourceName: "applogo"))
                cell.lbl_user_name_img.text = "\(appDelegate.objUserInfo?.name ?? "Customer") (Customer)"
                cell.lbl_user_time_img.text = getChatFormattedDate(stringdt: self.arrChat[indexPath.row].dateTime ?? "2023-01-01 10:10:10")
                return cell
            }
            
            let cell = tableView.dequeueReusableCell(withIdentifier: "CellUser", for: indexPath) as! ChatCell
            cell.lbl_user_msg.text = self.arrChat[indexPath.row].details
            cell.lbl_user_name.text = "\(appDelegate.objUserInfo?.name ?? "Customer") (Customer)"
            cell.lbl_user_time.text = getChatFormattedDate(stringdt: self.arrChat[indexPath.row].dateTime ?? "2023-01-01 10:10:10")
            return cell
        }
        else{
            
            if self.arrChat[indexPath.row].messageType! == "image"{
                let cell = tableView.dequeueReusableCell(withIdentifier: "CellSenderImg", for: indexPath) as! ChatCell
                cell.img_sender.imageFromURL(self.arrChat[indexPath.row].details ?? "", placeHolder: #imageLiteral(resourceName: "applogo"))
                cell.lbl_sender_name_img.text = "\(self.objRideInfo?.driverDetails?.name ?? "Driver") (Driver)"
                cell.lbl_sender_time_img.text = getChatFormattedDate(stringdt: self.arrChat[indexPath.row].dateTime ?? "2023-01-01 10:10:10")
                return cell
            }
            
            let cell = tableView.dequeueReusableCell(withIdentifier: "CellSender", for: indexPath) as! ChatCell
            cell.lbl_sender_msg.text = self.arrChat[indexPath.row].details
            cell.lbl_sender_name.text = "\(self.objRideInfo?.driverDetails?.name ?? "Driver") (Driver)"
            cell.lbl_sender_time.text = getChatFormattedDate(stringdt: self.arrChat[indexPath.row].dateTime ?? "2023-01-01 10:10:10")
            return cell
        }
    }
}


extension UITableView {

    func scrollToBottom(isAnimated:Bool = true){

        DispatchQueue.main.async {
            let indexPath = IndexPath(
                row: self.numberOfRows(inSection:  self.numberOfSections-1) - 1,
                section: self.numberOfSections - 1)
            if self.hasRowAtIndexPath(indexPath: indexPath) {
                self.scrollToRow(at: indexPath, at: .bottom, animated: isAnimated)
            }
        }
    }

    func scrollToTop(isAnimated:Bool = true) {

        DispatchQueue.main.async {
            let indexPath = IndexPath(row: 0, section: 0)
            if self.hasRowAtIndexPath(indexPath: indexPath) {
                self.scrollToRow(at: indexPath, at: .top, animated: isAnimated)
           }
        }
    }

    func hasRowAtIndexPath(indexPath: IndexPath) -> Bool {
        return indexPath.section < self.numberOfSections && indexPath.row < self.numberOfRows(inSection: indexPath.section)
    }
    
}

extension ChatVC{
    func getChatFormattedDate(stringdt: String) -> String{
        let dateFormatterGet = DateFormatter()
        dateFormatterGet.dateFormat = "yyyy-MM-dd HH:mm:ss"//yyyy-MM-dd HH:mm:ss
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "HH:mm"//

        let date: Date? = dateFormatterGet.date(from: stringdt)
        print(dateFormatter.string(from: date!))
        return dateFormatter.string(from: date!)
    }
}
