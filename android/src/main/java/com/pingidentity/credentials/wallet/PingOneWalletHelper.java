package com.pingidentity.credentials.wallet;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodSession;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pingidentity.credentials.wallet.cache.SimpleQueue;
import com.pingidentity.did.sdk.client.service.NotFoundException;
import com.pingidentity.did.sdk.client.service.model.Challenge;
import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.did.sdk.types.ClaimReference;
import com.pingidentity.did.sdk.types.ExpirationSignature;
import com.pingidentity.did.sdk.types.SaltedData;
import com.pingidentity.did.sdk.types.Share;
import com.pingidentity.did.sdk.w3c.verifiableCredential.OpenUriAction;
import com.pingidentity.did.sdk.w3c.verifiableCredential.PresentationAction;
import com.pingidentity.sdk.pingonewallet.client.PingOneWalletClient;
import com.pingidentity.sdk.pingonewallet.contracts.WalletCallbackHandler;
import com.pingidentity.sdk.pingonewallet.errors.WalletException;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.types.CredentialMatcherResult;
import com.pingidentity.sdk.pingonewallet.types.CredentialsPresentation;
import com.pingidentity.sdk.pingonewallet.types.PairingRequest;
import com.pingidentity.sdk.pingonewallet.types.PresentationRequest;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletCredentialEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletError;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletPairingEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletMessage.credential.CredentialAction;
import com.pingidentity.sdk.pingonewallet.types.regions.PingOneRegion;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import com.pingidentity.credentials.wallet.storage.SimpleStorageProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class PingOneWalletHelper implements WalletCallbackHandler {

    public static final String TAG = PingOneWalletHelper.class.getCanonicalName();
    public static final String SDK_HANDLE_CREDENTIAL_ISSUANCE = "P1SDK_handleCredentialIssuance";
    public static final String SDK_HANDLE_CREDENTIAL_REVOCATION = "P1SDK_handleCredentialRevocation";
    public static final String SDK_HANDLE_PAIRING_REQUEST = "P1SDK_handlePairingRequest";
    public static final String SDK_PRESENT_CREDENTIAL = "P1SDK_presentCredential";
    public static final String SDK_HANDLE_PRESENTATION_ACTION = "P1SDK_handlePresentationAction";
    public static final String SDK_HANDLE_PAIRING_EVENT = "P1SDK_handlePairingEvent";
    public static final String SDK_HANDLE_ERROR = "P1SDK_handleErrorEvent";

    private final PingOneWalletClient pingOneWalletClient;

    private final SimpleQueue<String> messagesQueue = new SimpleQueue<>();

    private final Gson gson = new GsonBuilder().create();

    private Promise promise;

    private PingOneCredentialsSDK.EventCallback eventCallback;



    private PingOneWalletHelper(PingOneWalletClient client) {
        this.pingOneWalletClient = client;
        client.registerCallbackHandler(this);
    }

    public void setPackageName(Context context) {

    }

    public static void initializeWallet(Context context, PingOneRegion pingOneRegion, Consumer<PingOneWalletHelper> onResult, Consumer<Throwable> onError, PingOneCredentialsSDK.EventCallback callback) {
        new PingOneWalletClient.Builder(context, pingOneRegion)
                .setStorageManager(new SimpleStorageProvider(context))
                        .build(pingOneWalletClient -> {
                            PingOneWalletHelper helper = new PingOneWalletHelper(pingOneWalletClient);
                            helper.eventCallback = callback;
                            onResult.accept(helper);
                        }, onError);
    }

    /**
     * This method returns the data repository used by the wallet for storing ApplicationInstances and Credentials. See DataRepository for more details.
     *
     * @return DataRepository used by Wallet Instance
     * @see DataRepository
     */
    public DataRepository getDataRepository() {
        return pingOneWalletClient.getDataRepository();
    }

    /**
     * Call this method to process PingOne Credentials QR codes and Universal links.
     *
     * @param qrContent: Content of the scanned QR code or Universal link used to open the app
     */
    public void processPingOneRequest(@NonNull String qrContent, Promise promise) {
        this.promise = promise;
        pingOneWalletClient.processPingOneRequest(qrContent);
    }

    /**
     * Call this method when a credential is deleted from the Wallet. Reporting this action will help admins view accurate stats on their dashboards in future.
     *
     * @param claim: Deleted credential
     */
    public void reportCredentialDeletion(@NonNull Claim claim) {
        pingOneWalletClient.reportCredentialDeletion(claim);
    }

    /**
     * Call this method to check if wallet has received any new messages in the mailbox.
     * This method can be used to check for messages on user action or if push notifications are not available.
     */
    public void checkForMessages() {
        pingOneWalletClient.checkForMessages();
    }

    /**
     * Call this method to start polling for new messages sent to the wallet. Use this method only if you are not using push notifications.
     */
    public void pollForMessages() {
        pingOneWalletClient.pollForMessages();
    }

    /**
     * Call this method to stop polling for messages sent to the wallet.
     */
    public void stopPolling() {
        pingOneWalletClient.stopPolling();
    }


    /////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// WalletCallbackHandler Implementation /////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle the newly issued credential.
     *
     * @param issuer:    ApplicationInstanceID of the credential issuer
     * @param message:   Optional string message
     * @param challenge: Optional challenge
     * @param claim:     Issued credential
     * @param errors:    List of any errors while processing/verifying the credential
     * @return boolean: True if the user has accepted the credential, False if the user has rejected the credential
     */
    @Override
    public boolean handleCredentialIssuance(String issuer, String message, Challenge challenge, Claim claim, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialIssuance: Credential issued: Issuer: " + issuer + " message: " + (message == null ? "no message" : message));
        pingOneWalletClient.getDataRepository().saveCredential(claim);
        successMessage("Credential '" + getCardType(claim) + "' received. Saved to wallet", SDK_HANDLE_CREDENTIAL_ISSUANCE);
        return true;
    }

    /**
     * Handle the revocation of a credential.
     *
     * @param issuer:         ApplicationInstanceID of the credential issuer
     * @param message:        Optional string message
     * @param challenge:      Optional challenge
     * @param claimReference: ClaimReference for the revoked credential
     * @param errors:         List of any errors while revoking the credential
     * @return True if the user has accepted the credential revocation, False if the user has rejected the credential revocation
     */
    @Override
    public boolean handleCredentialRevocation(String issuer, String message, Challenge challenge, ClaimReference claimReference, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialRevocation: Credential revoked: Issuer: " + issuer + " message: " + (message == null ? "no message" : message));
        // pingOneWalletClient.getDataRepository().saveCredentialReference(claimReference);
        Claim claim = pingOneWalletClient.getDataRepository().getCredential(claimReference.getId().toString()); // TODO: fix this
        if (claim != null) {
            pingOneWalletClient.getDataRepository().deleteCredential(claim.getId().toString());
            successMessage("Credential '" + getCardType(claim) + "' revoked. Removed from wallet", SDK_HANDLE_CREDENTIAL_REVOCATION);
        }
        return true;
    }

    /**
     * This callback is triggered when another wallet shares a credential with the current application instance.
     *
     * @param sender:    ApplicationInstanceID of the sender
     * @param message:   Optional string message
     * @param challenge: Optional challenge
     * @param claim:     Shared credential
     * @param errors:    List of any errors while verifying the shared credential
     */
    @Override
    public void handleCredentialPresentation(String sender, String message, Challenge challenge, List<Share> claim, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialPresentation");
        errorMessage("Coming soon...");
    }

    /**
     * This callback is triggered when a credential is requested from the current wallet using supported protocols.
     *
     * @param presentationRequest PresentationRequest for presenting Credentials from wallet
     */
    @Override
    public void handleCredentialRequest(PresentationRequest presentationRequest) {
        if (presentationRequest.isPairingRequest()) {
            handlePairingRequest(presentationRequest);
            return;
        }

        List<Claim> allClaims = pingOneWalletClient.getDataRepository().getAllCredentials();
        List<CredentialMatcherResult> credentialMatcherResults = pingOneWalletClient.findMatchingCredentialsForRequest(presentationRequest, allClaims).getResult();
        List<CredentialMatcherResult> matchingCredentials = Collections.emptyList();
        if (credentialMatcherResults != null) {
            matchingCredentials = credentialMatcherResults.stream().filter(result -> !result.getClaims().isEmpty()).collect(Collectors.toList());
        }
        if (matchingCredentials.isEmpty()) {
            errorMessage("Cannot find any credentials in your wallet matching the criteria in the request");
            return;
        }

        CredentialsPresentation credentialsPresentation = new CredentialsPresentation(presentationRequest);
        for (CredentialMatcherResult matchingCredential : matchingCredentials) {
            if (matchingCredential.getClaims() != null && !matchingCredential.getClaims().isEmpty()) {
                credentialsPresentation.addClaimForKeys(matchingCredential.getRequestedKeys(), matchingCredential.getClaims().get(0));
            }
        }

        this.presentCredential(credentialsPresentation);
    }

    @Override
    public void handleError(WalletException error) {
        Log.i(TAG, "handleError");
        Log.e(TAG, "Exception in message processing", error);
        if (error.getCause() instanceof NotFoundException) {
            errorMessage("Failed to process request");
        }
    }

    /**
     * Callback returns different events when using Wallet, including errors
     * Backward compatibility - Call handleEvent() if you're still using `handleError` callback to manage exceptions
     *
     * @param event: WalletEvent
     */
    @Override
    public void handleEvent(WalletEvent event) {
        switch (event.getType()) {
            case PAIRING:
                handlePairingEvent((WalletPairingEvent) event);
                break;
            case CREDENTIAL_UPDATE:
                handleCredentialEvent((WalletCredentialEvent) event);
                break;
            case ERROR:
                handleErrorEvent((WalletError) event);
                break;
            default:
                Log.e(TAG, "Received unknown event: " + event.getType());
        }

    }

    private void handlePairingRequest(PairingRequest pairingRequest) {
        try {
            pingOneWalletClient.pairWallet(pairingRequest, null);
            successMessage("Wallet pairing response sent", SDK_HANDLE_PAIRING_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Failed to pair wallet", e);
            errorMessage("Failed to pair wallet: " + e.getMessage());
        }
    }

    private void handlePairingRequest(@NonNull PresentationRequest presentationRequest) {
        PairingRequest pairingRequest = presentationRequest.getPairingRequest();
        if (pairingRequest != null) {
            handlePairingRequest(pairingRequest);
        } else {
            Log.e(TAG, "Wallet pairing failed: Invalid request for pairing");
            errorMessage("Wallet pairing failed: Invalid request for pairing");
        }
    }

    private void shareCredentialPresentation(@NonNull CredentialsPresentation credentialsPresentation) {
        presentCredential(credentialsPresentation);
    }

    /**
     * @noinspection ResultOfMethodCallIgnored
     */
    private void presentCredential(@NonNull CredentialsPresentation credentialsPresentation) {
        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() ->
                pingOneWalletClient.presentCredentials(credentialsPresentation)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(presentationResult -> {
                            switch (presentationResult.getPresentationStatus().getStatus()) {
                                case SUCCESS:
                                    successMessage("Information sent successfully", SDK_PRESENT_CREDENTIAL);
                                    break;
                                case FAILURE:
                                    errorMessage("Failed to present credential");
                                    if (presentationResult.getError() != null) {
                                        Log.e(TAG, "Error sharing information: ", presentationResult.getError());
                                    }
                                    Log.e(TAG, String.format("Presentation failed. %s", presentationResult.getDetails()));
                                    break;
                                case REQUIRES_ACTION:
                                    handlePresentationAction(presentationResult.getPresentationStatus().getAction());
                            }
                        }));
    }

    /**
     * @noinspection SwitchStatementWithTooFewBranches
     */
    private void handlePresentationAction(PresentationAction action) {
        if (action == null) {
            return;
        }
        switch (action.getActionType()) {
            case OPEN_URI:
                OpenUriAction openUriAction = (OpenUriAction) action;
                String appOpenUri = openUriAction.getRedirectUri();
                this.successMessage(appOpenUri, SDK_HANDLE_PRESENTATION_ACTION);
        }
    }

    private void handlePairingEvent(WalletPairingEvent event) {
        Log.i(TAG, "Wallet paired: " + event.isSuccess());
        switch (event.getPairingEventType()) {
            case PAIRING_REQUEST:
                handlePairingRequest(event.getPairingRequest());
                break;
            case PAIRING_RESPONSE:
                if (event.isSuccess()) {
                    this.successMessage("Wallet paired successfully", SDK_HANDLE_PAIRING_EVENT);
                } else {
 
                        this.errorMessage("Failed to pair wallet: " + (event.getError() == null ? "" : event.getError()));
                        Log.e(TAG, "Wallet Pairing Error" + (event.getError() == null ? "" : event.getError()));
                }
        }
    }

    private void handleErrorEvent(WalletError errorEvent) {
        Log.e(TAG, "Error in wallet callback handler", errorEvent.getError());
    }

    /**
     * @noinspection SwitchStatementWithTooFewBranches
     */
    private void handleCredentialEvent(WalletCredentialEvent event) {
        switch (event.getCredentialEvent()) {
            case CREDENTIAL_UPDATED:
                handleCredentialUpdate(event.getAction(), event.getReferenceCredentialId());
        }
    }

    /**
     * @noinspection SwitchStatementWithTooFewBranches
     */
    private void handleCredentialUpdate(CredentialAction action, String referenceCredentialId) {
        switch (action) {
            case DELETE:
                pingOneWalletClient.getDataRepository().deleteCredential(referenceCredentialId);
        }
    }

    public String getApplicationInstanceId(PingOneRegion pingOneRegion) {
        return pingOneWalletClient.getApplicationInstance(pingOneRegion).getId().toString();
    }

    private void sendNotification(@NonNull String title, @NonNull String message, String eventName) {
        Map<String, String> messageObj = Map.of("title", title, "message", message);
        String messageStr = gson.toJson(messageObj);
        if (this.promise != null) {
            if (title.equalsIgnoreCase("success")) {
                this.promise.resolve(messageStr);
            } else {
                this.promise.reject("error", messageStr);
            }
            this.promise = null;
        } else {
            this.messagesQueue.offer(messageStr);
            WritableNativeMap payload = new WritableNativeMap();
            payload.putString("message", message);
            sendJSEvent(eventName, payload);
        }
    }

    private void successMessage(@NonNull String message, String eventName) {
        sendNotification("success", message, eventName);
    }

    private void sendJSEvent(String eventName, WritableNativeMap payload) {
        eventCallback.emitEvent(eventName, payload);
    }

    private void errorMessage(@NonNull String message) {
        sendNotification("error", message, SDK_HANDLE_ERROR);
    }

    public void getCredentialsList(Promise promise) {

        WritableNativeMap credentialList = new WritableNativeMap();

        for (Claim claim : this.pingOneWalletClient.getDataRepository().getAllCredentials()) {
            List<ExpirationSignatures> idExpiries = new ArrayList<>();
            if (claim.getIdExpiries() != null) {
                for (ExpirationSignature expiry : claim.getIdExpiries()) {
                    idExpiries.add(new ExpirationSignatures(expiry));
                }
            }
            WritableNativeMap unSaltedClaimData = new WritableNativeMap();
            for (Map.Entry<SaltedData, SaltedData> claimData : claim.getClaimData().entrySet()) {
                unSaltedClaimData.putString(CredentialType.getData(claimData.getKey()),CredentialType.getData(claimData.getValue()));
            }
            credentialList.putMap(claim.getId().toString(), CredentialType.createCredentialType(claim, idExpiries, unSaltedClaimData).toMap());
        }
        promise.resolve(credentialList);
    }

    public void deleteCredential(String credentialId) {
        this.pingOneWalletClient.getDataRepository().deleteCredential(credentialId);
    }

    private String getCardType(Claim claim) {
        for (SaltedData claimDataKey : claim.getClaimData().keySet()) {
            if (claimDataKey.getData().equalsIgnoreCase("CardType")) {
                return claim.getClaimData().get(claimDataKey).getData();
            }
        }
        return "Unknown";
    }

    public void readNotifications(Promise promise) {
        String message = messagesQueue.poll();
        if (message == null)
            promise.reject("empty", "no more messages");
        else
            promise.resolve(message);
    }
}
