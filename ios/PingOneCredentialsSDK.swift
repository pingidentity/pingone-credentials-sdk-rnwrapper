//
//  PingOneCredentialsSDK.swift
//  RNPingOneCredentialsSDK
//
//  Created by Gaurav Khot on 3/19/25.
//

import Foundation
import React
import PingOneWallet

@objc(PingOneCredentialsSDK)
class PingOneCredentialsSDK: RCTEventEmitter {
    
    private var pingOneWalletHelper: PingOneWalletHelper?
    
    
    override func supportedEvents() -> [String]! {
            return [
                "P1SDK_handleCredentialIssuance",
                "P1SDK_handleCredentialRevocation",
                "P1SDK_handlePairingRequest",
                "P1SDK_presentCredential",
                "P1SDK_handlePresentationAction",
                "P1SDK_handlePairingEvent",
                "P1SDK_handleErrorEvent"
            ]
        }
        
        private func emitEvent(eventName: String, map: [String: Any]) {
            if self.bridge != nil {
                self.sendEvent(withName: eventName, body: map)
            }
        }
    
    @objc(initializeSDK:resolver:rejecter:)
    func initializeSDK(_ region: String, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject:@escaping RCTPromiseRejectBlock) -> Void {
        guard let pingOneRegion = self.getPingOneRegion(region) else {
            reject("error", "region \(region) not supported", nil)
            return
        }
        
        if let pingOneWalletHelper = self.pingOneWalletHelper,
           let applicationInstanceId = pingOneWalletHelper.getApplicationInstanceId(region: pingOneRegion){
            resolve(applicationInstanceId)
            return
        }
        
        PingOneWalletClient.Builder(forRegion: pingOneRegion).build() // TODO: take region parameter
            .onError { error in
                reject("error", error.localizedDescription, error)
            }
            .onResult { client in
                self.pingOneWalletHelper = PingOneWalletHelper(client)
                self.pingOneWalletHelper?.eventCallback = { [weak self] eventName, payload in
                    self?.emitEvent(eventName: eventName, map: payload)
                }
                if let applicationInstanceId = client.getApplicationInstance(forRegion: pingOneRegion)?.getId() {
                    print("*********", applicationInstanceId)
                    resolve(applicationInstanceId)
                }
            }
    }
    
    private func getPingOneRegion(_ region: String) -> PingOneRegion? {
        switch region.lowercased() {
        case "na" : return .NA
        case "eu" : return .EU
        case "apac" : return .APAC
        case "apac_2": return .APAC_2
        case "ca" : return .CA
        default:
            return nil
        }
    }
    
    
    @objc(checkForMessages:rejecter:)
    func checkForMessages(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject:@escaping RCTPromiseRejectBlock) -> Void {
        guard let pingOneWalletHelper = self.pingOneWalletHelper else {
            reject("error", "PingOneWalletClient not initialized", nil)
            return
        }
        pingOneWalletHelper.checkForMessages()
        resolve("Message checking initiated")
    }
    
    @objc(processRequest:resolver:rejecter:)
    func processRequest(_ request: String, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        guard let pingOneWalletHelper = self.pingOneWalletHelper else {
            reject("error", "PingOneWalletClient not initialized", nil)
            return
        }
        
        pingOneWalletHelper.processPingOneRequest(request: request, resolver: resolve, rejector:  reject)
    }
    
    @objc
    func getCredentialsList(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        guard let pingOneWalletHelper = self.pingOneWalletHelper else {
            reject("error", "PingOneWalletClient not initialized", nil)
            return
        }
        
        pingOneWalletHelper.getCredentialsList(resolver: resolve)
    }
    
    @objc
    func deleteCredential(_ credentialId: String) -> Void {
        guard let pingOneWalletHelper = self.pingOneWalletHelper else {
            return
        }
        
        pingOneWalletHelper.deleteCredential(credentialId: credentialId)
    }
    
    @objc(readNotifications:rejecter:)
    func readNotifications(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        guard let pingOneWalletHelper = self.pingOneWalletHelper else {
            reject("error", "PingOneWalletClient not initialized", nil)
            return
        }
        
        pingOneWalletHelper.readNotifications(resolver: resolve, rejecter: reject)
    }
    
    @objc
    func constantsToExport() -> [String: Any]! {
        return ["PAIR_WALLET_APPROVE": "approve_pairing",
                "PAIR_WALLET_DENY": "deny_pairing",
                "PRESENTATION_REQUEST_APPROVE": "approve_presentation",
                "PRESENTATION_REQUEST_DENY": "deny_presentation",
                "PAIRING_COMPLETE": "pairing_complete",
                "PRESENTATION_COMPLETE": "presentation_complete"
        ]
    }
    
    @objc
    func enablePolling(){
        guard let pingOneWalletHelper = self.pingOneWalletHelper else {
            return
        }
        pingOneWalletHelper.pollForMessages()
    }
    @objc
    func stopPolling(){
        guard let pingOneWalletHelper = self.pingOneWalletHelper else {
            return
        }
        pingOneWalletHelper.stopPolling()
    }
    
    
}
