# Authentication System V2.1 - Optimistic Auth with Firebase Initialization Handling

**Date:** January 19, 2026  
**Status:** Implemented  
**Previous Version:** V2 (January 18, 2026)

## Debug logging filter: FIREBASE_DEBUG | APP_INIT | AUTH_MANAGER | AUTH_REPO | AUTH_INTERCEPTOR | SEND_WORKER | FirebaseAuth | FirebaseApp | Firebear | KeyStore | AndroidKeyStore

## Problem Statement

The V2 auth system had a critical bug discovered on January 18-19, 2026:

### Original Problem (V1 → V2)

On cold start (app killed, phone restarted), users were incorrectly redirected to the AuthScreen even when they were previously logged in.

### New Problem (V2 → V2.1)

After login and cold start, Firebase Auth reports `currentUser = null` **permanently**, not just briefly. API calls with cached tokens
return 401 because:

1. Firebase ID tokens expire after 1 hour
2. Google ID tokens (used for session restoration) also expire after ~1 hour
3. Firebase's internal session persistence was not restoring on cold start

### Root Cause Analysis (V2.1)

Firebase Auth **should** automatically persist sessions across app restarts using its internal refresh token. However:

1. On cold start, Firebase's `currentUser` returns `null` immediately - this is a race condition
2. Firebase needs time to restore the session from disk (SharedPreferences)
3. The V2 system checked `hasFirebaseUser()` before Firebase finished initializing
4. `restoreFirebaseSession()` tried to use expired Google ID tokens (>1 hour old)

**The fundamental insight**: Firebase's AuthStateListener may fire with `SignedOut` on cold start even when it will eventually restore to
`SignedIn`.

## New Architecture: V2.1 - Await Firebase Initialization

### Key Changes from V2

1. **`awaitInitialization()`**: New method that waits for Firebase to finish initializing before making auth decisions
2. **Grace Period**: 1.5 second wait after first AuthStateListener callback to catch late sign-in events
3. **Better Logging**: Timestamps and timing information to debug Firebase state changes
4. **Clearer Token Fallback**: If Firebase can't restore and Google token expired, validate using cached Firebase token via API

### Components

#### 1. `PreferenceManager.isSessionActive` (unchanged from V2)

- Boolean flag persisted in DataStore
- Set to `true` when user successfully logs in
- Set to `false` only on explicit logout or confirmed auth failure
- Survives cold starts, app updates, etc.

#### 2. `TokenRefreshService` (enhanced in V2.1)

- `firebaseStatus: StateFlow<FirebaseAuthStatus>` - Firebase's internal state
- **NEW** `awaitInitialization(timeoutMs)` - Waits for Firebase to finish initializing
- `hasFirebaseUser(): Boolean` - Quick check for Firebase user
- `signOut()` - Sign out from Firebase
- `refreshFirebaseIdToken()` - Get fresh token
- `restoreFirebaseSession()` - Attempt session restore (returns Boolean)

#### 3. `GoogleAuthManager` (enhanced in V2.1)

```kotlin
// Track if AuthStateListener has been called
private val _hasReceivedAuthCallback = MutableStateFlow(false)

override suspend fun awaitInitialization(timeoutMs: Long): FirebaseAuthStatus {
    // If already signed in, return immediately
    if (_firebaseStatus.value == FirebaseAuthStatus.SignedIn) {
        return FirebaseAuthStatus.SignedIn
    }

    // Wait for first AuthStateListener callback
    withTimeoutOrNull(timeoutMs) {
        _hasReceivedAuthCallback.first { it }

        if (_firebaseStatus.value == FirebaseAuthStatus.SignedIn) {
            return@withTimeoutOrNull FirebaseAuthStatus.SignedIn
        }

        // Grace period for late sign-in
        delay(1500)
        _firebaseStatus.value
    } ?: _firebaseStatus.value
}
```

#### 4. `UserRepositoryImpl.authenticationState` (unchanged from V2)

```kotlin
combine(isSessionActive, userId) { sessionActive, userId ->
    if (!sessionActive) return NotAuthenticated
    if (userId == null) return NotAuthenticated
    
    val cachedUser = localDao.getUserById(userId)
    if (cachedUser != null) return Authenticated(cachedUser)
    
    // Fallback: try network
    return fetchUserFromNetwork()
}
```

