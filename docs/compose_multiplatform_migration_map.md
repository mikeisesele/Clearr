# Compose Multiplatform Migration Map

## Goal

Migrate Clearr from a single Android app module to a staged Compose Multiplatform setup without breaking the current Android app during the transition.

This plan keeps Android as the reference target until shared code is stable. The migration is incremental: no big-bang rewrite.

## Current State

The repo is a single Android module:

- `:app` only
- UI: Jetpack Compose
- DI: Hilt
- Persistence: Room + DataStore
- Navigation: `androidx.navigation.compose`
- AI integration: ML Kit Gemini Nano

Main Android lock-ins currently live in:

- `app/src/main/java/com/mikeisesele/clearr/MainActivity.kt`
- `app/src/main/java/com/mikeisesele/clearr/ClearrApplication.kt`
- `app/src/main/java/com/mikeisesele/clearr/di/*`
- `app/src/main/java/com/mikeisesele/clearr/data/database/*`
- `app/src/main/java/com/mikeisesele/clearr/data/dao/*`
- `app/src/main/java/com/mikeisesele/clearr/data/repository/*PreferencesRepository.kt`
- `app/src/main/java/com/mikeisesele/clearr/core/ai/GeminiNanoEngine.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/navigation/SystemBarsController.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/theme/Theme.kt`

## Recommended Target Structure

Use three modules first, not a full platform matrix immediately:

1. `:app`
   Android entrypoint shell.
   Keeps Activity, Manifest, Android DI bootstrap, and Android-specific implementations.

2. `:shared`
   Kotlin Multiplatform module.
   Holds shared domain models, state, view models, repositories interfaces, and most Compose UI.

3. `:shared-ui-android` or Android source set inside `:shared`
   Holds Android-only UI adapters until they can be abstracted or replaced.

Preferred source sets inside `:shared`:

- `commonMain`
- `androidMain`
- `desktopMain` later
- `iosMain` later if needed

## Migration Rule

Only move code into `commonMain` when it has no dependency on:

- `android.*`
- `androidx.room.*`
- `androidx.datastore.*`
- Hilt annotations or Hilt APIs
- `androidx.lifecycle.ViewModel`
- Android resource APIs
- Android-only navigation/runtime APIs that do not exist in Compose Multiplatform

## Classification

### Safe `commonMain` candidates after light refactor

These are mostly pure Kotlin or Compose code with low Android coupling:

- `app/src/main/java/com/mikeisesele/clearr/core/ai/ClearrEdgeAi.kt`
  Keep the heuristic logic in shared code, but split the Gemini Nano calls behind an interface.

- `app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt`
- `app/src/main/java/com/mikeisesele/clearr/domain/trackers/TrackerBootstrapper.kt`
- `app/src/main/java/com/mikeisesele/clearr/domain/repository/ClearrRepository.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/dashboard/utils/DashboardModels.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/todo/utils/TodoUiUtils.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/budget/utils/BudgetFormatters.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/budget/utils/BudgetCategoryPresets.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/goals/utils/GoalPalette.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/*/*State.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/navigation/NavRoutes.kt`
- most UI composables under `ui/feature/**/components`
- most screens under `ui/feature/**`
- most theme token files except the dynamic-color part of `Theme.kt`

### Move to shared only after model split

These currently mix domain models with Room entities:

- `app/src/main/java/com/mikeisesele/clearr/data/model/Budget.kt`
- `app/src/main/java/com/mikeisesele/clearr/data/model/Goal.kt`
- `app/src/main/java/com/mikeisesele/clearr/data/model/Todo.kt`
- `app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt`
- `app/src/main/java/com/mikeisesele/clearr/data/model/AppConfig.kt`

Problem:

- shared domain types and Android Room entity types are in the same files

Required refactor:

- create pure shared domain models in `commonMain`
- keep Room entities in `androidMain` or `:app`
- add explicit mappers between entity and domain models

### Shared in spirit, but blocked by Android lifecycle/Hilt

