//
//  WebService.swift
//  TaxiDriver
//
//

import UIKit
import Alamofire
import SwiftyJSON

//Swifty json ServiceResponse
//typealias ServiceResponse = (JSON, Error?) -> Void
typealias ServiceResponse = ([String:Any])->()

//Check internet connection
var isReachable: Bool {
    return NetworkReachabilityManager()!.isReachable
}
//MARK: - WebService
public class WebService {
    //Variable Declaration
    static var call: WebService = WebService()
    
    //MARK: - WebService Call
    func withPath(_ url: String, parameter: [String:Any] = [String: Any](), isWithLoading: Bool = true, images: [ModelAPIImg] = [ModelAPIImg](), videoKey: [String] = ["video"], videoData: [Data] = [Data](), audioKey: [String] = ["audio"], audioData: [Data] = [Data](), isNeedToken: Bool = true, methods: HTTPMethod = .post , completionHandler:@escaping ServiceResponse) {
        
        //var alamoFireManager : SessionManager?
        let alamoFireManager = Session.default
        
        if CheckConnection.isConnectedToNetwork() {
            
            if isWithLoading {
                if images.count > 0 || videoData.count > 0 || audioData.count > 0 {
                    appDelegate.showProgressHud(processDone: 0.0)
                } else {
                    appDelegate.showHud()
                }
                
            }
            
            var headers = HTTPHeaders()
            if isNeedToken {
                headers = [
                    WebURL.tokenKey : WebURL.tokenValue,
                        "Authorization" : "Bearer \(WebURL.bearerToken)",
                        "Accept": "application/json",
                        "role" : "Customer"
                    ]
            }
            
            let loadURL =  url
            
            
            if images.count > 0 || videoData.count > 0 || audioData.count > 0 {
                alamoFireManager.upload(multipartFormData: { multipartFormData in
                        for (key, value) in parameter {
                            multipartFormData.append("\(value)".data(using: String.Encoding.utf8)!, withName: key)
                        }
                        for i in 0..<images.count {
                            multipartFormData.append(images[i].image.jpegData(compressionQuality: 0.75)!, withName: images[i].key, fileName: "file.jpg", mimeType: "image/jpeg")
                        }
                        for i in 0..<videoData.count {
                            multipartFormData.append(videoData[i], withName: videoKey[i], fileName: "file.mp4", mimeType: "video/mp4")
                        }
                        for i in 0..<audioData.count {
                            multipartFormData.append(audioData[i], withName: audioKey[i], fileName: "file.m4a", mimeType: "audio/m4a")
                        }
                }, to: loadURL, method: methods, headers: headers).responseJSON { response in
                    if isWithLoading {
                        appDelegate.hideHud()
                    }
                    print("//-------------------------------------------------------")
                    print("URL \(loadURL)")
                    print("headers \(headers)")
                    print("Paramater \(parameter)")
                    if let responseData = response.data{
                        print("API Response\n\(String(data: responseData, encoding: String.Encoding.utf8) ?? "No response found")")
                    }
                    print("-------------------------------------------------------//")
                    self.handleResponse(statusCode: response.response?.statusCode, result: response.result, completionHandler: completionHandler)
                }
            }else{
            
                alamoFireManager.request(loadURL, method: methods ,parameters: parameter,headers: headers)
                    .responseJSON { response in
                        if isWithLoading {
                            appDelegate.hideHud()
                        }
                        print("//-------------------------------------------------------")
                        print("URL \(loadURL)")
                        print("Paramater \(parameter)")
                        if let responseData = response.data{
                            print("API Response\n\(String(data: responseData, encoding: String.Encoding.utf8) ?? "No response found")")
                        }
                        print("-------------------------------------------------------//")

                        self.handleResponse(statusCode: response.response?.statusCode, result: response.result, completionHandler: completionHandler)
                }
            }
        }else {
            showAlert(msg: "No Internet")
            appDelegate.hideHud()

        }
    }
    
    
    
