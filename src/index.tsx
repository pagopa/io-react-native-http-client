import IoReactNativeHttpClient from './NativeIoReactNativeHttpClient';
import type {
  HttpCallConfig,
  HttpClientFailureResponse,
  HttpClientResponse,
  HttpClientSuccessResponse,
} from './types';

export const nativeRequest = (
  config: HttpCallConfig
): Promise<HttpClientResponse> =>
  IoReactNativeHttpClient.nativeRequest(config) as Promise<HttpClientResponse>;

export const setCookie = (
  domain: string,
  path: string,
  name: string,
  value: string
): void =>
  IoReactNativeHttpClient.setCookieForDomain(domain, path, name, value);

export const removeAllCookiesForDomain = (domain: string): void =>
  IoReactNativeHttpClient.removeAllCookiesForDomain(domain);

export const cancelRequestWithId = (requestId: string): void =>
  IoReactNativeHttpClient.cancelRequestWithId(requestId);

export const cancelAllRunningRequests = (): void =>
  IoReactNativeHttpClient.cancelAllRunningRequests();

export const deallocate = (): void => IoReactNativeHttpClient.deallocate();

export const NonHttpErrorCode = 900;
export const CancelledMessage = 'Cancelled';
export const SerializationFailure = 'Serialization Failure';
export const TimeoutMessage = 'Timeout';
export const TLSMessage = 'TLS Failure';

export const isFailureResponse = (
  response: HttpClientResponse
): response is HttpClientFailureResponse => response.type === 'failure';

export const isSuccessResponse = (
  response: HttpClientResponse
): response is HttpClientSuccessResponse => response.type === 'success';

export const isCancelledFailure = (
  response: HttpClientResponse
): response is HttpClientFailureResponse =>
  isFailureResponse(response) &&
  response.code === NonHttpErrorCode &&
  response.message === CancelledMessage;

export const isSerializationFailure = (
  response: HttpClientResponse
): response is HttpClientFailureResponse =>
  isFailureResponse(response) &&
  response.code === NonHttpErrorCode &&
  response.message === SerializationFailure;

export const isTimeoutFailure = (
  response: HttpClientResponse
): response is HttpClientFailureResponse =>
  isFailureResponse(response) &&
  response.code === NonHttpErrorCode &&
  response.message === TimeoutMessage;

export const isTLSFailure = (
  response: HttpClientResponse
): response is HttpClientFailureResponse =>
  isFailureResponse(response) &&
  response.code === NonHttpErrorCode &&
  response.message === TLSMessage;

export * from './types';
