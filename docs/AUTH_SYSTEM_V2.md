# Authentication System V2 - Optimistic Auth

**Date:** January 18, 2026  
**Status:** Implemented

## Problem Statement

The previous auth system had a critical bug: on cold start (app killed, phone restarted), users were incorrectly redirected to the
AuthScreen even when they were previously logged in.

### Root Cause Analysis

The old system used `FirebaseAuth.AuthStateListener` as the source of truth:

1. On cold start, Firebase initially reports `currentUser = null` briefly before restoring the session
2. The auth flow reacted immediately to this, emitting `NotAuthenticated`
3. The splash screen's navigation was one-shot - it navigated to Auth before Firebase could restore

**The fundamental problem**: Firebase's initialization state was conflated with the app's login state.

## New Architecture: Optimistic Auth

### Core Principles

1. **Local Session is Source of Truth**: The app maintains its own `isSessionActive` flag in DataStore
2. **Firebase is for Sync, Not Auth State**: Firebase validates tokens async, but doesn't block UI
3. **Cached User = Immediate Access**: If session is active + cached user exists → Authenticated immediately
4. **Explicit Logout Only**: Session is only cleared on:
    - User explicitly logs out
    - Confirmed auth failure (401 after token refresh)
    - NOT on Firebase state changes or transient errors

### Components

#### 1. `PreferenceManager.isSessionActive`

- Boolean flag persisted in DataStore
- Set to `true` when user successfully logs in
- Set to `false` only on explicit logout or confirmed auth failure
- Survives cold starts, app updates, etc.

#### 2. `TokenRefreshService` (implemented by `GoogleAuthManager`)

- `firebaseStatus: StateFlow<FirebaseAuthStatus>` - Firebase's internal state
- `hasFirebaseUser(): Boolean` - Quick check for Firebase user
- `signOut()` - Sign out from Firebase
- `refreshFirebaseIdToken()` - Get fresh token

#### 3. `UserRepositoryImpl.authenticationState`

New flow:

```kotlin
combine(isSessionActive, userId) { sessionActive, userId ->
    if (!sessionActive) return NotAuthenticated
    if (userId == null) return NotAuthenticated  // shouldn't happen
    
    val cachedUser = localDao.getUserById(userId)
    if (cachedUser != null) return Authenticated(cachedUser)
    
    // Fallback: try network
    return fetchUserFromNetwork()
}
```

#### 4. Background Validation

On app start, after initial state is determined:

```kotlin
init {
    repositoryScope.launch {
        delay(500)  // Let initial state settle
        performBackgroundValidation()
    }
}
```

- Refreshes Firebase token if available
- Syncs user data from server
- Does NOT affect auth state on failure

#### 5. `AuthInterceptor` - Smarter Token Handling

- On 401: attempts token refresh + retry
- If retry also 401: logs error but does NOT clear session
- Let the app layer handle persistent auth failures
- Transient errors (network, Firebase issues) don't log out user

## Auth Flow Scenarios

### Scenario 1: Cold Start (Normal)

```
App starts
 → PreferenceManager loads: isSessionActive=true, userId="abc123"
 → UserRepository: sessionActive + cached user → Authenticated immediately
 → Splash → Main (no delay)
 → Background: refresh token, sync user (async)
```

### Scenario 2: Cold Start (Offline)

```
App starts
 → PreferenceManager loads: isSessionActive=true, userId="abc123"
 → UserRepository: sessionActive + cached user → Authenticated immediately
 → Splash → Main
 → Background: token refresh fails (network) → logged, not logged out
 → User can use app with cached data
```

### Scenario 3: Token Expired (Online)

```
User makes API call
 → AuthInterceptor: 401 received
 → Token refresh → new token
 → Retry request → success
 → User continues without interruption
```

### Scenario 4: Auth Actually Invalid

```
User makes API call
 → AuthInterceptor: 401 received
 → Token refresh → 401 again
 → Error logged, response returned to app
 → App can decide to logout or show error
```

### Scenario 5: Fresh Install / First Login

```
App starts
 → PreferenceManager: isSessionActive=false
 → UserRepository: NotAuthenticated
 → Splash → Auth
 → User logs in with Google
 → storeGoogleAuth() → sets isSessionActive=true
 → createUser/authenticateWithGoogle() → caches user
 → Authenticated → Main
```

### Scenario 6: Explicit Logout

```
User taps Logout
 → tokenRefreshService.signOut() → Firebase sign out
 → clearAllUserData() → clears DB + preferences (including isSessionActive)
 → authenticationState → NotAuthenticated
 → Navigate to Auth
```

## Debug Logging

All auth-related logging uses these tags:

- `AUTH_MANAGER` - GoogleAuthManager (Firebase operations)
- `AUTH_REPO` - UserRepositoryImpl (auth state resolution)
- `AUTH_INTERCEPTOR` - AuthInterceptor (token refresh on 401)
- `SPLASH_NAV` - GetNextScreenAfterLoginUseCase
- `SPLASH_VM` - SplashViewModel

Example log output:

```
D/AUTH_MANAGER: GoogleAuthManager init - current Firebase user: null
D/AUTH_REPO: authenticationState combine: sessionActive=true, userId=abc123
D/AUTH_REPO: resolveAuthState: sessionActive=true, userId=abc123
D/AUTH_REPO: Found cached user: JohnDoe (id=abc123)
D/SPLASH_VM: SplashViewModel init
D/SPLASH_NAV: Determining next screen...
D/SPLASH_NAV: Current user: JohnDoe
D/SPLASH_NAV: User exists with permissions -> Main
D/AUTH_REPO: Starting background validation...
D/AUTH_REPO: Firebase status: SignedIn, hasFirebaseUser: true
D/AUTH_MANAGER: refreshFirebaseIdToken(forceRefresh=false) - currentUser: abc123
D/AUTH_MANAGER: Firebase ID token refreshed successfully (length=1234)
D/AUTH_REPO: Background token refresh successful
D/AUTH_REPO: User data synced from server: JohnDoe
```

## Migration Notes

- **AuthSessionState**: Renamed to `FirebaseAuthStatus` (with deprecated alias for compatibility)
- **authState**: Renamed to `firebaseStatus` (with deprecated alias)
- Existing logged-in users will need to re-login once because `isSessionActive` is a new flag
    - On first cold start after update, isSessionActive=false → Auth screen
    - After login, subsequent cold starts work correctly

## Files Changed

1. `domain/.../auth/AuthSessionState.kt` - Renamed to FirebaseAuthStatus
2. `domain/.../auth/TokenRefreshService.kt` - Added signOut(), hasFirebaseUser()
3. `domain/.../preference/PreferenceManager.kt` - Added isSessionActive
4. `data/.../local/PreferenceManagerImpl.kt` - Implemented isSessionActive
5. `data/.../repository/UserRepositoryImpl.kt` - Complete rewrite of auth flow
6. `data/.../interceptors/AuthInterceptor.kt` - Smarter retry logic
7. `presentation/.../auth/GoogleAuthManager.kt` - Implemented new interface
8. `presentation/.../usecase/GetNextScreenAfterLoginUseCase.kt` - Added logging
9. `presentation/.../ui/splash/SplashViewModel.kt` - Added logging
