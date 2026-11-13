# AGENT.md

Guidance for AI coding agents (and humans) working in this repository. See [README.md](README.md) for features and build steps.

## Project snapshot

- Android app named M3Note (repository `MyMoneyManager`), single module `:app`.
- Kotlin 2.2.10, Jetpack Compose with Material 3, Room 2.7.0 (KSP), AGP 9.1.1, Gradle 9.3.1.
- Package and namespace `com.example`, applicationId `com.aistudio.pocketsense.kxvtqa`, minSdk 24, compile and target SDK 36.
- Started from a Google AI Studio template. Some build settings are required by AI Studio (see "Do not change without being asked").
- Offline-first. No DI framework, no navigation library, no network features.

## Commands

```bash
chmod +x gradlew                      # committed without the executable bit
./gradlew assembleDebug               # needs debug.keystore in the project root, see README
./gradlew testDebugUnitTest           # JVM unit tests
./gradlew connectedDebugAndroidTest   # needs a device or emulator
```

CI (`.github/workflows/android.yml`) runs only `assembleDebug`, so a green CI says nothing about tests. `debug.keystore` is gitignored and CI creates it with `keytool` (the command is in the workflow and the README).

## Architecture

Data flow: Room DAO `Flow` -> `FinanceRepository` -> `FinanceViewModel` (`StateFlow`, `stateIn(WhileSubscribed(5000))`) -> Compose screens.

- `MainActivity.kt`: `MainActivity` is a `FragmentActivity` (required by `BiometricPrompt`), sets `FLAG_SECURE`, and calls `lockApp()` in `onStop` unless the configuration is changing. `MyMoneyManagerApp` gates on setup, then lock, then shows a `Scaffold` with four tabs chosen by the `AppScreen` enum and a `when`.
- `data/model`: entities `TransactionEntity`, `CategoryEntity`, `MonthlyBudgetEntity`, `AccountEntity`, `RecurringAlertEntity`; enums; `SupportedCurrencies` (57) and `SupportedLanguages` (23).
- `data/database/AppDatabase.kt`: database `m3note_db`. Seeds 12 categories (ids 1 to 12) and 4 accounts (ids 1 to 4) in `onCreate`.
- `data/repository/FinanceRepository.kt`: transaction insert, update and delete with account balance updates, budget alert check, backup create and restore, clear data.
- `data/crypto/CryptoManager.kt`: AES-256-GCM backup encryption and hand-written `org.json` (de)serialization.
- `security/SecurityPreferences.kt`: SharedPreferences file `m3note_security_prefs` (PIN hash, lockout, base currency, language, toggles).
- `notification/NotificationHelper.kt`: channels `budget_alerts_channel` and `recurring_alerts_channel`.
- `ui/i18n/AppStrings.kt`: `AppStrings` data class (English defaults) and `appStringsFor(code)`, provided to the UI through `LocalAppStrings`.

## Working rules

Understand first

- Read the relevant code before changing it. If a requirement is ambiguous or missing, ask one clear question. If the task is clear, do not ask.
- Do not guess behavior, APIs, file locations or conventions.

Keep changes small

- Make the smallest correct change. Do not touch unrelated code, rename things, reformat, refactor nearby code or bump dependencies unless the task needs it.
- Preserve existing behavior outside the requested change. Keep bug fixes separate from cleanup.
- Search before writing a helper and reuse what exists: `incomeColors()`, `expenseColors()`, `warningColors()`, `CategoryIconHelper.getIcon`, `LocalAppStrings`, `SupportedCurrencies`, `filterTransactions`, and the ViewModel's `getCategorySlices`, `getHistoricalTrends` and `getBudgetProgress`.
- No extra abstractions, wrappers, interfaces, configuration, boilerplate, fallbacks or defensive code without a concrete need. Do not optimize without evidence.
- No new dependencies when the platform or an existing dependency already covers the need.

Errors and comments

- No `try/catch` that only logs and rethrows, no new logging without a reason, no silent swallowing. Follow existing patterns: the ViewModel reports export and import failures through callbacks, and `NotificationHelper` ignores `SecurityException` when notifications are denied.
- Comment only to explain why, in one short line. No comments that restate the code and no KDoc on trivial functions.

Style

- Match the surrounding file: 2-space indentation, explicit imports (no wildcards in `src/main`), no trailing commas, double quotes. There is no `.editorconfig`.
- `testTag` values are snake_case (for example `quick_add_save_button`). Keep existing tags stable.
- No em dashes in chat, code or docs.

Verify

- Verify Android, Compose, Room, Biometric and Keystore APIs against official documentation or source for the versions in `gradle/libs.versions.toml`. If an API cannot be verified, say so.
- Run the smallest relevant test. Never claim something was tested or verified when it was not, and keep code inspection, API verification and runtime testing separate in reports. Biometric and Compose UI behavior need a device or emulator.

Git and reporting

