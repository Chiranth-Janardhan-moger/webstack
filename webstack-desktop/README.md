# WebStack Desktop Client

Native desktop application for WebStack powered by Tauri v2 and native WebView2.

## Overview

WebStack Desktop provides a fast, lightweight (~3.5 MB) desktop client for managing and searching bookmarks synced from the WebStack Android app.

Unlike standard browser tabs or PWA installations on unencrypted LAN addresses, WebStack Desktop runs natively on the host OS:
- Zero CORS or mixed-content limitations when connecting to the phone's companion server.
- Automatic Wi-Fi discovery via UDP broadcast and mDNS, eliminating manual IP entry.
- Persistent local caching for complete offline functionality even when the phone is turned off.
- Native keyboard shortcuts, window controls, and system tray integration.

## Architecture

This folder is maintained as an isolated client codebase, separated from the Android application (`app/`) and the embedded web companion (`webcompanion/`):

```
webstack-desktop/
├── package.json          # Frontend dependencies (Vite, TypeScript, Tauri API)
├── tsconfig.json         # TypeScript configuration
├── vite.config.ts        # Vite configuration for Tauri dev server
├── index.html            # Desktop user interface
├── src/
│   ├── main.ts           # State management, keyboard shortcuts, DOM bindings
│   ├── api.ts            # Native HTTP requests and auto-discovery
│   ├── types.ts          # Bookmark and server data models
│   └── styles.css        # Desktop theme styles
└── src-tauri/
    ├── Cargo.toml        # Rust backend dependencies (Tauri v2, reqwest, tokio)
    ├── tauri.conf.json   # Window configurations, bundle targets, and permissions
    ├── build.rs          # Tauri build script
    └── src/
        ├── main.rs       # Executable entry point
        ├── lib.rs        # Native commands and plugin setup
        └── discovery.rs  # LAN UDP broadcast discovery probe
```

## Building Locally

### Prerequisites
- Node.js 18+ (tested with Node 22/24)
- Rust 1.77+ and Cargo
- Windows C++ Build Tools (MSVC) or Linux/macOS equivalent

### Development
```bash
# Install frontend dependencies
npm install

# Start Vite dev server with Tauri desktop window
npm run tauri dev
```

### Production Build
```bash
npm run tauri build
```
The compiled standalone binary (`webstack-desktop.exe`) and installer (`WebStack-Setup.exe`) are generated in `src-tauri/target/release/`.

## Cloud Builds via GitHub Actions

For environments without local Rust and MSVC C++ toolchains, a dedicated GitHub Actions workflow is provided at `.github/workflows/desktop-build.yml`.

Pushing a version tag (`v*`) or triggering `workflow_dispatch` automatically compiles the standalone Windows executable on `windows-latest` runners and publishes it as a downloadable release artifact (~3.5 MB).
