# SMART AGENDA — MASTER BLASTER STEP 4

## Phased Production-Quality Gmail + AI Email Assistant + Mood Replies + Real Notifications

**Project:** Smart Agenda
**Package:** `com.smartagenda.app`
**Platform:** Android
**Stack:** Kotlin + Jetpack Compose + Material 3
**IDE/Agent:** Antigravity Pro
**Current baseline:** Existing Smart Agenda project + latest `app-debug(2).apk`
**Design language:** Kinetic Obsidian / Smart Agenda
**Quality target:** 9/10 minimum, 10/10 only if genuinely earned

---

# 0. YOUR ROLE

Act as a:

* Senior Android engineer
* Senior Kotlin/Compose engineer
* Google OAuth/Gmail API engineer
* AI integration engineer
* Product designer
* UX engineer
* Notification engineer
* Security-conscious engineer
* QA engineer
* Release engineer

Your job is NOT simply to add features.

Your job is to take the existing Smart Agenda application from its current partial email/AI prototype state to a **stable, polished, real-device-tested 9–10/10 product-quality prototype**.

---

# 1. ABSOLUTE RULES

## NO CRASHES

## NO FAKE INTEGRATIONS

## NO FAKE SUCCESS STATES

## NO AUTONOMOUS EMAIL SENDING

## NO UNCONTROLLED GIANT REWRITE

Never claim a feature works unless it has actually been implemented and tested.

Compilation is NOT proof of functionality.

A successful static implementation is NOT proof of Gmail connectivity.

A local/demo email is NOT a Gmail email.

A database `sent` state is NOT proof that Gmail actually sent the message.

---

# 2. CURRENT APK BASELINE

The current APK already contains substantial architecture:

* `EmailAssistantScreen`
* `EmailAssistantViewModel`
* `EmailRepository`
* `EmailDao`
* `EmailMessageEntity`
* `EmailDraftEntity`
* `EmailDetailDialog`
* `HumanApprovalDraftDialog`
* `GeminiClient`
* `GeminiEmailAssistant`
* Room database
* Reminder architecture
* Notification architecture
* Date/time picker architecture

The draft model contains:

* `id`
* `emailId`
* `recipientEmail`
* `recipientName`
* `subject`
* `draftBody`
* `tone`
* `status`
* `createdAt`
* `approvedAt`
* `sentAt`

The APK also contains:

* tone handling
* draft regeneration
* human approval
* Gemini integration
* local/seeded email data
* offline/local NLP fallback

The current architecture appears approximately:

```text
LOCAL / SEEDED EMAIL
        ↓
     GEMINI
        ↓
   AI DRAFT
        ↓
      TONE
        ↓
 HUMAN APPROVAL
```

The target architecture is:

```text
GOOGLE AUTH
      ↓
GMAIL OAUTH
      ↓
REAL GMAIL
      ↓
GMAIL MESSAGE / THREAD
      ↓
EMAIL ANALYSIS
      ↓
GEMINI
      ↓
AI DRAFT
      ↓
MOOD / TONE
      ↓
USER EDITS
      ↓
HUMAN APPROVAL
      ↓
GMAIL API
      ↓
REAL SENT REPLY
```

---

# 3. PHASED IMPLEMENTATION PLAN

## IMPORTANT

Implement the project in the following phases.

### DO NOT jump directly to Phase 5.

At the end of every phase:

1. Build.
2. Install.
3. Run.
4. Test the phase.
5. Fix problems.
6. Rebuild.
7. Confirm acceptance criteria.
8. Only then continue.

If a phase fails, stop feature expansion and fix that phase first.

---

# PHASE 0 — FULL PROJECT AUDIT + BASELINE

## Objective

Understand exactly what already exists before changing anything.

### Inspect

* Entire source tree
* Gradle configuration
* Dependencies
* AndroidManifest
* Navigation
* Activities
* ViewModels
* Repositories
* Room
* Database migrations
* Email models
* Gemini implementation
* Authentication code
* Notification code
* Resources
* Icons
* Themes
* Settings
* Existing date/time picker
* Existing search
* Existing Agenda flows

### Determine

For every email feature, classify it as:

* Real
* Mock
* Seeded
* Local
* API-backed
* Partially implemented
* Broken

### Required output

Before coding, provide:

```text
CURRENT STATE
What works

PARTIAL
What is partially implemented

MISSING
What needs implementation

RISK
What could cause regressions

PLAN
What will be changed and in which phase
```

### Phase 0 acceptance

* Full architecture understood.
* No unnecessary rewrite planned.
* Existing good architecture identified for reuse.

---

# PHASE 1 — STABILITY + FOUNDATION

## Objective

Make the existing application stable before introducing real Gmail.

### Verify

* App launch
* Navigation
* Agenda
* Search
* Horizontal date row
* Add reminder
* Edit
* Delete
* Complete
* Month view
* Date picker
* Time picker
* Room persistence
* Existing email screen
* Existing Gemini draft flow

### Fix

* Null crashes
* Navigation crashes
* Database crashes
* Compose state problems
* Broken buttons
* Infinite loading
* malformed AI response handling
* invalid IDs
* lifecycle problems

### Database

Pay special attention to the existing `tone` addition.

Verify:

* schema version
* migration
* fresh install
* upgrade from previous version
* existing data preservation

### Phase 1 acceptance

The existing application must remain stable.

**No new Gmail work proceeds until this passes.**

---

# PHASE 2 — GOOGLE AUTHENTICATION

## Objective

Implement real Google authentication.

Do NOT blindly copy deprecated Google Sign-In tutorials.

Use the currently appropriate Google Android authentication approach, including Credential Manager / current Google Identity tooling where applicable.

### Implement

* Google sign-in
* account identification
* authenticated state
* sign-out
* cancellation
* failure handling
* network failure
* account switching
* returning to correct screen

### Security

Do not expose:

* client secrets
* access tokens
* refresh tokens
* authorization codes

in logs or UI.

If backend validation is required by the architecture, implement proper ID-token validation.

### UI

Settings should show:

```text
Google Account

Not connected

[Sign in with Google]
```

After success:

```text
Google Account

user@example.com

[Sign out]
```

### Phase 2 acceptance

Test:

* sign in
* cancel
* sign out
* sign in again
* wrong/unavailable account state
* network failure

No crash allowed.

---

# PHASE 3 — GMAIL OAUTH + CONNECTION

## Objective

Connect the authenticated Google account to Gmail through proper OAuth authorization.

Remember:

**Google authentication ≠ Gmail authorization.**

The user must explicitly authorize Gmail access.

### Implement

* Connect Gmail
* Gmail permission request
* authorization callback
* connected state
* disconnect
* reconnect
* revoked permission handling
* expired credential handling
* account switching

### OAuth scopes

Use only the narrowest scopes necessary for the implemented features.

Do not request unnecessary mailbox permissions.

Document:

```text
SCOPE
WHY IT IS REQUIRED
WHERE IT IS USED
```

### UI

Disconnected:

```text
Gmail

Not connected

[Connect Gmail]
```

Connected:

```text
Gmail

Connected
user@example.com

[Disconnect]
```

### Error states

Never leave the user at:

* blank screen
* infinite loading
* fake connected state

Instead show useful recovery.

Example:

> Gmail connection needs to be restored.

[Reconnect Gmail]

### Phase 3 acceptance

A real Google account must successfully authorize Gmail.

The app must correctly detect:

* connected
* disconnected
* denied
* revoked
* expired/reconnect-required

---

# PHASE 4 — REAL GMAIL READ + LOCAL CACHE

## Objective

Replace the normal demo/seeded email experience with real Gmail data when Gmail is connected.

### Implement

* Gmail message retrieval
* message IDs
* thread IDs
* sender
* recipient
* subject
* timestamp
* meaningful message body
* read/unread where supported
* local Room cache

### Deduplication

Use real Gmail IDs.

Never create fake Gmail IDs.

Avoid:

* duplicate messages
* duplicate threads
* repeated downloads
* unnecessary API calls

### Demo data

Demo emails may remain for development only.

When Gmail is connected:

## REAL GMAIL DATA MUST BE CLEARLY USED.

Never label seeded data as Gmail data.

### Email body handling

Handle:

* plain text
* HTML
* signatures
* quoted replies
* forwarded content
* long threads
* attachments

Do not blindly send huge HTML blobs to Gemini.

Extract meaningful content.

### Phase 4 acceptance

Test:

```text
Connect Gmail
↓
Open Email Assistant
↓
Fetch real email
↓
Display real sender
↓
Display real subject
↓
Open real body
↓
Persist/cache correctly
```

---

# PHASE 5 — GEMINI + AI EMAIL ANALYSIS

## Objective

Connect the existing Gemini architecture to real Gmail messages.

Reuse:

* `GeminiClient`
* `GeminiEmailAssistant`
* `EmailRepository`
* `EmailAssistantViewModel`

Do not rebuild the entire AI architecture unless necessary.

### Flow

```text
REAL GMAIL EMAIL
        ↓
BODY EXTRACTION
        ↓
CONTEXT CLEANING
        ↓
GEMINI
        ↓
SUGGESTED REPLY
```

### AI must NOT invent

Never invent:

* attachments
* facts
* prices
* dates
* qualifications
* job experience
* promises
* completed actions
* documents

Example:

If sender says:

> Please send your resume.

AI must NOT claim:

> I have attached my resume.

unless a real attachment exists.

### AI draft instruction

The model must understand:

> This is a draft for human review and approval.

It must never claim:

> Email sent.

unless the actual Gmail API has confirmed sending.

### Phase 5 acceptance

Real Gmail email:

→ Gemini analyzes it
→ draft appears
→ no crash
→ no fabricated facts
→ human remains in control

---

# PHASE 6 — MOOD / TONE ENGINE

## Objective

Make the tone feature a polished core Smart Agenda capability.

Required tones:

1. Friendly
2. Professional
3. Short & Direct
4. Polite
5. Warm
6. Casual
7. Confident
8. Firm
9. Detailed

### Recommended UI

Do NOT show nine giant buttons.

Use:

```text
Suggested Reply

Tone: Professional ▾

[Regenerate]
```

Then a compact selector/bottom sheet:

```text
Friendly
Professional
Short & Direct
Polite
Warm
Casual
Confident
Firm
Detailed
```

### Tone behavior

Changing tone may modify:

* vocabulary
* formality
* warmth
* sentence structure
* conciseness

It must NOT modify:

* facts
* names
* dates
* recipient
* commitments
* requested information

### Regeneration

When tone changes:

Prefer:

```text
Tone changed
↓
User taps Regenerate
↓
Generating...
↓
New draft
```

Do not repeatedly call Gemini automatically during UI recomposition.

### Optional

A custom tone may be added:

> Make this professional, friendly and very short.

But only if it does not compromise stability.

### Phase 6 acceptance

For the same email:

* Professional produces professional language.
* Friendly produces warmer language.
* Short & Direct is concise.
* Facts remain unchanged.
* Draft remains editable.

---

# PHASE 7 — HUMAN APPROVAL + REAL GMAIL SEND

## Objective

Complete the most important end-to-end workflow.

Target:

```text
Real Gmail email
↓
AI draft
↓
Choose tone
↓
Regenerate
↓
Edit
↓
Review
↓
APPROVE & SEND
↓
Gmail API
↓
Actual sent reply
```

## ABSOLUTE RULE

AI NEVER sends automatically.

Only an explicit user action:

# APPROVE & SEND

may trigger Gmail sending.

### Sending states

Use:

```text
Draft
Editing
Sending
Sent
Failed
Discarded
```

### Duplicate-send protection

When Send is tapped:

1. Disable button.
2. Show `Sending...`
3. Send once.
4. Wait for actual Gmail success.
5. Mark as Sent only after success.
6. Restore UI appropriately.

Prevent:

* double taps
* duplicate requests
* accidental repeat sends
* send on recomposition
* send after app restart

### Failure

If Gmail fails:

> The email wasn't sent.

[Try Again]

Do NOT display Sent.

### Threading

Use actual Gmail:

* message ID
* thread ID

Reply to the correct Gmail conversation where supported.

Do not fake threading merely by copying a subject.

### Phase 7 acceptance

On a real Gmail account:

