# Text Sharing Implementation Guide

## Overview

Your Android app now supports receiving shared text from other applications through implicit intents. Users can share text from browsers,
notes apps, social media, or any app that supports text sharing, and it will be automatically handled by your app.

## How It Works

### 1. Intent Filter Registration

The app registers for `ACTION_SEND` intents with `text/plain` MIME type in `AndroidManifest.xml`:

```xml

<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="text/plain" />
</intent-filter>
```

### 2. MainActivity Handling

- **Fresh Launch**: When the app is not running, `onCreate()` handles the shared text
- **Already Running**: When the app is running, `onNewIntent()` handles the shared text (thanks to `singleTop` launch mode)

### 3. SharedTextManager

A centralized manager that:

- Stores shared text temporarily
- Tracks whether text has been consumed
- Provides methods to consume or clear shared text
- Handles edge cases like empty/null text

### 4. MainViewModel / WriteTabViewModel Integration

- `MainViewModel` still observes `SharedTextManager.hasUnconsumedSharedText` to navigate the user to
  the Write tab when text arrives.
- `WriteTabViewModel` now performs the draft pre-population logic (respecting existing draft content
  and truncation to `AppConfig.MAX_MESSAGE_LENGTH`).

### 5. UI Navigation

- Automatically navigates to the WriteTab when shared text is received
- Smooth animation to the compose screen
- Preserves user's current state if they're already composing

## User Experience

### Sharing Text to Your App

1. User selects text in any app (browser, notes, etc.)
2. Taps "Share" and selects your app
3. Your app opens/comes to foreground
4. Automatically navigates to the WriteTab
5. Shared text appears in the message composition field
6. User can edit and send the message

### Edge Cases Handled

- **Empty/null text**: Ignored gracefully
- **Very long text**: Truncated with "..." indicator
- **Existing draft**: Won't overwrite substantial user content
- **Multiple shares**: Each new share replaces previous unconsumed text

## Technical Implementation

### Key Components

#### SharedTextManager

```kotlin
@Singleton
class SharedTextManager @Inject constructor() {
    fun setSharedText(text: String?)
    fun consumeSharedText(): String?
    fun clearSharedText()
    fun hasSharedText(): Boolean
}
```

#### MainActivity Intent Handling

```kotlin
private fun handleSharedText(intent: Intent?) {
    if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        sharedTextManager.setSharedText(sharedText)
    }
}
```

#### MainViewModel Integration

```kotlin
private fun setupSharedTextHandling() {
    viewModelScope.launch {
        sharedTextManagerImpl.sharedText.collect { sharedText ->
            // Handle shared text and pre-populate draft
        }
    }
}
```

### Configuration Details

#### AndroidManifest.xml

- `android:launchMode="singleTop"`: Prevents multiple instances
- Intent filter for `ACTION_SEND` with `text/plain` MIME type
- `android:exported="true"`: Allows external apps to launch your app

#### Dependency Injection

- SharedTextManager is provided as a Singleton
- Injected into MainActivity and MainViewModel
- Ensures consistent state across the app

## Testing

### Manual Testing

1. **From Browser**: Select text on a webpage, share to your app
2. **From Notes**: Share text from a notes app
3. **From Social Media**: Share a post or comment
4. **Multiple Shares**: Share different texts in succession
5. **App States**: Test when app is closed, backgrounded, and active

### Test Scenarios

- ✅ Fresh app launch with shared text
- ✅ App already running with shared text
- ✅ Empty/null shared text handling
- ✅ Very long text truncation
- ✅ Existing draft preservation
- ✅ Navigation to WriteTab
- ✅ Text consumption and clearing

### Edge Cases

- **Network Issues**: Shared text works offline
- **Authentication**: Works regardless of login state
- **Memory Pressure**: SharedTextManager survives configuration changes
- **Multiple Instances**: singleTop prevents issues

## Security Considerations

### Input Validation

- Text is trimmed and validated before processing
- Length limits are enforced (AppConfig.MAX_MESSAGE_LENGTH characters max)
- No executable content is processed (text only)

### Privacy

- Shared text is stored temporarily in memory only
- Cleared after consumption or app restart
- No persistent storage of shared content

## Future Enhancements

### Potential Improvements

1. **Rich Text Support**: Handle formatted text and links
2. **Image Sharing**: Support for `image/*` MIME types
3. **Multiple Text Shares**: Queue multiple shared texts
4. **Share History**: Optional local storage of recent shares
5. **Custom Share UI**: Dedicated screen for processing shares

### Analytics

Consider tracking:

- Share intent frequency
- Text length distribution
- User completion rates after sharing
- Most common sharing sources

## Troubleshooting

### Common Issues

1. **App not appearing in share menu**: Check intent filter configuration
2. **Text not pre-populating**: Verify SharedTextManager injection
3. **Navigation not working**: Check MainScreen LaunchedEffect
4. **Multiple app instances**: Ensure singleTop launch mode

### Debug Tips

- Check Logcat for SharedTextManager logs
- Verify intent extras in MainActivity
- Monitor SharedTextManager state flows
- Test with different text lengths and sources

This implementation provides a seamless text sharing experience while maintaining app stability and user experience quality.
