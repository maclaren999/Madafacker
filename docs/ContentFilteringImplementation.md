# Content Filtering Implementation

## Overview

This document describes the implementation of the content filtering system for the Madafacker application, which provides different levels
of content moderation based on the selected mode (Shine vs Shadow).

## Architecture

The content filtering system follows Clean Architecture principles with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Presentation  │    │      Domain     │    │      Data       │
│                 │    │                 │    │                 │
│ • ModerationDialog │ │ • ContentFilter │    │ • ContentFilter │
│ • MainViewModel │    │   Service       │    │   ServiceImpl   │
│ • Error Handling│    │ • FilterResult  │    │ • API DTOs      │
│                 │    │ • Exceptions    │    │ • Repository    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Components

### Domain Layer

#### ContentFilterService Interface

```kotlin
interface ContentFilterService {
    suspend fun filterContent(text: String, mode: Mode): FilterResult
    suspend fun checkProfanity(text: String): FilterResult
    suspend fun updateProfanityList(words: List<String>)
}
```

#### FilterResult Model

```kotlin
data class FilterResult(
    val isAllowed: Boolean,
    val violationType: ViolationType? = null,
    val confidence: Float = 0.0f,
    val suggestion: String? = null,
    val detectedWords: List<String> = emptyList()
)
```

#### ModerationException Hierarchy

- `ClientSideViolation`: For client-side filtering failures
- `ServerSideViolation`: For server-side moderation failures
- `RateLimited`: For rate limiting scenarios
- `ServiceError`: For general service errors

### Data Layer

#### ContentFilterServiceImpl

- Implements regex-based profanity detection
- Provides on-device filtering for SHINE mode
- Returns immediate results without network calls
- Configurable word patterns and violation types

#### Integration Points

- **MessageRepositoryImpl**: Integrates filtering into message creation flow
- **API Layer**: Prepared for server-side moderation integration
- **Error Handling**: Converts exceptions to user-friendly messages

### Presentation Layer

#### MainViewModel Enhancements

- Handles moderation errors with appropriate user feedback
- Manages moderation dialog state
- Provides mode switching functionality

#### ModerationDialog Component

- User-friendly dialog for content violations
- Contextual suggestions based on violation type
- Option to switch to Shadow mode when appropriate

## Filtering Logic

### Shine Mode (Comprehensive Filtering)

1. **Client-side**: Regex-based profanity detection
2. **Server-side**: OpenAI Moderation API (all categories)
3. **User Experience**: Immediate feedback with mode switch suggestion

### Shadow Mode (Minimal Filtering)

1. **Client-side**: No filtering applied
2. **Server-side**: OpenAI Moderation API (illegal content only)
3. **User Experience**: Minimal restrictions, legal compliance only

## Usage Examples

### Basic Content Filtering

```kotlin
val filterResult = contentFilterService.filterContent("Hello world!", Mode.SHINE)
if (!filterResult.isAllowed) {
    throw ModerationException.ClientSideViolation(
        violationType = filterResult.violationType!!,
        detectedWords = filterResult.detectedWords,
        mode = Mode.SHINE,
        message = filterResult.suggestion ?: "Content not allowed"
    )
}
```

### Error Handling in Repository

```kotlin
override suspend fun createMessage(body: String): Message {
    val currentMode = preferenceManager.currentMode.value

    // Client-side filtering
    val filterResult = contentFilterService.filterContent(body, currentMode)
    if (!filterResult.isAllowed) {
        throw ModerationException.ClientSideViolation(...)
    }

    // Server call with error handling
    try {
        return webService.createMessage(CreateMessageRequest(body, currentMode.apiValue))
    } catch (exception: HttpException) {
        if (exception.code() == 422) {
            throw ModerationException.ServerSideViolation(...)
        }
        throw exception
    }
}
```

### UI Error Handling

```kotlin
private fun handleSendMessageError(exception: Throwable) {
    when (exception) {
        is ModerationException.ClientSideViolation -> {
            showModerationDialog(
                title = "Content Not Allowed",
                message = exception.message,
                showSwitchToShadow = exception.mode == Mode.SHINE
            )
        }
        // ... other cases
    }
}
```

## Testing

### Unit Tests

- **ContentFilterServiceImplTest**: Tests filtering logic for various scenarios
- **ModerationExceptionTest**: Tests exception hierarchy and data integrity
- **MessageRepositoryImplModerationTest**: Tests integration with message creation

### UI Tests

- **ModerationDialogTest**: Tests dialog behavior and user interactions

### Test Coverage

- ✅ Clean content handling
- ✅ Profanity detection (case-insensitive, word boundaries)
- ✅ Different violation types (profanity, harassment, violence)
- ✅ Mode-specific behavior (SHINE vs SHADOW)
- ✅ Error handling and user feedback
- ✅ Dialog interactions and mode switching

## Configuration

### Profanity Patterns

The system uses configurable regex patterns for different violation types:

```kotlin
private val profanityPatterns = listOf(
    "\\b(damn|hell|crap|shit|fuck|bitch|ass|bastard)\\b".toRegex(RegexOption.IGNORE_CASE),
    // Add more patterns as needed
)
```

### Violation Suggestions

Each violation type has contextual suggestions:

```kotlin
private fun getSuggestionForViolation(violationType: ViolationType): String {
    return when (violationType) {
        ViolationType.PROFANITY -> "Please keep it positive or switch to Shadow mode!"
        ViolationType.HARASSMENT -> "Let's keep things respectful. Try Shadow mode if you need to express frustration."
        // ... other types
    }
}
```

## Future Enhancements

### Planned Improvements

1. **Dynamic Word Lists**: Server-managed profanity lists with periodic updates
2. **Machine Learning**: Integration with more sophisticated ML models
3. **Context Awareness**: Consider message context and user history
4. **Localization**: Support for multiple languages and cultural contexts

### Server-Side Integration

The backend specification (`docs/BackendModerationSpec.md`) provides complete implementation guidelines for:

- OpenAI Moderation API integration
- Different thresholds for Shine vs Shadow modes
- Error response formats and user messages
- Performance and security considerations

## Deployment

### Dependencies Added

- Testing libraries for unit and UI tests
- Mockito for mocking in tests
- Coroutines testing support

### No External Dependencies

The client-side filtering implementation uses only standard Kotlin/Android libraries, ensuring:

- Fast performance
- No network dependencies
- Offline functionality
- Easy maintenance

## Monitoring and Analytics

### Recommended Metrics

- Content filtering rejection rates by mode
- Most common violation types
- User behavior after moderation (mode switching, message editing)
- Performance metrics for filtering operations

### Privacy Considerations

- Content is not logged or stored for privacy
- Only violation types and counts are tracked
- User-specific data is anonymized in analytics

This implementation provides a solid foundation for content moderation while maintaining user experience and system performance.
