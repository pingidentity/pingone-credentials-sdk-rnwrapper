//
//  RNPingOneCredentialsSDK.m
//  RNPingOneCredentialsSDK
//
//  Created by Gaurav Khot on 3/19/25.
//

#import <React/RCTBridgeModule.h>

// To export a module named RCTNeoWallet
@interface RCT_EXTERN_MODULE(PingOneCredentialsSDK, NSObject)

RCT_EXTERN_METHOD(initializeSDK: (NSString *) region resolver: (RCTPromiseResolveBlock) resolve rejecter: (RCTPromiseRejectBlock) reject);
RCT_EXTERN_METHOD(processRequest: (NSString *) request resolver: (RCTPromiseResolveBlock) resolve rejecter: (RCTPromiseRejectBlock) reject);
RCT_EXTERN_METHOD(checkForMessages: (RCTPromiseResolveBlock) resolve rejecter: (RCTPromiseRejectBlock) reject);
RCT_EXTERN_METHOD(getCredentialsList: (RCTPromiseResolveBlock) resolve rejecter: (RCTPromiseRejectBlock) reject);
RCT_EXTERN_METHOD(deleteCredential: (NSString *) credentialId);
RCT_EXTERN_METHOD(readNotifications: (RCTPromiseResolveBlock) resolve rejecter: (RCTPromiseRejectBlock) reject)

@end
