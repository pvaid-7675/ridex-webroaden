//
//  LestSideMenu.swift
//  MTLogin
//
//

import UIKit

//MARK: - CellSideBar
class CellLeftSideMenu:UITableViewCell {
    
    //IBOutlet Declaration
    @IBOutlet var lblLeftSideMenuProperties: UILabel!
    @IBOutlet var imgSideMenuProperties: UIImageView!
    
}
//MARK: - LeftSideMenuVC
class LeftSideMenu: MTViewController {
    
    //IBOutlet Declaration
    @IBOutlet var imgUserProfile: UIImageView!
    @IBOutlet var lblUSerName: UILabel!
    @IBOutlet var lblNumber: UILabel!
    @IBOutlet var tblLeftSideMenu: UITableView!
    @IBOutlet weak var conWidth: NSLayoutConstraint!
    
    // Variable Declaration
    var arrSideBarProperties = [ModelSideBar]()
    var isMenuOpen:Bool = false
    let grayView = UIControl()
    var obj_ModelUserInfo = ModelUserInfo.init()
    
    //Override Method
    override func viewDidLoad() {
        super.viewDidLoad()
        self.revealViewController().delegate = self
        self.initialization()
    }
    
    //viewWillAppear
    override func viewWillAppear(_ animated: Bool) {
        grayView.isHidden = false
        grayView.backgroundColor = UIColor.red
        self.revealViewController().frontViewController.view.addSubview(grayView)
        self.displayUserInfo()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    func displayUserInfo(){
        print("user info displayin")
        if let user = appDelegate.objUserInfo{
            self.lblUSerName.text = user.name
            self.lblNumber.text = user.mobile
            if let image = user.image{
                let url = WebURL.uploadURL + image
                imgUserProfile.imageFromURL(url, placeHolder: #imageLiteral(resourceName: "ic_driver_profile"))
            } else {
                self.imgUserProfile.image = #imageLiteral(resourceName: "ic_driver_profile")
            }
        }
    }
    
    @IBAction func leftSidemenuBack(_ sender: Any) {
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
    }
    
    @IBAction func tappedOnEditProfile(_ sender: Any) {
        let Help = StoryBoard.SideBarList.instantiateViewController(withIdentifier: "SettingVC") as! SettingVC
        let navController = UINavigationController(rootViewController: Help)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([Help], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
    }
    
}
//Initialization
extension LeftSideMenu {
    
    func initialization(){
        self .revealViewController().rearViewRevealWidth = UIScreen.main.bounds.width - 75
        
        //Left Sidebar
        arrSideBarProperties.append(ModelSideBar.init(strTitle:Title.home.rawValue, image: #imageLiteral(resourceName: "home")))
        // arrSideBarProperties.append(ModelSideBar.init(strTitle: Title.payment.rawValue, image: #imageLiteral(resourceName: "payments")))
        arrSideBarProperties.append(ModelSideBar.init(strTitle: Title.history.rawValue, image: #imageLiteral(resourceName: "History")))
        arrSideBarProperties.append(ModelSideBar.init(strTitle: Title.scheduleRide.rawValue, image: #imageLiteral(resourceName: "shifttime")))
        //  arrSideBarProperties.append(ModelSideBar.init(strTitle: Title.notification.rawValue, image: #imageLiteral(resourceName: "notification")))
        //  arrSideBarProperties.append(ModelSideBar.init(strTitle: Title.setting.rawValue, image: #imageLiteral(resourceName: "img_setting")))
        
        imgUserProfile.layer.cornerRadius = imgUserProfile.frame.height/2
        imgUserProfile.layer.masksToBounds = true
        imgUserProfile.image = #imageLiteral(resourceName: "imgTaxiDriverProfile")
        lblUSerName.text = appDelegate.objUserInfo?.name ?? ""
        lblNumber.text = appDelegate.objUserInfo?.mobile ?? ""
    }
}
//TableView DataSource And Delegate
extension LeftSideMenu: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return arrSideBarProperties.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "CellLeftSideMenu") as! CellLeftSideMenu
        let sideBarProperties = arrSideBarProperties[indexPath.row]
        cell.imgSideMenuProperties.image = sideBarProperties.image
        cell.lblLeftSideMenuProperties.text = sideBarProperties.title
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if tableView == tblLeftSideMenu{
            
            switch arrSideBarProperties[indexPath.row].title {
            case Title.home.rawValue:
                self.redirectToPickUpVC()
                break
            case  Title.payment.rawValue:
                self.redirectTopaymentScreen()
                break
            case  Title.history.rawValue:
                self.redirectToHistoryScreen()
                break
            case  Title.scheduleRide.rawValue:
                self.redirectToScheduleRideScreen()
                break
            case  Title.setting.rawValue:
                self.redirectToSettingScreen()
                break
            case Title.notification.rawValue:
                self.redirectToNotificationScreen()
                break
            case Title.help.rawValue:
                self.redirectToHelpScreen()
                break
            case  Title.logout.rawValue:
                self.logoutApiCall()
                
                break
            default:
                break
            }
        }
    }
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 50
    }
}
//Redirect to other screens
extension LeftSideMenu {
    //TODO: - Redirect To NextScreen
    func redirectToHomeScreen() {
        let Home = StoryBoard.Main.instantiateViewController(withIdentifier: "HomeVC") as! HomeVC
        let navController = UINavigationController(rootViewController: Home)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([Home], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
//        self.navigationController?.pushViewController(AddPaymentDetail, animated: true)
    }
    
    func redirectToPickUpVC() {
        let PickUp = StoryBoard.SideBar.instantiateViewController(withIdentifier: "PickUpVC") as! PickUpVC
        let navController = UINavigationController(rootViewController: PickUp)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([PickUp], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
//        self.navigationController?.pushViewController(AddPaymentDetail, animated: true)
    }
    
    func redirectTopaymentScreen() {
        let PaymentType = StoryBoard.SideBarList.instantiateViewController(withIdentifier: "PaymentTypeVC") as! PaymentTypeVC
        let navController = UINavigationController(rootViewController: PaymentType)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([PaymentType], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
        self.revealViewController().revealToggle(self)
//        self.navigationController?.pushViewController(AddPaymentDetail, animated: true)
        
    }
    func redirectToHistoryScreen() {
        let History = StoryBoard.SideBarList.instantiateViewController(withIdentifier: "HistoryVC") as! HistoryVC
        let navController = UINavigationController(rootViewController: History)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([History], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
        //        self.navigationController?.pushViewController(AddPaymentDetail, animated: true)
    }
    
    func redirectToScheduleRideScreen() {
        let History = StoryBoard.SideBarList.instantiateViewController(withIdentifier: "ScheduledRidesVC") as! ScheduledRidesVC
        let navController = UINavigationController(rootViewController: History)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([History], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
    }
    
    func redirectToSettingScreen() {
        let Setting = StoryBoard.SideBarList.instantiateViewController(withIdentifier: "SettingVC") as! SettingVC
        let navController = UINavigationController(rootViewController: Setting)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([Setting], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
        //      self.navigationController?.pushViewController(AddPaymentDetail, animated: true)
    }
    func redirectToHelpScreen() {
        let Help = StoryBoard.SideBarList.instantiateViewController(withIdentifier: "HelpVC") as! HelpVC
        let navController = UINavigationController(rootViewController: Help)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([Help], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
        //      self.navigationController?.pushViewController(AddPaymentDetail, animated: true)
    }
    
    func redirectToNotificationScreen() {
        let Notification = StoryBoard.SideBarList.instantiateViewController(withIdentifier: "NotificationVC") as! NotificationVC
        let navController = UINavigationController(rootViewController: Notification)
        navController.navigationBar.isHidden = true
        navController.setViewControllers([Notification], animated:true)
        self.revealViewController().setFront(navController, animated: true)
        self.revealViewController().setFrontViewPosition(FrontViewPosition.left, animated: true)
        //      self.navigationController?.pushViewController(AddPaymentDetail, animated: true)
        
    }
    // tapped On tapped On Help
    @IBAction func tappedOnHelp(_ sender: Any) {
        self.redirectToHelpScreen()
    }
    
    // tapped On tapped On Logout
    @IBAction func tappedOnLogout(_ sender: Any) {
        self.logoutApiCall()
    }
}
//MARK: - Logout Api Call
extension LeftSideMenu {
    func logoutApiCall(){
        WebService.call.withPath(WebURL.logout) { [weak self] (responseDic) in
            guard let self = self else { return }
            WebService.objectFrom(dic: responseDic) { (APIResponse: Model_APIResponse<Model_Default>?) in
                if let response = APIResponse {
                    self.showAlertWithCompletion(title: "Alert", message: response.message ?? "Logout Successfully", options: "Ok") { indx in
                        UserInfo.sharedInstance.clearMyUserDefaluts()
                    }
                }else{
                    showAlert(title: "Alert", msg: APIResponse?.message ?? "Success status not found from response of the server.",completion: nil)
                }
                self.redirectToHomeScreen()
            }
        }
    }
}

extension LeftSideMenu : SWRevealViewControllerDelegate{
    
    func revealController(_ revealController: SWRevealViewController!, didMoveTo position: FrontViewPosition) {
        
        switch position {
            
        case FrontViewPosition.leftSideMostRemoved:
            print("LeftSideMostRemoved")
            // Left most position, front view is presented left-offseted by rightViewRevealWidth+rigthViewRevealOverdraw
            
        case FrontViewPosition.leftSideMost:
            print("LeftSideMost")
            // Left position, front view is presented left-offseted by rightViewRevealWidth
            
        case FrontViewPosition.leftSide:
            print("LeftSide")
            
            // Center position, rear view is hidden behind front controller
        case FrontViewPosition.left:
            print("Left")
            //Closed
            //0 rotation
            
            // Right possition, front view is presented right-offseted by rearViewRevealWidth
        case FrontViewPosition.right:
            print("Right")
            //Opened
            //rotated
            // Right most possition, front view is presented right-offseted by rearViewRevealWidth+rearViewRevealOverdraw
            
        case FrontViewPosition.rightMost:
            print("RightMost")
            
        case FrontViewPosition.rightMostRemoved:
            print("RightMostRemoved")
            
        default:
            print("unknown case")
            
        }
        
    }
}

