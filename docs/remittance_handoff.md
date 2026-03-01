# Clearr Remittance Handoff

## Purpose
Remittance is the group-collection feature inside Clearr. It tracks who has paid, partially paid, or not paid for a recurring obligation. In the current app it is presented as `Remittance`, but the data layer still contains legacy `Dues` naming and a compatibility path for `Expenses` rows that are treated as remittance.

This document is intended to let another implementation reproduce the remittance feature without needing the rest of Clearr.

---

## Companion documents
- Product PRD: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/docs/remittance_prd.md`
- Engineering spec: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/docs/remittance_engineering_spec.md`

Use this handoff as the bridge document. Use the PRD for product intent and the engineering spec for implementation detail.

---

## Product definition

### Core problem
A user needs to track recurring group payments or remittances for a set of people, see who has cleared, record full or partial payments, and share proof/status externally.

### Primary user outcomes
- Create multiple independent remittance trackers.
- Maintain a separate member list per remittance tracker.
- Auto-generate periods from the tracker frequency.
- Mark each member as paid, partially paid, or unpaid for the active period.
- Record exact amounts paid and notes.
- See progress at a glance for the current period.
- Inspect a member’s payment history.
- Share a visual status snapshot.
- Adjust due amount for the current remittance tracker.

### Current naming
- Product/UI term: `Remittance`
- Data layer canonical type: `TrackerType.DUES`
- Compatibility type still accepted: `TrackerType.EXPENSES`

If you move remittance into a new app, remove the `EXPENSES` compatibility layer unless you need legacy migration.

---

## Feature boundaries

### Included in remittance
- Remittance home/list screen
- Remittance detail screen
- Per-tracker settings for due amount
- Member management
- Payment toggling and partial payments
- Period generation and current-period selection
- Share/screenshot flow
- Dashboard remittance summary tile and urgency item

### Not part of remittance
- Goals
- Todos
- Budget
- Dashboard empty-state cards for those features

---

## Navigation map

### Entry points
- Bottom navigation `Remittance` tab opens remittance home.
- Dashboard remittance tile and quick actions can route to remittance.
- Empty-state remittance CTA currently says `Record payment` and routes to remittance tab.

### Screens
1. `RemittanceHomeScreen`
   - File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/RemittanceHomeScreen.kt`
   - Purpose: list all remittance trackers, create new one, rename, delete, open settings.

2. `HomeScreen` when opened with a remittance tracker id
   - File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeScreen.kt`
   - Purpose: detail view for a single remittance tracker.

3. `SettingsScreen` scoped to current remittance tracker
   - File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsScreen.kt`
   - Purpose: update due amount for the active remittance tracker, manage year config compatibility paths.

### Supporting dialogs/sheets
- Add member: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/components/AddMemberDialog.kt`
- Edit member: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/components/EditMemberDialog.kt`
- Partial payment: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/components/PartialPaymentDialog.kt`
- Member detail sheet: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/components/MemberDetailSheet.kt`
- Delete member dialog: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/components/DeleteMemberDialog.kt`
- Rename tracker dialog: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/components/RenameTrackerDialog.kt`
- Delete tracker dialog: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/components/DeleteTrackerDialog.kt`

---

## User flows

### 1. Create remittance tracker
Origin:
- `RemittanceHomeScreen`
- New Remittance FAB / pill action

Current implementation path:
- `TrackerListViewModel.handleCreateTracker(...)`
- File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/TrackerListViewModel.kt`

What happens:
1. Insert `Tracker` with type `TrackerType.DUES`.
2. Set frequency.
3. Set `defaultAmount` for amount due per member per period.
4. Insert initial members if provided.
5. Create current `TrackerPeriod` and mark it current.
6. Tracker appears in remittance home.

### 2. Open remittance tracker
Origin:
- tap tracker card in `RemittanceHomeScreen`

What happens:
1. `TrackerListViewModel` clears `isNew` if needed.
2. Navigation opens `HomeScreen(trackerId = trackerId)`.
3. `HomeViewModel` sets current tracker id in app state.
4. Screen loads members, periods, and records for that tracker.

### 3. Record full payment
Origin:
- tap a month cell/member cell in remittance detail grid

Current implementation:
- `HomeViewModel.togglePayment(...)`
- File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeViewModel.kt`

