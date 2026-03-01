# iOS Host App

This folder contains a minimal iOS host app scaffold for the shared Compose Multiplatform module.

## Generate the Xcode project

If you have `xcodegen` installed:

```bash
cd iosApp
xcodegen generate
open ClearrIOS.xcodeproj
```

## How it works

- The app boots the shared Compose UI through `MainViewController()`.
- Xcode runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode` as a pre-build step.
- The generated project links the framework from `shared/build/xcode-frameworks/.../shared.framework`.

## Current scope

This is the first host-app scaffold. It gives the repo an iOS entrypoint and Xcode wiring, but it has not been verified with a local Xcode build from this environment.
