package com.pagopa.ioreactnativehttpclient

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableNativeMap
import com.facebook.react.bridge.WritableNativeMap
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope

class IoReactNativeHttpClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  private val runningCoroutines = HashMap<String, CoroutineScope>()
  private var client: HttpClient? = null;

  private fun initializeClientIfNeeded() {
    if (client == null) {
      client = HttpClient(Android)
    }
  }

  private fun deallocate() {
    cancelAllRunningRequests()
    client?.close()
    client = null
  }

  @ReactMethod
  fun nativeRequest(config: ReadableMap, promise: Promise) {
    initializeClientIfNeeded()
    val verb = stringFromConfigForKey(config, "verb") ?: run {
      handleNonHttpFailure("Bad configuration, missing 'verb'", promise)
      return
    }

    val methodOpt = optMethodFromVerb(verb) ?: run {
      handleNonHttpFailure("Unsupported configuration, unknown verb '$verb'", promise)
      return
    }

    val url = stringFromConfigForKey(config, "url") ?: run {
      handleNonHttpFailure("Bad configuration, missing 'url'", promise)
      return
    }

    // TODO
  }

  private fun stringFromConfigForKey(configOpt: ReadableMap?, key: String): String? =
    configOpt?.let {
      return try {
        it.getString(key)
      } catch (e: Exception) {
        null
      }
    }


  private fun optMethodFromVerb(verb: String): HttpMethod? =
    if ("get".equals(verb, ignoreCase = true)) HttpMethod.Get else if ("post".equals(
        verb,
        ignoreCase = true
      )
    ) HttpMethod.Post else null

  fun handleNonHttpFailure(message: String, promise: Promise) {
    val failureHttpResponse = WritableNativeMap()
    failureHttpResponse.putString("type", "failure")
    failureHttpResponse.putInt("code", 900)
    failureHttpResponse.putString("message", message)
    failureHttpResponse.putMap("headers", WritableNativeMap())
    promise.resolve(failureHttpResponse)
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
