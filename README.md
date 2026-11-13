# M3Note (MyMoneyManager)

Income and expense tracker for Android, built with Kotlin, Jetpack Compose and Material 3. Data lives in a local Room database, the app locks behind a PIN or biometrics, and backups are encrypted on the device before they are shared.

- App name: M3Note (repository: `MyMoneyManager`)
- Application ID: `com.aistudio.pocketsense.kxvtqa`, Kotlin package: `com.example`
- Version 1.0 (versionCode 1), minSdk 24, targetSdk 36

## Features

### Tracking

- Add income and expenses with a title, amount, category, account, date and optional note. Tap a transaction in the Transactions tab to edit it.
- Two entry points: a Quick Add dialog (dashboard button, always dated now) and a full Add Transaction sheet with title suggestions, a date picker and an optional recurring setting (weekly, monthly or yearly).
- 12 default categories (8 expense, 4 income) and 4 default accounts: Cash Wallet, Main Bank Checking, Credit Card, Savings. Account balances change when a transaction is added, edited or deleted. Categories and accounts can be added and edited from Vault.
- Search by title, note, category or date. Filter by type, category and date range. Deleting asks for confirmation.

### Insights

- Dashboard: month navigation, net cash flow, income and expense totals, a category donut chart, a 6-month income vs expense chart and the 5 latest transactions.
- Monthly budget goals per category. A budget shows a warning from 75% and goes over budget at 100%.
- A notification is sent when a new expense brings a category to 80% of its budget or more.

### Security and data

- 6-digit PIN stored with PBKDF2-HMAC-SHA256 (600,000 iterations, random salt). After the third wrong PIN the app locks out for 5 seconds, and the delay doubles with each further failure.
- Optional biometric unlock through `BiometricPrompt` (`BIOMETRIC_STRONG`) with an Android Keystore key.
- The app locks when it goes to the background. Screenshots and the recent-apps preview are blocked (`FLAG_SECURE`).
- Encrypted backup and restore, shared through the Android share sheet (for example to Google Drive).
- Clear All Data removes transactions, budgets and recurring alerts and resets account balances to zero. Categories and accounts are kept.

### Preferences

- 57 currencies and 23 languages, with right-to-left layout for Arabic, Persian and Urdu.
- Material 3 theme with dynamic color on Android 12 and later. Light and dark mode follow the system.

## Privacy

- The app source contains no network code. Data leaves the device only when you export and share a backup.
- Android auto-backup and device-to-device transfer are disabled for app data (`allowBackup="false"` and exclude-all backup rules).
- The build still includes Firebase, Retrofit, Moshi and OkHttp dependencies that no source file uses (template leftovers). The app's own manifest requests no network permission, but the merged manifest of the committed debug APK includes `INTERNET` and `ACCESS_NETWORK_STATE`, most likely contributed by those libraries.

## Tech stack

| Area | Details |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2024.09.00), Material 3, Material Icons Extended |
| State | One `FinanceViewModel`, Room `Flow` exposed as `StateFlow`, no DI framework |
| Storage | Room 2.7.0 (KSP) for data, SharedPreferences for settings and the PIN hash |
| Security | `androidx.biometric` 1.2.0-alpha05, Android Keystore, JCA (AES-GCM, PBKDF2) |
| Async | Kotlin Coroutines 1.10.2 |
| Build | Android Gradle Plugin 9.1.1, Gradle 9.3.1, KSP 2.3.5 |
| Tests | JUnit 4, Robolectric 4.16.1, coroutines-test, Compose UI test, Espresso |

## Getting started

### Requirements

- JDK 17 (the version CI uses)
- Android SDK with API level 36 (the build requests minor API level 1)
- Android Studio recent enough for AGP 9.1.1, or just the command line

### Build a debug APK

```bash
chmod +x gradlew
keytool -genkeypair -keystore debug.keystore -storepass android \
  -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 \
  -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
./gradlew assembleDebug
```

`gradlew` is committed without the executable bit. The `keytool` step is needed once: the `debugConfig` signing config reads `debug.keystore` from the project root and the file is gitignored. The APK is written to `app/build/outputs/apk/debug/`.

### Run tests

```bash
./gradlew testDebugUnitTest          # JVM tests in app/src/test
./gradlew connectedDebugAndroidTest  # instrumented tests, needs a device or emulator
```

CI builds the debug APK but does not run tests.

### CI and releases

The workflow in `.github/workflows/android.yml` runs on pushes to `main`, pull requests, `v*` tags and manual dispatch.

- Every run builds a debug APK and uploads it as the `app-debug` artifact.
- Pushing a tag that starts with `v` (for example `v1.0.1`) also builds a signed release APK and attaches it to a GitHub release. This needs the repository secrets `KEYSTORE_BASE64`, `STORE_PASSWORD` and `KEY_PASSWORD`.
- A local `assembleRelease` reads `KEYSTORE_PATH` (default: `my-upload-key.jks` in the project root), `STORE_PASSWORD` and `KEY_PASSWORD`. The key alias is `upload`.

A prebuilt debug APK is also checked in at `build-outputs/app-debug.apk` and may not match the latest source.

## Project structure

```
app/src/main/java/com/example/
  MainActivity.kt              Activity, setup and lock gates, bottom navigation, biometric prompt
  data/
    model/                     Room entities, enums, supported currencies and languages
    dao/FinanceDaos.kt         Room DAOs
    database/AppDatabase.kt    Room database "m3note_db" and first-run seed data
    repository/                FinanceRepository: CRUD, balance updates, budget alerts, backup and restore
    crypto/CryptoManager.kt    Backup encryption and JSON (de)serialization
  security/                    SecurityPreferences: PIN hashing, lockout, settings
  notification/                NotificationHelper: budget and recurring-bill channels
  ui/
    screens/                   Dashboard, Transactions, Budgets, Vault (BackupSettingsScreen), lock, setup, add sheets
    components/                Charts, suggestion field, amount scroller, pickers, category icons
    theme/                     Material 3 colors, typography, theme
    i18n/AppStrings.kt         UI strings for 23 languages
    viewmodel/                 FinanceViewModel
app/src/test/                  JVM unit tests
app/src/androidTest/           Instrumented tests
```

## Backup file format

`.m3note` files are laid out as:

```
"M3NOTE_ENC_V1" | salt (16 bytes) | IV (12 bytes) | AES-256-GCM ciphertext with 128-bit tag
```

The key comes from `PBKDF2WithHmacSHA256` over your passphrase (65,536 iterations, 256 bits). The plaintext is JSON with `version`, `exportTimestamp`, `transactions`, `categories`, `budgets`, `accounts` and `recurringAlerts`.

Restore replaces all existing data. It also accepts files with the older headers `MYMONEYMANAGER_ENC_V1`, `EXPENSEWISE_ENC_V1` and `POCKETSENSE_ENC_V1`. A backup cannot be recovered if the passphrase is lost.

## Known limitations

Observed at commit `3dca4ae` by reading the code, not reproduced at runtime.

- Budgets are loaded for the month the app was started in. Selecting another month on the dashboard shows no budgets for it.
- Recurring transactions only create a reminder banner on the dashboard. Nothing schedules reminder notifications or posts the next transaction.
- Quick Add always stamps the current time. Editing a transaction cannot change its recurring setting, and categories and accounts cannot be deleted.
- Account balances are tracked but not displayed anywhere in the UI.
- Some dialogs, messages and notifications are English only.

## Contributing

See [AGENT.md](AGENT.md) for conventions, the architecture map and guidance for AI coding agents.

## License

Licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE).
