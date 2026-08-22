# WebStack

A visual bookmark stack and link management application for Android built with Jetpack Compose and Material Design 3.

## Overview

WebStack allows users to save, organize, search, and visually browse websites through automated snapshot rendering, category tagging, and offline caching.

## Key Features

- Visual Website Cards: Captures and renders responsive page snapshots and favicons.
- Dual Layout Modes: Switch seamlessly between full visual screenshot cards and high-density compact rows.
- Fast Search: Filter bookmarks instantly across titles, URLs, and custom tags with hardware-accelerated transitions.
- Category Management: Create, rename, delete, and filter by custom tags with interactive action sheets.
- Offline Snapshot Caching: Snapshots are cached locally on device storage for instant offline loading.
- Native System Sharing: Receive incoming links directly from browsers and other apps via Android Share Sheet integration.
- Clipboard Detection: Quick-paste pill appears automatically when valid URLs are present in the clipboard.

## Architecture & Tech Stack

- UI: Jetpack Compose, Material 3, custom design tokens
- Image Loading: Coil Compose with disk and memory caching
- Local Persistence: Room Database with Kotlin Coroutines and Flow
- Architecture Pattern: MVVM (Model-View-ViewModel) with Android Architecture Components
- Testing: JUnit 4, Robolectric, Roborazzi screenshot tests
- Build System: Gradle Kotlin DSL (`build.gradle.kts`) with ABI splits (arm64-v8a, armeabi-v7a)

## Getting Started

### Prerequisites

- Android Studio Ladybug or later
- JDK 17 or higher
- Android SDK Platform 35
- Android device or emulator running Android 8.0 (API 26) or higher

### Building from Source

Clone the repository and build the debug APK:

```bash
git clone https://github.com/Chiranth-Janardhan-moger/webstack.git
cd webstack
./gradlew assembleDebug
```

Output APKs will be located in:
`app/build/outputs/apk/debug/`

### Running Tests

Execute local unit and Robolectric tests:

```bash
./gradlew testDebugUnitTest
```

## License

This project is licensed under the MIT License.
