# SMART AGENDA — MASTER PROMPT 2/3
## CORE INTERACTIONS + REMINDERS + NOTIFICATIONS + STABILITY

### ROLE
Continue as a senior Android engineer, UX engineer, notification engineer, and QA engineer.

This step starts from the project produced by **Master Prompt 1/3**. Inspect the current state first; do not assume everything was completed.

## SHARED NON-NEGOTIABLE RULES

- Work on the EXISTING Smart Agenda project. Do not rebuild it.
- Preserve Kotlin + Jetpack Compose + Material 3.
- Preserve existing navigation, ViewModels, repositories, Room/database, reminder model, notification architecture, date/time logic, AI architecture, email architecture, package/application identity, and working business logic.
- Do not migrate architecture or introduce a new framework unless absolutely required to fix an existing issue.
- Do not remove working functionality.
- Do not redesign the product into something else.
- **SECURITY MUST NOT BE MODIFIED.** Do not change API/security configuration, authentication, credentials, API keys, network security, permissions strategy, backend/API architecture, email authorization, or data-storage security. If a security concern is found, report it only.
- Do not add unrelated features. This is a polish, reliability, and release-readiness pass.
- Build/test incrementally. Never accumulate large untested changes.
- If something is already good, leave it alone.


## STEP 2 OBJECTIVE
Make the **core reminder experience reliable end-to-end**:

**Add → title/details → date → time → save → agenda → notification**

Then verify edit, delete, complete/uncomplete, navigation, persistence, and notification behavior.

## EXECUTION ORDER

1. Build the current project before further changes.
2. Audit Add Reminder:
   - input/focus
   - keyboard behavior
   - validation
   - date/time selection
   - Save/Cancel
   - back behavior
   - duplicate taps
   - successful persistence
3. Audit Edit Reminder:
   - existing values load
   - date/time changes
   - update does not create duplicates
   - UI/database remain synchronized
4. Audit Delete Reminder:
   - safe deletion
   - no stale card
   - database updates
   - notification handling follows existing architecture
5. Audit Complete/Uncomplete:
   - obvious target
   - immediate visual feedback
   - state persists
   - no accidental navigation
6. Audit Month View:
   - date readability
   - selected/today state
   - task indicators
   - Month → selected date → Agenda works
7. Audit all navigation and system Back behavior.
8. Audit filters and search behavior.
9. Handle graceful errors/loading/input states where needed.
10. Verify persistence after closing/reopening the app.

# NOTIFICATION UX — HIGH PRIORITY

The current notification should no longer feel visually weak or boring.

Preserve the existing notification scheduling/architecture, but improve its presentation and behavior.

### Notification identity
- Use the existing Smart Agenda brand identity/logo where Android notification rules allow.
- Use a proper notification-safe icon/monochrome version when required.
- Do not replace the app's core logo with an unrelated icon.

### Notification information
Make the hierarchy immediately understandable:

**Smart Agenda**  
**Reminder title**  
Optional concise context such as:
`Today • 9:00 PM`
or
`Tomorrow • 9:00 AM`

Avoid vague text, duplicated content, technical text, excessive emojis, long paragraphs, or fake urgency.

### Sound
Add a **single, short, pleasant bell-like reminder sound**.

Required behavior:
- one clear notification sound
- short and recognizable
- calm/professional
- no continuous ringing
- no sound loop
- no vibration abuse
- respect Android/user notification settings

Prefer the system/default notification sound or a tiny bundled bell-like asset only if necessary. Do not create a complicated audio system.

The sound should occur once per reminder notification, subject to Android OS/channel/user settings.

### Notification channel
Inspect the existing channel. Make only minimal safe corrections if needed:
- meaningful name/description
- appropriate reminder importance
- sound configured correctly at channel creation where Android requires it
- no unnecessary channels
- no repeated conflicting channel creation
- do not forcibly override user settings

### Notification actions
Keep only useful actions already supported by the architecture, such as Complete or Open Reminder. Every action must actually work.

### Notification behavior
Verify:
**Reminder saved → scheduled → reminder time arrives → notification appears → sound plays once → user understands it → tap opens correct destination**

Also verify:
- app closed
- edit/delete/completion does not cause stale notifications
- duplicate scheduling does not duplicate notifications
- notification tap opens correct context
- permission behavior works where required
- reboot handling is preserved if already supported

### Platform limitation
Do not attempt pixel-perfect control of the Android notification shade. Optimize icon, title, content, channel, sound, importance, actions, and behavior while accepting OS/device rendering differences.

## STEP 2 ACCEPTANCE TEST
Before moving to Step 3:
- Add/save works
- Edit works
- Delete works
- Complete/uncomplete works
- Month/date navigation works
- Search/filter works
- persistence works
- notification schedules and fires
- notification information is clear
- Smart Agenda notification identity is correct
- bell-like sound plays once
- no repeating ringing
- no duplicate notification
- notification tap/action works
- no critical runtime regression
- security was not modified

Do NOT declare final release readiness yet.