```text
Receive email
↓
Smart Agenda displays it
↓
Generate reply
↓
Choose mood
↓
Edit
↓
Approve & Send
↓
Gmail confirms send
↓
Message appears in Sent
↓
Correct conversation/thread
```

This is the **critical Step 4 milestone**.

---

# PHASE 8 — REAL SMART AGENDA NOTIFICATIONS

## Objective

Make notifications feel like a real finished product rather than a technical prototype.

---

## 8.1 Notification design

Example:

```text
Smart Agenda

Dentist appointment

Today • 6:30 PM

[Complete] [Open]
```

Keep:

* clear title
* useful date/time
* concise body
* recognizable identity

Avoid:

* technical IDs
* database terminology
* duplicate information
* huge text
* excessive emojis

---

# 8.2 Smart Agenda notification logo

Create/use a proper notification-safe icon.

IMPORTANT:

Android notification icons have special requirements.

Do NOT blindly use the full-color launcher logo as the small notification icon.

Create an appropriate:

## monochrome notification icon

while preserving Smart Agenda branding.

Verify:

* launcher icon
* adaptive icon
* notification icon
* light/dark system behavior

---

# 8.3 Notification sound

Add a short Smart Agenda reminder sound.

Desired:

* pleasant
* premium
* bell-like
* recognizable
* short

It must:

## PLAY ONCE

Never:

* loop
* continuously ring
* behave like an alarm
* excessively vibrate

Prefer a short bundled notification sound or appropriate system sound.

Respect Android channel/system settings.

---

# 8.4 Notification channel

Inspect existing channel creation.

Ensure:

* meaningful channel name
* useful description
* correct importance
* correct sound
* no unnecessary duplicate channels
* channel is not destructively recreated
* user settings are respected

Example:

**Smart Agenda Reminders**

> Notifications for scheduled Smart Agenda reminders.

---

# 8.5 Notification actions

Useful actions may include:

* Complete
* Open

Do not add unnecessary actions.

---

# 8.6 Notification lifecycle

Verify:

```text
Reminder saved
↓
Notification scheduled
↓
App closed/background
↓
Reminder time arrives
↓
Notification appears
↓
Smart Agenda identity
↓
Reminder title
↓
Date/time
↓
Bell plays once
↓
Tap
↓
Correct reminder
```

---

# 8.7 Notification edge cases

Test:

* app open
* app background
* app closed
* device restart if practical
* reminder edited
* reminder deleted
* reminder completed
* duplicate scheduling
* notification already displayed
* notification permission denied
* past reminder

Never leave stale notifications after deleting a reminder.

Never create duplicates after editing.

---

# PHASE 8.8 — FINAL UX POLISH

After functionality works, polish:

* typography
* spacing
* card hierarchy
* button hierarchy
* loading states
* empty states
* error states
* touch targets
* transitions
* keyboard handling
* scrolling
* accessibility
* visual consistency

Keep Smart Agenda:

**premium + calm + human + intelligent**

Avoid:

* neon AI visuals
* excessive gradients
* giant AI labels
* unnecessary glass effects
* excessive 3D
* flashy animation

---

# 9. SECURITY PASS

Before release, inspect:

* API keys
* OAuth secrets
* tokens
* refresh tokens
* authorization codes
* logs
* local storage
* Room data
* network errors

Never expose credentials in:

* source constants
* UI
* logs
* crash messages
* screenshots

Do not put production secrets into a distributed Android APK simply because it is convenient.

If secure backend handling is required, implement it correctly.

---

# 10. TOKEN LIFECYCLE

Test:

* access token expiration
* refresh
* revoked access
* disconnected account
* account switching
* reauthorization

Expected behavior:

```text
Gmail authorization invalid
↓
Clear explanation
↓
Reconnect
↓
Return to Gmail feature
```

No crash.

No infinite spinner.

---

# 11. NETWORK FAILURE

Every network operation needs:

* loading state
* failure state
* retry
* timeout/recovery
* user-friendly error

Never:

* freeze
* crash
* remain indefinitely loading
* show fake success

---

# 12. OFFLINE BEHAVIOR

The local Agenda must continue working offline.

Separate:

## LOCAL

