# Smart Agenda — System Specification

## Phase 1: Local Agenda & Reminder MVP

You are working on Phase 1 of a larger Android application called **Smart Agenda**.

The long-term product will eventually contain:

1. A day-to-day agenda/reminder system.
2. Natural-language reminder input using AI.
3. Gmail integration and AI-generated email drafts with human approval.

However, **DO NOT BUILD THE AI, Gmail, Firebase, OAuth, SMS, WhatsApp, or cloud features yet.**

The only goal of this phase is to build a polished, reliable **local agenda/reminder application** that works completely offline.

---

# 1. SOURCE OF TRUTH

There are two important sources of truth for this project:

### A. Product architecture

The project blueprint supplied with this project defines the intended technical direction:

* Android
* Kotlin
* Jetpack Compose
* Room / SQLite for local storage
* WorkManager and/or AlarmManager for reminder scheduling
* Android notifications
* No external APIs for the Phase 1 MVP

Use the blueprint as the architectural reference.

### B. Visual design

A **Stitch-generated UI design has already been uploaded into Antigravity**.

IMPORTANT:

The existing Stitch design is the **visual source of truth** for the application UI.

Do NOT redesign the interface from scratch.

Do NOT replace the Stitch design with a generic Android template.

Do NOT introduce unrelated UI patterns.

Preserve the Stitch design's:

* overall visual hierarchy
* screen structure
* spacing
* typography hierarchy
* card structure
* navigation style
* button placement
* icon usage
* colors
* backgrounds
* visual density
* rounded corners
* component relationships
* empty states
* interaction flow

Where the Stitch design contains visual elements that are not required for Phase 1 functionality, preserve their appearance where practical but keep their underlying implementation simple.

If the Stitch design conflicts with this system specification, prioritize:

1. Functional correctness
2. Android platform requirements
3. This SYSTEM.md architecture
4. Stitch visual design

Do not silently introduce a completely different design.

---

# 2. CURRENT PHASE

## PHASE 1 ONLY

Build:

> A local day-to-day agenda and reminder application.

The user should be able to open the application, see today's agenda, create reminders, edit them, mark them complete, delete them, and receive Android notifications at the scheduled time.

Everything must work without internet access.

---

# 3. TECHNOLOGY

Use:

* Kotlin
* Jetpack Compose
* Android SDK
* Room
* SQLite
* Android notification system
* AlarmManager where exact scheduling is required
* WorkManager only where appropriate for non-exact background work

Do not add a framework or library unless it provides a clear benefit to this MVP.

Avoid unnecessary dependencies.

Prefer official Android / Jetpack components.

The application should remain understandable to a BCA student who is learning Android development through vibe coding.

---

# 4. ARCHITECTURE

Use a clean but intentionally simple architecture.

Recommended structure:

app/
├── data/
│   ├── local/
│   │   ├── AppDatabase
│   │   ├── ReminderEntity
│   │   └── ReminderDao
│   │
│   └── repository/
│       └── ReminderRepository
│
├── notification/
│   ├── NotificationHelper
│   └── ReminderScheduler
│
├── ui/
│   ├── agenda/
│   ├── add/
│   ├── edit/
│   └── components/
│
├── navigation/
│
└── MainActivity

Do not create dozens of unnecessary abstraction layers.

Do not introduce enterprise-level architecture for a small local MVP.

Keep responsibilities clear:

UI
→ ViewModel/state
→ Repository
→ Room

and

Reminder
→ Scheduler
→ Android Alarm/Notification

---

# 5. DATA MODEL

Create a local Room entity for agenda/reminder items.

At minimum, a reminder should contain:

* id
* title
* description or notes
* scheduled date
* scheduled time
* completed status
* created timestamp

Use appropriate Android/Kotlin date/time types or a reliable representation compatible with the chosen Room setup.

The database must survive:

* app closing
* app reopening
* device restart where scheduling is restored appropriately

Do not store agenda data only in Compose state.

Room must be the source of truth for persistent agenda data.

---

# 6. CORE USER FEATURES

## 6.1 Today's Agenda

The main screen should show the user's agenda.

The screen must clearly communicate:

* today's date
* scheduled reminders
* completed reminders
* upcoming reminders
* empty state when there are no reminders

Follow the Stitch design for the actual visual presentation.

The UI should update when reminders are added, edited, completed, or deleted.

---

# 7. CREATE REMINDER

Provide a clear action for creating a new agenda item.

The user should be able to enter:

* title
* optional notes
* date
* time

The minimum valid reminder requires:

* title
* scheduled date
* scheduled time

Validate user input before saving.

After saving:

1. Store the reminder in Room.
2. Schedule its notification.
3. Return to the agenda.
4. Display the newly created reminder.

Do not require internet access.

---

# 8. EDIT REMINDER

The user must be able to edit an existing reminder.

When editing:

* update the Room record
* cancel the old scheduled alarm if necessary
* schedule the new alarm
* update the agenda UI

Never leave the old notification scheduled after a reminder's time has been changed.