- `app/src/main/java/com/mikeisesele/clearr/core/base/BaseViewModel.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/budget/BudgetViewModel.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/dashboard/DashboardViewModel.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/goals/GoalsViewModel.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/OnboardingViewModel.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/feature/todo/TodoViewModel.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/navigation/AppConfigViewModel.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/navigation/AppShellViewModel.kt`

Problem:

- they use `androidx.lifecycle.ViewModel`
- they use `viewModelScope`
- they are created with Hilt

Required refactor:

- introduce shared presenter/store classes or KMP-friendly view models in `commonMain`
- keep Android wrappers if needed during transition
- replace Hilt construction with constructor injection from the platform shell

### Android-only for now

- `app/src/main/java/com/mikeisesele/clearr/MainActivity.kt`
- `app/src/main/java/com/mikeisesele/clearr/ClearrApplication.kt`
- `app/src/main/java/com/mikeisesele/clearr/di/*`
- `app/src/main/java/com/mikeisesele/clearr/data/database/ClearrDatabase.kt`
- `app/src/main/java/com/mikeisesele/clearr/data/dao/*`
- `app/src/main/java/com/mikeisesele/clearr/data/repository/ClearrRepositoryImpl.kt`
- `app/src/main/java/com/mikeisesele/clearr/data/repository/BudgetPreferencesRepository.kt`
- `app/src/main/java/com/mikeisesele/clearr/data/repository/OnboardingRepository.kt`
- `app/src/main/java/com/mikeisesele/clearr/data/repository/TodoPreferencesRepository.kt`
- `app/src/main/java/com/mikeisesele/clearr/core/ai/GeminiNanoEngine.kt`
- `app/src/main/java/com/mikeisesele/clearr/ui/navigation/SystemBarsController.kt`
- Android resources in `app/src/main/res`
- `app/src/main/AndroidManifest.xml`

## Key Refactors Needed Before Real Sharing

### 1. Split domain models from persistence models

This is the biggest structural blocker.

Today, files like `Budget.kt`, `Goal.kt`, `Todo.kt`, and `Tracker.kt` contain Room annotations on types you also treat as app models. That prevents clean reuse in `commonMain`.

Target:

- `commonMain`: `Budget`, `BudgetCategory`, `BudgetEntry`, `Goal`, `GoalCompletion`, `TodoItem`, `Tracker`, `AppConfig`, enums, summaries
- `androidMain` or `:app`: `BudgetEntity`, `GoalEntity`, `TodoEntity`, `TrackerEntity`, `AppConfigEntity`, DAO-only models

### 2. Remove Android lifecycle from shared state holders

`BaseViewModel.kt` is Android-specific because it extends `ViewModel`.

Target:

- `commonMain`: a plain state holder using `CoroutineScope`, `StateFlow`, and `Channel`
- Android shell can still adapt it to lifecycle if desired

### 3. Replace Hilt as the shared composition mechanism

Hilt can stay in Android, but not in shared code.

Target:

- `commonMain`: constructor-injected classes only
- Android: Hilt provides platform implementations and shared object graph roots
- other targets: manual wiring or a KMP DI library later

### 4. Hide platform services behind interfaces

Create interfaces in shared code for:

- AI assistance
- onboarding/settings persistence
- clock/date source if deterministic behavior matters
- optional export/file sharing later

Suggested interfaces:

- `AiAssistant`
- `OnboardingStore`
- `BudgetPreferencesStore`
- `TodoPreferencesStore`

### 5. Stop relying on Android-only Compose helpers in shared screens

Most screens are portable, but some call Android-only APIs through surrounding infrastructure:

- `collectAsStateWithLifecycle`
- `hiltViewModel()`
- system bar control
- dynamic color via `LocalContext`

Target:

- shared screens should take plain state plus callbacks
- platform shell handles view model lookup, lifecycle-aware collection, and system UI

## Phase Plan

### Phase 0: Prepare the build

Outcome:

- add `:shared` KMP module
- keep `:app` building exactly as it does now
- no feature behavior changes

Work:

- add KMP and Compose Multiplatform plugins
- add `commonMain` and `androidMain`
- do not move large code yet

