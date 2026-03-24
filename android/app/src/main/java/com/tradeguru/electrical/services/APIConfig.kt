package com.tradeguru.electrical.services

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request

object APIConfig {
    const val BASE_URL = "https://tradeguru.com.au/api/v1"
    const val PLATFORM = "android"
    const val APP_VERSION = "1.0.0"
    const val WORKOS_CLIENT_ID = "client_01JWQK8QD9RJVTCTMR8ACE9CKB"
    const val AUTH_PROXY_URL = "https://tradeguru.com.au/api/workos-auth-proxy"
    const val OAUTH_REDIRECT_URI = "tradeguru://auth-callback"

    fun url(path: String): HttpUrl {
        val clean = if (path.startsWith("/")) path.drop(1) else path
        return "$BASE_URL/$clean".toHttpUrlOrNull()
            ?: BASE_URL.toHttpUrlOrNull()
            ?: throw IllegalStateException("Invalid base URL")
    }

    fun requestBuilder(
        path: String,
        method: String = "POST",
        deviceId: String,
        jwt: String? = null
    ): Request.Builder {
        val builder = Request.Builder()
            .url(url(path))
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Device-ID", deviceId)

        if (jwt != null) {
            builder.addHeader("Authorization", "Bearer $jwt")
        }

        return builder
    }
}
