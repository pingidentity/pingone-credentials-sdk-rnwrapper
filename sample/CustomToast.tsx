import React from 'react';
import {View, Text, StyleSheet} from 'react-native';
import Ionicons from 'react-native-vector-icons/Ionicons';
import {ToastProps} from 'react-native-toast-message';

const CustomToast: React.FC<ToastProps> = ({text1, text2}) => {
  return (
    <View style={styles.toast}>
      <Ionicons name="checkmark-circle" size={24} color="green" />
      <View style={styles.textContainer}>
        {text1 && <Text style={styles.title}>{text1}</Text>}
        {text2 && <Text style={styles.message}>{text2}</Text>}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  toast: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#e0ffe0',
    borderRadius: 10,
    padding: 10,
    marginHorizontal: 20,
  },
  textContainer: {
    marginLeft: 10,
  },
  title: {
    fontWeight: 'bold',
    fontSize: 18,
    color: 'green',
  },
  message: {
    fontSize: 16,
    color: '#444',
  },
});

export default CustomToast;
