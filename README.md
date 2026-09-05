# MediaFetch — Universal Social Media Downloader (Android)

MediaFetch is a modern, production-grade Android media downloader and companion API gateway focused on legitimate public and user-authorized media from **TikTok**, **Instagram**, **YouTube**, and **Facebook**.

The project is built with **Clean Architecture + MVVM**, **Jetpack Compose**, **Material 3 (with Dynamic Monet Theming)**, and modern Android 10+ **Scoped Storage (`MediaStore`)** APIs.

---

## 📱 Features

- **Provider-Based Architecture**: Modular extractors for TikTok, Instagram, YouTube, and Facebook.
- **Smart Privacy-First Clipboard Detection**: Non-intrusively detects supported links when foregrounded without continuous background spying.
- **Android Share Sheet Integration (`ACTION_SEND`)**: Tap "Share" in any social media app, select MediaFetch, and instantly open the Media Analyzer.
- **Resilient Download Engine**:
  - Background downloading via Android Foreground Service (`FOREGROUND_SERVICE_TYPE_DATA_SYNC`).
  - HTTP `Range: bytes=X-` resume capability for paused or interrupted downloads.
  - Smooth rolling-average download speed and remaining time (ETA) estimation.
  - Concurrency queue management (1–5 simultaneous downloads).
  - Mobile data confirmation & Wi-Fi only mode.
- **In-App Media Library**:
  - Categorized tabs (Videos, Images, Audio, All).
  - Grid and List view toggle.
  - Multi-select batch deletion and sharing.
  - Direct integration with Android system media players.
- **Material 3 Design Language**:
  - Deep dark theme with accessible contrast and elevated cards.
  - Android 12+ Dynamic Color (Monet) support.
  - Polished loading skeletons, animated progress indicators, and informative empty states.
- **Security & Privacy**:
  - Anti-SSRF URL validation (blocks localhost/private subnets).
  - Path traversal defense & filename sanitization.
  - Zero collection of credentials, authentication cookies, or private browsing data.

---

## 🏗 Modular Architecture

```
MediaFetch/
├── app/                  # Application initialization, MainActivity, Navigation Host
├── core/
│   ├── model/            # Normalized domain models (Platform, MediaInfo, MediaFormat, DownloadItem)
│   ├── common/           # Result monads, DataError definitions, Coroutine dispatchers, Formatters
│   ├── security/         # UrlValidator (anti-SSRF), FilenameSanitizer, SecurityPreferences (DataStore)
│   ├── network/          # OkHttp (TLS 1.3), Retrofit API client, NetworkMonitor
│   ├── database/         # Room Database, DownloadDao, MediaLibraryDao, TypeConverters
│   ├── download/         # MediaProvider interface, TikTok/IG/YT/FB providers, DownloadManager, ForegroundService
│   └── ui/               # Material 3 Theme, Typography, Reusable UI Components
├── feature/
│   ├── home/             # Home dashboard, Smart clipboard banner, Quick statistics
│   ├── analyzer/         # Analysis modal bottom sheet, Format picker, Mobile data warning
│   ├── downloads/        # Active downloads (live speed/progress) & History screen
│   ├── library/          # In-app media library (Grid/List, Filter, Search, Batch actions)
│   └── settings/         # Download preferences, Theming, Storage meter, Privacy policy
├── backend/              # Companion Fastify + TypeScript API Gateway with OpenAPI docs
├── docs/                 # Legal compliance guidelines and Play Store checklist
└── tests/                # Unit and integration test suites
```

---

## 🚀 Getting Started & Building

### Android Application
1. **Prerequisites**:
   - Android Studio Ladybug or newer.
   - JDK 17 or higher (`JAVA_HOME` set).
   - Android SDK API 35 installed.
2. **Build debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```
4. **Build Release Bundle**:
   ```bash
   ./gradlew bundleRelease
   ```

### Backend Gateway Service
1. **Prerequisites**:
   - Node.js 20+ and npm.
2. **Setup and run**:
   ```bash
   cd backend
   npm install
   npm run dev
   ```
3. **Run automated API tests**:
   ```bash
   npm test
   ```
4. **OpenAPI / Swagger Documentation**:
   Navigate to `http://localhost:8080/docs` in your browser.

---

## 🔒 Security & Fair Use Policy

MediaFetch strictly extracts legitimate, public, and user-authorized media. It **does not** bypass Widevine DRM, paywalls, or private account restrictions. See [LEGAL_COMPLIANCE.md](docs/LEGAL_COMPLIANCE.md) for details.
