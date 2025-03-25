import React, {useEffect, useRef} from 'react';
import {View, Text, StyleSheet, TouchableOpacity, Alert} from 'react-native';
import {
  Camera,
  useCameraDevices,
  useCodeScanner,
  CameraDevice,
} from 'react-native-vision-camera';
import {useCameraPermission} from './useCameraPermission';

interface Props {
  navigation: any;
  onScanComplete?: (value: string) => void;
}

const QRCodeScannerScreen: React.FC<Props> = ({navigation, onScanComplete}) => {
  const hasPermission = useCameraPermission();
  const camera = useRef<Camera>(null);
  const devices = useCameraDevices();
  const device: CameraDevice | undefined = devices.find(
    d => d.position === 'back',
  );

  const isScanningRef = useRef(false);

  useEffect(() => {
    if (hasPermission === false) {
      Alert.alert(
        'Permission Denied',
        'You need to allow camera access in settings.',
      );
    }
  }, [hasPermission]);

  const codeScanner = useCodeScanner({
    codeTypes: ['qr'],
    onCodeScanned: codes => {
      console.log('Scanned codes count: ', codes.length);
      if (codes.length > 0 && !isScanningRef.current) {
        isScanningRef.current = true;
        console.log('Scanned code: ', codes[0].value);
        if (onScanComplete && codes[0].value) {
          onScanComplete(codes[0].value);
          navigation.goBack();
        }
      }
    },
  });

  if (hasPermission === null) {
    return (
      <View style={styles.centered}>
        <Text>Checking camera permissions...</Text>
      </View>
    );
  }

  if (!hasPermission) {
    return (
      <View style={styles.centered}>
        <Text>No access to camera</Text>
      </View>
    );
  }

  if (!device) {
    return (
      <View style={styles.centered}>
        <Text>No back camera found</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Camera
        ref={camera}
        style={StyleSheet.absoluteFill}
        device={device}
        isActive={true}
        codeScanner={codeScanner}
      />
      <View style={styles.overlay}>
        <Text style={styles.instruction}>Scan a QR Code</Text>
        <TouchableOpacity
          style={styles.button}
          onPress={() => navigation.goBack()}>
          <Text style={styles.buttonText}>Close Scanner</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'black',
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  overlay: {
    position: 'absolute',
    bottom: 50,
    alignItems: 'center',
  },
  instruction: {
    fontSize: 18,
    color: '#fff',
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#ff4757',
    padding: 12,
    borderRadius: 5,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
  },
});

export default QRCodeScannerScreen;
