import Alamofire

@objc(IoReactNativeHttpClient)
class IoReactNativeHttpClient: NSObject {

  @objc(multiply:withResolver:withRejecter:)
    func multiply(a: String, resolve: @escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {

      AF.request(a, encoding: JSONEncoding.default).responseJSON { response in
          let statusCodeOpt = response.response?.statusCode
          
          if let statusCode = statusCodeOpt, statusCode == 200 {
              
              let result = response.value as! [String: Any]
              
              resolve(result["origin"]);
              return
          }
          
          resolve("Error")
    }
  }
}
