# SpendSee Android

Modern expense tracking and budget management app for Android.

## Features

✅ **Smart Expense Tracking** - Calculator-style input for quick transactions
✅ **Budget Management** - Create and track budgets with visual progress
✅ **Analytics & Insights** - Beautiful charts and spending trends
✅ **Receipt Scanning** - OCR-powered receipt data extraction (Premium)
✅ **Multi-Account Support** - Track cash, bank, credit cards, and more
✅ **Home Screen Widgets** - Quick glance at your finances
✅ **Budget Notifications** - Reminders for upcoming payments (Premium)
✅ **Passcode Protection** - Secure with biometric authentication (Premium)
✅ **8 Beautiful Themes** - Customize your experience
✅ **24 Currencies** - Support for global currencies
✅ **Data Export/Import** - Full backup and restore capability

## Tech Stack

- **Language**: Kotlin 1.9+
- **UI**: Jetpack Compose + Material Design 3
- **Database**: Room Persistence Library
- **Architecture**: MVVM with ViewModels + StateFlow
- **Charts**: Vico library
- **OCR**: ML Kit Text Recognition v2
- **Billing**: Google Play Billing Library 6.0+
- **Widgets**: Jetpack Glance
- **Notifications**: AndroidX WorkManager
- **Camera**: CameraX
- **Biometrics**: AndroidX Biometric

## Requirements

- Android Studio Hedgehog | 2023.1.1 or later
- Gradle 8.0+
- minSdk: 26 (Android 8.0)
- targetSdk: 34 (Android 14)
- Kotlin 1.9+

## Project Structure

```
app/
├── src/main/java/com/spendsee/
│   ├── data/          # Room entities, DAOs, repositories
│   ├── ui/            # Compose screens and components
│   ├── viewmodels/    # ViewModels for each screen
│   ├── managers/      # Business logic managers
│   └── utils/         # Utilities and helpers
```

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/spendsee-android.git
   cd spendsee-android
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

## Premium Features

SpendSee offers a one-time premium purchase ($6.99) that unlocks:

- Receipt Scanning with OCR
- Custom Categories (unlimited)
- Quick Pay Budget
- Budget Notifications
- Passcode Protection with Biometric Auth

## Development Status

🚧 **In Development** - Active development following the [implementation plan](../ANDROID_IMPLEMENTATION_PLAN.md)

### Current Phase: Day 1 - Project Setup
- [x] Create repository
- [x] Initialize Git
- [ ] Configure Gradle and dependencies
- [ ] Set up Room database structure
- [ ] Implement core data models

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint checks
./gradlew lint
```

## Contributing

This is a personal portfolio project. Contributions, issues, and feature requests are welcome!

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Related Projects

- [SpendSee iOS](https://github.com/YOUR_USERNAME/spendsee) - iOS version built with SwiftUI

## Contact

- GitHub Issues: [Report bugs or request features](https://github.com/YOUR_USERNAME/spendsee-android/issues)
- Email: YOUR_EMAIL@example.com

---

**Built with ❤️ for better personal finance management**
