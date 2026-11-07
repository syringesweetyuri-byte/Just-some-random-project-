# WA Classic - Implementation Plan

A lightweight, dedicated Android WebView application designed to load WhatsApp Web (`https://web.whatsapp.com/`) with desktop browser emulation, persistent session handling, data management, and compatibility optimizations for Android 5.0+ (API 21+) devices such as the Samsung Galaxy Note 3.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Android 5.0 (Lollipop) Compatibility Limitation:**
> WhatsApp Web requires modern web standards (ES6+, WebCrypto, IndexedDB, Web Workers, WASM). On Android 5.0 (Galaxy Note 3), WhatsApp Web will render the QR code and login screen smoothly **provided the device has its Google Android System WebView package updated** (or Bromite/Trichrome WebView) rather than the factory-bundled Chromium 37 from 2014. The app includes a non-intrusive Diagnostic & Compatibility panel that displays the active WebView package version and actionable update steps.

- **Confirmed Decision 1**: Desktop Chrome User-Agent spoofing (`Mozilla/5.0 (Windows NT 10.0; Win64; x64) ...`) to bypass WhatsApp's mobile redirect.
- **Confirmed Decision 2**: Slim, non-intrusive top action bar with quick controls (Refresh, Clear Data, Fullscreen toggle, Info/Diagnostics) and graceful hardware back-button handling.
- **Confirmed Decision 3**: Strict zero-telemetry architecture — no credentials, cookies, or chat data are ever touched, inspected, or transmitted outside the native Android `WebView` sandbox.

---

## 1. Overview & Core Concept

- **What It Does**: Emulates a desktop workstation browser to access WhatsApp Web directly on an Android device, maintaining active session cookies across app restarts, supporting QR scanning from a primary phone, and handling network fluctuations gracefully.
- **Target Audience / Persona**: Users repurposing older tablets or smartphones (like the Galaxy Note 3 SM-N900K, 3GB RAM, Android 5.0) as dedicated secondary communications consoles, desk displays, or lightweight chat terminals.
- **Key Value**: Provides a distraction-free, native wrapper around WhatsApp Web with minimal memory footprint (under 60MB base memory), zero bloatware, and dedicated controls for refreshing and clearing session caches.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Cold Start & Session Initialization**:
   - App launches directly into a sleek, dark-toned interface themed with WhatsApp classic palette (`#075E54` deep teal, `#128C7E` teal accent, `#111B21` background).
   - `CookieManager` is verified and sync-flushed.
   - Desktop User-Agent is applied to `WebSettings`.
   - Smooth horizontal indeterminate loading bar signals page loading progress.
2. **QR Code Pairing**:
   - `https://web.whatsapp.com/` displays the pairing QR code.
   - The user scans the QR code using their primary phone's WhatsApp ("Linked Devices").
   - Session tokens and IndexedDB records persist across restarts via `CookieManager.flush()`.
3. **In-Session Use & Navigation**:
   - Slim app bar provides: Refresh, Fullscreen mode (collapses app bar for maximum screen real estate), Diagnostics info, and Clear Data.
   - Hardware Back button triggers `webView.goBack()` if history exists; exits app only if at the root page.
4. **Network Loss & Error Recovery**:
   - If Wi-Fi/data drops or WhatsApp fails to load, a clean native error view appears with error code, friendly explanation, and an instant "Retry Connection" button.
5. **Clear Website Data**:
   - Accessible via the top bar menu. Displays a confirmation dialog before wiping cookies, web storage, and cache, allowing a clean sign-out if needed.

### Visual Identity & Theme
- **Color Palette**:
  - Background: `#111B21` (Dark, power-saving on Note 3 Super AMOLED display)
  - Surface: `#202C33` (Chat surface elevated)
  - Primary / Brand: `#00A884` (WhatsApp Vibrant Green)
  - Secondary Accent: `#128C7E` (Classic WhatsApp Teal)
  - Text Primary: `#E9EDEF`
  - Text Secondary: `#8696A0`
