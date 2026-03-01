# Clearr Remittance Engineering Spec

## Scope
This document defines how remittance is implemented in the current Clearr codebase and how to reproduce it cleanly in a separate app.

Use this together with:
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/docs/remittance_prd.md`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/docs/remittance_handoff.md`

---

## Current implementation map

### List/home of remittance trackers
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/RemittanceHomeScreen.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/TrackerListViewModel.kt`

### Single remittance detail
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeScreen.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeViewModel.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeState.kt`

### Remittance settings
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsScreen.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsViewModel.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsState.kt`

### Shared dialogs/components used by remittance
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/components/AddMemberDialog.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/components/EditMemberDialog.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/components/PartialPaymentDialog.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/components/MemberDetailSheet.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/components/StatsRow.kt`

---

## Domain model

### Core entities
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt`

#### Tracker
For remittance, important fields are:
- `id: Long`
- `name: String`
- `type: TrackerType`
- `frequency: Frequency`
- `defaultAmount: Double`
- `layoutStyle: LayoutStyle`
- `isNew: Boolean`
- `createdAt: Long`

Expected remittance type:
- `TrackerType.DUES`

Compatibility behavior still in code:
- `TrackerType.EXPENSES` is treated as remittance in multiple places

#### TrackerMember
- `id`
- `trackerId`
- `name`
- `phone`
- `isArchived`
- `createdAt`

#### TrackerPeriod
- `id`
- `trackerId`
- `label`
- `startDate`
- `endDate`
- `isCurrent`
- `createdAt`

#### TrackerRecord
- `id`
- `trackerId`
- `periodId`
- `memberId`
- `status`
- `amountPaid`
- `note`
- `updatedAt`

Relevant statuses for remittance:
- `PAID`
- `PARTIAL`
- `UNPAID`

### Legacy entities still bridged by remittance detail
- `PaymentRecord`
  - `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/data/model/PaymentRecord.kt`
- `YearConfig`
  - `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/data/model/YearConfig.kt`

Recommendation for extraction:
- remove `PaymentRecord` and `YearConfig` if migration is not required
- use only `Tracker`, `TrackerMember`, `TrackerPeriod`, `TrackerRecord`

---

## Data access contract
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/domain/repository/DuesRepository.kt`

### Tracker APIs
- `getAllTrackers()`
- `getTrackerById(...)`
- `getTrackerByIdFlow(...)`
- `insertTracker(...)`
- `updateTracker(...)`
- `deleteTracker(...)`
- `clearTrackerNewFlag(...)`

### Member APIs
- `getAllMembersForTracker(trackerId)`
- `insertTrackerMember(...)`
- `updateTrackerMember(...)`
- `setTrackerMemberArchived(...)`
- `deleteTrackerMember(...)`

### Period APIs
- `getPeriodsForTracker(trackerId)`
- `getCurrentPeriodFlow(trackerId)`
- `getCurrentPeriod(trackerId)`
- `insertPeriod(...)`
- `setCurrentPeriod(trackerId, periodId)`

### Record APIs
- `getRecordsForPeriod(trackerId, periodId)`
- `getRecordsForTracker(trackerId)`
- `getRecord(trackerId, periodId, memberId)`
- `insertRecord(...)`
- `updateRecord(...)`
- `deleteRecord(...)`

### Legacy APIs still touched by remittance
- `getPaymentsForYear(year)`
- `getPaymentsForMemberYear(memberId, year)`
- `getLatestPayment(memberId, year, monthIndex)`
- `insertPayment(...)`
- `undoPayment(...)`
- `getAllYearConfigs()`
- `getYearConfig(year)`
- `getYearConfigFlow(year)`
- `insertYearConfig(...)`
- `updateYearConfig(...)`
- `ensureYearConfig(year, defaultAmount)`

---

## ViewModel contracts

### TrackerListViewModel
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/TrackerListViewModel.kt`

Responsibilities for remittance:
- bootstraps static trackers in the host app
- creates new remittance trackers
- inserts initial members
- creates and selects current period
- clears `isNew`
- renames trackers
- deletes trackers
- exposes `TrackerSummary` list used by remittance home

Important creation path:
- `handleCreateTracker(...)`
- creates `Tracker`
- inserts `TrackerMember` rows
- builds current `TrackerPeriod`
- sets current period

### HomeViewModel
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeViewModel.kt`

Responsibilities for remittance:
- loads current tracker
- loads members, periods, and records
- converts records to payment-oriented UI state
- toggles paid/unpaid on tap
- records partial payments on long press flow
- computes collected/expected/outstanding stats
- manages member CRUD and archive/delete
- supports share path and AI risk hint

Important actions:
- `TogglePayment`
- `RecordPartialPayment`
- `AddMember`
- `UpdateMember`
- `SetMemberArchived`
- `DeleteMember`
- `MarkOutstandingMonthsPaid`
- `SetCurrentTrackerId`