#### 5. Background Validation (enhanced in V2.1)

```kotlin
private suspend fun performBackgroundValidation() {
    // CRITICAL: Wait for Firebase to finish initializing
    val firebaseStatus = tokenRefreshService.awaitInitialization(timeoutMs = 5000)

    when (firebaseStatus) {
        FirebaseAuthStatus.SignedIn -> {
            // Firebase has user - refresh token normally
            refreshTokenAndSyncUser()
        }
        FirebaseAuthStatus.SignedOut -> {
            // Firebase confirmed no user - attempt recovery
            attemptFirebaseRecovery()
        }
        FirebaseAuthStatus.Initializing -> {
            // Still initializing after timeout - unusual
            // Skip validation, let AuthInterceptor handle
        }
    }
}

private suspend fun attemptFirebaseRecovery() {
    // 1. Try Google ID token (works if <1 hour old)
    val restorationSuccessful = tokenRefreshService.restoreFirebaseSession(storedGoogleToken)

    if (restorationSuccessful) {
        refreshTokenAndSyncUser()
    } else {
        // Google token expired - try cached Firebase token via API
        try {
            webService.getCurrentUser()  // If this works, token is still valid
        } catch (e: HttpException) {
            if (e.code() == 401) {
                forceLogout("Firebase session lost and cached token expired")
            }
        }
    }
}
```

#### 6. `AuthInterceptor` (enhanced logging in V2.1)

## Auth Flow Scenarios

### Scenario 1: Cold Start (Normal - Firebase restores session)

```
App starts
 → PreferenceManager loads: isSessionActive=true, userId="abc123"
 → UserRepository: sessionActive + cached user → Authenticated immediately
 → Splash → Main (no delay for user)
 → Background: awaitInitialization() waits for Firebase
 → Firebase restores session → SignedIn
 → Token refresh successful
```

### Scenario 2: Cold Start (Firebase slow to restore)

```
App starts
 → PreferenceManager loads: isSessionActive=true, userId="abc123"  
 → UserRepository: sessionActive + cached user → Authenticated immediately
 → Splash → Main (user sees main screen)
 → Background: awaitInitialization() waits...
 → Firebase first callback: SignedOut (premature)
 → Grace period: 1.5 seconds
 → Firebase restores session → SignedIn
 → Token refresh successful
```

### Scenario 3: Cold Start (Firebase doesn't restore, but cached token valid)

```
App starts
 → Authenticated immediately from cache
 → Background: awaitInitialization() → SignedOut after timeout
 → attemptFirebaseRecovery()
 → restoreFirebaseSession() fails (Google token expired)
 → Try API with cached Firebase token → 200 OK
 → User continues with cached token
```

### Scenario 4: Cold Start (All tokens expired)

```
App starts
 → Authenticated immediately from cache
 → Background: awaitInitialization() → SignedOut
 → attemptFirebaseRecovery()
 → restoreFirebaseSession() fails (Google token expired)
 → Try API with cached Firebase token → 401
 → forceLogout() → NotAuthenticated
 → Navigate to Auth
```

### Scenario 5: Cold Start (Offline)

```
App starts
 → Authenticated immediately from cache
 → Background: awaitInitialization() times out
 → Token refresh fails (network) → logged, not logged out
 → User can use app with cached data
```

### Scenario 6: Token Expired During Use (Online)

```
User makes API call
 → AuthInterceptor: 401 received
 → Token refresh → new token
 → Retry request → success
 → User continues without interruption
```

### Scenario 7: Auth Actually Invalid

```
User makes API call
 → AuthInterceptor: 401 received
 → Token refresh → 401 again
 → Error logged, response returned to app
 → App can decide to logout or show error
```

### Scenario 8: Fresh Install / First Login

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

### Scenario 9: Explicit Logout

```
User taps Logout
 → tokenRefreshService.signOut() → Firebase sign out
 → clearAllUserData() → clears DB + preferences (including isSessionActive)
 → authenticationState → NotAuthenticated
 → Navigate to Auth
```

## Debug Logging

All auth-related logging uses these tags:

