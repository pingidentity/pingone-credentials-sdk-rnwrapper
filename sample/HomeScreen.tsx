import React, {useEffect, useState, useMemo} from 'react';
import {
  Alert,
  View,
  Text,
  Platform,
  StatusBar,
  SafeAreaView,
  FlatList,
  TouchableOpacity,
  Modal,
  NativeEventEmitter,
  NativeModules,
} from 'react-native';
import {StackNavigationProp} from '@react-navigation/stack';
import Icon from 'react-native-vector-icons/Ionicons'; // Import icon library
import Toast, {ToastProps} from 'react-native-toast-message';
import CustomToast from './CustomToast';
import {CredentialTypeMap} from '../src/index'; // Import the type for credentials

const toastConfig = {
  success: (props: ToastProps) => <CustomToast {...props} />,
};

const {PingOneCredentialsSDK} = NativeModules;
const eventEmitter = new NativeEventEmitter(PingOneCredentialsSDK);

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

const HomeScreen: React.FC<Props> = ({navigation}) => {
  const [applicationInstanceId, setApplicationInstanceId] = useState<
    string | null
  >('Initializing...');
  const [messages, setMessages] = useState<Item[]>([]);
  const [credentials, setCredentials] = useState<CredentialTypeMap>({});
  const [messagesVisible, setMessagesVisible] = useState(false);

  // Convert dictionary into an array of objects [{ key, message }]
  const credentialsArray = useMemo(() => {
    return Object.keys(credentials).map(key => ({
      key,
      message: credentials[key].claimData.CardType,
    }));
  }, [credentials]);

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
        let _applicationInstanceId = await PingOneCredentialsSDK.initializeSDK('na');
        console.log(`Application Instance ID: ${_applicationInstanceId}`);
        setApplicationInstanceId(_applicationInstanceId);
        refreshCredentialsList();
      } catch (e) {
        console.error(e);
      }
    };

    fetchResult();

    // Add event listeners for various events
    const handleCredentialIssuanceListener = eventEmitter.addListener(
      'P1SDK_handleCredentialIssuance',
      (event) => {
        console.log('Credential Issuance Event received:', event);
        showEventDialog(event.message);
        appendToMessage(`Credential Issued: ${event.message}`);
      }
    );

    const handleCredentialRevocationListener = eventEmitter.addListener(
      'P1SDK_handleCredentialRevocation',
      (event) => {
        console.log('Credential Revocation Event received:', event);
        showEventDialog(event.message);
        appendToMessage(`Credential Revoked: ${event.message}`);
      }
    );

    const handlePairingRequestListener = eventEmitter.addListener(
      'P1SDK_handlePairingRequest',
      (event) => {
        console.log('Pairing Request Event received:', event);
        showEventDialog(event.message);
        appendToMessage(`Pairing Request: ${event.message}`);
      }
    );

    const presentCredentialListener = eventEmitter.addListener(
      'P1SDK_presentCredential',
      (event) => {
        console.log('Present Credential Event received:', event);
        showEventDialog(event.message);
        appendToMessage(`Credential Presented: ${event.message}`);
      }
    );

    const handlePresentationActionListener = eventEmitter.addListener(
      'P1SDK_handlePresentationAction',
      (event) => {
        console.log('Presentation Action Event received:', event);
        showEventDialog(event.message);
        appendToMessage(`Presentation Action: ${event.message}`);
      }
    );

    const handlePairingEventListener = eventEmitter.addListener(
      'P1SDK_handlePairingEvent',
      (event) => {
        console.log('Pairing Event received:', event);
        showEventDialog(event.message);
        appendToMessage(`Pairing Event: ${event.message}`);
      }
    );

    const handleErrorEventListener = eventEmitter.addListener(
      'P1SDK_handleErrorEvent',
      (event) => {
        console.error('Error Event received:', event);
        showEventDialog(event.message);
        appendToMessage(`Error: ${event.message}`);
      }
    );

    return () => {
      // Clean up all event listeners
      handleCredentialIssuanceListener.remove();
      handleCredentialRevocationListener.remove();
      handlePairingRequestListener.remove();
      presentCredentialListener.remove();
      handlePresentationActionListener.remove();
      handlePairingEventListener.remove();
      handleErrorEventListener.remove();
    };
  }, []);

  const showEventDialog = async(event: string) => {
    Alert.alert(
      "Event Received",
      event,
      [
        {
          text: "OK",
          onPress: () => console.log("OK Pressed"),
        },
      ],
      { cancelable: false }
    );
  };

  const handleScanComplete = async (scannedCode: string) => {
    Alert.alert(
      "Confirm Pairing",
      "Are you sure you want to pair with the scanned code?",
      [
        {
          text: "Cancel",
          style: "cancel",
        },
        {
          text: "Pair",
          onPress: async () => {
            try {
              let response = await PingOneCredentialsSDK.processRequest(scannedCode);
              appendToMessage(response);
              console.log(response);
              checkForMessages();
            } catch (error) {
              console.error("Error processing scanned code:", error);
            }
          },
        },
      ]
    );
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
    console.log('Refreshing credentials list');
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
