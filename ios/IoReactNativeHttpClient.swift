import Alamofire

@objc(IoReactNativeHttpClient)
class IoReactNativeHttpClient: NSObject {

    @objc(standardRequest:withResolver:withRejecter:)
    func standardRequest(url: String, resolve: @escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {


        AF.request(url).responseData { response in


            switch response.result{
            case .success(let resData):

                guard let statusCode = response.response?.statusCode else{
                    resolve(nil)
                    return
                }

                let resString:String? = String(data:resData,encoding: .utf8)

                let formedResponse=[
                    "status": statusCode, "body":resString as Any]

                resolve(formedResponse)

            case .failure(let error):

                resolve("error \(error)")
                return;


            }

        }
        print("foo bar");
    }
    @objc(fooBar:withResolver:withRejecter:)
    func fooBar(num:Int,resolve: @escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock
    ) -> Void{
        resolve(0);
    }

}



