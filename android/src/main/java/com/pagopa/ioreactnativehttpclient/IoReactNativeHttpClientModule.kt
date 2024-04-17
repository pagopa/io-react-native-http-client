package com.pagopa.ioreactnativehttpclient

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
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
  fun httpClientRequest(url: String, promise: Promise) {
    var client = OkHttpClient()
    val request: Request = Request.Builder()
      .url(url)
      .build()
    client.newCall(request).enqueue(
      object : Callback {
        override fun onResponse(call: Call, response: Response) {
          var fullResponse= Arguments.createMap()

          fullResponse.putInt("status",response.code)

          response.body?.let {
            try {
              fullResponse.putString("body", it.string())
            }catch (e:IOException) {
              promise.reject("internal error", "failed to deserialize body ")
              return
            }
          }

          promise.resolve(fullResponse)
          return
          }

        override fun onFailure(call: Call, e: IOException) {
          promise.reject("code",e.message)
          return
        }
      }

    )

  }
  @ReactMethod
  fun fooBar(a:Int,promise:Promise){
    promise.resolve(0)
  }

  companion object {
    const val NAME = "IoReactNativeHttpClient"
  }
}
