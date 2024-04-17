import Alamofire


@objc(IoReactNativeHttpClient)
class IoReactNativeHttpClient: NSObject {

    @objc(httpClientRequest:withResolver:withRejecter:)
    func httpClientRequest(url: String, resolve: @escaping RCTPromiseResolveBlock,reject: @escaping RCTPromiseRejectBlock) -> Void {
        
        AF.request(url).responseData { response in
            
            if let statusCode = response.response?.statusCode{
                
                
                var formedResponse:Dictionary<String,Any> = ["status":
                                                                statusCode as Any];
                
                if let resData = response.data {
                    guard let body = String(data:resData,encoding: .utf8) else{
                        reject("error", "failed to deserialize body",NSError(domain: "", code: 418))
                        return
                    }
                    
                    formedResponse["body"] = body
                }
                
                resolve(formedResponse)
            }else if let error = response.error {
                reject( "error", error.errorDescription, error )
            } else{
                
              reject ("error","i am a teapot",NSError(domain: "", code: 418))
            }
        }
    }
   
    
    @objc(fooBar:withResolver:withRejecter:)
    func fooBar(num:Int,resolve: @escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock
    ) -> Void{
        resolve(0);
    }

}



