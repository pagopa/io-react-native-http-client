import Alamofire


@objc(IoReactNativeHttpClient)
class IoReactNativeHttpClient: NSObject {

    @objc(httpClientRequest:isPost:withFormEncodedParams:withHeaders:shouldFollowRedirects:withResolver:withRejecter:)
    func httpClientRequest(url: String,isPost:Bool,formEncodedParams:[String:String]?, headers:[String:String]?,shouldFollowRedirects:Bool, resolve: @escaping RCTPromiseResolveBlock,reject: @escaping RCTPromiseRejectBlock) -> Void {

        // request building
        
        let redirector = Redirector(
            behavior: shouldFollowRedirects ?
                                    Redirector.Behavior.follow
                                    :
                                    Redirector.Behavior.doNotFollow
        )
        
        let formedHeaders = HTTPHeaders(headers ?? [:])
        
        // post preparation shenanigans
        let (httpMethod,httpPostParams) = isPost ?  (HTTPMethod.post,(formEncodedParams ?? [:] )) : (HTTPMethod.get,nil)
        
        
        // response handling
        
        AF.request(url, method:httpMethod, parameters:httpPostParams, headers: formedHeaders).redirect(using: redirector).responseData { response in

            if let statusCode = response.response?.statusCode{

                var formedResponse:[String:Any] = ["status":statusCode as Any, "headers":[:]];

                if let resHeaders=response.response?.headers{
                    formedResponse["headers"]=resHeaders.dictionary
                }

                if let resData = response.data {
                    guard let body = String(data:resData,encoding: .utf8) else{
                        reject("error", "failed to deserialize body",NSError(domain: "", code: 418))
                        return
                    }

                    formedResponse["body"] = body
                }

                resolve(formedResponse)
                
            // fail when making the call
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



