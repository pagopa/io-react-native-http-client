import Alamofire


@objc(IoReactNativeHttpClient)
class IoReactNativeHttpClient: NSObject {

    @objc(httpClientRequest:withHeaders:withResolver:withRejecter:)
    func httpClientRequest(url: String, headers:[String:String]?,  resolve: @escaping RCTPromiseResolveBlock,reject: @escaping RCTPromiseRejectBlock) -> Void {

        let formedHeaders = HTTPHeaders(headers ?? [:])

        AF.request(url,headers: formedHeaders).responseData { response in

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