    func handleResponse<T>(statusCode: Int?,result: Result<T,AFError>,completionHandler:@escaping ServiceResponse){
        
            switch result {
            case .success(let value):
                completionHandler(value as? [String:Any] ?? [String:Any]())
                
            case .failure(let error):
                print(error)
                if error.localizedDescription != ""{
                    showToast(text: error.localizedDescription)
                }
                completionHandler([String:Any]())
            }
    }
    
    static func objectFrom<D,Model:Codable>(dic: D, completion: (Model?) -> Void){
        //Where D is dictionary || array of dictionary
        do {
            let decoder = JSONDecoder()
            decoder.keyDecodingStrategy = .convertFromSnakeCase
            let jsonData = try JSONSerialization.data(withJSONObject: dic, options: [])
            let objModel = try decoder.decode(Model.self, from: jsonData)
            completion(objModel)
        } catch let myJSONError {
            print(myJSONError)
            if myJSONError.localizedDescription != ""{
                showToast(text: "\(myJSONError.localizedDescription)")
            }
            completion(nil)
        }
    }
}

//ModelAPIImg
class ModelAPIImg {
    
    var key = "images[]"
    var image = UIImage()
    
    init() {
        self.key = "images[]"
        self.image = UIImage()
    }
    
    init(image:UIImage) {
        self.key = "images[]"
        self.image = image
    }
    
