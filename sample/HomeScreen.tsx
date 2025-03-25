import React, {useEffect, useState, useMemo} from 'react';
import {
  View,
  Text,
  Platform,
  StatusBar,
  SafeAreaView,
  FlatList,
  TouchableOpacity,
  Modal,
} from 'react-native';
import {StackNavigationProp} from '@react-navigation/stack';
import Icon from 'react-native-vector-icons/Ionicons'; // Import icon library
import Toast, {ToastProps} from 'react-native-toast-message';
import CustomToast from './CustomToast';

const toastConfig = {
  success: (props: ToastProps) => <CustomToast {...props} />,
};

import {NativeModules} from 'react-native';

const {PingOneCredentialsSDK} = NativeModules;

type RootStackParamList = {
  Home: undefined;
  QRCodeScanner: {onScanComplete: (scannedCode: string) => void};
};

type HomeScreenNavigationProp = StackNavigationProp<RootStackParamList, 'Home'>;

interface Props {
  navigation: HomeScreenNavigationProp;
}

interface Item {
  id: number;
  message: string;
}

type CredentialType = {[key: string]: string};

const HomeScreen: React.FC<Props> = ({navigation}) => {
  const [applicationInstanceId, setApplicationInstanceId] = useState<
    string | null
  >('Initializing...');
  const [messages, setMessages] = useState<Item[]>([]);
  const [credentials, setCredentials] = useState<CredentialType>({});
  const [messagesVisible, setMessagesVisible] = useState(false);

  // Convert dictionary into an array of objects [{ key, message }]
  const credentialsArray = useMemo(() =>
    Object.keys(credentials).map(key => ({
      key,
      message: credentials[key],
    })),
  );

  const appendToMessage = (newMessage: string) => {
    let messageBody;
    let messageTitle;
    // json parse the message to check if it is a json object
    try {
      let parsedMessage = JSON.parse(newMessage);
      console.log('Parsed Message', parsedMessage);
      if (parsedMessage) {
        messageBody = parsedMessage.message;
        messageTitle = parsedMessage.title;
      } else {
        messageBody = newMessage;
        messageTitle = 'Message Received';
      }
    } catch (e) {
      messageBody = newMessage;
      messageTitle = 'Message Received';
    }

    const newMessageItem = {
      id: messages.length,
      title: messageTitle,
      message: messageBody,
    };

    Toast.show({
      type: 'success', // success, error, info
      text1: messageTitle,
      text2: messageBody,
      visibilityTime: 3000,
      position: 'bottom',
      bottomOffset: 40,
      autoHide: true,
    });

    setMessages(prev => {
      return [...prev, newMessageItem];
    });
  };

  // Function to delete a message
  const handleDeleteMessage = async (id: string) => {
    console.log('Deleting message with id: ', id);
    await PingOneCredentialsSDK.deleteCredential(id);
    refreshCredentialsList();
  };

  useEffect(() => {
    const fetchResult = async () => {
      try {
        let _applicationInstanceId = await PingOneCredentialsSDK.initializeSDK(
          'na',
        );
        console.log(`Application Instance ID: ${_applicationInstanceId}`);
        setApplicationInstanceId(_applicationInstanceId);
        refreshCredentialsList();
      } catch (e) {
        console.error(e);
      }
    };

    fetchResult();
  }, []);

  const handleScanComplete = async (scannedCode: string) => {
    let response = await PingOneCredentialsSDK.processRequest(scannedCode);
    appendToMessage(response);
    console.log(response);
    checkForMessages();
  };

  const checkForMessages = async () => {
    await PingOneCredentialsSDK.checkForMessages();
    readAllNotifications();
    refreshCredentialsList();
  };

  const readAllNotifications = async () => {
    while (1) {
      try {
        let message = await PingOneCredentialsSDK.readNotifications();
        appendToMessage(message);
      } catch (e) {
        console.log('No more messages');
        break;
      }
    }
  };

  const refreshCredentialsList = async () => {
    let _credentials = await PingOneCredentialsSDK.getCredentialsList();
    console.log('Credentials', _credentials);
    setCredentials(_credentials);
  };

  return (
    <SafeAreaView style={styles.safeContainer}>
      <Text style={styles.title}>PingOne Credentials Wallet</Text>
      <Text style={{marginBottom: 20, paddingLeft: 20}}>
        Application Instance ID: {'\n'} {applicationInstanceId}
      </Text>

      {/* Scrollable Messages */}
      <View style={styles.credentialsContainer}>
        <FlatList
          data={credentialsArray}
          keyExtractor={item => item.key}
          renderItem={({item}) => (
            <View style={styles.messageItem}>
              <Text style={styles.messageText}>{item.message}</Text>
              <TouchableOpacity
                onPress={() => handleDeleteMessage(item.key)}
                style={styles.deleteButton}>
                <Text style={styles.deleteButtonText}>Delete</Text>
              </TouchableOpacity>
            </View>
          )}
          extraData={credentialsArray}
        />
      </View>

      {/* Buttons */}
      <View style={styles.buttonContainer}>
        <TouchableOpacity
          style={styles.button}
          onPress={() =>
            navigation.navigate('QRCodeScanner', {
              onScanComplete: handleScanComplete,
            })
          }>
          <Icon name="qr-code" size={28} color="white" />
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={checkForMessages}>
          <Icon name="refresh" size={28} color="white" />
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => setMessagesVisible(true)}>
          <Icon name="chatbubbles-outline" size={28} color="white" />
        </TouchableOpacity>
      </View>

      {/* Message Modal */}
      {/* Messages Button */}

      {/* Messages Popup Modal */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={messagesVisible}
        onRequestClose={() => setMessagesVisible(false)}>
        <View style={styles.modalContainer}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Messages</Text>

            <FlatList
              data={messages}
              keyExtractor={(item, index) => index.toString()}
              renderItem={({item}) => (
                <Text style={styles.messageText}>{item.message}</Text>
              )}
            />

            {/* Close Button */}
            <TouchableOpacity
              style={styles.closeButton}
              onPress={() => setMessagesVisible(false)}>
              <Text style={styles.closeButtonText}>Close</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
      <Toast config={toastConfig} />
    </SafeAreaView>
  );
};

