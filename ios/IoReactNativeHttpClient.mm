#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(IoReactNativeHttpClient, NSObject)

RCT_EXTERN_METHOD(
                 httpClientRequest:(NSString *)url
                 isPost:(BOOL)isPost
                 withFormEncodedParams:(NSDictionary *) formEncodedParams
                 withHeaders:(NSDictionary *)headers
                 shouldFollowRedirects:(BOOL)shouldFollowRedirects
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(fooBar:(NSInteger)a
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
