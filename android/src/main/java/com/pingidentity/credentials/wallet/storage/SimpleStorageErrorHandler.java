package com.pingidentity.credentials.wallet.storage;

import android.util.Log;

import com.pingidentity.sdk.pingonewallet.errors.StorageError;
import com.pingidentity.sdk.pingonewallet.storage.StorageErrorHandler;

public class SimpleStorageErrorHandler implements StorageErrorHandler {
    @Override
    public void handleStorageError(StorageError storageError) {
        Log.e(this.getClass().getCanonicalName(), storageError.getMessage());
    }
}
