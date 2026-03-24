package com.tradeguru.electrical.services

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PKCEHelper {
    private const val UNRESERVED =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"

    fun generateVerifier(): String {
        val random = SecureRandom()
        return (1..64)
            .map { UNRESERVED[random.nextInt(UNRESERVED.length)] }
            .joinToString("")
    }

    fun generateChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(verifier.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }
}
