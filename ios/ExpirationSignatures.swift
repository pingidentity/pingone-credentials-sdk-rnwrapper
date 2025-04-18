import DIDSDK

public class ExpirationSignatures {
    var applicationInstanceId: String
    var hash: String
    var expiryTimestamp: String
    var expirySignature: String

    init(
        applicationInstanceId: String,
        hash: String,
        expiryTimestamp: String,
        expirySignature: String
    ) {
        self.applicationInstanceId = applicationInstanceId
        self.hash = hash
        self.expiryTimestamp = expiryTimestamp
        self.expirySignature = expirySignature
    }
    convenience init(expiry: ExpirationSignature) {
        self.init(
            applicationInstanceId: expiry.getApplicationInstanceId() ?? "",
            hash: expiry.getHash(),
            expiryTimestamp: expiry.getExpiryTimestamp(),
            expirySignature: expiry.getExpirySignature()
        )
    }
}

