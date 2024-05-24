import { NativeModules, Platform } from 'react-native';
import type { HttpCallConfig, HttpClientResponse } from './types';

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

export const nativeRequest = (
  config: HttpCallConfig
): Promise<HttpClientResponse> => IoReactNativeHttpClient.nativeRequest(config);

export const setCookie = (
  domain: string,
  path: string,
  name: string,
  value: string
) => IoReactNativeHttpClient.setCookieForDomain(domain, path, name, value);

export const removeAllCookiesForDomain = (domain: string) =>
  IoReactNativeHttpClient.removeAllCookiesForDomain(domain);

export const cancelRequestWithId = (requestId: string) =>
  IoReactNativeHttpClient.cancelRequestWithId(requestId);

export const cancelAllRunningRequests = () =>
  IoReactNativeHttpClient.cancelAllRunningRequests();

export const NonHttpErrorCode = 900;
export const isCancelledResponse = (response: HttpClientResponse) =>
  response.type === 'failure' &&
  response.code === NonHttpErrorCode &&
  response.message === 'Cancelled';