---

# 9. COMPLETE REMINDER

The user must be able to mark a reminder as completed.

Completion should:

* update Room
* immediately update the UI
* visually distinguish the completed item according to the Stitch design

If a future notification exists for a reminder that has been completed early, cancel that scheduled notification.

---

# 10. DELETE REMINDER

The user must be able to delete a reminder.

Deleting must:

1. Cancel its scheduled notification.
2. Delete it from Room.
3. Update the UI.

Do not leave orphaned alarms.

---

# 11. NOTIFICATION SYSTEM

Notifications are a core requirement of Phase 1.

The reminder engine must schedule an Android notification for the selected date and time.

The notification should contain:

* reminder title
* useful reminder information
* application identity

When the user taps the notification:

→ open the application
→ preferably navigate to the relevant reminder or agenda screen

Use stable reminder IDs so each reminder can be independently scheduled and cancelled.

---

# 12. ANDROID PERMISSIONS

Handle Android notification permissions correctly.

For Android 13+:

* request POST_NOTIFICATIONS at runtime when appropriate.

If exact alarm scheduling is required by the implementation and Android version:

* handle the relevant exact-alarm requirement correctly.

Do not blindly request every permission.

Only request permissions that are actually necessary.

Explain any permission-related code clearly before introducing complicated workarounds.

---

# 13. DEVICE RESTART

Reminder scheduling must be designed with device restarts in mind.

If the chosen scheduling mechanism requires alarms to be restored after reboot:

* implement a lightweight boot receiver
* read pending reminders from Room
* reschedule appropriate future reminders

Do not create a permanent foreground service just to make reminders work.

Do not create unnecessary background services.

---

# 14. PAST REMINDERS

Handle reminders whose scheduled time has already passed sensibly.

Do not repeatedly fire old notifications.

Past reminders should remain available in the agenda/history according to the UI design, but should not continuously reschedule themselves.

---

# 15. DATE AND TIME

Use the device's local timezone.

Do not hard-code timezone offsets.

Use Android/Kotlin date-time APIs appropriately.

When scheduling a reminder, interpret the selected date and time according to the device's current local timezone.

---

# 16. OFFLINE-FIRST REQUIREMENT

Phase 1 must work with:

* Wi-Fi OFF
* mobile data OFF
* airplane mode where Android notification behavior permits

No network request should be necessary to:

* open the agenda
* create a reminder
* edit a reminder
* complete a reminder
* delete a reminder
* schedule a notification

The application must be genuinely local.

---

# 17. NAVIGATION

Implement only the navigation required by the Stitch design and Phase 1.

Possible screens:

* Agenda/Home
* Add Reminder
* Edit Reminder
* Optional reminder detail screen if present in the Stitch design

Do not build future Gmail, AI, chat, or settings screens unless the Stitch design requires their visual presence.

If future navigation elements appear in the Stitch design, they may exist visually as disabled/placeholders, but they must not contain fake functionality.

Never pretend an unimplemented feature works.

---

# 18. UI RULES

The Stitch design is the visual reference.

Do not:

* randomly change colors
* add gradients unless present in the design
* add excessive animations
* add unnecessary glassmorphism
* add unnecessary shadows
* add dark mode unless the design specifically requires it
* add a generic Material 3 dashboard just because it is easier
* replace designed components with default-looking components

Use Compose components to reproduce the design accurately.

Animations should be subtle and only used when they improve usability.

Functionality is more important than decorative effects.

---

# 19. ACCESSIBILITY

Use:

* readable text sizes
* meaningful content descriptions for icons
* sufficient touch target sizes
* sensible contrast
* clear visual distinction between active and completed reminders

Do not sacrifice usability to reproduce a visual effect.

---

# 20. ERROR HANDLING

The application should gracefully handle:

* empty title
* invalid date/time
* database errors
* notification permission denied
* exact alarm permission unavailable
* alarm scheduling failure

Do not crash because a notification permission was denied.

If a permission is unavailable, explain the problem to the user through a simple UI message.

---

# 21. STATE MANAGEMENT

Compose UI should observe persistent data rather than maintaining an independent duplicate copy.

Prefer:

Room
→ Flow
→ ViewModel
→ Compose UI

The UI should automatically reflect database changes.

Avoid unnecessary global mutable state.

---

# 22. TESTING REQUIREMENT

Do not consider the feature complete merely because the application compiles.

After implementing each major feature, test it.

Minimum test sequence:

### Test 1

Create reminder for a future time.

Expected:

* reminder appears in agenda
* reminder exists after app restart

### Test 2

Wait for scheduled notification.

Expected:

* Android notification appears at the scheduled time

### Test 3

Tap notification.

Expected:

* application opens

### Test 4

Edit reminder time.

Expected:

* old alarm is cancelled
* new alarm is scheduled

### Test 5

Delete reminder.

Expected:

* reminder disappears
* its alarm is cancelled

### Test 6

Complete reminder.

Expected:

* UI shows completed state
* future notification is cancelled if appropriate