    init(image:UIImage,key:String) {
        self.key = key
        self.image = image
    }
}
//MARK: - WebService
//public class WebService {
//    //Variable Declaration
//    static var call: WebService = WebService()
//
//    //MARK: - WebService Call
//    func withPath(_ url: String, parameter: [String:Any] = [String: Any](), isWithLoading: Bool = true, imageKey: [String] = ["image"], imageString: [String] = [String](), videoKey: [String] = ["video"], videoData: [Data] = [Data](), audioKey: [String] = ["audio"], audioData: [Data] = [Data](), isNeedToken: Bool = true, methods: HTTPMethod = .post , completionHandler:@escaping ServiceResponse) {
//
////        print("URL :- \(url)")
////        print("Parameter :- \(parameter)")
//
//        if isReachable {
//
//            if isWithLoading {
//                //showLoading()
//            }
//
//            var headers = HTTPHeaders()
//            if isNeedToken {
//                headers = [
//                    WebURL.tokenKey : WebURL.tokenValue,
//                    "Accept": "application/json",
//                    "role" : "Customer"
//                 ]
//            }
//
//            let testUrl = url
//
//            if imageString.count > 0 || videoData.count > 0 || audioData.count > 0 {
//
//                Alamofire.upload (
//                    multipartFormData: { multipartFormData in
//
//                        for (key, value) in parameter {
//                            multipartFormData.append("\(value)".data(using: String.Encoding.utf8)!, withName: key)
//                        }
//
//                        for i in 0..<imageString.count {
//
//                            let imgData = imageString[i].convertBase64ToData()
//                            multipartFormData.append(imgData, withName: imageKey[i], fileName: "file.jpeg", mimeType: "image/jpeg")
//
//                        }
//
//                        for i in 0..<videoData.count {
//                            multipartFormData.append(videoData[i], withName: videoKey[i], fileName: "file.mp4", mimeType: "video/mp4")
//                        }
//
//                        for i in 0..<audioData.count {
//                            multipartFormData.append(audioData[i], withName: audioKey[i], fileName: "file.m4a", mimeType: "audio/m4a")
//                        }
//                },
//                    to: url,
//                    headers : headers,
//                    encodingCompletion: { encodingResult in
//                        switch encodingResult {
//                        case .success(let upload, _, _):
//                            upload.responseJSON { result in
//
////                                print(result)
////                                print(result.result)
//
//                                if let httpError = result.result.error {
//
////                                    print(NSString(data: result.data!, encoding: String.Encoding.utf8.rawValue)!)
////                                    print(httpError._code)
//
//                                    let response: [String: Any] = [
//                                        "errorCode": httpError._code,
//                                        "status": false,
//                                        "message": "eror"
//                                    ]
//
//                                    let json = JSON(response)
//                                    completionHandler(json, nil)
//                                    print("****************** Api Call *******************")
//                                    print("URL:- \(url)\n\(headers)\nParam:- \(parameter)\nResponse\n\(json)")
//                                    print("***********************************************")
//                                }
//
//                                if  result.result.isSuccess {
//                                    if let response = result.result.value {
//                                        let json = JSON(response)
//                                        completionHandler(json, nil)
//                                        print("****************** Api Call *******************")
//                                        print("URL:- \(url)\n\(headers)\nParam:- \(parameter)\nResponse\n\(json)")
//                                        print("***********************************************")
//                                    }
//                                }
//
//                                if isWithLoading {
//                                    //self.hideLoading()
//                                }
//                            }
//                        case .failure(let encodingError):
//                            print(encodingError)
//                        }
//                })
//            }
//            else
//            {
//
//
//                Alamofire.request(testUrl, method: methods ,parameters: parameter,headers: headers)
//                    .responseJSON {  result in
//
////                        print(result)
////                        print(result.result)
//
//                        if let httpError = result.result.error {
////                            print(NSString(data: result.data!, encoding: String.Encoding.utf8.rawValue)!)
////                            print(httpError._code)
//
//                            let response: [String: Any] = [
//                                "errorCode": httpError._code,
//                                "StatusCode": result.response?.statusCode as Any,
//                                "status": false,
//                                "message": "ValidationMessage.somthingWrong"
//                            ]
//                            let json = JSON(response)
//                            completionHandler(json,httpError)
//                            print("****************** Api Call *******************")
//                            print("URL:- \(url)\n\(headers)\nParam:- \(parameter)\nResponse\n\(json)")
//                            print("***********************************************")
//                        }
//
//                        if  result.result.isSuccess {
//                            if let response = result.result.value {
//                                let json = JSON(response)
//                                completionHandler(json, nil)
//                                print("****************** Api Call *******************")
//                                print("URL:- \(url)\n\(headers)\nParam:- \(parameter)\nResponse\n\(json)")
//                                print("***********************************************")
//                            }
//                        }
//
//                        if isWithLoading {
//                            //self.hideLoading()
//                        }
//                }
//            }
//        }
//        else {
//            // self.showTostMessage(message: "ValidationMessage.internetNotAvailable")
//        }
//    }
//
//    //TODO: - Upload Image Api
//    func uploadImageApi(finalURL:String, imageName: String, imageData: UIImage, parameters: [String : Any], completion:((NSDictionary,String) -> ())?){
//
//        //Final URL
//        let finalUrl = URL(string:finalURL)
//
//        let headers = [
//            WebURL.tokenKey : WebURL.tokenValue
//        ]
//
//        Alamofire.upload(multipartFormData: { (multipartFormData) in
//
//            multipartFormData.append(UIImageJPEGRepresentation(imageData, 0.1)!, withName: imageName, fileName: "image.jpg", mimeType: "image/jpeg")
//
//            for (key, value) in parameters {
//                multipartFormData.append("\(value)".data(using: String.Encoding.utf8)!, withName: key)
//            }
//
//        }, usingThreshold: UInt64.init(), to: finalUrl!, method: .post, headers: headers) { (result) in
//            switch result{
//            case .success(let upload, _, _):
//                upload.responseJSON { response in
//                    print("Succesfully uploaded")
//                    if let JSON:NSDictionary = response.result.value as? NSDictionary {
//
//                        if completion != nil {
//
//                            completion!(JSON , "")
//                            print("****************** Api Call *******************")
//                            print("URL:- \(finalURL)\nParam:- \(parameters)\n\(JSON)")
//                            print("***********************************************")
//                        }
//                    }
//
//                }
//            case .failure(let error):
//                print("Error in upload: \(error.localizedDescription)")
//
//            }
//        }
//    }
//}

//Block for throw response
typealias WSBlock = (_ json: NSDictionary?, _ flag: Int) -> ()
//typealias NewBlock = (_ json: NSDictionary, _ flag: Int) -> ()
typealias WSProgress = (Progress) -> ()?
typealias WSFileBlock = (_ path: String?, _ success: Bool) -> ()


