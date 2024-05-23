#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(IoReactNativeHttpClient, NSObject)

RCT_EXTERN_METHOD(multiply:(NSString *)a
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(nativeRequest:(NSDictionary *)config
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(setCookieForDomain:(NSString *)domain
                  path:(NSString *)path
                  name:(NSString *)name
                  value:(NSString *)value)

RCT_EXTERN_METHOD(removeAllCookiesForDomain:(NSString *)domain)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