### SettingsViewModel
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsViewModel.kt`

Responsibilities for remittance:
- load current tracker type from app state
- expose current tracker due amount
- update due amount if current tracker is remittance
- manage year selection/config compatibility path

---

## Screen behavior details

### RemittanceHomeScreen
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/RemittanceHomeScreen.kt`

Behavior:
- filters summaries to `DUES` and `EXPENSES`
- uses `RemittanceSwipeCard`
- swipe delete available here only
- long press opens rename dialog
- tap opens detail and clears `isNew`
- top bar trailing action opens settings
- floating actions create new remittance

### HomeScreen for remittance
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeScreen.kt`

Behavior:
- top bar shows tracker title and share/layout actions
- stats row uses:
  - `totalCollected`
  - `totalExpected`
  - `outstanding`
  - `pct`
- main content is layout-driven via `TrackerLayoutData`
- for remittance, each member/month cell supports:
  - tap: toggle full payment
  - long press: open partial payment dialog
- add member via FAB
- snackbar supports undo for payment removal
- confetti can trigger on completion

### SettingsScreen for remittance
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsScreen.kt`

Behavior:
- if current tracker type is `DUES` or `EXPENSES`, due amount is editable
- otherwise due amount control is disabled/read-only
- selected year persists in app state
- year config support still exists for compatibility

---

## Summary and dashboard integration

### Tracker summary generation
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt`

For remittance-like trackers:
- load members for tracker
- load current period
- load records for current period
- treat `PAID` as completed
- compute:
  - `totalMembers`
  - `completedCount`
  - `completionPercent`
  - `amountTargetKobo = tracker.defaultAmount * memberCount`
  - `amountCompletedKobo = sum(amountPaid)`
  - `currentPeriodLabel`

### Dashboard merge behavior
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/dashboard/utils/DashboardModels.kt`

Current host-app behavior:
- all remittance trackers are merged into one dashboard tile
- total members, completed count, collected, and target are summed

For a standalone remittance app:
- keep per-tracker detail screens
- only add aggregate dashboard if desired explicitly

---

## Current business rules

### Type mapping
- `DUES` == remittance
- `EXPENSES` also treated as remittance for compatibility

### Monetary logic
- `defaultAmount` is due amount per member per period
- full payment sets `amountPaid = defaultAmount`
- partial payment sets `amountPaid < defaultAmount`
- total collected is sum of all recorded amounts

### Completion logic
Current completion summary counts only records with status `PAID` as completed.
This means partially paid members may contribute money without counting as cleared.

### Member visibility
- archived members remain in storage
- active calculations generally exclude archived members at UI level

### Share/privacy logic
- remittance share path supports name blurring
- output is image-based

---

## Technical debt to decide on during extraction

### 1. Legacy dues bridge
Current remittance detail still bridges modern tracker models to legacy payment/year records.

Decision:
- keep bridge for migration
- or remove bridge and simplify to modern tracker records only

Recommended:
- remove bridge for a clean standalone app

### 2. AppStateHolder coupling
Current host app uses global app state for selected tracker id and year.

Recommended standalone replacement:
- route argument for tracker id
- local screen/viewmodel state for selected period/year

### 3. Naming cleanup
Current code mixes `Dues` and `Remittance`.

Recommended:
- rename all product-facing and internal classes to `Remittance` in the extracted app

### 4. Settings scope
Current settings screen is mixed with host-app settings.

Recommended:
- create a dedicated remittance settings screen
- keep only remittance-relevant controls

---

## Clean extraction target

### Recommended package structure
- `ui/feature/remittance/list`
- `ui/feature/remittance/detail`
- `ui/feature/remittance/settings`
- `ui/feature/remittance/components`
- `domain/remittance`
- `data/remittance`

### Recommended models
- `RemittanceTracker`
- `RemittanceMember`
- `RemittancePeriod`
- `RemittancePayment`

### Recommended ViewModels
- `RemittanceListViewModel`
- `RemittanceDetailViewModel`
- `RemittanceSettingsViewModel`

### Recommended repository surface
- create tracker
- update tracker
- delete tracker
- get tracker by id
- list trackers
- add/update/archive/delete member
- get current period
- ensure current period
- list periods
- record full payment
- record partial payment
- list payments for member or period
- share/export summary

---

## Acceptance criteria for engineering recreation
- Multiple remittance trackers can coexist without shared members or shared records.
- Opening a remittance tracker always shows the correct active period.
- Full payment is one tap.
- Partial payment records amount and note.
- Completion percent and money totals stay consistent after updates.
- Renaming and deleting a remittance tracker work from the remittance home list.
- Settings can update the due amount for the active remittance tracker.
- Share flow produces an image summary suitable for WhatsApp.
