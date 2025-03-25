package com.pingidentity.credentials.wallet.storage;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pingidentity.did.sdk.client.service.model.ApplicationInstance;
import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.did.sdk.types.ClaimReference;
import com.pingidentity.sdk.pingonewallet.contracts.StorageManagerContract;
import com.pingidentity.sdk.pingonewallet.storage.StorageErrorHandler;
import com.pingidentity.sdk.pingonewallet.types.regions.PingOneRegion;

import org.jose4j.jwk.JsonWebKey;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleStorageProvider implements StorageManagerContract {

    private static final String TAG = SimpleStorageProvider.class.getCanonicalName();
    private static final String APPLICATION_INSTANCE_MAP = "ApplicationInstances";
    private static final String CLAIMS_MAP = "Claims";
    private static final String CLAIM_REFERENCES_MAP = "ClaimReferences";

    private static final Type applicationInstancesMapType = new TypeToken<Map<String, ApplicationInstance>>() {}.getType();
    private static final Type claimsMapType  = new TypeToken<Map<String, Claim>>() {}.getType();
    private static final Type claimReferencesMapType = new TypeToken<Map<String, ClaimReference>>() {}.getType();


    private final StorageErrorHandler storageErrorHandler;
    private Map<String, ApplicationInstance> applicationInstanceMap = null;
    private Map<String, Claim> claimsMap = null;
    private Map<String, ClaimReference> claimReferencesMap = null;

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SimpleStorageProvider(Context context) {
        this.storageErrorHandler = new SimpleStorageErrorHandler();
        this.sharedPreferences = context.getSharedPreferences("NEOWALLET", MODE_PRIVATE);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ApplicationInstance.class, new ApplicationInstanceDeserializer())
                .registerTypeAdapter(Claim.class, new ClaimDeserializer())
                .create();
    }

    @Override
    public StorageErrorHandler getStorageErrorHandler() {
        return storageErrorHandler;
    }

    @Override
    public void setStorageErrorHandler(StorageErrorHandler storageErrorHandler) {
        // this.storageErrorHandler = storageErrorHandler;
    }

    @Override
    public void saveApplicationInstance(PingOneRegion pingOneRegion, ApplicationInstance applicationInstance) {
        Log.d(TAG, "Storing applicationInstance with id (" + applicationInstance.getId() + ") for region " + pingOneRegion.toString());
        this.loadApplicationInstancesMap();
        this.applicationInstanceMap.put(pingOneRegion.toString(), applicationInstance);
        this.saveApplicationInstancesMap();
    }

    @Override
    public ApplicationInstance getApplicationInstance() {
        return null;
    }

    @Override
    public ApplicationInstance getApplicationInstance(PingOneRegion pingOneRegion) {
        this.loadApplicationInstancesMap();
        return this.applicationInstanceMap.get(pingOneRegion.toString());
    }

    @Override
    public void saveClaim(Claim claim) {
        this.loadClaimMap();
        this.claimsMap.put(claim.getId().toString(), claim);
        this.saveClaimMap();
    }

    @Override
    public Claim getClaim(String claimId) {
        this.loadClaimMap();
        return this.claimsMap.get(claimId);
    }

    @Override
    public void saveClaimReference(ClaimReference claimReference) {
        this.loadClaimReferenceMap();
        this.claimReferencesMap.put(claimReference.getId().toString(), claimReference);
        this.saveClaimReferencesMap();
    }

    @Override
    public ClaimReference getClaimReference(String claimId) {
        this.loadClaimReferenceMap();
        return this.claimReferencesMap.get(claimId);
    }

    @Override
    public void deleteClaim(String claimId) {
        this.loadClaimMap();
        this.claimsMap.remove(claimId);
        this.saveClaimMap();
    }

    @Override
    public void saveString(String s, String s1) {
        this.sharedPreferences.edit().putString(s, s1).apply();
    }

    @Override
    public String getString(String s) {
        return this.sharedPreferences.getString(s, null);
    }

    @Override
    public void saveStringSet(Set<String> set, String s) {
        this.sharedPreferences.edit().putStringSet(s, set).apply();
    }

    @Override
    public Set<String> getStringSet(String s) {
        return this.sharedPreferences.getStringSet(s, new HashSet<>());
    }

    private void loadApplicationInstancesMap() {
        if (this.applicationInstanceMap == null) {
            String storedMap = this.sharedPreferences.getString(APPLICATION_INSTANCE_MAP, "{}");
            this.applicationInstanceMap = gson.fromJson(storedMap, applicationInstancesMapType);
            if (this.applicationInstanceMap == null) {
                this.applicationInstanceMap = new HashMap<>();
            }
        }
    }

    private void saveApplicationInstancesMap() {
        SharedPreferences.Editor preferencesEditor = this.sharedPreferences.edit();
        preferencesEditor.putString(APPLICATION_INSTANCE_MAP, gson.toJson(applicationInstanceMap));
        preferencesEditor.apply();
    }

    private void loadClaimMap() {
        if (this.claimsMap == null) {
            String storedMap = this.sharedPreferences.getString(CLAIMS_MAP, "");
            this.claimsMap = gson.fromJson(storedMap, claimsMapType);
            if (this.claimsMap == null) {
                this.claimsMap = new HashMap<>();
            }
        }
    }

    private void saveClaimMap() {
        SharedPreferences.Editor preferencesEditor = this.sharedPreferences.edit();
        preferencesEditor.putString(CLAIMS_MAP, gson.toJson(this.claimsMap));
        preferencesEditor.apply();
    }

    private void loadClaimReferenceMap() {
        if (this.claimReferencesMap == null) {
            String storedMap = this.sharedPreferences.getString(CLAIM_REFERENCES_MAP, "");
            this.claimReferencesMap = gson.fromJson(storedMap, claimReferencesMapType);
            if (this.claimReferencesMap == null) {
                this.claimReferencesMap = new HashMap<>();
            }

        }
    }

    private void saveClaimReferencesMap() {
        SharedPreferences.Editor preferencesEditor = this.sharedPreferences.edit();
        preferencesEditor.putString(CLAIM_REFERENCES_MAP, gson.toJson(this.claimReferencesMap));
        preferencesEditor.apply();
    }
}
