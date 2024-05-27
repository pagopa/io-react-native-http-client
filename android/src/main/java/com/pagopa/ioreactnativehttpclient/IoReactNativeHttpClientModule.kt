package com.pagopa.ioreactnativehttpclient

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import okhttp3.OkHttpClient
import okhttp3.Request

class IoReactNativeHttpClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun nativeRequest(config: ReadableMap, promise: Promise) {
    // TODO
  }

  @ReactMethod
  fun setCookieForDomain(domain: String, path: String, name: String, value: String) {
    // TODO
  }

  @ReactMethod
  fun removeAllCookiesForDomain(domain: String) {
    // TODO
  }

  @ReactMethod
  fun cancelRequestWithId(requestId: String) {
    // TODO
  }

  @ReactMethod
  fun cancelAllRunningRequests() {
    // TODO
  }

  companion object {
    const val NAME = "IoReactNativeHttpClient"
  }
}
