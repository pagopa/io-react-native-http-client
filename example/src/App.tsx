import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import {
  fooBar,
  nativeClientRequest,
  type HttpClientResponse,
} from '@pagopa/io-react-native-http-client';

export default function App() {
  const [result, setResult] = React.useState<HttpClientResponse | undefined>();

  React.useEffect(() => {
    nativeClientRequest('https://google.com').then((res) => {
      setResult(res);
      console.log('result is', res);
    });
  }, []);

  console.log('in app.tsx');
  fooBar(123).then((res) => console.log('foo is:', res));

  return (
    <View style={styles.container}>
      <Text>Result: {JSON.stringify(result)}</Text>
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