What happens for remittance:
1. Ensure or fetch monthly period for that year/month.
2. Get existing `TrackerRecord` for member + period.
3. Toggle between `PAID` and `UNPAID`.
4. When paid, `amountPaid = tracker.defaultAmount`.
5. Persist `TrackerRecord`.
6. Also mirror into legacy `PaymentRecord` path in some flows for compatibility/history.

### 4. Record partial payment
Origin:
- long press a month cell/member cell
- or partial payment dialog flow

Current implementation:
- `HomeAction.RecordPartialPayment`
- `HomeViewModel.recordPartialPayment(...)`

What happens:
1. Open `PartialPaymentDialog`.
2. User enters amount and optional note.
3. Save `PaymentRecord` in legacy table.
4. Update or create `TrackerRecord` for current tracker period.
5. If total paid for the member-period reaches due amount, status becomes effectively paid.

### 5. View member detail
Origin:
- tap member name/card

What happens:
1. Open `MemberDetailSheet`.
2. Show member payment history.
3. Allow edit, archive, delete, and bulk-mark outstanding months paid.

### 6. Bulk mark outstanding months paid
Origin:
- member detail sheet

Current implementation:
- `HomeAction.MarkOutstandingMonthsPaid`
- `HomeViewModel.markOutstandingMonthsPaid(...)`

What happens:
1. Walk outstanding past/current months for the selected year.
2. Create full `PaymentRecord` entries.
3. Update corresponding `TrackerRecord` entries.

### 7. Rename or delete remittance tracker
Origin:
- long press on remittance card in home list
- swipe left to delete on remittance list card

Current implementation:
- `TrackerListAction.RenameTracker`
- `TrackerListAction.DeleteTracker`

Delete behavior:
- UI removes item optimistically from local state.
- Repository deletes tracker and associated data.

### 8. Share remittance status
Origin:
- top-bar share action in remittance detail

Current implementation:
- `shareHomeScreenshot(...)`
- File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/utils/HomeShareUtils.kt`
- uses screenshot utility at `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/util/ScreenshotUtil.kt`

Behavior:
- Capture screen bitmap
- Optionally blur names when sharing remittance
- Save image to cache
- Share via platform intent, typically WhatsApp/messages

---

## Data model

### Modern multi-tracker models
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt`

#### Tracker
Represents one independent remittance collection.

Key fields:
- `id: Long`
- `name: String`
- `type: TrackerType` (`DUES` for remittance)
- `frequency: Frequency`
- `defaultAmount: Double` amount due per member per period
- `layoutStyle: LayoutStyle`
- `isNew: Boolean`
- `createdAt: Long`

#### TrackerMember
Represents one member within a specific remittance tracker.

Key fields:
- `id: Long`
- `trackerId: Long`
- `name: String`
- `phone: String?`
- `isArchived: Boolean`
- `createdAt: Long`

#### TrackerPeriod
Represents one generated cycle.

Key fields:
- `id: Long`
- `trackerId: Long`
- `label: String`
- `startDate: Long`
- `endDate: Long`
- `isCurrent: Boolean`
- `createdAt: Long`

#### TrackerRecord
Represents one member’s status for one period.

Key fields:
- `id: Long`
- `trackerId: Long`
- `periodId: Long`
- `memberId: Long`
- `status: RecordStatus`
- `amountPaid: Double`
- `note: String?`
- `updatedAt: Long`

Relevant statuses for remittance:
- `PAID`
- `PARTIAL`
- `UNPAID`

### Legacy compatibility models still present

#### PaymentRecord
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/data/model/PaymentRecord.kt`

Used by the old dues flow and still bridged by `HomeViewModel`.

Typical meaning:
- a payment row for member + year + monthIndex
- exact amount paid and expected amount stored

#### YearConfig
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/data/model/YearConfig.kt`

Legacy per-year due configuration.
Still used in settings and some remittance detail compatibility logic.

### Recommendation for extraction
If remittance is moved to a standalone app, keep only the modern model set:
- `Tracker`
- `TrackerMember`
- `TrackerPeriod`
- `TrackerRecord`

Then decide whether you still need:
- `PaymentRecord`
- `YearConfig`

If you do not need backward compatibility, remove them and collapse all remittance logic onto `TrackerRecord` plus period generation.

---

