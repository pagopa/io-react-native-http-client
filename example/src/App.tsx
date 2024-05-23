import * as React from 'react';

import { StyleSheet, View, Text, Pressable } from 'react-native';
import {
  nativeRequest,
  removeAllCookiesForDomain,
  setCookie,
} from '@pagopa/io-react-native-http-client';
import type { HttpClientResponse } from '../../src/types';

export default function App() {
  const [clientResponse, setClientResponse] =
    React.useState<HttpClientResponse | null>(null);

  return (
    <View style={styles.container}>
      <Pressable
        onPress={() =>
          nativeRequest({
            verb: 'get',
            url: 'https://www.gasdasdoogle.com',
            followRedirects: false,
            headers: { f1: 'v1', f2: 'v2' },
            timeoutMilliseconds: 20000,
          }).then(setClientResponse)
        }
      >
        <Text>Native Call</Text>
      </Pressable>
      <Pressable
        onPress={() => setCookie('google.com', '', 'MyCookie', 'Walakazam')}
      >
        <Text>Set Cookie</Text>
      </Pressable>
      <Pressable onPress={() => removeAllCookiesForDomain('google.com')}>
        <Text>Remove All Cookies</Text>
      </Pressable>
      <Text>
        {clientResponse
          ? `${clientResponse.type === 'failure' ? clientResponse.message : clientResponse.status}`
          : 'Null'}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
