package com.tradeguru.electrical.services

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.tradeguru.electrical.models.AuthState
import com.tradeguru.electrical.models.AuthTokens
import com.tradeguru.electrical.models.AuthUser
import com.tradeguru.electrical.models.UserTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

class AuthManager(
    private val keychainHelper: KeychainHelper,
    private val apiConfig: APIConfig
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Anonymous)
    val authState: StateFlow<AuthState> = _authState

    private val _tier = MutableStateFlow(UserTier.FREE)
    val tier: StateFlow<UserTier> = _tier

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    val currentJwt: String? get() = keychainHelper.load("access_token")

    val currentUser: AuthUser?
        get() = when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }

    val isAuthenticated: Boolean get() = currentUser != null

    fun startSignIn(context: Context, provider: String? = null) {
        _authError.value = null

        val verifier = PKCEHelper.generateVerifier()
        val challenge = PKCEHelper.generateChallenge(verifier)
        keychainHelper.save("pkce_verifier", verifier)

        val state = UUID.randomUUID().toString()
        keychainHelper.save("auth_state", state)

        val uriBuilder = Uri.parse("https://api.workos.com/user_management/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", APIConfig.WORKOS_CLIENT_ID)
            .appendQueryParameter("redirect_uri", APIConfig.OAUTH_REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")

        if (provider != null) {
            uriBuilder.appendQueryParameter("provider", provider)
        }

        val url = uriBuilder.build()
        CustomTabsIntent.Builder().build().launchUrl(context, url)
    }

    suspend fun handleAuthCallback(uri: Uri) {
        val code = uri.getQueryParameter("code") ?: return
        val verifier = keychainHelper.load("pkce_verifier") ?: return

        try {
            val tokens = exchangeCode(code, verifier)
            val user = JWTDecoder.decode(tokens.accessToken) ?: return

            keychainHelper.save("access_token", tokens.accessToken)
            keychainHelper.save("refresh_token", tokens.refreshToken)

            val expiry = JWTDecoder.getExpiry(tokens.accessToken) ?: 0L
            keychainHelper.save(
                "tokens",
                AuthTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    expiresAt = expiry
                ),
                AuthTokens::class.java
            )
            keychainHelper.save("user", user, AuthUser::class.java)

            _authState.value = AuthState.Authenticated(user)

            linkDevice()
        } catch (e: Exception) {
            _authError.value = "Sign in failed: ${e.message}"
        }
    }

    fun signOut() {
        keychainHelper.clear()
        _authState.value = AuthState.Anonymous
        _tier.value = UserTier.FREE
    }

    suspend fun linkDevice(deviceId: String? = null) {
        val jwt = currentJwt ?: return
        val id = deviceId ?: keychainHelper.load("device_id") ?: return

        try {
            withContext(Dispatchers.IO) {
                val body = gson.toJson(mapOf("device_id" to id))
                    .toRequestBody("application/json".toMediaType())

                val request = APIConfig.requestBuilder("device/link", deviceId = id, jwt = jwt)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val json = gson.fromJson(responseBody, Map::class.java)
                        val tierString = json["tier"] as? String
                        if (tierString != null) {
                            val newTier = UserTier.fromValue(tierString)
                            if (newTier != null) {
                                _tier.value = newTier
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) { }
    }

    suspend fun restoreSession() {
        val accessToken = keychainHelper.load("access_token") ?: return
        val refreshToken = keychainHelper.load("refresh_token") ?: return
        val savedUser = keychainHelper.load("user", AuthUser::class.java)

        val expiry = JWTDecoder.getExpiry(accessToken)
        if (expiry != null && expiry > System.currentTimeMillis()) {
            val user = savedUser ?: JWTDecoder.decode(accessToken) ?: return
            _authState.value = AuthState.Authenticated(user)
            linkDevice()
        } else {
            try {
                refreshTokenWithValue(refreshToken)
            } catch (_: Exception) {
                signOut()
            }
        }
    }

    suspend fun refreshTokenIfNeeded() {
        val token = keychainHelper.load("access_token") ?: return
        val expiry = JWTDecoder.getExpiry(token) ?: return
        if (expiry - System.currentTimeMillis() < 300_000) {
            val refreshToken = keychainHelper.load("refresh_token") ?: return
            refreshTokenWithValue(refreshToken)
        }
    }

    private suspend fun refreshTokenWithValue(refreshToken: String) {
        val tokens = exchangeRefresh(refreshToken)
        val user = JWTDecoder.decode(tokens.accessToken)
        if (user == null) {
            signOut()
            return
        }

        keychainHelper.save("access_token", tokens.accessToken)
        keychainHelper.save("refresh_token", tokens.refreshToken)
        val expiry = JWTDecoder.getExpiry(tokens.accessToken) ?: 0L
        keychainHelper.save(
            "tokens",
            AuthTokens(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                expiresAt = expiry
            ),
            AuthTokens::class.java
        )
        keychainHelper.save("user", user, AuthUser::class.java)
        _authState.value = AuthState.Authenticated(user)
    }

    private suspend fun exchangeCode(
        code: String,
        verifier: String
    ): TokenResponse = withContext(Dispatchers.IO) {
        val body = gson.toJson(
            mapOf(
                "grant_type" to "authorization_code",
                "client_id" to APIConfig.WORKOS_CLIENT_ID,
                "code" to code,
                "code_verifier" to verifier
            )
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${APIConfig.AUTH_PROXY_URL}/user_management/authenticate")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty response")
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }
        gson.fromJson(responseBody, TokenResponse::class.java)
    }

    private suspend fun exchangeRefresh(
        refreshToken: String
    ): TokenResponse = withContext(Dispatchers.IO) {
        val body = gson.toJson(
            mapOf(
                "grant_type" to "refresh_token",
                "client_id" to APIConfig.WORKOS_CLIENT_ID,
                "refresh_token" to refreshToken
            )
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${APIConfig.AUTH_PROXY_URL}/user_management/authenticate")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty response")
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }
        gson.fromJson(responseBody, TokenResponse::class.java)
    }
}

private data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)
