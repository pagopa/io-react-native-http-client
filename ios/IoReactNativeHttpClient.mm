#import "IoReactNativeHttpClient.h"
#import "IoReactNativeHttpClient-Swift.h"

@interface IoReactNativeHttpClient ()

@property(nonatomic, strong) IoReactNativeHttpClientCore *implementation;

@end

@implementation IoReactNativeHttpClient

- (instancetype)init
{
    self = [super init];
    if (self) {
        _implementation = [IoReactNativeHttpClientCore new];
    }

    return self;
}

- (void)nativeRequest:(NSDictionary *)config
               resolve:(RCTPromiseResolveBlock)resolve
                reject:(RCTPromiseRejectBlock)reject
{
    [self.implementation nativeRequest:config withResolver:resolve withRejecter:reject];
}

- (void)setCookieForDomain:(NSString *)domain
                      path:(NSString *)path
                      name:(NSString *)name
                     value:(NSString *)value
{
    [self.implementation setCookieForDomain:domain path:path name:name value:value];
}

- (void)removeAllCookiesForDomain:(NSString *)domain
{
    [self.implementation removeAllCookiesForDomain:domain];
}

- (void)cancelRequestWithId:(NSString *)requestId
{
    [self.implementation cancelRequestWithId:requestId];
}

- (void)cancelAllRunningRequests
{
    [self.implementation cancelAllRunningRequests];
}

- (void)deallocate
{
    [self.implementation deallocate];
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
        (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeIoReactNativeHttpClientSpecJSI>(params);
}

+ (NSString *)moduleName
{
    return @"IoReactNativeHttpClient";
}

@end
