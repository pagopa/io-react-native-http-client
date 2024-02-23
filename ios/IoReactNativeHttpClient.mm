#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(IoReactNativeHttpClient, NSObject)

RCT_EXTERN_METHOD(multiply:(NSString *)a
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