- `AUTH_MANAGER` - GoogleAuthManager (Firebase operations, awaitInitialization)
- `AUTH_REPO` - UserRepositoryImpl (auth state resolution, background validation)
- `AUTH_INTERCEPTOR` - AuthInterceptor (token refresh on 401)
- `SPLASH_NAV` - GetNextScreenAfterLoginUseCase
- `SPLASH_VM` - SplashViewModel
- `APP_INIT` - MadafakerApp (Firebase state on app start)
- `FIREBASE_DEBUG` - Firebase persistence diagnostics

Example log output (cold start with Firebase restoration):

```
D/APP_INIT: === MadafakerApp.onCreate() ===
D/APP_INIT: Firebase Auth currentUser: null
D/APP_INIT: Firebase Auth has NO USER on app start!
D/AUTH_MANAGER: === GoogleAuthManager INIT ===
D/AUTH_MANAGER: Firebase currentUser (sync check): null
D/AUTH_REPO: authenticationState combine: sessionActive=true, userId=abc123
D/AUTH_REPO: resolveAuthState: sessionActive=true, userId=abc123
D/AUTH_REPO: Found cached user: JohnDoe (id=abc123)
D/SPLASH_VM: SplashViewModel init
D/SPLASH_NAV: Current user: JohnDoe
D/SPLASH_NAV: User exists with permissions -> Main
D/AUTH_REPO: === Starting background validation ===
D/AUTH_REPO: Waiting for Firebase to initialize...
D/AUTH_MANAGER: awaitInitialization(timeout=5000ms) - current status: Initializing
D/AUTH_MANAGER: Waiting for first auth callback...
D/AUTH_MANAGER: === Firebase AuthStateListener TRIGGERED ===
D/AUTH_MANAGER: Time since app start: 450ms
D/AUTH_MANAGER: Previous status: Initializing
D/AUTH_MANAGER: New status: SignedOut
D/AUTH_MANAGER: Got SignedOut after first callback, waiting grace period...
D/AUTH_MANAGER: === Firebase AuthStateListener TRIGGERED ===
D/AUTH_MANAGER: Time since app start: 850ms
D/AUTH_MANAGER: Previous status: SignedOut
D/AUTH_MANAGER: New status: SignedIn
D/AUTH_MANAGER: User UID: abc123
D/AUTH_MANAGER: awaitInitialization completed: SignedIn
D/AUTH_REPO: Firebase initialization complete: SignedIn
D/AUTH_REPO: Firebase confirmed SignedIn - refreshing token
D/AUTH_MANAGER: Firebase ID token refreshed successfully (length=1234)
D/AUTH_REPO: Token refresh successful
D/AUTH_REPO: User data synced from server: JohnDoe
D/AUTH_REPO: === Background validation complete ===
```

## Token Expiration Reference

| Token Type             | Expiration   | Notes                                        |
|------------------------|--------------|----------------------------------------------|
| Firebase ID Token      | 1 hour       | Refreshed automatically by Firebase SDK      |
| Google ID Token        | ~1 hour      | From Credential Manager, cannot be refreshed |
| Firebase Refresh Token | Months/Years | Internal to Firebase, auto-managed           |

**Key Insight**: The cached `firebaseIdToken` in PreferenceManager can be used for API calls even after Firebase session is lost, as long as
it hasn't expired. This provides a grace period of up to 1 hour after process death.

## Migration Notes

- **AuthSessionState**: Renamed to `FirebaseAuthStatus` (with deprecated alias for compatibility)
- **authState**: Renamed to `firebaseStatus` (with deprecated alias)
- Existing logged-in users will need to re-login once because `isSessionActive` is a new flag
    - On first cold start after update, isSessionActive=false → Auth screen
    - After login, subsequent cold starts work correctly

## Files Changed (V2.1)

1. `domain/.../auth/TokenRefreshService.kt` - Added `awaitInitialization()`, changed `restoreFirebaseSession()` to return Boolean
2. `data/.../repository/UserRepositoryImpl.kt` - Added proper Firebase initialization waiting, improved recovery logic
3. `data/.../interceptors/AuthInterceptor.kt` - Enhanced logging for 401 handling
4. `presentation/.../auth/GoogleAuthManager.kt` - Implemented `awaitInitialization()`, tracks auth callbacks
