# PocketSense: Secure Income & Expense Tracker with Data Visualizations & Budget Goals

An offline-first, privacy-focused financial management Android application built with Jetpack Compose, Room Database, and Material Design 3. Features interactive spending analytics, monthly budget tracking with threshold alerts, AES-256 encrypted backups shareable directly to Google Drive, biometric/PIN access lock, multi-currency support, and quick-entry tools like tactile touch scroll balance adjustments and transaction auto-suggestions.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were aligned during the clarification interview:
> - **Biometrics & Security**: Biometric fingerprint/face unlock with 4-digit PIN fallback to ensure seamless security across both physical devices and emulators.
> - **Encrypted Cloud Backup**: AES-256-GCM encrypted export/import with direct Android Share Sheet integration for Google Drive, ensuring zero-knowledge privacy and GDPR compliance without requiring unneeded third-party cloud SDK bloat.
> - **Notifications & Alerts**: Native Android system notifications paired with real-time in-app budget milestone warnings (50%, 80%, 100% threshold alerts) and recurring transaction reminders.

---

## 1. Overview & Core Concept

- **What It Does**: PocketSense gives users complete sovereignty over their personal finances. Users can record income and expenses in seconds with smart auto-suggestions, adjust values with a custom touch-drag scroll bar, visualize monthly and historical cash flow through interactive charts, set category budgets, and safeguard their sensitive financial records behind biometric authentication and military-grade AES-256 encryption.
- **Target Audience**: Privacy-conscious individuals, budgeters, and professionals who want a fast, beautiful, and secure expense tracker without being tracked or locked into a subscription cloud.
- **Key Value**: 100% offline-first privacy, zero data collection, GDPR compliance, seamless encrypted backups to Google Drive, and fluid Material 3 ergonomics.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **App Launch & Biometric Verification**:
   - Splash screen checks lock status.
   - Prompts for Biometric authentication (or 4-digit PIN fallback).
   - Once verified, smoothly navigates to the Dashboard.

2. **Dashboard & Visual Analytics**:
   - **Hero Financial Health Card**: Net balance, total income, total expense for the active month, and base currency switcher.
   - **Interactive Charts Section**:
     - *Category Doughnut Chart*: Tap segments to inspect expense distribution and top spending categories.
     - *Income vs Expense Monthly Bar Chart*: 6-month historical cash flow trend with comparative bars.
     - *Spending Velocity Area Chart*: Cumulative spending pace versus budget over the month.
   - **Recent Transactions Feed**: Clean list with category icons, account tags, timestamps, and swipe-to-delete or tap-to-edit.

3. **Fast Data Entry (Speed & Precision)**:
   - Bottom sheet / dialog with segmented toggle (Expense vs Income).
   - **Auto-Suggestions Bar**: As the user types the title (e.g. "Gr..."), chips for "Groceries", "Green Tea", "Grab" appear with prefilled category icons.
   - **Tactile Touch Balance Scroller**: Free typing numeric input paired with a vertical touch slider / stepper bar for tactile fine-tuning (drag or tap `+1`, `+10`, `+50`, `+100`).
   - Category picker, account selector (Cash, Bank Account, Credit Card, Savings), date picker, and recurring toggle (Weekly, Monthly, Yearly).

4. **Monthly Budget Goals & Real-Time Alerts**:
   - Set monthly spending ceilings per category (e.g. Dining: $400, Shopping: $250).
   - Visual gauge progress bars:
     - Emerald Green (< 70% of budget)
     - Amber Orange (70% - 99% warning)
     - Crimson Red (Over budget alert with badge)
   - Real-time Android notification triggers when approaching or exceeding limits.

5. **Encrypted Backup & Google Drive Integration**:
   - Export screen prompts for a backup passphrase.
   - Encrypts entire database (transactions, categories, accounts, budgets, settings) into an AES-256-GCM package.
   - Opens Android system share sheet to save directly into Google Drive or local storage.
   - Import flow allows selecting any encrypted `.pocketsense` file, verifying the passphrase, and restoring data safely.

### Visual Identity & Theme
- **Color Palette (Google M3 Dynamic Modern)**:
  - *Primary*: Deep Sapphire / Indigo (`#1A73E8` / `#8AB4F8`)
  - *Secondary / Accent*: Mint Emerald (`#0F9D58` / `#81C995`) for income, Coral Vermilion (`#EA4335` / `#F28B82`) for expenses
  - *Tonal Surfaces*: Light surface (`#F8F9FA`), Dark surface (`#121316`), elevated card containers with subtle tonal borders (`#E0E3E7`)
- **Typography**:
  - Clean M3 typography scale: `DisplaySmall` for balances, `TitleMedium` for section headers, `BodyMedium` and `LabelSmall` for data points and chips.
