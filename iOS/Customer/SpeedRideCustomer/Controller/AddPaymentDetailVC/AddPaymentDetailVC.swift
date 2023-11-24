//
//  AddPaymentVC.swift
//  RidexCustomer
//
//

import UIKit

//MARK: - Add Payment Detail Cell
class CellAddPaymentDetail: UICollectionViewCell {
    
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblcardNumber: UILabel!
   
}
//MARK: - ModelCardList
class ModelCardList{
    
    var cardHolderName:String = ""
    var cardNumber:String = ""
    var date:String? = ""
    
    //init Method
    init(cardHolderName:String, cardNumber:String, date:String?) {
        
        self.cardHolderName = cardHolderName
        self.cardNumber = cardNumber
        self.date = date
    }
}

//MARK: - Add Payment DetailViewController

class AddPaymentDetailVC: MTViewController {

    //TODO: - IBOutlet Declaration
    @IBOutlet weak var lblCardNumber: UILabel!
    @IBOutlet weak var lblCardName: UILabel!
    @IBOutlet weak var lblCardDate: UILabel!
    @IBOutlet weak var colCardInfo: UICollectionView!
    
    
    //TODO: - Variable Declaration
     var arrCardDetails = [ModelCardList]()

    override func viewDidLoad() {
        super.viewDidLoad()
    
        initialization()
        
    }

    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //TODO: - initialization Method
    func initialization(){
        
        //Add Card Details
        arrCardDetails.append(ModelCardList.init(cardHolderName: "abc", cardNumber: "9879 9879 9879 9870", date: "12/05"))
        arrCardDetails.append(ModelCardList.init(cardHolderName: "bcd", cardNumber: "9879 9879 9879 9871", date: "31/12"))
        arrCardDetails.append(ModelCardList.init(cardHolderName: "hkjs", cardNumber: "9879 9879 9879 9872", date: "30/01"))
        arrCardDetails.append(ModelCardList.init(cardHolderName: "jfgj", cardNumber: "9879 9879 9879 9873", date: "30/03"))
        arrCardDetails.append(ModelCardList.init(cardHolderName: "hkhjkd", cardNumber: "9879 9879 9879 9874", date: "30/05"))
        arrCardDetails.append(ModelCardList.init(cardHolderName: "dfnsgs", cardNumber: "9879 9879 9879 9875", date: "25/08"))
        
        //TODO: - Card defaulf value
        self.lblCardName?.text =  arrCardDetails[0].cardHolderName
        self.lblCardNumber.text = arrCardDetails[0].cardNumber
        self.lblCardDate.text = arrCardDetails[0].date
    }
}

//MARK: - tapped Action
extension AddPaymentDetailVC {
    //TODO: - tapObBack
    @IBAction func tapObBack(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }

    //TODO: - tap On next
    @IBAction func tappedOnNext(_ sender: Any) {
    }

}

//MARK: - Collection View Methods
extension AddPaymentDetailVC: UICollectionViewDataSource, UICollectionViewDelegate,UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return arrCardDetails.count
        
    }
    
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "CellAddPaymentDetail", for: indexPath) as! CellAddPaymentDetail
       
        cell.lblName.text = arrCardDetails[(indexPath.row)].cardHolderName
        cell.lblcardNumber.text = arrCardDetails[(indexPath.row)].cardNumber
       
        
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: UIScreen.main.bounds.width - 40, height: 75)
    }
    //TODO: ScrollView Method
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        for cell: UICollectionViewCell in colCardInfo.visibleCells {
            let indexPath: IndexPath? = colCardInfo.indexPath(for: cell)
            if let aPath = indexPath {
                print("\(aPath)")
              
                self.lblCardName.text = arrCardDetails[(indexPath?.row)!].cardHolderName
                self.lblCardNumber.text = arrCardDetails[(indexPath?.row)!].cardNumber
                self.lblCardDate.text = arrCardDetails[(indexPath?.row)!].date
                
            }
        }
        self.colCardInfo.reloadData()
    }
}