- **Typography & Layout**:
  - Clean Material 3 typography with readable system font stack.
  - Safe-area aware (`WindowInsets.safeDrawing`) for modern edge-to-edge screens while remaining backwards compatible down to API 21.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct Android WebView with Jetpack Compose Wrapper vs. Pure View System**
  - *Chosen Approach*: Lightweight Jetpack Compose Scaffold wrapping a configured `AndroidView(factory = { WebView(...) })`.
  - *Why*: Allows crisp Material 3 dialogs, snackbars, and progress indicators, while keeping the web runtime 100% native Android WebView with full control over `WebSettings`, `CookieManager`, `WebViewClient`, and `WebChromeClient`.
- **Decision 2: Memory & Battery Optimization for 3GB RAM / ARMv7**
  - *Chosen Approach*: Enable hardware acceleration, cache mode `LOAD_DEFAULT`, disable unnecessary background services, and call `CookieManager.flush()` explicitly on lifecycle pause.
  - *Why*: Galaxy Note 3 handles standard Chromium rendering comfortably with 3GB RAM, but needs memory disciplined to prevent background OS kills.
- **Decision 3: Zero-Telemetry & Zero-Mock Guarantee**
  - *Chosen Approach*: Absolutely no third-party analytics, no tracking libraries, no mock chat feeds. The app is a direct conduit to WhatsApp Web.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                      WA Classic                        │
│                 (Jetpack Compose + M3)                 │
├────────────────────────────────────────────────────────┤
│  TopAppBar (Title, Reload, Fullscreen, Data, Info)     │
│  Progress Indicator (Smooth linear progress bar)       │
├────────────────────────────────────────────────────────┤
│                 AndroidView Container                  │
│  ┌──────────────────────────────────────────────────┐  │
│  │                android.webkit.WebView            │  │
│  │  - User-Agent: Desktop Chrome (Win64)            │  │
│  │  - JavaScriptEnabled: true                       │  │
│  │  - DomStorageEnabled: true                       │  │
│  │  - DatabaseEnabled: true                         │  │
│  │  - MixedContent: MIXED_CONTENT_NEVER_ALLOW       │  │
│  └──────────────────────────────────────────────────┘  │
│                           ▲                            │
│                           │                            │
│             CookieManager & WebStorage                 │
│  - setAcceptCookie(true)                               │
│  - setAcceptThirdPartyCookies(webView, true)           │
│  - CookieManager.getInstance().flush() on pause        │
└────────────────────────────────────────────────────────┘
```

### Components & Responsibilities
- **`MainActivity.kt`**: Main entry point, managing edge-to-edge window insets, back-press handling via `BackHandler`, and state orchestration (loading state, error state, fullscreen toggle).
- **`WAWebView.kt`**: Encapsulates WebView creation, desktop User-Agent configuration, `WebViewClient` (error interception, URL routing), `WebChromeClient` (progress updates, title sync), and cookie persistence.
- **`CompatibilityDialog.kt`**: Informational dialog displaying device details, active WebView package version, and guidance for updating WebView on Android 5.0.
- **`ClearDataDialog.kt`**: Confirmation modal to purge cookies, cache, and HTML5 Web Storage.
- **`AndroidManifest.xml`**: Configures `INTERNET`, `ACCESS_NETWORK_STATE`, hardware acceleration, and cleartext traffic security.
- **`metadata.json`**: Sets project name to "WA Classic" and description.

---

## 5. Verification Plan

1. **Compilation Verification**:
   - Run `compile_applet` to confirm Gradle build and Kotlin/Compose compilation succeed cleanly.
2. **Configuration Inspection**:
   - Verify `minSdk = 21` compatibility in `build.gradle.kts` and check that dependencies do not introduce API > 21 requirements.
   - Verify User-Agent string conforms to Desktop Chrome standards.
   - Verify `CookieManager` initialization and `flush()` lifecycle hooks.
3. **Interactive & Visual Checks**:
   - Confirm proper display of top app bar, loading progress bar, back navigation, and error recovery screens.
