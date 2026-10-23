# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

An ad-free, open-source, interactive periodic table for Android. Element and
isotope data is scraped from Wikipedia into a bundled JSON file; the app reads
it at runtime. Application ID `com.frozendevs.periodictable`. Single Gradle
module named `PeriodicTable`.

Originally a 2016-era Android Support Library app, it has been migrated to
**AndroidX** and current tooling so it builds and runs on recent Android
versions (verified on Android 10 and Android 16). The app's features are
unchanged from the original.

## Build & toolchain

- Android Gradle Plugin **9.4.1**, Gradle **9.8.2** (via the committed wrapper —
  use `./gradlew`), **AndroidX**.
- `compileSdk 35`, `minSdk 24`, `targetSdk 34`, Java 17 source/target.
- Builds on **JDK 17 or newer** (confirmed on JDK 26).
- Needs the Android SDK with platform `android-35`, and a `local.properties` at
  the repo root (`sdk.dir=/path/to/Android/Sdk`). Both `local.properties` and the
  signing files are gitignored.

Commands:

- Debug APK: `./gradlew assembleDebug`
- Signed release APK: `./gradlew assembleRelease` (see Release signing)
- Lint: `./gradlew lint` (`GoogleAppIndexingWarning`/`UnusedAttribute` ignored;
  `abortOnError false`)

There are **no unit or instrumentation tests**, so `assembleDebug` plus a manual
run is the only verification. Dependencies resolve from `google()` and
`mavenCentral()`.

## Release signing

Release signing is driven by a gitignored `keystore.properties` at the repo root:

```
storeFile=release.keystore
storePassword=...
keyAlias=...
keyPassword=...
```

`PeriodicTable/build.gradle` loads it conditionally — when the file is absent
(e.g. a fresh checkout), the release build is produced **unsigned** instead of
failing. The release build intentionally keeps **R8 / resource shrinking off**:
the database is parsed via Gson reflection, so shrinking risks stripping model
fields. Keep the keystore backed up — updates must reuse the same key.

## Regenerating the element database

`build_db.py` (repo root, **Python 3**, needs internet) scrapes the English
Wikipedia `Special:Export` API, parses the element infoboxes/templates in
parallel, and overwrites `PeriodicTable/src/main/res/raw/database.json` (sorted
by atomic number). Run it from the repo root — the output path is hardcoded
relative to it. The JSON keys map directly onto the Gson-deserialized model
fields (see `model/`), so renaming a field in the script or the Java models
requires changing the other.

## Architecture

Data flows one direction: `res/raw/database.json` → `content/Database` → model
objects → adapters → views.

- **`content/Database`** is the single read path into the JSON. It uses Gson's
  streaming `JsonReader` so the whole file is never held in memory:
  `getAllElements` iterates the array, `getElement(number)` skips to the Nth
  entry by index (1-based = atomic number). All loads go through the custom
  **`content/AsyncTaskLoader`** (an AndroidX `AsyncTaskLoader` subclass that
  caches its result across config changes), driven by the fragments'
  `LoaderManager`.

- **Models** (`model/`) are Gson/`Parcelable` data holders. `TableElementItem`
  is the base; `ElementProperties` extends it with the full per-element detail
  set and an `Isotope[]`. Element detail is passed between activities as a
  `Parcelable` extra.

- **Two-screen main UI**: `activity/MainActivity` hosts a `ViewPager` with two
  tabs — `fragment/ElementsFragment` (searchable `RecyclerView` list) and
  `fragment/TableFragment` (the zoomable table). Tapping an element launches
  **`activity/PropertiesActivity`**, itself a `ViewPager` of `PropertiesFragment`
  (grouped properties list) and `IsotopesFragment`.

- **Custom rendering**: `view/PeriodicTableView` extends
  `view/ZoomableScrollView` (a `FrameLayout` implementing pan/fling/pinch-zoom
  via `OverScroller`, `ScaleGestureDetector`, and the custom `widget/Zoomer`).
  The table is not a grid of child views — element tiles are rendered off the
  main thread onto a software `Canvas`, cached in an `LruCache`, then blitted.
  (The original used the `View` drawing cache, which is a no-op on modern
  Android.) Treat these two classes as performance-sensitive; the zoom math is
  guarded against an unset tile size producing an infinite scale.

- **Isotopes list** (`fragment/IsotopesFragment` + `model/adapter/IsotopesAdapter`)
  is an expandable list built on **advrecyclerview** (`com.h6ah4i…`). Its
  `getGroupId`/`getChildId` return *positions*, not `hashCode`s: advrecyclerview
  packs ids into a limited bit range that a 32-bit hash overflows (crash).

- **Shared-element transitions**: `PeriodicTableApplication` is a deliberately
  minimal holder that stashes a `SharedElementCallback` and an
  `OnAttachStateChangeListener` so the table fragment and `PropertiesActivity`
  can coordinate the activity-transition animation across the process-global
  `Application` instance.

- Other custom widgets live in `view/` and `widget/` (e.g. `RecyclerView` and
  `AppBarLayout` subclasses, `ExpandableIndicatorView`, `DividerDecoration`).

## Conventions

- Dependencies are **AndroidX** + Material (`androidx.appcompat`,
  `com.google.android.material`, `androidx.recyclerview`, `androidx.viewpager`,
  `androidx.loader`, `androidx.legacy:legacy-support-v4`) plus Gson, Picasso, and
  advrecyclerview. Do not reintroduce `com.android.support` dependencies.
- Member fields use the `mName` prefix; follow the existing style.
- Resource qualifiers carry the responsive layout: `values-sw600dp*` /
  `values-sw720dp*` and `-land` variants override `dimens.xml`; `-v17`/`-v21`
  variants exist for layouts and themes.
