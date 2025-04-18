//
//  PingOneWalletHelper.swift
//  reactWallet
//
//  Created by Gaurav Khot on 3/4/25.
//

import Foundation
import PingOneWallet
import DIDSDK
import React

public class PingOneWalletHelper : WalletCallbackHandler {
    
    private var pingOneWalletClient: PingOneWalletClient!
    
    private var resolver: RCTPromiseResolveBlock?
    private var rejecter: RCTPromiseRejectBlock?
    
    private var notifications: Queue<String> = Queue<String>()
    
    
    init(_ pingOneWalletClient: PingOneWalletClient) {
        self.pingOneWalletClient = pingOneWalletClient
        self.pingOneWalletClient.registerCallbackHandler(self)
    }
    
    public func handleCredentialIssuance(issuer: String, message: String?, challenge: DIDSDK.Challenge?, claim: DIDSDK.Claim, errors: [PingOneWallet.WalletException]) -> Bool {
        print("handleCredentialIssuance: Got credential issued from \(issuer) with message: \(message ?? "no message")")
        self.successMessage(message: "Credential \(self.getCardType(claim)) received. Saved to wallet")
        return true
    }
    
    public func handleCredentialRevocation(issuer: String, message: String?, challenge: DIDSDK.Challenge?, claimReference: DIDSDK.ClaimReference, errors: [PingOneWallet.WalletException]) -> Bool {
        print("Credential revoked: Issuer: \(issuer) message: \(message ?? "no message")")
        if let claim = self.pingOneWalletClient.getDataRepository().getCredential(for: claimReference.getId()) {
            self.pingOneWalletClient.getDataRepository().deleteCredential(forId: claim.getId())
            self.successMessage(message: "Credential \(self.getCardType(claim)) revoked. Removed from wallet")
        }
        return true
    }
    
    public func handleCredentialPresentation(sender: String, message: String?, challenge: DIDSDK.Challenge?, claim: [DIDSDK.Share], errors: [PingOneWallet.WalletException]) {
        errorMessage(message: "Coming soon...")
        return
    }
    
    public func handleCredentialRequest(_ presentationRequest: PingOneWallet.PresentationRequest) {
        if (presentationRequest.isPairingRequest()) {
            self.handlePairingRequest(presentationRequest)
            return
        }
        
        let credentialMatcherResults = self.pingOneWalletClient.findMatchingCredentialsForRequest(presentationRequest).getResult()
        let matchingCredentials = credentialMatcherResults.filter { !$0.claims.isEmpty }
        
        guard !matchingCredentials.isEmpty else {
            errorMessage(message: "Cannot find any credentials in your wallet matching the criteria in the request")
            return
        }
        
        let result = CredentialsPresentation(presentationRequest: presentationRequest)
        for credential in credentialMatcherResults {
            if !credential.claims.isEmpty {
                result.addClaimForKeys(credential.claims[0], keys: credential.requestedKeys)
            }
        }
        
        //    let message: String = matchingCredentials.count == credentialMatcherResults.count ? "Please confirm to present the requested credentials from your wallet." : "You wallet does not have all the requested credentials. Would you like to share partial information?"
        //    let title: String = matchingCredentials.count == credentialMatcherResults.count ? "Share Information" : "Missing Credentials"
        //
        //    self.askUserPermission(title: title , message: message) { isPositiveAction in
        //        if (isPositiveAction) {
        self.presentCredential(result)
        //        } else {
        //            self.notifyUser(message: "Presentation canceled")
        //        }
        //    }
        return
    }
    
    private func presentCredential(_ credentialPresentation: CredentialsPresentation) {
        self.pingOneWalletClient.presentCredentials(credentialPresentation)
            .onResult { result in
                switch result.getPresentationStatus() {
                case .success:
                    if let resolver = self.resolver {
                        resolver("Information sent successfully")
                        self.resolver = nil
                    }
                case .failure:
                    logerror("Error sharing information: \(result.getDetails()?.debugDescription ?? "None")")
                    self.errorMessage(message: "Failed to present credential")
                default:
                    print("No expected")
                }
            }
            .onError { err in
                logerror("Error sharing information: \(err.localizedDescription)")
                self.errorMessage(message: "Failed to present Credential")
            }
    }
    
    
    private func handlePairingRequest(_ presentationRequest: PresentationRequest) {
        guard let pairingRequest = presentationRequest.getPairingRequest() else {
            logerror("Wallet pairing failed: Invalid request for pairing")
            //          self.notifyUser(message: "Wallet pairing failed")
            return
        }
        self.handlePairingRequest(pairingRequest)
    }
    