* Agenda
* reminders
* date/time
* Room
* local notification scheduling

from:

## ONLINE

* Gmail
* Google authentication
* Gemini

Never falsely claim Gmail/AI is available offline.

---

# 13. DATABASE / MIGRATION TESTING

Because `tone` was added to the email draft model:

Test:

### Fresh install

and:

### Upgrade installation

Verify:

* database version
* migration
* existing reminders
* existing emails
* existing drafts
* tone values
* no crashes

---

# 14. EXISTING AGENDA REGRESSION

Step 4 must NOT break the original application.

Test:

* launch
* Agenda
* date switching
* horizontal date row
* search
* filters
* Add Reminder
* date picker
* time picker
* Save
* Edit
* Delete
* Complete
* Month view
* navigation
* Back
* persistence
* notifications

---

# 15. PERFORMANCE

Avoid:

* blocking main thread
* network calls during Compose recomposition
* repeated Gemini calls
* repeated Gmail synchronization
* database calls directly from rendering
* unnecessary polling
* excessive memory use
* leaks
* large email bodies kept unnecessarily

Use appropriate:

* coroutines
* ViewModels
* repositories
* state management

---

# 16. CRASH-PREVENTION AUDIT

Search specifically for:

* null values
* invalid IDs
* missing records
* navigation failures
* OAuth failures
* Gmail malformed data
* Gemini malformed responses
* JSON parsing failures
* Room migrations
* notification receivers
* permission denial
* lifecycle cancellation
* rapid repeated taps
* configuration changes
* missing resources

Every external operation must fail gracefully.

Avoid silent exception swallowing such as:

```kotlin
catch (Exception) {
}
```

Log safely and expose useful UI state.

Never log secrets.

---

# 17. REAL-DEVICE TEST MATRIX

Minimum required tests:

### Authentication

1. Cold launch
2. Google sign-in
3. Cancel sign-in
4. Sign out
5. Sign in again

### Gmail

6. Connect Gmail
7. Deny Gmail permission
8. Reconnect
9. Fetch real inbox
10. Open real email
11. Read real body
12. Verify thread ID

### AI

13. Generate draft
14. Regenerate
15. Professional
16. Friendly
17. Short & Direct
18. Other tones
19. Edit draft

### Sending

20. Approve & Send
21. Verify Gmail Sent
22. Verify correct thread
23. Double-tap Send
24. Network failure during Send
25. Reauthorization after token failure

### Agenda

26. Create reminder
27. Edit reminder
28. Delete reminder
29. Complete reminder
30. Date switching
31. Search
32. Month view
33. Persistence

### Notification

34. Permission granted
35. Notification appears
36. Logo/icon correct
37. Bell plays once
38. No looping
39. Tap Open
40. Complete action
41. Edit reminder
42. Delete reminder
43. Duplicate notification check
44. App closed
45. Device restart if practical

### Final

46. Restart app
47. Full regression
48. Clean reinstall
49. Upgrade install
50. Final end-to-end test

---

# 18. RELEASE BLOCKERS

The application is NOT ready if any of these exist:

* launch crash
* Google auth crash
* Gmail OAuth crash
* Gmail API crash
* Gemini crash
* malformed AI response crash
* Room migration crash
* notification crash
* broken Save
* broken date/time
* broken Search
* broken navigation
* broken Back
* fake Gmail connection
* fake Sent state
* duplicate email sends
* duplicate notifications
* stale notifications
* missing permission handling
* exposed secrets
* infinite loading
* dead primary buttons
* severe clipping/overlap

---

# 19. BUILD / TEST GATE SYSTEM

Every phase has a gate.

Use:

```text
PHASE START
↓
IMPLEMENT
↓
COMPILE
↓
INSTALL
↓
TEST
↓
FIX
↓
REBUILD
↓
ACCEPTANCE CHECK
↓
NEXT PHASE
```

Do not stack multiple untested phases together.

If Phase 4 fails, do not start Phase 5.

If Phase 7 fails, do not spend time polishing visual effects.

---

# 20. FINAL BUILD PROCESS

At the end:

