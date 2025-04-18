// Type definitions for PingOneCredentialsSDK 0.1.0
import { NativeModules } from "react-native";

const { PingOneCredentialsSDK } = NativeModules;

export type CredentialTypeMap = { [key: string]: CredentialType };

type PingOneCredentialsSDKType = {
  initializeSDK(): Promise<string>;
  processRequest(request: string): Promise<string>;
  checkForMessages(): Promise<string>;
  getCredentialsList(): Promise<CredentialType>;
  deleteCredential(credentialId: string): Promise<string>;
};

export default PingOneCredentialsSDK as PingOneCredentialsSDKType;

export interface CredentialType {
  id: string;
  version: number;
  issuer: string;
  subject: string;
  holder: string;
  referenceClaimId: string;
  createDate: string;
  dataJson: string;
  dataSignature: string;
  dataHash: string;
  partitionId: string;
  idExpiries: ExpirationSignatures[];
  claimData: ClaimData;
}

export interface ExpirationSignatures {
  applicationInstanceId: string;
  hash: string;
  expiryTimestamp: string;
  expirySignature: string;
}

export type ClaimData = { [key: string]: string };
