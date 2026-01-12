# Server-side Google Authentication Implementation Guide

## Overview

Your Android client now sends Google ID tokens in the `token` header to your backend. The server needs to verify these tokens using Firebase
Admin SDK to ensure they're valid and extract user information.

## Required Dependencies

Add Firebase Admin SDK to your server project:

### Node.js/Express

```bash
npm install firebase-admin
```

### Python/Django/Flask

```bash
pip install firebase-admin
```

### Java/Spring Boot

```xml

<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

## Firebase Admin SDK Setup

### 1. Generate Service Account Key

1. Go to Firebase Console → Project Settings → Service Accounts
2. Click "Generate new private key"
3. Download the JSON file
4. Store it securely on your server (DO NOT commit to version control)

### 2. Initialize Firebase Admin SDK

#### Node.js Example

```javascript
const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
const serviceAccount = require('./path/to/serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  // Optional: Add your project ID
  projectId: 'your-project-id'
});
```

#### Python Example

```python
import firebase_admin
from firebase_admin import credentials, auth

# Initialize Firebase Admin SDK
cred = credentials.Certificate('./path/to/serviceAccountKey.json')
firebase_admin.initialize_app(cred)
```

## Token Verification Implementation

### Node.js/Express Middleware

```javascript
const verifyGoogleToken = async (req, res, next) => {
  try {
    const idToken = req.headers.token;
    
    if (!idToken) {
      return res.status(401).json({ error: 'No token provided' });
    }

    // Verify the ID token
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    
    // Extract user information
    req.user = {
      uid: decodedToken.uid,
      email: decodedToken.email,
      name: decodedToken.name,
      picture: decodedToken.picture,
      emailVerified: decodedToken.email_verified
    };
    
    next();
  } catch (error) {
    console.error('Token verification failed:', error);
    return res.status(401).json({ error: 'Invalid token' });
  }
};

// Apply middleware to protected routes
app.use('/api/user', verifyGoogleToken);
app.use('/api/message', verifyGoogleToken);
```

### Python/Django Middleware

```python
from firebase_admin import auth
from django.http import JsonResponse
import json


class FirebaseAuthenticationMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        # Skip authentication for certain paths
        if request.path.startswith('/api/user') or request.path.startswith('/api/message'):
            id_token = request.headers.get('token')

            if not id_token:
                return JsonResponse({'error': 'No token provided'}, status=401)

            try:
                # Verify the ID token
                decoded_token = auth.verify_id_token(id_token)

                # Add user info to request
                request.firebase_user = {
                    'uid': decoded_token['uid'],
                    'email': decoded_token.get('email'),
                    'name': decoded_token.get('name'),
                    'picture': decoded_token.get('picture'),
                    'email_verified': decoded_token.get('email_verified', False)
                }

            except Exception as e:
                return JsonResponse({'error': 'Invalid token'}, status=401)

        response = self.get_response(request)
        return response
```

## API Endpoint Updates

### User Creation Endpoint

```javascript
// POST /api/user
app.post('/api/user', verifyGoogleToken, async (req, res) => {
  try {
    const { name, registrationToken } = req.body;
    const firebaseUser = req.user;
    
    // Check if user already exists
    const existingUser = await getUserByFirebaseUid(firebaseUser.uid);
    if (existingUser) {
      return res.status(400).json({ error: 'User already exists' });
    }
    
    // Create new user in your database
    const newUser = await createUser({
      firebaseUid: firebaseUser.uid,
      email: firebaseUser.email,
      name: name,
      registrationToken: registrationToken,
      createdAt: new Date(),
      updatedAt: new Date()
    });
    
    res.json(newUser);
  } catch (error) {
    res.status(500).json({ error: 'Failed to create user' });
  }
});
```

### Get Current User Endpoint

```javascript
// GET /api/user/current
app.get('/api/user/current', verifyGoogleToken, async (req, res) => {
  try {
    const firebaseUser = req.user;
    
    // Find user by Firebase UID
    const user = await getUserByFirebaseUid(firebaseUser.uid);
    
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    res.json(user);
  } catch (error) {
    res.status(500).json({ error: 'Failed to get user' });
  }
});
```

## Database Schema Updates

Add Firebase UID to your user table:

```sql
ALTER TABLE users ADD COLUMN firebase_uid VARCHAR(255) UNIQUE;
CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);
```

## Security Considerations

1. **Always verify tokens server-side** - Never trust client-side validation
2. **Store service account keys securely** - Use environment variables or secure key management
3. **Implement rate limiting** - Prevent token verification abuse
4. **Log authentication attempts** - Monitor for suspicious activity
5. **Handle token expiration** - Tokens expire after 1 hour

## Error Handling

Common error scenarios:

- **Token expired**: Return 401, client should refresh token
- **Invalid token**: Return 401, client should re-authenticate
- **User not found**: Return 404 for existing user endpoints, create user for new user endpoints
- **Network issues**: Return 503, implement retry logic

## Testing

Test your implementation with:

1. Valid Google ID tokens from your Android app
2. Expired tokens
3. Invalid/malformed tokens
4. Missing tokens

## Environment Variables

Set these environment variables:

```bash
FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/serviceAccountKey.json
FIREBASE_PROJECT_ID=your-project-id
```

This implementation ensures secure authentication and proper integration with your existing API structure.