const styles = {
  safeContainer: {
    flex: 1,
    backgroundColor: '#fff',
    paddingTop: Platform.OS === 'ios' ? 50 : StatusBar.currentHeight, // Fixes notch issue
    paddingHorizontal: 20,
    paddingBottom: 20,
  },
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#fff',
    justifyContent: 'center',
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 10,
  },
  messagesContainer: {
    flex: 1,
    backgroundColor: 'aqua',
    padding: 10,
    borderRadius: 10,
    marginBottom: 20,
    maxHeight: 200,
  },
  credentialsContainer: {
    flex: 1,
    backgroundColor: 'lightgray',
    padding: 10,
    borderRadius: 10,
    marginBottom: 20,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 20,
  },
  button: {
    flex: 1,
    backgroundColor: '#007AFF',
    padding: 10,
    borderRadius: 8,
    alignItems: 'center',
    marginHorizontal: 5,
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
  },
  messageItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255,255,255,0.3)',
  },
  deleteButton: {
    backgroundColor: '#FF3B30',
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 5,
  },
  deleteButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
  iconButton: {
    backgroundColor: '#007AFF',
    padding: 12,
    borderRadius: 50, // Makes it circular
    alignItems: 'center',
    justifyContent: 'center',
    width: 50,
    height: 50,
  },
  modalContainer: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(0,0,0,0.5)',
  },
  modalContent: {
    backgroundColor: 'white',
    padding: 20,
    height: '50%', // Covers half the screen
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 10,
    textAlign: 'center',
  },
  messageText: {
    fontSize: 16,
    paddingVertical: 5,
  },
  closeButton: {
    backgroundColor: '#FF3B30',
    padding: 10,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 20,
  },
  closeButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
};

export default HomeScreen;
