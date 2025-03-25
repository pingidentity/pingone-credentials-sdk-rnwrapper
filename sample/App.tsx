import 'react-native-gesture-handler'; // Must be at the top
import React from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createStackNavigator} from '@react-navigation/stack';
import HomeScreen from './HomeScreen';
import QRCodeScannerScreen from './QRCodeScannerScreen';

type RootStackParamList = {
  Home: undefined;
  QRCodeScanner: {onScanComplete: (scannedCode: string) => void};
};

const Stack = createStackNavigator<RootStackParamList>();

const App: React.FC = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          presentation: 'modal', // Makes the scanner a popup
          headerShown: false, // Hides header for cleaner UI
        }}>
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen
          name="QRCodeScanner"
          component={({route, navigation}: {route: any; navigation: any}) => (
            <QRCodeScannerScreen
              navigation={navigation}
              onScanComplete={route.params?.onScanComplete}
            />
          )}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;