### Test 7

Close and reopen app.

Expected:

* reminders remain available

### Test 8

Restart device.

Expected:

* appropriate future reminders are restored/rescheduled

---

# 23. DEVELOPMENT METHOD

IMPORTANT:

Do NOT generate the entire application blindly in one huge operation.

Build incrementally.

Recommended order:

## Step A

Inspect the existing project and Stitch design.

## Step B

Create/verify the Android project structure.

## Step C

Implement the basic Compose UI from the Stitch design using static/mock data.

## Step D

Implement Room.

## Step E

Connect the agenda UI to Room.

## Step F

Implement create reminder.

## Step G

Implement edit/delete/complete.

## Step H

Implement notification scheduling.

## Step I

Implement notification permission handling.

## Step J

Test notification behavior on a real Android device.

## Step K

Fix bugs and polish the Stitch UI.

Only after Phase 1 is stable should Phase 2 begin.

---

# 24. ANTIGRAVITY BEHAVIOR

You are acting as an implementation agent.

Before modifying files:

1. Inspect the existing project.
2. Inspect the Stitch-generated design available in this Antigravity workspace.
3. Identify the current Android SDK/configuration.
4. Identify existing dependencies.
5. Reuse working code where appropriate.
6. Do not overwrite useful existing work without reason.

Before adding a dependency:

* explain why it is needed
* check whether an existing dependency can solve the problem

Before making large architectural changes:

* explain the reason
* keep the change limited to Phase 1

When code generation creates complex Android permission, alarm, receiver, or background behavior:

* explain what the generated code does
* keep it minimal
* avoid unnecessary services

---

# 25. CODE QUALITY

The code should be:

* readable
* modular
* beginner-friendly
* maintainable
* reasonably concise
* production-minded without overengineering

Do not optimize for minimum line count at the expense of reliability.

Do not optimize for maximum abstraction.

Prefer straightforward Kotlin.

Use meaningful names.

Add comments only where they explain non-obvious Android behavior.

---

# 26. FUTURE FEATURES — DO NOT IMPLEMENT YET

The following belong to later phases.

## Phase 2

Natural-language reminder input.

Example:

"Remind me tomorrow at 9 PM to call Rahul."

Future pipeline:

User text
→ AI parsing
→ structured date/time/message
→ local scheduler

The Phase 1 application should be architected so this can be added later without rebuilding the reminder engine.

## Phase 3

Gmail integration.

Future components:

* Google OAuth
* Gmail API
* Firebase
* Gemini
* email classification
* AI-generated draft replies
* human approval
* send action

The blueprint specifically requires the human approval step before sending an AI-generated reply.

## DO NOT BUILD THESE NOW.

---

# 27. FUTURE-PROOFING WITHOUT OVERBUILDING

The Phase 1 reminder model should be capable of being reused later by:

* manual reminder creation
* AI-generated reminders
* future chat input

Therefore, keep reminder scheduling independent from the UI.

For example:

UI
→ ReminderRepository
→ ReminderScheduler

and later:

AI parser
→ ReminderRepository
→ ReminderScheduler

Do not put scheduling logic directly inside Compose UI.

---

# 28. NO FAKE AI

Do not create fake AI functionality.

Do not make a button that says "AI" and does nothing.

Do not simulate Gmail.

Do not use hard-coded fake email responses.

Do not claim that Gemini is connected.

Do not claim that Gmail is connected.

Phase 1 should honestly be a local agenda/reminder application.

---

# 29. NO EXTERNAL API DEPENDENCIES

Phase 1 must not require:

* Gemini API key
* Firebase project
* Gmail credentials
* Google OAuth
* backend server
* internet connection

The app should compile and run locally without any external service configuration.

---

# 30. DEFINITION OF DONE

Phase 1 is complete only when:

* [ ] Application builds successfully.
* [ ] Application launches successfully.
* [ ] Stitch design has been implemented as closely as practical.
* [ ] Agenda screen works.
* [ ] Reminder can be created.
* [ ] Reminder is saved in Room.
* [ ] Reminder can be edited.
* [ ] Reminder can be completed.
* [ ] Reminder can be deleted.
* [ ] Notification permission is handled.
* [ ] Reminder notification is scheduled.
* [ ] Notification fires at the intended time.
* [ ] Notification opens the application.
* [ ] Old alarms are cancelled when reminders are edited/deleted/completed.
* [ ] Data survives app restart.
* [ ] Future alarms are restored after device reboot where required.
* [ ] Application works without external APIs.
* [ ] No fake AI/Gmail functionality exists.
* [ ] No major unnecessary dependencies were added.
* [ ] The project is ready for Phase 2 natural-language reminders.

---

# 31. MOST IMPORTANT RULE

Build the smallest reliable version first.

Do not try to impress the user with complexity.

The objective of Phase 1 is:

> "I can create a reminder, close the app, and my phone still reminds me at the correct time."

If that works reliably and the UI matches the Stitch design, Phase 1 is successful.

Only then move toward AI and Gmail.
