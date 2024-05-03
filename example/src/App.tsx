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
    console.log('making call');
    nativeClientRequest(
      'http://localhost:3000/fims/relyingParty/1/landingPage',
      false,
      {
        key1: 'testing',
        key2: 'testing2',
      },
      undefined,
      true

      // {
      //   key1: 'testing',
      //   key2: 'testing2',
      // }
    )
      .then((res) => {
        setResult(res);
        console.log('result is', res);
      })
      .catch((err) => {
        console.log('error is :', JSON.stringify(err.message));
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
