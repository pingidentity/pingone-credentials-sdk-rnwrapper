import {useEffect, useState} from 'react';
import {Alert, Platform} from 'react-native';
import {check, request, PERMISSIONS, RESULTS} from 'react-native-permissions';

export const useCameraPermission = () => {
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);

  useEffect(() => {
    checkCameraPermission();
  }, []);

  const checkCameraPermission = async () => {
    const permission =
      Platform.OS === 'ios'
        ? PERMISSIONS.IOS.CAMERA
        : PERMISSIONS.ANDROID.CAMERA;

    const result = await check(permission);

    if (result === RESULTS.GRANTED) {
      setHasPermission(true);
    } else {
      requestCameraPermission();
    }
  };

  const requestCameraPermission = async () => {
    const permission =
      Platform.OS === 'ios'
        ? PERMISSIONS.IOS.CAMERA
        : PERMISSIONS.ANDROID.CAMERA;

    const result = await request(permission);

    if (result === RESULTS.GRANTED) {
      setHasPermission(true);
    } else {
      setHasPermission(false);
      Alert.alert(
        'Permission Denied',
        'Camera access is required for scanning QR codes.',
      );
    }
  };

  return hasPermission;
};
