# FireBoost

## Android gaming launcher and control center

FireBoost is a Free Fire-focused Android utility for launching the game, inspecting device/input state, saving gaming profiles, testing connectivity and exposing safe device-side gaming controls.

### Included in this build

- Red/black FireBoost dashboard UI
- Free Fire package detection and launcher
- Android 8+ target (`minSdk 26`)
- Battery and temperature reporting where Android exposes the data
- RAM and display information
- Wi-Fi/mobile/connected network detection
- Network reachability test
- Keyboard, mouse and gamepad/HID detection
- Input-device vendor/product information
- Physical key/button capture into a profile
- Mouse information page and control recommendations
- Sensitivity/control profiles stored locally
- Performance recommendations and Android battery controls
- Cooling/thermal information
- Safe storage-settings shortcut
- Free Fire graphics/FPS guide
- Floating gaming dashboard using Android overlay permission and a foreground service
- GitHub Actions debug APK builder

### Important Android limitations

FireBoost reports what Android exposes. DPI, polling rate, hardware temperature sensors and some performance controls are manufacturer-dependent and are not invented when unavailable.

The mapper is a legitimate input-configuration layer. Android and the game determine whether relative mouse camera input can be consumed. FireBoost does not inject automated aim, recoil control, modify game memory/files, spoof emulator detection or bypass anti-cheat.

The floating dashboard requires the user to grant **Display over other apps** permission. Android 14+ also applies foreground-service rules that can vary by device/version.

### Build without Android Studio

Push the repository to GitHub, open **Actions**, select **Build FireBoost APK**, and run it manually. The workflow uses Java 17, Android SDK 35 and Gradle 8.10.2 on GitHub's Ubuntu runner.

The output is uploaded as the `FireBoost-APK` artifact.

### Project structure

```text
FireBoost-Android/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/com/fireboost/launcher/
│           ├── MainActivity.kt
│           └── OverlayDashboardService.kt
├── .github/workflows/build-apk.yml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

### License

MIT