- Stage only the files you changed. Keep commit messages short and about the actual change. Do not push unless asked. Do not commit build outputs.
- Final reports: concise and technically precise. State the root cause, list the changed files and say what remains unverified.

Security

- No hardcoded credentials or keys. Do not weaken the PIN, lock or crypto checks. Do not add network access, analytics or telemetry.

## Do not change without being asked

- `signingConfigs` and the `signingConfig` assignments in `app/build.gradle.kts`. Comments there say AI Studio needs them.
- `FLAG_SECURE` and the lock in `MainActivity.onStop`.
- Room schema: `AppDatabase` is version 1 with `exportSchema = false`, no migrations and no destructive fallback. Any entity change needs a version bump and a `Migration`, plus matching edits to `BackupData`, `CryptoManager.serializeToJson` and `parseFromJson` so backups still round-trip.
- Backup format: header `M3NOTE_ENC_V1`, 16-byte salt, 12-byte IV, PBKDF2 with 65,536 iterations. A format or KDF change needs a new header while `decryptBackup` keeps accepting the old ones (including `MYMONEYMANAGER_ENC_V1`, `EXPENSEWISE_ENC_V1` and `POCKETSENSE_ENC_V1`).
- PIN storage: PBKDF2 with 600,000 iterations, plus the migration of legacy SHA-256 hashes in `SecurityPreferences.verifyPin`.
- Template leftovers: Firebase, Retrofit, Moshi, OkHttp, the Secrets plugin and google-services are not used by any source file. Do not build on them. Do not remove them unless asked, and if you do, check the merged manifest afterwards.

## Conventions and gotchas

- Month keys are `"yyyy-MM"` strings from `SimpleDateFormat` with `Locale.getDefault()`, repeated in several files. The ViewModel calls `Locale.setDefault` on language change, so formatting depends on it.
- Amounts are `Double`. Each transaction stores the base currency code at insert time and there is no conversion.
- Account balances change only through `FinanceRepository.insertTransaction`, `updateTransaction` and `deleteTransaction`. Route any new insert or delete path through them. Restore writes the backed-up balances as they are.
- New UI text goes into `AppStrings` (English default) and the other 22 branches of `appStringsFor`. A locale that omits a key falls back to English. Many dialogs, ViewModel messages and notifications are still hardcoded English.
- Pure screen logic is exposed as `internal` top-level functions so it can be unit tested without Compose (`filterTransactions`, `utcDateMillisToLocalMillis`, `sanitizeQuickAddAmountInput`, `parseQuickAddAmount`, `resolveQuickAddTitle`). Follow that pattern only when logic needs a test.
- The add flows preselect the account whose name contains "Bank", otherwise the first account.

## Known issues

Observed at commit `3dca4ae` by reading the code. Not reproduced at runtime. Fix only when asked, and update this list when one is fixed.

1. **Budgets ignore the selected month.** `FinanceViewModel.monthlyBudgets` calls `getBudgetsForMonth(getCurrentMonthYear())` once when the ViewModel is created. Budgets for other months do not load, and one saved while another month is selected is not shown.
2. **Recurring reminders never fire.** `sendRecurringBillReminder` is only called from the Vault test button and `sendMilestoneNotification` has no caller. Nothing calls `updateAlert` or `deleteAlert`, so alerts are never advanced or deactivated, and the dashboard banner shows only `activeAlerts.first()`.
3. **Restore is not atomic.** `restoreEncryptedBackup` clears and refills tables without a Room transaction, so a failure part-way leaves partial data.
4. **Missing flows.** `DashboardScreen` receives `accounts` but no screen reads balances, so they are never shown. Quick Add always uses the current time, the edit sheet hides the recurring setting, and categories and accounts cannot be deleted.
5. **Thresholds differ.** The UI warns from 75% and flags over budget at 100%. Notifications fire at 80% and above.

## Tests

- `SecurityPreferencesTest`: PIN hashing, lockout, legacy hash migration, currency and language persistence.
- `TransactionsFilterTest`: search and filters, UTC to local date range conversion, date picker to local timestamp conversion.
- `QuickAddTransactionHelperTest`: amount sanitizing and parsing, title fallback.
- `ExampleUnitTest.testAllSupportedLanguagesHaveTranslations`: checks a few keys per language. `testAes256GcmCryptoAlgorithm` repeats the JCA steps inline and does not call `CryptoManager`.
- `ExampleRobolectricTest`, `ExampleUnitTest.addition_isCorrect` and `ExampleInstrumentedTest` are template placeholders.
- No coverage yet for `CryptoManager`, `FinanceRepository`, `FinanceViewModel` or Compose UI, and no test uses the `testTag` values.

## Repo hygiene

- `public/assets/aistudio/` holds AI Studio images and a pasted diff. It is not part of the build.
- `.gitignore` covers `debug.keystore`, `.env`, `local.properties` and `*.patch`.
