package com.tradeguru.electrical.services

import android.util.Base64
import com.tradeguru.electrical.models.AuthUser
import org.json.JSONObject

object JWTDecoder {

    fun decode(jwt: String): AuthUser? {
        val segments = jwt.split(".")
        if (segments.size != 3) return null

        val payload = try {
            val padded = segments[1]
                .replace("-", "+")
                .replace("_", "/")
                .let { it + "=".repeat((4 - it.length % 4) % 4) }
            String(Base64.decode(padded, Base64.DEFAULT), Charsets.UTF_8)
        } catch (_: Exception) {
            return null
        }

        val json = try {
            JSONObject(payload)
        } catch (_: Exception) {
            return null
        }

        val sub = json.optString("sub").takeIf { it.isNotEmpty() } ?: return null
        val email = json.optString("email").takeIf { it.isNotEmpty() } ?: return null

        return AuthUser(
            id = sub,
            email = email,
            name = json.optString("name", null),
            pictureURL = json.optString("picture", null)
        )
    }

    fun getExpiry(jwt: String): Long? {
        val segments = jwt.split(".")
        if (segments.size != 3) return null

        val payload = try {
            val padded = segments[1]
                .replace("-", "+")
                .replace("_", "/")
                .let { it + "=".repeat((4 - it.length % 4) % 4) }
            String(Base64.decode(padded, Base64.DEFAULT), Charsets.UTF_8)
        } catch (_: Exception) {
            return null
        }

        val json = try {
            JSONObject(payload)
        } catch (_: Exception) {
            return null
        }

        val exp = json.optLong("exp", 0)
        if (exp <= 0) return null
        return exp * 1000
    }
}
