package com.pingidentity.credentials.wallet;

import com.pingidentity.did.sdk.types.ExpirationSignature;

public class ExpirationSignatures {
    private String applicationInstanceId;
    private String hash;
    private String expiryTimestamp;
    private String expirySignature;

    public ExpirationSignatures(
            String applicationInstanceId,
            String hash,
            String expiryTimestamp,
            String expirySignature
    ) {
        this.applicationInstanceId = applicationInstanceId;
        this.hash = hash;
        this.expiryTimestamp = expiryTimestamp;
        this.expirySignature = expirySignature;
    }

    public ExpirationSignatures(ExpirationSignature expirationSignature){
        this(
                expirationSignature.getApplicationInstanceId().toString(),
                expirationSignature.getHash(),
                expirationSignature.getExpiryTimestamp(),
                expirationSignature.getExpirySignature()
        );
    }

    public String getApplicationInstanceId() {
        return applicationInstanceId;
    }

    public void setApplicationInstanceId(String applicationInstanceId) {
        this.applicationInstanceId = applicationInstanceId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(String expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }

    public String getExpirySignature() {
        return expirySignature;
    }

    public void setExpirySignature(String expirySignature) {
        this.expirySignature = expirySignature;
    }
}