- **Micro-Interactions**:
  - Tactile slider feedback on value adjustments.
  - Smooth animated progress bars and chart animations on tab changes.
  - Seamless animated bottom bar switching between *Dashboard*, *Transactions*, *Budgets*, and *Settings/Backup*.

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Why | Alternatives Considered |
| :--- | :--- | :--- | :--- |
| **Data Storage** | Local Room Database (SQLite) | 100% offline-first, instant response, zero latency, and absolute user privacy without external account requirements. | Cloud Firestore (would require forced Google Sign-In and online dependencies for basic ledger math). |
| **Cloud Sync & Backup** | AES-256-GCM Encrypted File + Drive Share | Full user data sovereignty, zero-knowledge encryption, GDPR compliant, works with any cloud drive without proprietary backend servers. | Direct Google Drive REST API with OAuth (requires OAuth consent screens, complex sync conflicts, and network access). |
| **Authentication** | BiometricPrompt + 4-Digit In-App PIN Fallback | Works immediately on devices with enrolled fingerprints/face as well as emulators without hardware biometrics. | Biometrics only (fails on un-enrolled emulator) or PIN only (slower user entry). |
| **Charts Engine** | Custom Jetpack Compose Canvas Charts | Ultra-lightweight, zero external dependencies, 100% customizable colors matching Material 3 theme, responsive animations. | Third-party chart libraries (risk of AGP/Kotlin incompatibility or bloat). |
| **Rapid Input** | Keyboard + Touch Scroller + Auto-Suggest | Combines precision typing with rapid tactile adjustments and one-tap re-use of common expenses. | Standard text box only (cumbersome on mobile). |

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├────────────────────────────────────────────────────────────────────────┤
│  ┌────────────────┐  ┌──────────────────┐  ┌────────────────────────┐  │
│  │   Auth Screen  │  │  Dashboard Screen│  │ Transactions & Entry   │  │
│  │ (Biometric/PIN)│  │ (Charts & Trends)│  │ (Auto-Suggest/Scroller)│  │
│  └────────────────┘  └──────────────────┘  └────────────────────────┘  │
│  ┌────────────────────────┐  ┌──────────────────────────────────────┐  │
│  │     Budgets Screen     │  │      Settings & Encrypted Backup     │  │
│  │  (Goals & Alert Gauge) │  │       (AES-256 & Drive Share)        │  │
│  └────────────────────────┘  └──────────────────────────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / Events
┌───────────────────────────────────▼────────────────────────────────────┐
│                       FinanceViewModel (MVVM)                          │
├────────────────────────────────────────────────────────────────────────┤
│  • Currencies & Formatting        • Filter & Range State (Month/Year)  │
│  • Auto-suggestions Generator     • Budget Threshold Calculations      │
│  • Security Lock State Machine    • Export / Import Crypto Operations  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Repository Pattern
┌───────────────────────────────────▼────────────────────────────────────┐
│                         FinanceRepository                              │
├────────────────────────────────────────────────────────────────────────┤
│  • TransactionsDao                • MonthlyBudgetsDao                  │
│  • CategoriesDao                  • AccountsDao                        │
│  • PreferencesDataStore / Crypto  • NotificationHelper                 │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ KSP Room Database
┌───────────────────────────────────▼────────────────────────────────────┐
│                    Room SQLite Database (AppDatabase)                  │
│  [transactions]  [categories]  [budgets]  [accounts]  [recurring]      │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model Entities
- **TransactionEntity**: `id`, `title`, `amount` (Double), `type` (EXPENSE/INCOME), `categoryId`, `accountId`, `currencyCode`, `timestamp`, `note`, `isRecurring`, `recurringInterval`.
- **CategoryEntity**: `id`, `name`, `iconName`, `colorHex`, `isDefault`, `type`.
- **MonthlyBudgetEntity**: `id`, `monthYear` ("YYYY-MM"), `categoryId`, `budgetLimit`, `currencyCode`.
- **AccountEntity**: `id`, `name`, `type` (Cash, Bank, Card, Wallet), `balance`, `currencyCode`, `colorHex`.
- **SecurityConfig & UserPreferences**: Base currency, biometric enabled, hashed PIN salt, last backup date.

### Security & Encryption Lifecycle (AES-256-GCM)
1. **Key Derivation**: PBKDF2WithHmacSHA256 (65,536 iterations, 128-bit random salt) converts user passphrase into a 256-bit AES key.
2. **Encryption**: AES/GCM/NoPadding generates a 12-byte initialization vector (IV) and 128-bit authentication tag.
3. **Payload Structure**: `[SALT (16B)] + [IV (12B)] + [CIPHERTEXT (JSON)]`.
4. **Export/Import**: Clean byte array encoded into a `.pocketsense` backup file, securely shared to Google Drive or verified and unpacked during import.

---

## 5. Verification Plan

1. **Compilation Verification**:
   - Execute `compile_applet` to ensure Room DAOs, entities, and Compose UI compile cleanly with zero errors.
2. **Security & Biometric Flow**:
   - Verify PIN creation, unlock verification, and lock state persistence.
3. **Data Management & Entry**:
   - Test adding income/expense with auto-suggestions and touch adjustment bar.
   - Verify monthly balance calculation and category filtering.
4. **Data Visualization**:
   - Verify Canvas-drawn doughnut, comparative bar charts, and spending trend curves adapt smoothly to sample and real entries.
5. **Budget Milestones & Notifications**:
   - Test adding budget targets, verifying gauge updates (green/orange/red), and triggering alerts.
6. **Encrypted Export & Import**:
   - Perform an encrypted export test with a sample passphrase, confirm payload validity, and test import restoration.
