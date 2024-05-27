import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import {
  cancelAllRunningRequests,
  cancelRequestWithId,
  deallocate,
  nativeRequest,
  removeAllCookiesForDomain,
  setCookie,
} from '@pagopa/io-react-native-http-client';
import type { HttpClientResponse } from '../../src/types';

export default function App() {
  const [clientResponse, setClientResponse] =
    React.useState<HttpClientResponse | null>(null);
  const [requestId, setRequestId] = React.useState<string | null>(null);

  return (
    <View style={styles.container}>
      <TouchableOpacity
        onPress={() => {
          const newRequestId = `${Math.random() * 1000000000}`;
          setRequestId(newRequestId);
          nativeRequest({
            verb: 'get',
            url: 'https://www.google.com',
            followRedirects: false,
            headers: { f1: 'v1', f2: 'v2' },
            requestId: newRequestId,
            timeoutMilliseconds: 60000,
          }).then((response) => {
            setRequestId(null);
            setClientResponse(response);
          });
        }}
      >
        <Text>Native Call</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.buttonWithMargin}
        onPress={() =>
          setCookie(
            'https://www.google.com',
            '/',
            'MyCookie',
            `${Math.random() * 1000}`
          )
        }
      >
        <Text>Set Cookie</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.buttonWithMargin}
        onPress={() => removeAllCookiesForDomain('https://www.google.com')}
      >
        <Text>Remove All Cookies</Text>
      </TouchableOpacity>
      <TouchableOpacity
        disabled={!requestId}
        style={styles.buttonWithMargin}
        onPress={() => {
          cancelRequestWithId(requestId!);
          setRequestId(null);
        }}
      >
        <Text>Cancel Request</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.buttonWithMargin}
        onPress={() => {
          cancelAllRunningRequests();
          setRequestId(null);
        }}
      >
        <Text>Cancel All</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.buttonWithMargin}
        onPress={() => {
          deallocate();
          setRequestId(null);
        }}
      >
        <Text>Deallocate</Text>
      </TouchableOpacity>
      <Text numberOfLines={7} style={styles.textWithMargin}>
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
    padding: 16,
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  buttonWithMargin: {
    marginTop: 8,
  },
  textWithMargin: {
    marginTop: 16,
  },
});
