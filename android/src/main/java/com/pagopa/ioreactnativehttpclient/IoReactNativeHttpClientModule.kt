package com.pagopa.ioreactnativehttpclient

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class IoReactNativeHttpClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun httpClientRequest(url: String, isPost:Boolean,formEncodedParams:ReadableMap?, headers:ReadableMap?, shouldFollowRedirects:Boolean?, promise: Promise ) {
    var client = OkHttpClient.Builder().followRedirects(shouldFollowRedirects?: true).build()
    val requestBuilder = Request.Builder()
      .url(url)

    headers?.let {
      for (entry in it.entryIterator) {
        requestBuilder.header(entry.key, entry.value.toString())
      }
    }

    if(isPost) {
      val formBuilder=FormBody.Builder()
      formEncodedParams?.let {
        for (entry in it.entryIterator) {
          formBuilder.add(entry.key,entry.value.toString())
        }
      }
      requestBuilder.post(formBuilder.build())
    }



    client.newCall(requestBuilder.build()).enqueue(
      object : Callback {
        override fun onResponse(call: Call, response: Response) {
          var fullResponse= Arguments.createMap()

          fullResponse.putInt("status",response.code)

          var headersMap=Arguments.createMap()
          for (entry in response.headers){
            headersMap.putString(
              entry.first,entry.second
            )
          }
          fullResponse.putMap("headers",headersMap)

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
