package com.pagopa.ioreactnativehttpclient

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class IoReactNativeHttpClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun standardRequest(a: String, promise: Promise) {
    var client = OkHttpClient()
    val request: Request = Request.Builder()
      .url(a)
      .build()
    client.newCall(request).enqueue(
      object : Callback {
        override fun onResponse(call: Call, response: Response) {
              if (response.isSuccessful) {
              Log.e("verylongstring", "log in res")
              promise.resolve(response.body!!.string())
            } else {
              promise.resolve("Error")
            }
          }

        override fun onFailure(call: Call, e: IOException) {

        }
      }

    )
    Log.e("verylongstring", "hehe debug log")

  }
  @ReactMethod
  fun fooBar(a:Int,promise:Promise){
    promise.resolve(0)
  }

  companion object {
    const val NAME = "IoReactNativeHttpClient"
  }
}
