# Deductive: Case Files

Deductive: Case Files is an open-source Android detective game released under the GNU General Public License, version 3 (GPLv3). The source code is available to read, modify, and redistribute under the terms of that license. See [LICENSE](LICENSE).

The game includes 120 offline investigations in English and Ukrainian. Read the evidence, fill in the logic grid, and identify the culprit. Some cases add alibis, ciphers, timelines, or visual evidence.

## Build

Use JDK 17 and Android SDK Platform 36. Set up the Android SDK through Android Studio or point `local.properties` to your installation.

```sh
./gradlew :app:assembleDebug
bash tools/verify.sh
```

The SVG artwork and its Android drawable exports can be regenerated with `python3 tools/artwork/generate.py`. Previews for Google Play are in [artwork/google-play](artwork/google-play).

## Sign a release

Set `DCF_KEYSTORE`, `DCF_KEY_ALIAS`, `DCF_STORE_PASSWORD`, and `DCF_KEY_PASSWORD` in your local environment. Run `./gradlew :app:assembleRelease :app:bundleRelease` to build the APK and Android App Bundle. Store the signing key and passwords outside the repository.

The bundled Gradle Wrapper retains its upstream Apache 2.0 license.
