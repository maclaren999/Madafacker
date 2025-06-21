package com.bbuddies.madafaker.common_domain.model

sealed class AuthenticationState {
    object NotAuthenticated : AuthenticationState()
    object Loading : AuthenticationState()
    data class Authenticated(val user: User) : AuthenticationState()
    data class Error(val exception: Throwable) : AuthenticationState()
}