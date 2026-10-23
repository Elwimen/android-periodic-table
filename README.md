Periodic Table
==============

An ad-free, open source, interactive periodic table for Android with a list of
element properties and a list of isotopes, based on Wikipedia's database.

This is a modernized fork of the original
[Frozen-Developers](https://github.com/Frozen-Developers/android-periodic-table)
app. The build and the source have been migrated to AndroidX and current
tooling so it builds and runs on recent Android versions (verified on Android 10
and Android 16); the app's features are unchanged.

Building
--------

Requirements:

- JDK 17 or newer (builds on up to JDK 26)
- Android SDK with platform `android-35` installed
- A `local.properties` at the repo root pointing at the SDK, e.g.
  `sdk.dir=/path/to/Android/Sdk`

Build a debug APK:

    ./gradlew assembleDebug

Build a signed release APK (see Signing below):

    ./gradlew assembleRelease

The output APKs are written under `PeriodicTable/build/outputs/apk/`.

Toolchain: Android Gradle Plugin 9.4.1, Gradle 9.8.2 (via the wrapper),
`compileSdk 35`, `minSdk 24`, `targetSdk 34`.

Signing
-------

Release signing is configured from a `keystore.properties` file at the repo
root, which is intentionally gitignored. Create it alongside your keystore:

    storeFile=release.keystore
    storePassword=...
    keyAlias=...
    keyPassword=...

When `keystore.properties` is absent (e.g. a fresh checkout), the release build
is produced unsigned instead of failing.

build_db.py
-----------

You can rebuild the element database at any time with `build_db.py`. The script
requires Python 3 and an internet connection; run it from the repo root. It
scrapes Wikipedia and overwrites
`PeriodicTable/src/main/res/raw/database.json`.

database.json
-------------

This file uses material from the Wikipedia article "Periodic table", which is
released under the Creative Commons Attribution-Share-Alike License 3.0.

Download
--------

Prebuilt APKs are published on the
[Releases](https://github.com/Elwimen/android-periodic-table/releases) page.