## Repository contract
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/domain/repository/DuesRepository.kt`

### Tracker-level APIs used by remittance
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

### Legacy payment/year APIs still used in remittance detail
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

## Screen contracts

### Remittance home screen
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/RemittanceHomeScreen.kt`

Responsibilities:
- show all remittance trackers only
- create remittance
- open remittance settings
- rename tracker
- delete tracker by swipe
- open tracker detail

Data source:
- `TrackerListViewModel.uiState.summaries`
- filtered for `DUES` and `EXPENSES`

### Remittance detail screen
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeScreen.kt`

Responsibilities:
- show top bar with tracker name
- show stats row: collected, expected, outstanding, percent
- display member payment layout
- toggle full payment
- record partial payment
- add/edit/archive/delete members
- share screenshot
- show AI risk hint string when available

State source:
- `HomeViewModel`
- `HomeUiState`

Important remittance-only behaviors in `HomeScreen`:
- blur names toggle on share/privacy
- bulk mark paid available
- due amount comes from year config or tracker default amount

### Remittance settings screen
Files:
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsScreen.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsViewModel.kt`

Responsibilities:
- select active year
- update due amount for current remittance tracker
- start new year from current year config
- reset setup/app state

Important limitation in current app:
- settings is local to the currently selected remittance tracker in app state
- due amount save only applies if current tracker type is `DUES` or `EXPENSES`

---

## Business rules

### Remittance type mapping
- Treat both `TrackerType.DUES` and `TrackerType.EXPENSES` as remittance.
- This is for backward compatibility only.

### Progress calculation
Current summary flow:
- completion count = number of records with status `PAID`
- total members = active member count for that tracker
- percent = `completedCount / totalMembers`
- monetary target = `tracker.defaultAmount * memberCount`
- monetary collected = sum of `amountPaid`

### Due amount semantics
- `Tracker.defaultAmount` is the amount due per member per period.
- For full payment, this amount is written as `amountPaid`.
- Settings can update this amount for the active tracker.

### Period semantics
- Remittance periods are generated according to tracker frequency.
- Monthly remittance is the dominant path.
- Current-period label is shown in summary and detail.

### Partial payments
- Partial amount less than due amount is allowed.
- Partial payment history is preserved with note.
- Final payment state is derived from cumulative paid amount vs due amount in the relevant flow.

### Archived members
- Archived members remain in data but can be hidden in UI.
- Active stats use non-archived members.

### Share behavior
- Shared remittance screenshot can blur member names.
- Share target is standard Android share sheet.

### AI hint
- `ClearrEdgeAi.remittanceRiskLabel(...)` currently generates a short risk string per member.
- This is optional and should not be core to standalone remittance unless explicitly desired.

---

## ViewModel responsibilities

### TrackerListViewModel
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/TrackerListViewModel.kt`

Remittance-related responsibilities:
- ensure static trackers exist
- create new remittance tracker
- insert initial members
- create current period
- clear `isNew`
- rename/delete remittance tracker
- expose prioritized tracker summaries for home list

### HomeViewModel
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeViewModel.kt`

Remittance-related responsibilities:
- bind to current tracker id
- load tracker/members/periods/records
- bridge records into UI-friendly `PaymentRecord` shape for screen rendering
- toggle payment
- record partial payment
- member CRUD
- bulk mark outstanding paid
- compute summary stats
- compute AI risk hint

### SettingsViewModel
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsViewModel.kt`

Remittance-related responsibilities:
- determine whether current tracker is remittance
- save tracker due amount
- manage year selection/config

---

## Summary generation and dashboard integration

### Tracker summary use case
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt`

Remittance path:
- For non-budget/non-goal/non-todo trackers, use members + current period + current records.
- Build `TrackerSummary` with:
  - total members
  - completed count (`PAID`)
  - percent complete
  - current period label
  - amount target kobo
  - amount completed kobo

### Remittance merge behavior on dashboard
File: `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/dashboard/utils/DashboardModels.kt`

If multiple remittance trackers exist:
- they are merged into a single dashboard remittance health tile
- total members, completed count, collected amount, target amount are summed

This is dashboard-specific behavior. A standalone remittance app should not need this merge unless it has a dashboard that summarizes multiple remittance trackers.

---

## Current UI components worth reusing

