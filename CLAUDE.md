# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

kcurses is a Kotlin/Native wrapper and DSL over the ncurses C library for building terminal UIs. Published to Maven
Central and GitHub Packages.

## Commands

- `./gradlew assemble` — build all configured native targets for the host
- `./gradlew check` — run tests (currently no test sources)
- `./gradlew :cinteropNcurses<Target>` — regenerate the ncurses cinterop bindings, e.g. `cinteropNcursesMacosArm64`
- `./gradlew publishToMavenLocal` — publish a local snapshot for downstream consumption
- `./gradlew dokkaGeneratePublicationHtml` — build the API docs; `bash dokka.sh <sha>` deploys them to the `gh-pages`
  branch
- `./gradlew publish<Target>PublicationToMavenLocal` — publish a single target locally (e.g. `MacosArm64`); useful
  because the host-detection logic only ever exposes one target at a time

Local builds require ncurses headers on the host: `brew install ncurses` on macOS, `apt-get install libncurses-dev` on
Linux.

## Architecture

### Host-detection target config

`build.gradle.kts` configures **exactly one** Kotlin/Native target at a time, chosen from the host's OS + arch (
`linuxX64`, `macosArm64`). You cannot build a target whose host you aren't running on locally —
multi-target coverage happens in CI via separate runners, not via cross-compilation.

The Xcode SDK include dir is only injected into the cinterop block for `macos*` targets; Linux relies on default header
search paths (which is why `libncurses-dev` must be installed).

### Cinterop layer

`src/interop/ncurses.def` declares which headers + libraries to bind. The generated Kotlin package is `ncurses`;
consumers see it as `import ncurses.<symbol>`. macOS uses the Homebrew ncurses install path; Linux uses the system
default. The C bindings are wrapped by hand-written Kotlin idiomatic functions in `src/common/main/kcurses/`.

### Non-standard source set layout

`configureSourceSets()` (in `build.gradle.kts`) overrides the conventional `src/<sourceSet>Main/kotlin/` layout. Sources
for a target named `<platform>Main` live at `src/<platform>/main/` (and `src/<platform>/test/` for tests). When adding
new files, place them under `src/<platform>/main/<package-path>/` — not the default
`src/<platform>Main/kotlin/<package-path>/`.

### Multi-host publishing pattern

- `.github/workflows/build.yml` and `release.yml` define a matrix per OS, each entry tagged with
  `publication: <TargetName>` and `is_main: <bool>`.
- Each host runs `publish<Target>PublicationTo<Repo>Repository` for its own target's artifact.
- Only the `is_main` entry (linuxX64) also runs `publishKotlinMultiplatformPublicationTo<Repo>Repository` — this avoids
  `.module` metadata clobbering between hosts. Keep exactly one matrix entry marked `is_main: true`.
- `mavenPublishing { publishToMavenCentral(true) }` enables auto-release on Central Portal, so each per-target
  deployment promotes itself after upload (no separate close/release step needed).

`gradle.properties` sets `kotlin.native.ignoreDisabledTargets=true` and `kotlin.native.enableKlibsCrossCompilation=true`
to make this work cleanly.

### Versioning

Version is set in `build.gradle.kts` as `val v = "1.0.0"`. CI mutates it via `-Psnapshot=true` (appends `-SNAPSHOT`) and
`-Psuffix=<sha>` (appends `-<sha>`). The publishing block only configures the `GithubPackages` repo for non-SNAPSHOT
versions.
