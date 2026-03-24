package com.tradeguru.electrical.models

sealed class AuthState {
    data object Anonymous : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
}
