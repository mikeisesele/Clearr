# Clearr Remittance PRD

## Overview
Remittance is a standalone payment-tracking product for recurring group collections. It helps a user create one or more remittance trackers, attach members to each tracker, record who has paid, partially paid, or not paid for the active period, and share proof/status externally.

This PRD defines the product behavior and user experience. It intentionally avoids low-level implementation detail.

---

## Product goal
Give a user a fast, trustworthy way to answer three questions for any group collection:
- Who has paid?
- Who has not paid?
- How much has been collected versus expected?

---

## Core user problems
- Payment status is easy to lose across WhatsApp chats, paper notes, and memory.
- Partial payments are hard to track consistently.
- Group collections need proof and summary views that can be shared quickly.
- A user may manage multiple independent collections and must keep them isolated.

---

## Target user
A person who manages money collection for a group, team, class, association, church unit, client list, or internal roster.

Examples:
- School fee coordinator
- Church/unit treasurer
- Event organizer
- Cooperative lead
- Team admin managing monthly contributions
- Freelancer tracking recurring client remittances

---

## Product principles
- Independent trackers: each remittance collection is isolated.
- Fast recording: full payment should take one tap.
- Exact money trail: partial payments and notes must be preserved.
- Proof-first: the user should be able to share a clear summary view.
- Current period awareness: the app should always foreground the active period.
- No ambiguity: paid, partial, and unpaid must be explicit.

---

## Primary user stories

### US-REM-001 Create a remittance tracker
As a user, I want to create a remittance tracker with a name, frequency, due amount, and optional starter members so I can begin tracking a specific collection.

Acceptance criteria:
- User can create multiple remittance trackers.
- Each tracker has its own members and records.
- New tracker appears immediately on remittance home.
- Current period is created automatically.

### US-REM-002 View remittance list
As a user, I want to see all my remittance trackers in one place so I can open the right collection quickly.

Acceptance criteria:
- Only remittance trackers appear on this screen.
- User can open a tracker by tapping it.
- User can rename a tracker.
- User can delete a tracker.

### US-REM-003 Record full payment
As a user, I want to mark a member as paid with one tap so payment logging is fast.

Acceptance criteria:
- Tapping a member/period cell toggles paid state.
- Full payment uses the tracker’s configured due amount.
- UI updates immediately.

### US-REM-004 Record partial payment
As a user, I want to record a partial payment and note so I do not lose real payment detail.

Acceptance criteria:
- User can open partial-payment flow from the member-period cell.
- User can enter amount and optional note.
- Partial payments contribute to collected total.
- A member-period becomes effectively cleared once cumulative paid amount reaches the due amount.

### US-REM-005 View member history
As a user, I want to inspect a member’s payment history so I can answer questions or resolve disputes.

Acceptance criteria:
- Member detail shows payment history.
- User can edit/archive/delete member from member flows.
- User can bulk mark outstanding months paid if supported.

### US-REM-006 Adjust due amount
As a user, I want to update the amount due for a tracker so the tracker stays aligned with the real collection amount.

Acceptance criteria:
- User can update due amount from settings for the current remittance tracker.
- New due amount affects future calculations and full-payment toggles.

### US-REM-007 Share proof/status
As a user, I want to share a clean status snapshot so others can verify remittance status externally.

Acceptance criteria:
- User can share the remittance screen as an image.
- User can optionally blur names for privacy.
- Output is suitable for WhatsApp or similar channels.

---

## Screen inventory

### 1. Remittance home
Purpose:
- Show all remittance trackers.
- Create a new remittance.
- Rename/delete trackers.
- Open tracker settings.

Key UI elements:
- top bar with title and settings access
- list of remittance tracker cards
- New Remittance primary action
- swipe to delete for remittance cards only

### 2. Remittance detail
Purpose:
- Show the current collection state for a single tracker.

Key UI elements:
- top bar with tracker title and share action
- summary stats row: collected, expected, outstanding, completion percent
- member layout (grid/cards/other supported layouts)
- tap to mark paid
- long press for partial payment
- member detail access
- add member FAB

### 3. Remittance settings
Purpose:
- Configure the active remittance tracker.

Key UI elements:
- due amount input
- year selection if yearly context is preserved
- start new year action if yearly config is preserved

### 4. Supporting overlays
- add member
- edit member
- partial payment
- member detail
- delete tracker confirmation
- rename tracker

---

## Functional requirements

### Tracker creation
- Name is required.
- Frequency is required.
- Due amount is required for meaningful tracking.
- Members may be seeded at creation time or added later.

### Member management
- Add member with name and optional phone.
- Edit member details.
- Archive member without losing history.
- Delete member when necessary.

### Payment state
- Full payment: paid amount equals due amount.
- Partial payment: paid amount less than due amount.
- Unpaid: no payment recorded for that member-period.

### Period behavior
- The active period must always exist.
- Period label must match the real current cycle.
- Periods should be generated automatically from frequency.

### Summary stats
For the active period, show:
- total collected
- total expected
- outstanding amount
- percent complete

### Sharing
- Share current tracker summary as image.
- Preserve a clean visual hierarchy suitable for messaging apps.
- Support optional privacy mode for names.

---

## Non-functional requirements
- Compose UI
- Room persistence
- StateFlow-driven state
- Offline-first behavior
- Fast interaction for payment toggling
- Safe handling of partial amounts and notes

---

## Out of scope
- Goals
- Todos
- Budget
- General dashboard product decisions
- AI features as a core dependency
- Cross-tracker analytics beyond simple list summary

---

## Success criteria
- User can create and manage multiple remittance trackers without data bleed.
- User can record full and partial payments reliably.
- User can see current period status accurately.
- User can share a proof/status screenshot confidently.
- User no longer needs chats, paper, or memory to know who has cleared.
