package com.bbuddies.madafaker.common_domain.auth

/**
 * Represents FirebaseAuth readiness and sign-in state without exposing Firebase types.
 */
sealed class AuthSessionState {
    object Initializing : AuthSessionState()
    object SignedIn : AuthSessionState()
    object SignedOut : AuthSessionState()
}