Exit criteria:

- Android app still compiles and runs
- empty shared module is wired into the project

### Phase 1: Move pure Kotlin domain logic first

Outcome:

- shared module starts carrying real business logic with low risk

Move first:

- route definitions
- state classes
- summary calculators and pure utilities
- heuristic portions of `ClearrEdgeAi.kt`
- tracker domain use cases

Do not move yet:

- Room entities
- repositories implementation
- Android view models
- navigation host

Exit criteria:

- Android app builds against shared domain code
- no behavior change

### Phase 2: Split models and repository contracts

Outcome:

- shared code owns domain types cleanly

Work:

- create shared domain model package
- convert repository interface to use shared domain types only
- keep entity-to-domain mapping in Android data layer

This is the critical phase.

Exit criteria:

- `ClearrRepository` depends only on shared models
- Room annotations are no longer in shared types

### Phase 3: Introduce shared state holders for one feature

Start with the smallest vertical slice:

- onboarding, or
- dashboard read-only summary flow

Recommended first slice: dashboard

Reason:

- mostly read-oriented
- fewer create/edit flows than budget/todo/goals
- validates shared UI + shared state + Android repository wiring

Work:

- convert one feature’s view model logic to a shared presenter/store
- make the screen receive state and callbacks
- keep Android navigation shell in `:app`

Exit criteria:

- dashboard logic lives in `commonMain`
- Android screen still behaves the same

### Phase 4: Share feature UIs one by one

Recommended order:

1. dashboard
2. onboarding
3. goals
4. todos
5. budget

Reason:

- budget has the most persistence and period logic surface area
- goals and todos are easier to stabilize first

### Phase 5: Replace Android-only navigation boundary

Current host:

- `app/src/main/java/com/mikeisesele/clearr/ui/navigation/ClearrNavHost.kt`

This should remain Android-first until the screens and state holders are shared. After that:

- either keep platform-native navigation shells per target
- or adopt a multiplatform navigation library

Recommendation:

- keep navigation platform-specific at first
- do not migrate navigation before the screens and state models are stable in shared code

### Phase 6: Platform services

After the shared app is stable:

- Android keeps Room, DataStore, ML Kit
- desktop or iOS get their own implementations

Examples:

- database: SQLDelight or platform-specific repository implementations
- settings storage: multiplatform settings abstraction
- AI: no-op or alternate provider outside Android

## First Concrete Slice

The safest first implementation slice for this repo is:

1. create `:shared`
2. move `NavRoutes.kt`
3. move all `*State.kt` files
4. move pure dashboard utility models
5. extract heuristic logic from `ClearrEdgeAi.kt` into a shared `AiAssistant` default implementation
6. keep Android app using the shared code with no UI behavior change

Why this slice:

- it creates real migration momentum
- it avoids Room and Hilt immediately
- it proves shared code can serve the existing Android app without destabilizing storage and DI

## Risks

### Model churn risk

Splitting Room entities from domain models will touch a large portion of the app. This is necessary and should happen before broad UI migration.

### DI churn risk

Hilt is deeply embedded in the current Android composition root. Removing it from shared logic will require constructor-level refactors across all feature state holders.

### Navigation mismatch risk

Do not assume Android Navigation Compose will be your long-term shared navigation solution. Keep it at the shell boundary until later.

### Theme portability risk

Most design tokens are portable, but `Theme.kt` currently uses Android dynamic color APIs and `LocalContext`. Keep those parts Android-specific.

### Persistence portability risk

Room and DataStore should be treated as Android implementations, not shared foundations.

## Definition of Done for Each Phase

Each phase should satisfy all of these:

- Android app still compiles
- existing Android tests still pass or are updated intentionally
- no feature regression in dashboard, onboarding, todo, goals, or budget
- shared code has no accidental Android imports in `commonMain`

## Recommended Next Implementation Step

Implement Phase 0 and the start of Phase 1 only:

- add `:shared`
- move pure shared-safe files first
- do not touch Room-backed models yet

That is the highest-leverage, lowest-risk starting point for this codebase.