### High-value reusable components
- `RemittanceSwipeCard`
- `DeleteTrackerDialog`
- `RenameTrackerDialog`
- `AddMemberDialog`
- `EditMemberDialog`
- `PartialPaymentDialog`
- `MemberDetailSheet`
- `DuesSnackbar`
- `StatsRow`
- `HomeTopBar`

### Utilities worth reusing
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/util/FormatUtils.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/commons/util/ScreenshotUtil.kt`
- `/Users/admin/AndroidStudioProjects/JssDurumiBrothers/app/src/main/java/com/mikeisesele/clearr/ui/feature/home/utils/HomeShareUtils.kt`

---

## Known technical debt and migration caveats

### 1. Legacy dues system still exists
The current remittance implementation is not fully isolated. `HomeViewModel` still bridges between:
- legacy `PaymentRecord` + `YearConfig`
- modern `TrackerRecord` + `TrackerPeriod`

If building a new remittance app, choose one model. Recommended: use only modern tracker models.

### 2. `EXPENSES` is treated as remittance
This is compatibility behavior. It should be removed in a clean extraction unless you are migrating legacy rows.

### 3. App-state coupling
Current screens rely on `AppStateHolder.currentTrackerId` and selected year. A standalone remittance app can simplify this by keeping tracker id in route arguments and local screen state.

### 4. Settings screen is broader than remittance
The current `SettingsScreen` contains app-level items plus remittance-specific due amount logic. In a dedicated remittance app, separate these concerns.

### 5. Naming inconsistency
UI uses `Remittance`, internal names often still say `Dues`. Normalize this in the extracted app.

---

## Recommended standalone remittance app architecture

### Recommended domain entities
- `RemittanceTracker`
- `RemittanceMember`
- `RemittancePeriod`
- `RemittanceRecord`

### Recommended screens
1. `RemittanceDashboardScreen`
   - list of remittance trackers
2. `RemittanceDetailScreen`
   - active period matrix/list
3. `RemittanceMemberDetailScreen` or sheet
4. `RemittanceSettingsScreen`
   - due amount
   - period settings
   - share/privacy options

### Recommended ViewModels
- `RemittanceListViewModel`
- `RemittanceDetailViewModel`
- `RemittanceSettingsViewModel`

### Recommended actions
- create tracker
- rename tracker
- delete tracker
- add member
- archive member
- record payment
- record partial payment
- bulk mark paid
- set due amount
- share summary

---

## Acceptance criteria to recreate remittance elsewhere

### Home/list
- User can create multiple remittance trackers.
- Only remittance trackers appear on remittance home.
- User can swipe left to delete a remittance tracker.
- User can long-press to rename.

### Detail
- User sees members and their payment status for the current period.
- Tap marks full payment/unpayment.
- Long press opens partial payment flow.
- Stats show collected, expected, outstanding, percent.
- User can add/edit/archive/delete members.
- User can share a status image.

### Settings
- User can update due amount for current remittance tracker.
- Due amount affects future full-payment toggles and summaries.

### Data
- Each tracker has isolated members, periods, and records.
- Current period is persisted and queryable.
- Full and partial payments are stored with amount and note.

---

## Minimal prompt you can paste into another chat

Build a standalone Android remittance app extracted from Clearr.

Feature requirements:
- Multiple independent remittance trackers
- Each tracker has its own members, periods, and payment records
- Tracker fields: name, frequency, dueAmount, createdAt
- Member fields: name, phone, archived
- Period fields: label, startDate, endDate, isCurrent
- Record fields: memberId, periodId, status(PAID/PARTIAL/UNPAID), amountPaid, note, updatedAt
- Remittance home screen lists only remittance trackers
- FAB creates a new remittance tracker
- Swipe left deletes tracker
- Long press renames tracker
- Opening a tracker shows a detail screen with member payment grid/list for the current period
- Tap toggles full payment
- Long press opens partial payment dialog
- Stats row shows collected, expected, outstanding, and percent complete
- Member detail allows edit, archive, delete, and bulk mark outstanding months paid
- Settings screen lets user update due amount for the current tracker
- Share action exports a screenshot/image summary suitable for WhatsApp
- Use Room, Hilt, StateFlow, and Jetpack Compose
- Do not include goals, todos, or budget
- Do not keep legacy Clearr dues compatibility unless required for migration
