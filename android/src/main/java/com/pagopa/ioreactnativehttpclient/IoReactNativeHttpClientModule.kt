package com.pagopa.ioreactnativehttpclient

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import okhttp3.OkHttpClient
import okhttp3.Request

class IoReactNativeHttpClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun multiply(a: String, promise: Promise) {
    var client = OkHttpClient()
    val request: Request = Request.Builder()
      .url(a)
      .build()
    client.newCall(request).execute().use { response ->
      if (response.isSuccessful) {
        promise.resolve(response.body!!.string())
      } else {
        promise.resolve("Error")
      }
    }
  }

  companion object {
    const val NAME = "IoReactNativeHttpClient"
  }
}
