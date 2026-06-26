# 🐉 Dragonic Decoder

<div align="center">

![Dragonic Decoder Banner](https://img.shields.io/badge/Dragonic-Decoder-00D4FF?style=for-the-badge&logo=android&logoColor=white)
![Version](https://img.shields.io/badge/Version-1.0.0-00FF88?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)
![License](https://img.shields.io/badge/License-MIT-7B2FFF?style=for-the-badge)
![Build](https://img.shields.io/github/actions/workflow/status/your-username/dragonic-decoder/build.yml?style=for-the-badge&label=BUILD)

**A powerful cyberpunk-themed encoding/decoding tool for Android**

*100% Offline · No Ads · No Login · No Server*

</div>

---

## 📸 Screenshots

> Cyberpunk neon UI · Dark mode only · Futuristic hacker interface

| Splash | Home | Decode | History |
|--------|------|--------|---------|
| Dragon Logo | Input + Quick Tools | Result + Info | Workflow Stats |

| Analyzer | Downloads | Settings |
|----------|-----------|----------|
| File Detection | Saved Files | App Config |

---

## ✨ Features

### 🔓 Universal Decoder / Encoder
| Type | Decode | Encode |
|------|--------|--------|
| Base64 | ✅ | ✅ |
| Base32 | ✅ | ✅ |
| Base16 / Hex | ✅ | ✅ |
| URL | ✅ | ✅ |
| JWT | ✅ | — |
| Unicode Escape | ✅ | ✅ |
| HTML Entity | ✅ | ✅ |
| Binary | ✅ | ✅ |
| Octal | ✅ | ✅ |
| ROT13 | ✅ | ✅ |
| Caesar Cipher | ✅ | ✅ |

### 📁 File Decoder
- Open and decode TXT, JSON, XML, CSV, LOG, CONF, DAT, BIN files
- Automatic encoding detection per file
- Hex viewer for binary files

### 🔬 Analyzer
- **Auto-detect encoding** (Base64, Hex, Binary, JWT, URL, etc.)
- **Entropy calculation** to measure randomness / encryption strength
- File metadata viewer (size, lines, characters)
- One-tap decode recommendation

### 📜 History (Workflow Status)
- All decode operations logged with status (Success / Failed)
- Animated statistics counters (Total / Success / Failed / Running)
- Search through history
- Export history as text
- Long-press to delete individual entries

### 💾 Downloads
- Save decoded results as files on device
- View all saved files with size and date
- Open in external app
- Delete individual files or clear all

### ⚙️ Settings
- Auto Save toggle
- Save History toggle
- Vibrate feedback toggle
- Clear History / Downloads
- Reset all settings
- App version & about info

---

## 🎨 Design

- **Theme:** Cyberpunk neon blue — dark mode only
- **Colors:** `#00D4FF` neon blue · `#050A0F` deep black · `#00FF88` neon green
- **Typography:** Monospace + condensed sans-serif for that hacker aesthetic
- **Cards:** Glassmorphism with glowing neon borders
- **Animations:** Fade in/out · slide transitions · animated stat counters
- **Navigation:** Bottom nav bar with 5 tabs

---

## 🏗️ Architecture

```
app/
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt       # Room database
│   │   ├── HistoryEntry.kt      # History entity
│   │   ├── HistoryDao.kt        # History queries
│   │   ├── SavedFile.kt         # File entity
│   │   └── SavedFileDao.kt      # File queries
│   └── repository/
│       └── DecoderRepository.kt # Single source of truth
├── ui/
│   ├── splash/
│   │   └── SplashActivity.kt
│   ├── MainActivity.kt          # Bottom nav host
│   ├── home/
│   │   └── HomeFragment.kt
│   ├── history/
│   │   ├── HistoryFragment.kt
│   │   ├── HistoryViewModel.kt
│   │   └── HistoryAdapter.kt
│   ├── analyzer/
│   │   └── AnalyzerFragment.kt
│   ├── downloads/
│   │   ├── DownloadsFragment.kt
│   │   └── DownloadsAdapter.kt
│   ├── settings/
│   │   └── SettingsFragment.kt
│   └── decoder/
│       └── DecoderDetailActivity.kt
└── utils/
    ├── DecoderEngine.kt         # All encode/decode logic
    ├── FileUtils.kt             # File I/O helpers
    └── Extensions.kt           # Kotlin extension functions
```

### Tech Stack
- **Language:** Kotlin 100%
- **Architecture:** MVVM (ViewModel + LiveData)
- **Database:** Room (SQLite)
- **Async:** Coroutines + viewModelScope
- **UI:** Material Design 3 + ViewBinding
- **Navigation:** Navigation Component
- **Min SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34 (Android 14)

---

## 🚀 Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 34

### Clone & Open
```bash
git clone https://github.com/your-username/dragonic-decoder.git
cd dragonic-decoder
```

Open in Android Studio → `File > Open` → select the `DragonicDecoder` folder.

### Build Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

## 🤖 GitHub Actions CI/CD

Every push to `main` / `master` automatically:

1. **Builds** debug + release APK
2. **Runs** unit tests
3. **Uploads** APKs as GitHub Actions Artifacts
4. **Creates** a GitHub Release with the APK attached

### Workflow file: `.github/workflows/build.yml`

```yaml
# Triggers on push to main/master
# Jobs: build → test → release
```

### Download Latest APK
→ Go to **Actions** tab → latest workflow run → **Artifacts** section → download `DragonicDecoder-Release-*`

Or go to **Releases** tab for tagged release APKs.

---

## 📦 Installation

1. Download the latest APK from [Releases](https://github.com/your-username/dragonic-decoder/releases)
2. On your Android device: **Settings → Apps → Special app access → Install unknown apps**
3. Enable for your file manager / browser
4. Tap the downloaded APK to install

**Requirements:** Android 8.0+ (API 26+) · ~8 MB installed

---

## 🔒 Privacy

- **100% offline** — zero network requests
- **No analytics** — no Firebase, no Crashlytics
- **No ads** — completely ad-free
- **No login** — no account needed
- All data stays on your device

---

## 📄 License

```
MIT License

Copyright (c) 2024 Leonore Tech Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

---

## 👥 Credits

Developed by **Leonore Tech Team**

---

<div align="center">

**🐉 Dragonic Decoder** — *Decode the Matrix*

</div>
