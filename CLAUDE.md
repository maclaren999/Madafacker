# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Madafacker** is a random message social Android app built with **Clean Architecture** and **Jetpack Compose**. Users broadcast single-line
messages to strangers in two modes: "Shine" (positive, moderated) and "Shadow" (unfiltered). The app emphasizes low-friction engagement with
push notifications delivering messages randomly between users.

## Architecture

### Modular Structure

- **`app/`** - Application entry point, dependency injection setup
- **`data/`** - Data layer (API, Room database, repositories)
- **`domain/`** - Business logic and entities (pure Kotlin)
- **`presentation/`** - UI layer with Jetpack Compose and ViewModels

### Dependency Flow

```
app → presentation, domain, data
presentation → domain
data → domain
domain → (pure Kotlin, no Android dependencies)
```

## Development Commands

### Build & Test

```bash
# Standard builds
./gradlew assembleDebug
./gradlew assembleRelease

# Run tests
./gradlew test
./gradlew connectedAndroidTest

# Clean build
./gradlew clean build
```

### Linting & Code Quality

```bash
# This project doesn't have explicit lint commands configured
# Use Android Studio's built-in linting or add custom lint tasks as needed
```

## Key Technical Stack

### Core Frameworks

- **Jetpack Compose** (BOM 2025.05.01) - UI toolkit
- **Dagger Hilt 2.55** - Dependency injection
- **Room 2.7.1** - Local database with coroutines
- **Retrofit 2.9.0** + **OkHttp 4.12.0** - Networking
- **Firebase** (BOM 33.14.0) - Push notifications, analytics, crashlytics
- **WorkManager 2.9.0** - Background tasks

### Architecture Patterns

- **MVVM** with Compose and StateFlow
- **Repository Pattern** for data abstraction
- **Use Cases** for business logic encapsulation
- **Clean Architecture** with clear layer separation

## Navigation Structure

### Main Tabs (Bottom Navigation)

1. **Write** - Compose messages and view last 3 sent
2. **My Posts** - Sent messages with replies
3. **Inbox** - Received messages to review
4. **Account** - User settings and profile

## Data Models & API Integration

### Core Entities

- **User** - nickname, FCM token, authentication
- **Message** - body, mode (shine/shadow), ratings
- **Reply** - threaded responses to messages
- **Rating** - like/dislike/superlike system

### API Modes

- Backend uses `light`/`dark` mode values
- UI uses `shine`/`shadow` terminology
- Mode translation handled in data layer

## Development Guidelines

### Module Dependencies

Each module has specific responsibilities - maintain clean boundaries:

- **Domain** should never import Android framework
- **Data** implements repository interfaces from domain
- **Presentation** only accesses domain via use cases

### Hilt Configuration

- All modules use Hilt for dependency injection
- WorkManager integration for background message sending
- Custom scopes for repository and use case lifecycles

### Testing Approach

- **Unit tests** in each module using JUnit 4.13.2
- **Mocking** with Mockito 4.6.1
- **Assertions** with Truth 1.4.2
- **Android tests** with Espresso and Compose testing

### Firebase Integration

- **FCM** for push notification delivery
- **Analytics** for user behavior tracking
- **Crashlytics** for crash reporting
- Google Services configuration required

## Background Processing

### WorkManager Usage

- **SendMessageWorker** handles queued message delivery
- **Hilt WorkManager** integration for dependency injection
- Retry policies for network failures
- Offline-first architecture with sync

## Content Moderation

### Shine Mode Filtering

- Server-side OpenAI Moderation API integration
- Client-side Firebase ML Kit profanity detection
- Custom blocklist for brand-specific terms

## Security & Privacy

### Authentication

- JWT Bearer token authentication
- FCM registration token management
- No PII required - nickname + token only

### API Security

- Bearer token headers for user requests
- x-api-key for cron job endpoints
- Rate limiting (30-second minimum between sends)

## Development Environment

### SDK Requirements

- **Compile/Target SDK:** 35
- **Min SDK:** 27
- **Kotlin:** 2.1.21
- **Java:** 21 compatibility

### Debug Tools

- **Chucker** for network debugging (debug builds only)
- **LeakCanary** for memory leak detection
- **Timber** for structured logging

## Project Requirements Reference

Detailed product requirements and API specifications are documented in:

- `app/src/main/java/com/bbuddies/madafaker/AppRequirements`
- `app/src/main/java/com/bbuddies/madafaker/API requests.postman_collection.json`

## Future Architecture Considerations

### Kotlin Multiplatform Readiness

- Codebase structured for potential KMP migration
- Kotlin-first libraries preferred over Java alternatives
- Business logic and network layer designed for cross-platform sharing