# Deep Sleep — Sounds & Stories (v3.1 Clean Edition)

- **Package Name (`applicationId` / `namespace`)**: `com.sleepsounds.storytelling`
- **Play Store Version**: `versionCode = 31`, `versionName = "3.1"`
- **SDK Configuration**: `minSdk = 26`, `targetSdk = 37`, `compileSdk = 37`
- **Architecture**: Clean Architecture + MVVM + Hilt + Jetpack Compose + Room SQLite (`somni.db` v6) + Media3 ExoPlayer Foreground Service + Google Play Billing v7.1.1
- **Cloud & Database Integration**:
  - **Firebase Realtime Database**: `https://musicsleep-d3b67-default-rtdb.firebaseio.com/` (`content` and `store` nodes)
  - **Firebase Remote Config**: `r2_base_url`, `worker_base_url`, `use_worker_routes`
  - **Cloudflare R2 & Worker CDN**: `https://pub-902b89e71204483092cf27a11d18d989.r2.dev/` with Worker route support (`/files/{path}`)