1. Clean project.
2. Build.
3. Fix all compile errors.
4. Install.
5. Test.
6. Upgrade-install over previous APK.
7. Test migration.
8. Fix every blocker.
9. Clean build again.
10. Install final APK.
11. Perform complete real-device matrix.
12. Re-test critical Gmail send flow.
13. Re-test notification.
14. Re-test Agenda.
15. Produce final APK.

---

# 21. FINAL QUALITY SCORE

Rate separately:

| Area                  | Score /10 |
| --------------------- | --------: |
| Android architecture  |           |
| Agenda                |           |
| Database              |           |
| Google authentication |           |
| Gmail OAuth           |           |
| Gmail reading         |           |
| Gmail threading       |           |
| Gemini                |           |
| Mood/tone system      |           |
| Draft UX              |           |
| Human approval        |           |
| Real Gmail sending    |           |
| Notifications         |           |
| UI/UX                 |           |
| Security              |           |
| Reliability           |           |
| Performance           |           |
| Testing               |           |

Then provide:

# HONEST OVERALL SCORE: X/10

Do NOT automatically give 10/10.

---

# 22. FINAL REPORT

Return:

## 1. IMPLEMENTED

Everything actually completed.

## 2. NOT IMPLEMENTED

Anything still missing.

## 3. GOOGLE AUTH

Implementation + test status.

## 4. GMAIL

OAuth + scopes + reading + threading + sending.

## 5. GEMINI

AI implementation + fallback behavior.

## 6. MOODS

Exactly which tones work.

## 7. HUMAN APPROVAL

Explain how sending is protected.

## 8. NOTIFICATIONS

Explain:

* icon
* logo
* channel
* sound
* actions
* app-closed behavior
* stale notification handling

## 9. DATABASE

Migration + persistence status.

## 10. SECURITY

Credential/token handling.

## 11. TEST DEVICE

Actual device/emulator used.

## 12. TEST RESULTS

Passed/failed.

## 13. KNOWN BUGS

Only genuine known bugs.

## 14. FINAL RATING

Honest score.

---

# 23. FINAL DEFINITION OF DONE

Step 4 is complete ONLY when:

### Google

Google authentication works.

### Gmail

Real Gmail OAuth works.

### Inbox

Real Gmail messages appear.

### Threads

Real Gmail thread/message identifiers are used.

### AI

Real Gmail messages can produce Gemini drafts.

### Mood

Multiple meaningful tones work.

### Editing

User can edit the draft.

### Approval

User explicitly approves.

### Sending

Approved response is actually sent through Gmail.

### Safety

AI cannot autonomously send.

### Notifications

Notifications have:

* Smart Agenda identity
* notification-safe logo/icon
* clear reminder information
* short pleasant bell sound
* one-time playback
* useful action(s)
* correct app opening

### Reliability

No known release-blocking crash.

### Regression

Agenda/reminder functionality remains functional.

### Testing

Real-device end-to-end testing passes.

### Release

Clean final APK builds, installs, launches and survives the full test matrix.

---

# 24. FINAL COMMAND

Do not rush.

Do not blindly rewrite the project.

Do not add unrelated features.

Do not fake Gmail.

Do not fake AI.

Do not fake Sent status.

Do not fake notification behavior.

Do not claim crash-free simply because compilation succeeded.

Work phase-by-phase.

At every phase, prove the result before continuing.

The final product should feel like:

> **A real premium productivity application that combines a reliable personal agenda with an intelligent Gmail drafting assistant while keeping the human completely in control.**

The target flow must ultimately work on a real device:

```text
GOOGLE LOGIN
↓
CONNECT GMAIL
↓
REAL EMAIL
↓
AI UNDERSTANDS EMAIL
↓
CHOOSE MOOD
↓
GENERATE DRAFT
↓
EDIT
↓
APPROVE & SEND
↓
REAL GMAIL REPLY
```

And independently:

```text
CREATE REMINDER
↓
DATE
↓
TIME
↓
SAVE
↓
SCHEDULE
↓
SMART AGENDA NOTIFICATION
↓
BRANDED ICON
↓
SHORT BELL
↓
OPEN / COMPLETE
```

## Final target:

**Stable → Real → Polished → Tested → Release-ready**

**9/10 minimum.
10/10 must be earned.**
