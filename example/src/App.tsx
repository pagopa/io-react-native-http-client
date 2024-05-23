import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
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
      <Button
        title="Native Call"
        onPress={() =>
          nativeRequest({
            verb: 'post',
            url: 'https://www.google.com',
            body: { Uno: 'due' },
            followRedirects: false,
            headers: { f1: 'v1', f2: 'v2' },
            timeoutMilliseconds: 20000,
          }).then(setClientResponse)
        }
      />
      <Button
        title="Set Cookie"
        onPress={() =>
          setCookie(
            'https://www.google.com',
            '/',
            'MyCookie',
            `${Math.random() * 1000}`
          )
        }
      />
      <Button
        title="Remove All Cookies"
        onPress={() => removeAllCookiesForDomain('https://www.google.com')}
      />
      <Text numberOfLines={7}>
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
});
