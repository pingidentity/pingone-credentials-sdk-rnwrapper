
package com.pingidentity.credentials.wallet;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.did.sdk.types.SaltedData;

import java.util.List;
import java.util.Map;

public class CredentialType {
    private String id;
    private int version;
    private String issuer;
    private String subject;
    private String holder;
    private String referenceClaimId;
    private String createDate;
    private String dataJson;
    private String dataSignature;
    private String dataHash;
    private String partitionId;
    private List<ExpirationSignatures> idExpiries;
    private ReadableMap claimData;

    public CredentialType(
        String id,
        int version,
        String issuer,
        String subject,
        String holder,
        String referenceClaimId,
        String createDate,
        String dataJson,
        String dataSignature,
        String dataHash,
        String partitionId,
        List<ExpirationSignatures> idExpiries,
        ReadableMap claimData
    ) {
        this.id = id;
        this.version = version;
        this.issuer = issuer;
        this.subject = subject;
        this.holder = holder;
        this.referenceClaimId = referenceClaimId;
        this.createDate = createDate;
        this.dataJson = dataJson;
        this.dataSignature = dataSignature;
        this.dataHash = dataHash;
        this.partitionId = partitionId;
        this.idExpiries = idExpiries;
        this.claimData = claimData;
    }
    public static CredentialType createCredentialType(Claim claim, List<ExpirationSignatures> expirationSignatures, ReadableMap claimData){
        return new CredentialType(claim.getId().toString(),
                claim.getVersion(),
                getData(claim.getIssuer()),
                getData(claim.getSubject()),
                getData(claim.getHolder()),
                getData(claim.getReferenceClaimId()),
                claim.getCreateDate().toString(),
                claim.getDataJson(),
                claim.getDataSignature(),
                claim.getDataHash(),
                claim.getPartitionId(),
                expirationSignatures,
                claimData);
    }

    public ReadableMap toMap(){
        WritableNativeMap map = new WritableNativeMap();
        map.putString("id", this.id);
        map.putInt("version", this.version);
        map.putString("issuer", this.issuer);
        map.putString("subject", this.subject);
        map.putString("holder", this.holder);
        map.putString("referenceClaimId", this.referenceClaimId);
        map.putString("createDate", this.createDate);
        map.putString("dataJson", this.dataJson);
        map.putString("dataSignature", this.dataSignature);
        map.putString("dataHash", this.dataHash);
        map.putString("partitionId", this.partitionId);
        // Convert idExpiries to a WritableArray
        WritableNativeArray idExpiriesArray = new WritableNativeArray();
        for (ExpirationSignatures expiry : this.idExpiries) {
            WritableNativeMap expiryMap = new WritableNativeMap();
            expiryMap.putString("applicationInstanceId", expiry.getApplicationInstanceId());
            expiryMap.putString("hash", expiry.getHash());
            expiryMap.putString("expiryTimestamp", expiry.getExpiryTimestamp());
            expiryMap.putString("expirySignature", expiry.getExpirySignature());
            idExpiriesArray.pushMap(expiryMap);
        }
        map.putArray("idExpiries", idExpiriesArray);
        map.putMap("claimData", claimData);
        return map;
    }
    public static String getData(SaltedData saltedData){
        return saltedData == null ? null : saltedData.getData() ;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public String getReferenceClaimId() {
        return referenceClaimId;
    }

    public void setReferenceClaimId(String referenceClaimId) {
        this.referenceClaimId = referenceClaimId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public String getDataSignature() {
        return dataSignature;
    }

    public void setDataSignature(String dataSignature) {
        this.dataSignature = dataSignature;
    }

    public String getDataHash() {
        return dataHash;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public List<ExpirationSignatures> getIdExpiries() {
        return idExpiries;
    }

    public void setIdExpiries(List<ExpirationSignatures> idExpiries) {
        this.idExpiries = idExpiries;
    }
}