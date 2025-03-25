// Type definitions for PingOneCredentialsSDK 0.1.0
import { NativeModules } from "react-native";

const { PingOneCredentialsSDK } = NativeModules;

type CredentialType = { [key: string]: string };

type PingOneCredentialsSDKType = {
  initializeSDK(): Promise<string>;
  processRequest(request: string): Promise<string>;
  checkForMessages(): Promise<string>;
  getCredentialsList(): Promise<CredentialType>;
  deleteCredential(credentialId: string): Promise<string>;
};

export default PingOneCredentialsSDK as PingOneCredentialsSDKType;
