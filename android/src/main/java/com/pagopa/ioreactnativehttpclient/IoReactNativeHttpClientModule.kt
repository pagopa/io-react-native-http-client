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

  override fun getName(): String {
    return NAME
  }

  private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  private val runningRequestJobs = HashMap<String, Job>()
  private var inMemoryCookieStorage = AcceptAllCookiesStorage()

  private var followsRedirectClient: HttpClient? = null
  private var doesNotFollowRedirectClient: HttpClient? = null

  private var defaultRequestTimeoutMilliseconds = 60000

  @ReactMethod
  fun nativeRequest(config: ReadableMap, promise: Promise) {

    val verb = stringFromConfigForKey(config, "verb") ?: run {
      handleNonHttpFailure("Bad configuration, missing 'verb'", promise)
      return
    }

    val requestMethod = optMethodFromVerb(verb) ?: run {
      handleNonHttpFailure("Unsupported configuration, unknown verb '$verb'", promise)
      return
    }

    val url = stringFromConfigForKey(config, "url") ?: run {
      handleNonHttpFailure("Bad configuration, missing 'url'", promise)
      return
    }

    val bodyOpt = optBodyFromMethodAndConfig(requestMethod, config)

    val headers = headersFromConfig(config)
    val followsRedirects = followsRedirectsFromConfig(config)
    val timeoutMilliseconds = longFromConfigForKey(config, "timeout")
    val requestId = requestIdFromConfigOrRandom(config)

    initializeClientIfNeeded(followsRedirects)

    (if (followsRedirects) followsRedirectClient else doesNotFollowRedirectClient)?.let { client ->

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
      }
    }
  }

  @ReactMethod
  fun cancelRequestWithId(requestId: String) {
    val jobOpt = runningRequestJobs[requestId]
    jobOpt?.let {
      jobOpt.cancel()
      runningRequestJobs.remove(requestId)
    }
  }

  @ReactMethod
  fun cancelAllRunningRequests() {
    runningRequestJobs.values.forEach {
      it.cancel()
    }
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
      if (followsRedirectClient == null) {
        followsRedirectClient = HttpClient(Android, generateEngineConfiguration(true))
      }
    } else {
      if (doesNotFollowRedirectClient == null) {
        doesNotFollowRedirectClient = HttpClient(Android, generateEngineConfiguration(false))
      }
    }
  }

  private fun stringFromConfigForKey(configOpt: ReadableMap?, key: String): String? =
    configOpt?.let {
      return try {
        it.getString(key)
      } catch (e: Exception) {
        null
      }
    }

  private fun readableMapFromConfigForKey(configOpt: ReadableMap?, key: String): ReadableMap? =
    configOpt?.let {
      return try {
        it.getMap(key)
      } catch (e: Exception) {
        null
      }
    }

  @Suppress("SameParameterValue")
  private fun booleanFromConfigForKey(configOpt: ReadableMap?, key: String): Boolean? =
    configOpt?.let {
      return try {
        it.getBoolean(key)
      } catch (e: Exception) {
        null
      }
    }

  @Suppress("SameParameterValue")
  private fun longFromConfigForKey(configOpt: ReadableMap?, key: String): Long = configOpt?.let {
    return (try {
      it.getDouble(key)
    } catch (e: Exception) {
      defaultRequestTimeoutMilliseconds
    }).toLong()
  } ?: run {
    defaultRequestTimeoutMilliseconds.toLong()
  }

  private fun optMethodFromVerb(verb: String): HttpMethod? =
    if ("get".equals(verb, ignoreCase = true)) HttpMethod.Get else if ("post".equals(
        verb, ignoreCase = true
      )
    ) HttpMethod.Post else null

  private fun optBodyFromMethodAndConfig(
    method: HttpMethod, configOpt: ReadableMap?
  ): FormDataContent? {
    if (method == HttpMethod.Post) {
      readableMapFromConfigForKey(configOpt, "body")?.let {
        val formDataContent = FormDataContent(Parameters.build {
          for ((parametersName, parameterValue) in it.entryIterator) {
            if (parameterValue is String) {
              append(parametersName, parameterValue)
            }
          }
        })
        return if (!formDataContent.formData.isEmpty()) formDataContent else null
      }
    }
    return null
  }

  private fun headersFromConfig(configOpt: ReadableMap?): StringValues {
    val headers = StringValuesBuilderImpl()
    readableMapFromConfigForKey(configOpt, "headers")?.let {
      for ((headerName, headerValue) in it.entryIterator) {
        if (headerValue is String) {
          headers[headerName] = headerValue
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
      val joinedValues = headerValues.joinToString()
      responseHeaders.putString(headerName, joinedValues)
    }

    val isSuccessHttpStatusCode = responseStatusCode < 400
    val responseMap = WritableNativeMap()
    responseMap.putString("type", if (isSuccessHttpStatusCode) "success" else "failure")
    responseMap.putInt(if (isSuccessHttpStatusCode) "status" else "code", responseStatusCode)
    responseMap.putString(if (isSuccessHttpStatusCode) "body" else "message", responseBody)
    responseMap.putMap("headers", responseHeaders)

    runningRequestJobs.remove(requestId)

    promise.resolve(responseMap)
  }

  private fun handleRequestException(
    requestId: String, e: Exception, promise: Promise
  ) {
    runningRequestJobs.remove(requestId)

    var message = e.message ?: "Unable to send network request, unknown error"
    if (e is HttpRequestTimeoutException || e is ConnectTimeoutException || e is SocketTimeoutException) {
      message = "Timeout"
    } else if (e is CancellationException) {
      message = "Cancelled"
    } else if (e is SSLHandshakeException) {
      message = "TLS Failure"
    }

    handleNonHttpFailure(message, promise)
  }

  private fun handleNonHttpFailure(message: String, promise: Promise) {
    val failureHttpResponse = WritableNativeMap()
    failureHttpResponse.putString("type", "failure")
    failureHttpResponse.putInt("code", 900)
    failureHttpResponse.putString("message", message)
    failureHttpResponse.putMap("headers", WritableNativeMap())
    promise.resolve(failureHttpResponse)
  }

  companion object {
    const val NAME = "IoReactNativeHttpClient"
  }
}