    private func handlePairingRequest(_ pairingRequest: PairingRequest) {
        //      self.askUserPermission(title: "Pair Wallet", message: "Please confirm to pair your wallet to receive a credential.") { isPositiveAction in
        //          guard (isPositiveAction) else {
        //              logattention("Pairing canceled by user")
        //              return
        //          }
        self.pingOneWalletClient.pairWallet(for: pairingRequest)
            .onResult({ _ in
                if let resolver = self.resolver {
                    resolver("Wallet Pairing Succeeded")
                    self.resolver = nil
                }
                //                  self.notifyUser(message: "Pairing wallet...")
                //                  self.pollForMessages()
            })
            .onError { err in
                logerror("Wallet pairing failed: \(err.localizedDescription)")
                //                  self.notifyUser(message: "Wallet pairing failed")
            }
        //      }
    }
    
    public func handleEvent(_ event: any PingOneWallet.WalletEvent) {
        switch event {
        case let event as WalletPairingEvent:
            self.handlePairingEvent(event)
            //    case let event as WalletCredentialEvent:
            //      self.handleCredentialEvent(event)
            //    case let event as WalletError:
            //      self.handleErrorEvent(event)
        default:
            logattention("Received unknown event. \(event.getType())")
        }
    }
    
    private func handlePairingEvent(_ event: WalletPairingEvent) {
        switch event.getPairingEventType() {
        case .PAIRING_REQUEST:
            self.handlePairingRequest(event.getPairingRequest())
        case .PAIRING_RESPONSE:
            //          if (!self.isPollingEnabled()) { //Poll only until Push notification token gets registered
            //              self.stopPolling()
            //          }
            logattention("Wallet paired: \(String(describing: event.isSuccess())) - error: \(event.getError()?.localizedDescription ?? "None")")
            if let isSuccess = event.isSuccess() {
                if let resolver = resolver {
                    resolver(isSuccess ? "Wallet paired successfully" : "Wallet pairing failed")
                    self.resolver = nil
                }
            }
        }
    }
}

extension PingOneWalletHelper {
    public func checkForMessages() {
        self.pingOneWalletClient.checkForMessages()
    }
    
    public func processPingOneRequest(request: String, resolver: @escaping RCTPromiseResolveBlock, rejector: @escaping RCTPromiseRejectBlock) {
        self.resolver = resolver
        self.rejecter = rejector
        self.pingOneWalletClient.processPingOneRequest(request)
    }
    
    public func getApplicationInstanceId(region: PingOneRegion) -> String? {
        return self.pingOneWalletClient.getApplicationInstance(forRegion: region)?.getId()
    }
}

extension PingOneWalletHelper {
    public func getCredentialsList(resolver: @escaping RCTPromiseResolveBlock) {
        var credentialList:[String: [String : Any]] = [:]
        for claim in self.pingOneWalletClient.getDataRepository().getAllCredentials() {
            var idExpiries:[ExpirationSignatures] = []
            for expiry in claim.getIdExpiries() ?? [] {
                idExpiries.append(ExpirationSignatures(expiry: expiry))
            }
            var unSaltedClaimData:[String:String] = [:]
            for claimData in claim.getClaimData() {
                unSaltedClaimData[claimData.key.getData()] = claimData.value.getData()
            }
            credentialList[claim.getId()] = CredentialType(from: claim, expirationSignatures: idExpiries, claimData: unSaltedClaimData).toDictionary()
        }
        resolver(credentialList)
        self.resolver = nil
    }
    
    public func deleteCredential(credentialId: String) {
        self.pingOneWalletClient.getDataRepository().deleteCredential(forId: credentialId)
    }
    
    public func readNotifications(resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        if let message = notifications.dequeue() {
            resolve(message)
        } else {
            reject("empty", "no more messages", nil)
        }
    }
    
    private func getCardType(_ claim: DIDSDK.Claim) -> String {
        for claimData in claim.getClaimData() {
            if claimData.key.getData() == "CardType" {
                return claimData.value.getData()
            }
        }
        return "Unknown"
    }
    
    private func createMessagePayload(from map: [String: Any]) -> String? {
        // Step 2: Serialize the dictionary to JSON
        if let jsonData = try? JSONSerialization.data(withJSONObject: map, options: .prettyPrinted),
           let jsonString = String(data: jsonData, encoding: .utf8) {
            return jsonString
        } else {
            return nil
        }
    }
    
    
    private func sendNotification(title: String, message: String) {
        let map: [String: Any] = [
            "title": title,
            "message": message
        ]
        if let payloadString = createMessagePayload(from: map) {
            print("Sending notification: \(payloadString)")
            if (title == "success") {
                if let resolver = self.resolver {
                    resolver(payloadString)
                    self.resolver = nil
                } else {
                    self.notifications.enqueue(payloadString)
                }
            } else {
                if let rejecter = self.rejecter {
                    rejecter("error", payloadString, nil)
                    self.rejecter = nil
                } else {
                    self.notifications.enqueue(payloadString)
                }
            }
        }
    }
    
    private func successMessage(message: String) {
        self.sendNotification(title: "success", message: message)
    }
    
    private func errorMessage(message: String) {
        self.sendNotification(title: "error", message: message)
    }
}
