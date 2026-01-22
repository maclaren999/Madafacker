# Repository Guidelines

## General guidelines

- you MUST build project after code changes to ensure nothing is broken
- Main app **business** specifications document is /docs/AppRequirements.MD It has to be followed, referenced and updated as project
  evolves. You can also divide it into smaller docs in /docs/ as needed.
- If you have meaningfull questions to clarify of improve the task definition - ask them!

## Project Structure & Module Organization

- `app/`: Android application entry point, DI wiring, manifest, and build variants.
- `presentation/`: Jetpack Compose UI, navigation, ViewModels, and UI resources in `presentation/src/main/res`.
- `data/`: Repositories, API clients, Room/database, and persistence adapters.
- `domain/`: Pure Kotlin entities and use cases (no Android dependencies).
- `docs/`: Product requirements and API reference material.
- `gradle/`: Version catalog and shared build configuration.
- Kotlin Multiplatform Preparedness: Structure the Android codebase (business logic, network layer) to facilitate a future migration to
  Kotlin Multiplatform (KMP) for shared modules across mobile platforms.

## Build, Test, and Development Commands

Use the Gradle wrapper (`./gradlew` on macOS/Linux or `gradlew.bat` on Windows):

- `./gradlew assembleDebug`: Build a debug APK.
- `./gradlew assembleRelease`: Build a release APK.
- `./gradlew test`: Run JVM unit tests across modules.
- `./gradlew connectedAndroidTest`: Run instrumented tests on a device/emulator.
- `./gradlew clean build`: Full clean build.

## Coding Style & Naming Conventions

- Kotlin-first; Java 21 compatibility. Follow Android Studio/Kotlin formatting (4-space indent).
- Packages use `com.bbuddies.madafaker.*`.
- Class names in PascalCase; functions/vars in lowerCamelCase; Compose `@Composable` functions in PascalCase.
- No repo-wide formatter configured; run Android Studio lint inspections before submitting changes.

## Testing Guidelines

- Unit tests live under `*/src/test` (JUnit4 + MockK + Robolectric where needed).
- Instrumented tests live under `*/src/androidTest` (AndroidX JUnit + Espresso).
- Name tests with `*Test`/`*InstrumentedTest` (see existing examples).

## Commit & Pull Request Guidelines

- Commit messages follow Conventional Commits with optional scopes, e.g. `feat(Navigation): ...`, `docs: ...`, `ref(Theme): ...`.
- PRs should include: concise summary, testing notes (commands + results), and screenshots for UI changes.
- Link related issues/tickets and call out any API or behavior changes.

## Security & Configuration

- Secrets are loaded from `MADAFAKER_SECRETS_JSON` or `app/secrets.json`.
- Required keys include `GOOGLE_WEB_CLIENT_ID`, `API_BASE_URL`, and debug keystore fields.
- Do not commit secrets or local machine configs (keep them in `local.properties` or env vars).
