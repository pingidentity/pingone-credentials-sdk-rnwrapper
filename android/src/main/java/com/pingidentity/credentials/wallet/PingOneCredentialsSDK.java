package com.pingidentity.credentials.wallet;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.pingidentity.sdk.pingonewallet.types.regions.PingOneRegion;

public class PingOneCredentialsSDK extends ReactContextBaseJavaModule {

    private static final String ID_ROUTING_SERVICE_URL = "https://api.pingone.com/v1/";

    private PingOneWalletHelper pingOneWalletHelper = null;
    private final ReactApplicationContext reactApplicationContext;

    PingOneCredentialsSDK(ReactApplicationContext context) {
       super(context);
       this.reactApplicationContext = context;
   }

    @NonNull
    @Override
    public String getName() {
        return "PingOneCredentialsSDK";
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void initializeSDK(String region, Promise promise) {
        try {
            PingOneRegion pingOneRegion = getPingOneRegion(region);

            // in case initializeSDK is called multiple times.
            if (this.pingOneWalletHelper != null) {
                promise.resolve(pingOneWalletHelper.getApplicationInstanceId(pingOneRegion));
                return;
            }

            PingOneWalletHelper.initializeWallet(this.reactApplicationContext, pingOneRegion, helper -> {
                this.pingOneWalletHelper = helper;
                promise.resolve(helper.getApplicationInstanceId(pingOneRegion));
            }, throwable -> {
                promise.reject("Error initializing Wallet", throwable);
            });
        } catch (Exception e) {
            promise.reject("Error initializing Wallet", e);
        }
    }


    private PingOneRegion getPingOneRegion(String region) {
        return switch (region.toLowerCase()) {
            case "na" -> PingOneRegion.NA;
            case "eu" -> PingOneRegion.EU;
            case "ca" -> PingOneRegion.CA;
            case "apac" -> PingOneRegion.APAC;
            case "apac_2" -> PingOneRegion.APAC_2;
            default -> throw new RuntimeException("region " + region + " not supported");
        };
    }

    @ReactMethod(isBlockingSynchronousMethod = false)
    public void processRequest(String request, Promise promise) {
       if (this.pingOneWalletHelper == null) {
           promise.reject("PingOneWallet not initialized", (Throwable) null);
           return;
       }
       pingOneWalletHelper.processPingOneRequest(request, promise);
    }

    @ReactMethod(isBlockingSynchronousMethod = false)
    public void checkForMessages(Promise promise) {
        if (this.pingOneWalletHelper == null) {
            promise.reject("PingOneWallet not initialized", (Throwable) null);
            return;
        }
        pingOneWalletHelper.checkForMessages();
        promise.resolve("Message checking initiated");
    }

    @ReactMethod(isBlockingSynchronousMethod = false)
    public void getCredentialsList(Promise promise) {
        if (this.pingOneWalletHelper == null) {
            promise.reject("PingOneWallet not initialized", (Throwable) null);
            return;
        }
        pingOneWalletHelper.getCredentialsList(promise);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void deleteCredential(String credentialId) {
       this.pingOneWalletHelper.deleteCredential(credentialId);
    }

    @ReactMethod(isBlockingSynchronousMethod = false)
    public void readNotifications(Promise promise) {
        this.pingOneWalletHelper.readNotifications(promise);
    }
}
