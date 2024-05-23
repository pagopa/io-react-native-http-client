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

    @objc(nativeRequest:withResolver:withRejecter:)
    func nativeRequest(config: [String: Any], resolve: @escaping RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        guard let verb = config["verb"] as? String else {
          handleNonHttpFailure("Bad configuration, missing 'verb'", resolve: resolve)
          return;
        }
        let methodOpt = optMethodFromVerb(verb)
        guard let method = methodOpt else {
          handleNonHttpFailure("Unsupported configuration, unknown verb '\(verb)'", resolve: resolve)
          return
        }

        guard let url = config["url"] as? String else {
          handleNonHttpFailure("Bad configuration, missing 'url'", resolve: resolve)
          return;
        }

        let parametersOpt = optParametersFromMethod(method, andConfig: config)

        let headers = headersFromConfig(config)
        let redirector = redirectorFromConfig(config)
        let timeoutSeconds = timeoutSecondsFromConfig(config)

        AF.request(url, 
                   method: method, 
                   parameters: parametersOpt,
                   headers: headers,
                   requestModifier: { $0.timeoutInterval = timeoutSeconds })
            .redirect(using: redirector)
            .responseString { response in
                self.handleResponse(response, resolve: resolve)
            }
    }
    
    @objc(setCookieForDomain:path:name:value:)
    func setCookieForDomain(_ domain: String, path: String, name: String, value: String) -> Void {
        let cookieProperties: [HTTPCookiePropertyKey:Any] = [
            HTTPCookiePropertyKey.domain: domain,
            HTTPCookiePropertyKey.path: path,
            HTTPCookiePropertyKey.name: name,
            HTTPCookiePropertyKey.value: value,
            HTTPCookiePropertyKey.secure: true
        ]
        if let cookie = HTTPCookie(properties: cookieProperties) {
            if let cookieStorage = AF.session.configuration.httpCookieStorage {
                cookieStorage.setCookie(cookie)
            }
        }
    }
    
    @objc(removeAllCookiesForDomain:)
    func removeAllCookiesForDomain(_ domain: String) -> Void {
        if let domainURL = URL(string: domain) {
            if let cookieStorage = AF.session.configuration.httpCookieStorage {
                if let domainCookies = cookieStorage.cookies(for: domainURL) {
                    domainCookies.forEach { domainCookie in
                        cookieStorage.deleteCookie(domainCookie)
                    }
                }
            }
        }
    }
    
    func optMethodFromVerb(_ verb: String) -> HTTPMethod? {
        return ("get".caseInsensitiveCompare(verb) == .orderedSame)
            ? HTTPMethod.get
            : ("post".caseInsensitiveCompare(verb) == .orderedSame)
            ? HTTPMethod.post
            : nil
    }
    
    func optParametersFromMethod(_ method: HTTPMethod, andConfig configOpt: [String:Any]?) -> [String:String]? {
        if (method == HTTPMethod.post) {
            return configOpt?["body"] as? [String:String]
        }
        return nil
    }
    
    
    func headersFromConfig(_ configOpt: [String: Any]?) -> HTTPHeaders {
        var headers: HTTPHeaders = []
        guard let config = configOpt else {
            return headers;
        }
        guard let configHeaders = config["headers"] as? [String: Any] else {
            return headers;
        }
        for (headerKey, headerValueAny) in configHeaders {
            if let headerValueString = headerValueAny as? String {
                headers[headerKey] = headerValueString
            }
        }
        return headers
    }
    
    func redirectorFromConfig(_ configOpt: [String: Any]?) -> Redirector {
        let followRedirects = (configOpt?["followRedirects"] as? Bool) ?? true
        return Redirector(behavior: followRedirects ? Redirector.Behavior.follow : Redirector.Behavior.doNotFollow)
    }
    
    func timeoutSecondsFromConfig(_ configOpt: [String: Any]?) -> TimeInterval {
        if let timeoutMilliseconds = configOpt?["timeoutMilliseconds"] as? TimeInterval {
            return timeoutMilliseconds / 1000
        }
        return 60
    }
    
    func handleResponse(_ response: AFDataResponse<String>, resolve: @escaping RCTPromiseResolveBlock) -> Void {
        let result = response.result
        if case .failure = result {
            if let error = response.error {
                handleNonHttpFailure(error.localizedDescription, resolve: resolve)
            } else {
                handleNonHttpFailure("Unable to send network request, unknown error", resolve: resolve)
            }
            return
        }
        
        guard let statusCode = response.response?.statusCode else {
            handleNonHttpFailure("Unable to read 'status code' from network response", resolve: resolve)
            return;
        }
        let body = response.value ?? ""
        let headers = response.response?.headers.dictionary ?? [:]
        
        let httpResponse: [String: Any] = statusCode < 400 ? [
            "type": "success",
            "status": statusCode,
            "body": body,
            "headers": headers
        ] : [
            "type": "failure",
            "code": statusCode,
            "message": body,
            "headers": headers
        ]
        resolve(httpResponse)
    }
    
    func handleNonHttpFailure(_ message: String, resolve: @escaping RCTPromiseResolveBlock) -> Void {
        let httpResponse: [String: Any] = [
            "type": "failure",
            "code": 900,
            "message": message,
            "headers": [:]
        ]
        resolve(httpResponse)
    }
   
}
