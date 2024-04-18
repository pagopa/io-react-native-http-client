import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package '@pagopa/io-react-native-http-client' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const IoReactNativeHttpClient = NativeModules.IoReactNativeHttpClient
  ? NativeModules.IoReactNativeHttpClient
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export type HttpClientResponse =
  | { status: number; body: string; headers: Record<string, string> }
  | undefined;

export function nativeClientRequest(
  url: string,
  headers?: Record<string, string>
): Promise<HttpClientResponse> {
  return IoReactNativeHttpClient.httpClientRequest(url, headers);
}

export function fooBar(a: number): Promise<number> {
  return IoReactNativeHttpClient.fooBar(a);
}
