package com.tradeguru.electrical.models

import com.google.gson.annotations.SerializedName

data class AuthUser(
    val id: String,
    val email: String,
    val name: String? = null,
    @SerializedName("picture_url") val pictureURL: String? = null
)
