package com.pagopa.ioreactnativehttpclient

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableNativeMap
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.android.AndroidEngineConfig
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.addCookie
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.Cookie
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.Url
import io.ktor.util.StringValues
import io.ktor.util.StringValuesBuilderImpl
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLHandshakeException

class IoReactNativeHttpClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = NAME

  private val coroutineScope by lazy {
    CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  }

  private val runningRequestJobs = mutableMapOf<String, Job>()
  private var inMemoryCookieStorage = AcceptAllCookiesStorage()

  private var followsRedirectClient: HttpClient? = null
  private var doesNotFollowRedirectClient: HttpClient? = null

  private var defaultRequestTimeoutMilliseconds = 60_000L

  @ReactMethod
  fun nativeRequest(config: ReadableMap, promise: Promise) {
    val verb = stringFromConfigForKey(config, "verb")
      ?: return handleNonHttpFailure("Bad configuration, missing 'verb'", promise)

    val requestMethod = optMethodFromVerb(verb)
      ?: return handleNonHttpFailure("Unsupported configuration, unknown verb '$verb'", promise)

    val url = stringFromConfigForKey(config, "url")
      ?: return handleNonHttpFailure("Bad configuration, missing 'url'", promise)

    val bodyOpt = optBodyFromMethodAndConfig(requestMethod, config)

    val headers = headersFromConfig(config)
    val followsRedirects = followsRedirectsFromConfig(config)
    val timeoutMilliseconds = longFromConfigForKey(config, "timeout")
    val requestId = requestIdFromConfigOrRandom(config)

    initializeClientIfNeeded(followsRedirects)

    val httpClient = if (followsRedirects) followsRedirectClient else doesNotFollowRedirectClient
    httpClient?.let { client ->
      val requestJob = coroutineScope.launch {
        try {
          sendRequestAndHandleResponse(
            client, url, requestMethod, bodyOpt, headers, timeoutMilliseconds, requestId, promise
          )
        } catch (e: Exception) {
          handleRequestException(requestId, e, promise)
        }
      }
      runningRequestJobs[requestId] = requestJob
    }
  }

  @ReactMethod
  fun setCookieForDomain(domain: String, path: String, name: String, value: String) {
    coroutineScope.launch {
      try {
        val domainUrl = Url(domain)
        val domainHost = domainUrl.host
        val cookie = Cookie(name, value, path = path, domain = domainHost)
        inMemoryCookieStorage.addCookie(domain, cookie)
      } catch (_: Exception) {
        // TODO: what about handling this scenario?
        // All the above code is throwing? What about wrapping only what throws and use runCatching?
      }
    }
  }

  @ReactMethod
  fun removeAllCookiesForDomain(domain: String) {
    coroutineScope.launch {
      try {
        val cookiesForDomain = inMemoryCookieStorage.get(Url(domain))
        cookiesForDomain.forEach {
          val expiredCookie =
            Cookie(it.name, it.value, path = it.path, domain = it.domain, expires = GMTDate(0))
          inMemoryCookieStorage.addCookie(domain, expiredCookie)
        }
      } catch (_: Exception) {
        // TODO: what about handling this scenario?
        // All the above code is throwing? What about wrapping only what throws and use runCatching?
      }
    }
  }

  @ReactMethod
  fun cancelRequestWithId(requestId: String) {
    runningRequestJobs[requestId]?.let { job ->
      job.cancel()
      runningRequestJobs.remove(requestId)
    }
  }

  @ReactMethod
  fun cancelAllRunningRequests() {
    runningRequestJobs.values.forEach { it.cancel() }
    runningRequestJobs.clear()
  }

  @ReactMethod
  fun deallocate() {
    cancelAllRunningRequests()

    followsRedirectClient?.close()
    doesNotFollowRedirectClient?.close()

    followsRedirectClient = null
    doesNotFollowRedirectClient = null

    inMemoryCookieStorage = AcceptAllCookiesStorage()
  }

  private fun generateEngineConfiguration(shouldFollowRedirects: Boolean): HttpClientConfig<AndroidEngineConfig>.() -> Unit =
    {
      followRedirects = shouldFollowRedirects
      install(HttpCookies) {
        storage = inMemoryCookieStorage
      }
      install(HttpTimeout)
    }

  private fun initializeClientIfNeeded(shouldFollowRedirects: Boolean) {
    if (shouldFollowRedirects) {
      followsRedirectClient ?: run {
        followsRedirectClient = HttpClient(Android, generateEngineConfiguration(true))
      }
    } else {
      doesNotFollowRedirectClient ?: run {
        doesNotFollowRedirectClient = HttpClient(Android, generateEngineConfiguration(false))
      }
    }
  }

  private fun stringFromConfigForKey(configOpt: ReadableMap?, key: String): String? =
    try {
      configOpt?.getString(key);
    } catch (_: Exception) {
      null
    }

  private fun readableMapFromConfigForKey(configOpt: ReadableMap?, key: String): ReadableMap? =
    try {
      configOpt?.getMap(key)
    } catch (_: Exception) {
      null
    }

  @Suppress("SameParameterValue")
  private fun booleanFromConfigForKey(configOpt: ReadableMap?, key: String): Boolean? =
    try {
      configOpt?.getBoolean(key)
    } catch (_: Exception) {
      null
    }

  @Suppress("SameParameterValue")
  private fun longFromConfigForKey(configOpt: ReadableMap?, key: String): Long =
    try {
      configOpt?.getDouble(key)?.toLong() ?: defaultRequestTimeoutMilliseconds
    } catch (_: Exception) {
      defaultRequestTimeoutMilliseconds
    }

  private fun optMethodFromVerb(verb: String): HttpMethod? =
    when (verb.lowercase()) {
      "get" -> HttpMethod.Get
      "post" -> HttpMethod.Post
      else -> null
    }

  private fun optBodyFromMethodAndConfig(
    method: HttpMethod, configOpt: ReadableMap?
  ): FormDataContent? {
    if (method != HttpMethod.Post) return null

    val bodyMap = readableMapFromConfigForKey(configOpt, "body") ?: return null

    val formDataContent = FormDataContent(Parameters.build {
      bodyMap.entryIterator.forEach { (parametersName, parameterValue) ->
        if (parameterValue is String) {
          append(parametersName, parameterValue)
        }
      }
    })

    return if (formDataContent.formData.isEmpty()) null else formDataContent
  }

  private fun headersFromConfig(configOpt: ReadableMap?): StringValues {
    val headers = ParametersBuilder()
    readableMapFromConfigForKey(configOpt, "headers")?.let { map ->
      val iterator = map.keySetIterator()
      while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        map.getString(key)?.let { value ->
          headers.append(key, value)
        }
      }
    }
    return headers.build()
  }

  private fun followsRedirectsFromConfig(configOpt: ReadableMap?): Boolean =
    booleanFromConfigForKey(configOpt, "followRedirects") ?: true

  private fun requestIdFromConfigOrRandom(configOpt: ReadableMap?): String =
    stringFromConfigForKey(configOpt, "requestId") ?: UUID.randomUUID().toString()

  private suspend fun sendRequestAndHandleResponse(
    client: HttpClient,
    url: String,
    requestMethod: HttpMethod,
    bodyOpt: FormDataContent?,
    headers: StringValues,
    timeoutMilliseconds: Long,
    requestId: String,
    promise: Promise
  ) {
    val response = client.request(url) {
      method = requestMethod
      if (bodyOpt != null) {
        setBody(bodyOpt)
      }
      headers {
        appendAll(headers)
      }
      timeout {
        requestTimeoutMillis = timeoutMilliseconds
      }
    }
    val responseStatusCode = response.status.value
    val responseBody = response.body<String>()

    val responseHeaders = WritableNativeMap()
    response.headers.forEach { headerName: String, headerValues: List<String> ->
      responseHeaders.putString(headerName.lowercase(), headerValues.joinToString())
    }

    val isSuccessHttpStatusCode = responseStatusCode < 400
    val responseMap = WritableNativeMap().apply {
      putString("type", if (isSuccessHttpStatusCode) "success" else "failure")
      putInt(if (isSuccessHttpStatusCode) "status" else "code", responseStatusCode)
      putString(if (isSuccessHttpStatusCode) "body" else "message", responseBody)
      putMap("headers", responseHeaders)
    }

    runningRequestJobs.remove(requestId)

    promise.resolve(responseMap)
  }

  private fun handleRequestException(
    requestId: String, e: Exception, promise: Promise
  ) {
    runningRequestJobs.remove(requestId)

    val message = when (e) {
      is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> "Timeout"
      is CancellationException -> "Cancelled"
      is SSLHandshakeException -> "TLS Failure"
      else -> e.message ?: "Unable to send network request, unknown error"
    }

    handleNonHttpFailure(message, promise)
  }

  private fun handleNonHttpFailure(message: String, promise: Promise) {
    val failureHttpResponse = WritableNativeMap()
    failureHttpResponse.apply {
      putString("type", "failure")
      putInt("code", 900)
      putString("message", message)
      putMap("headers", WritableNativeMap())
    }
    promise.resolve(failureHttpResponse)
  }

  companion object {
    const val NAME = "IoReactNativeHttpClient"
  }
}
