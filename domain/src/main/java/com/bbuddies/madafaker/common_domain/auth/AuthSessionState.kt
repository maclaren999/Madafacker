package com.bbuddies.madafaker.common_domain.auth

/**
 * Represents Firebase auth state.
 * Note: This is used for Firebase sync status, NOT for determining if user is logged in.
 * User login status is determined by local session data.
 */
sealed class FirebaseAuthStatus {
    /** Firebase is still initializing, user state unknown */
    object Initializing : FirebaseAuthStatus()

    /** Firebase has a signed-in user */
    object SignedIn : FirebaseAuthStatus()

    /** Firebase has no signed-in user */
    object SignedOut : FirebaseAuthStatus()

    override fun toString(): String = when (this) {
        Initializing -> "Initializing"
        SignedIn -> "SignedIn"
        SignedOut -> "SignedOut"
    }
}

/**
 * @deprecated Use FirebaseAuthStatus instead. This alias is kept for compatibility.
 */
@Deprecated("Use FirebaseAuthStatus", ReplaceWith("FirebaseAuthStatus"))
typealias AuthSessionState = FirebaseAuthStatus

