# WebStack

A high-performance visual bookmark manager and website stack for Android built with Jetpack Compose, Material Design 3, Room Database, and Kotlin Coroutines.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2024.09.00-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![F-Droid](https://img.shields.io/badge/F--Droid-Ready-3DDC84.svg)](metadata/com.chiranth7.webstack.yml)

---

## Overview

Modern web navigation often leads to tab clutter and lost links buried deep within browser submenus. **WebStack** solves this by providing Android users with a centralized, visual bookmarking hub. Every saved link is captured with responsive snapshot rendering, automated favicon extraction, custom category tagging, and local offline caching.

---

## Key Features

- **Visual Website Cards**: Captures and renders high-resolution page previews and favicons for fast visual scanning.
- **Dual Display Modes**: Seamlessly toggle between full visual screenshot cards and dense compact row listings.
- **Hardware-Accelerated Search**: Zero-lag filtering across website titles, domains, and custom tags with 60/120 FPS transitions.
- **Dynamic Category Management**: Create, rename, delete, and filter custom category tags with interactive bottom sheets.
- **Offline Snapshot Caching**: Previews and metadata are persisted locally on-device for instant loading without mobile data consumption.
- **Native Android Share Sheet**: Receive and save links directly from Google Chrome, Firefox, or any application in one tap.
- **Smart Clipboard Detection**: Automatically suggests saving valid URLs detected on the system clipboard.
- **Zero Trackers & Privacy First**: Fully open source with no telemetry, advertising SDKs, or third-party tracking libraries.

---

## Screenshots

| Visual Bookmark Stack | Save Link Sheet | Settings & Layout Modes |
| :---: | :---: | :---: |
| <img src="docs/screenshots/webstack-home.png" width="260" alt="WebStack Home Feed" /> | <img src="docs/screenshots/webstack-save-link.png" width="260" alt="Save Link Sheet" /> | <img src="docs/screenshots/webstack-settings.png" width="260" alt="Settings Sheet" /> |

---

## Architecture & Tech Stack

```
webstack/
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/
│   │   │   ├── Website.kt           # Room entity definition
│   │   │   ├── WebsiteDao.kt        # Data access object with Flow streams
│   │   │   ├── WebsiteDatabase.kt   # Room database singleton
│   │   │   └── WebsiteRepository.kt # Repository abstraction & network fetching
│   │   ├── ui/
│   │   │   ├── theme/               # Color tokens, typography, and theme styling
│   │   │   └── viewmodel/           # WebsiteViewModel (StateFlow, category persistence)
│   │   └── MainActivity.kt          # Compose UI architecture, Navigation Header, Cards & Sheets
│   └── src/test/                    # Unit tests, Robolectric, and Roborazzi snapshot tests
├── fastlane/metadata/android/       # F-Droid Fastlane metadata
├── metadata/                        # F-Droid build recipe specification
└── .github/workflows/               # CI/CD automated release pipeline
```

### Core Technologies
| Component | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.2.10 | Core application logic and asynchronous coroutines |
| **UI Framework** | Jetpack Compose | Declarative UI rendering with Material 3 design tokens |
| **Persistence** | Room SQLite Database | Local structured storage with reactive Flow observables |
| **Image Pipeline** | Coil Compose | Asynchronous image loading with memory and disk caches |
| **HTTP Client** | OkHttp 4.12.0 | Website metadata fetching and favicon resolution |
| **Testing** | JUnit 4, Robolectric | Instrumented unit tests and screenshot regression testing |

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or higher
- JDK 17 or JDK 21
- Android SDK Platform 35
- Physical device or emulator running Android 8.0 (API 26) or higher

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/Chiranth-Janardhan-moger/webstack.git
   cd webstack
   ```

2. Build debug APKs:
   ```bash
   ./gradlew assembleDebug
   ```

3. Build optimized release APKs:
   ```bash
   ./gradlew assembleRelease
   ```

Generated APK binaries will be located in:
`app/build/outputs/apk/release/`

### Running Unit Tests

Run local unit tests and Robolectric verification:
```bash
./gradlew testDebugUnitTest
```

---

## Download Binaries

Pre-compiled release APKs are available on the [GitHub Releases](https://github.com/Chiranth-Janardhan-moger/webstack/releases) page:

- **ARM 64-bit (`arm64-v8a`)**: Recommended for modern Android smartphones and tablets.
- **ARM 32-bit (`armeabi-v7a`)**: Compatible with older 32-bit hardware.
- **Universal**: Contains all ABI binaries in a single package.

---

## F-Droid Submission

WebStack complies with F-Droid inclusion policies:
- Free and Open Source (MIT License)
- Reproducible Gradle builds without proprietary dependencies
- Offline build capability
- Recipe file: [`metadata/com.chiranth7.webstack.yml`](metadata/com.chiranth7.webstack.yml)


---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
