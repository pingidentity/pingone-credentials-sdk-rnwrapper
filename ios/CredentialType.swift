import DIDSDK
import React

public class CredentialType {
    var id: String
    var version: Int
    var issuer: String
    var subject: String
    var holder: String
    var referenceClaimId: String
    var createDate: String
    var dataJson: String
    var dataSignature: String
    var dataHash: String
    var partitionId: String
    var idExpiries: [ExpirationSignatures]
    var claimData: [String:String]

    init(
        id: String,
        version: Int,
        issuer: String,
        subject: String,
        holder: String,
        referenceClaimId: String,
        createDate: String,
        dataJson: String,
        dataSignature: String,
        dataHash: String,
        partitionId: String,
        idExpiries: [ExpirationSignatures],
        claimData: [String:String]
    ) {
        self.id = id
        self.version = version
        self.issuer = issuer
        self.subject = subject
        self.holder = holder
        self.referenceClaimId = referenceClaimId
        self.createDate = createDate
        self.dataJson = dataJson
        self.dataSignature = dataSignature
        self.dataHash = dataHash
        self.partitionId = partitionId
        self.idExpiries = idExpiries
        self.claimData = claimData
    }
    convenience init(from claim: Claim, expirationSignatures: [ExpirationSignatures], claimData: [String:String]) {
        self.init(
            id: claim.getId(),
            version: Int(claim.getVersion()),
            issuer: claim.getIssuer().getData(),
            subject: claim.getSubject().getData(),
            holder: claim.getHolder().getData(),
            referenceClaimId: claim.getReferenceClaimId()?.getData() ?? "",
            createDate: claim.getCreateDate(),
            dataJson: claim.getDataJson(),
            dataSignature: claim.getDataSignature(),
            dataHash: claim.getDataHash(),
            partitionId: claim.getPartitionId() ?? "",
            idExpiries: expirationSignatures,
            claimData: claimData
        )
    }
    public func toDictionary() -> [String: Any] {
        var map: [String: Any] = [:]
        
        map["id"] = self.id
        map["version"] = self.version
        map["issuer"] = self.issuer
        map["subject"] = self.subject
        map["holder"] = self.holder
        map["referenceClaimId"] = self.referenceClaimId
        map["createDate"] = self.createDate
        map["dataJson"] = self.dataJson
        map["dataSignature"] = self.dataSignature
        map["dataHash"] = self.dataHash
        map["partitionId"] = self.partitionId
        
        // Convert idExpiries to an array of dictionaries
        let idExpiriesArray = self.idExpiries.map { expiry in
            return [
                "applicationInstanceId": expiry.applicationInstanceId,
                "hash": expiry.hash,
                "expiryTimestamp": expiry.expiryTimestamp,
                "expirySignature": expiry.expirySignature
            ]
        }
        map["idExpiries"] = idExpiriesArray
        
        // Add claimData as a dictionary
        map["claimData"] = self.claimData
        
        return map
    }
}
