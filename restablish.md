MASTER PROMPT — FINAL PRODUCTION POLISH FOR MY REMINDER APP

You are a senior product engineer, UX/UI designer, frontend engineer,
AI integration engineer, accessibility specialist, and QA engineer.

You are working on my EXISTING reminder/calendar application.

============================================================
ABSOLUTE PRIORITY — DO THESE FIRST
============================================================

Before touching secondary improvements, implement and verify ALL of
the following P0 requirements.

These requirements have higher priority than all other instructions.

------------------------------------------------------------
P0-1. AI QUICK ADD MUST CLEAR PREDEFINED / PLACEHOLDER TEXT
------------------------------------------------------------

CURRENT PROBLEM:

When I click/open "AI Quick Add", the input currently contains
predefined/example text.

REQUIRED BEHAVIOR:

When the user clicks/taps AI Quick Add:

1. The input must immediately become EMPTY.
2. Any predefined example/demo text must disappear.
3. The predefined text must NOT be submitted to Gemini.
4. The user should see a real empty input ready for typing.
5. If placeholder/help text is needed, use a visual placeholder only.
6. Placeholder text must NOT be actual input value.
7. If the user previously entered text and intentionally reopened
   the component, preserve it only if that is already the existing
   UX behavior; do NOT automatically restore demo/example text.
8. Autofocus the input when appropriate.
9. The cursor should be visible and ready for typing.
10. Clicking AI Quick Add must never create a reminder using the
    predefined example text.

IMPORTANT:

Do not implement this by simply hiding the text visually.

The actual input value/state must be empty.

Example:

WRONG:

value = "e.g. Meeting tomorrow at 5 PM"

CORRECT:

value = ""

placeholder = "e.g. Meeting tomorrow at 5 PM"

The example must only be a placeholder.

------------------------------------------------------------
P0-2. GEMINI 3.5 FLASH API INTEGRATION
------------------------------------------------------------

Implement/fix the Gemini API integration for AI Quick Add.

Use the official Gemini model ID:

gemini-3.5-flash

Do not use an outdated, fake, or incorrect model name.

The AI request must be used specifically for natural-language
reminder parsing.

The AI should return STRUCTURED reminder information rather than
free-form prose.

Expected logical structure:

{
  "title": "...",
  "date": "YYYY-MM-DD",
  "time": "HH:mm",
  "category": "...",
  "recurrence": "...",
  "notification": "...",
  "confidence": 0.0
}

Only include fields that are actually supported by the existing
application.

IMPORTANT:

Do not trust arbitrary AI output blindly.

Validate the response before creating a reminder.

------------------------------------------------------------
P0-3. GEMINI API ERROR HANDLING
------------------------------------------------------------

This is mandatory.

If the Gemini API call fails for ANY reason:

- invalid API key
- missing API key
- quota/rate limit
- network failure
- timeout
- invalid request
- malformed response
- model/API error
- server error
- parsing error

DO NOT:

- crash the application
- create a fake reminder
- silently fall back to incorrect data
- display raw API errors to the user
- leave the loading state permanently active

Instead:

1. Stop loading.
2. Keep the user's original text in the input.
3. Show a friendly error message.
4. Allow the user to retry.
5. Provide a manual reminder option if available.

Example:

"AI couldn't process that right now."

[Try Again]

The user's entered text MUST remain available.

For development/debugging, log the actual technical error safely.

Do not expose API keys or sensitive information in the UI.

------------------------------------------------------------
P0-4. NEVER EXPOSE THE GEMINI API KEY IN CLIENT CODE
------------------------------------------------------------

Inspect the architecture.

If this is a web application:

DO NOT hard-code the Gemini API key into:

- frontend JavaScript
- HTML
- React/Vue client bundle
- public source files
- localStorage
- query parameters

Use a secure server-side/API route/proxy mechanism appropriate to
the existing stack.

The browser should call my own backend/API endpoint, and the backend
should call Gemini.

Do not leak the API key through error messages.

------------------------------------------------------------
P0-5. AI PARSING MUST HANDLE AMBIGUITY
------------------------------------------------------------

Do not silently guess when the user input is ambiguous.

Examples:

"meeting Friday afternoon"

"doctor at 8"

"call John sometime tomorrow"

If the model cannot confidently determine the required information,
show a confirmation/edit state.

Example:

I understood:

Meeting
Friday
Afternoon

Please confirm the time.

[Edit] [Confirm]

Another example:

I understood:

Doctor
8:00 AM

Is this correct?

[Edit] [Confirm]

Never silently create an incorrect reminder.

------------------------------------------------------------
P0-6. NOTIFICATION MUST SHOW APP IDENTITY + MAIN CONTENT
------------------------------------------------------------

When an actual reminder notification appears, it must be branded
and useful.

The notification should contain:

1. The APP LOGO / APP ICON.
2. The MAIN REMINDER CONTENT.
3. A clear title.
4. Relevant time/context where supported.

Example:

[APP LOGO]

Reminder
Submit assignment

Tomorrow · 8:00 PM

Do NOT show a generic notification such as:

"Reminder notification"

The notification should clearly tell the user WHAT the reminder is.

Use the platform's proper notification/icon mechanism.

IMPORTANT:

Use the actual application icon/logo assets already present in the
project where the platform supports them.

Do not use a random emoji as the application icon.

If the platform requires different icon assets/sizes, generate or
configure the required assets from the existing app branding.

------------------------------------------------------------
P0-7. NOTIFICATION ACTIONS
------------------------------------------------------------

Where supported by the platform, provide useful actions such as:

- Open
- Complete
- Snooze

Do not add actions that the platform or current architecture cannot
reliably support.

If notification actions are implemented:

- Complete must actually complete the reminder.
- Snooze must actually reschedule it.
- Open must open the correct reminder.

------------------------------------------------------------
P0-8. NOTIFICATION DATA MUST BE CORRECT
------------------------------------------------------------

Audit notification scheduling.

Verify:

- correct reminder title
- correct date
- correct time
- correct timezone
- correct reminder after editing
- deleted reminders do not trigger
- cancelled reminders do not trigger
- completed reminders do not incorrectly trigger
- recurring reminders behave correctly
- app restart does not unexpectedly lose reminder state

Pay particular attention to timezone and date conversion bugs.

============================================================
P0-9. TEXT SIZE, PLACEMENT AND SPACING
============================================================

Perform a complete typography and layout audit.

This is NOT just "make the font bigger."

Analyze every visible text element.

Check:

- font size
- font weight
- line height
- letter spacing
- text width
- alignment
- vertical placement
- horizontal placement
- distance from surrounding elements
- text-to-icon spacing
- text-to-card spacing
- text-to-button spacing
- section spacing
- screen edge padding

Create a consistent typography hierarchy.

Suggested baseline:

Page title:
22–24px

Section title:
17–18px

Reminder title:
16px

Body:
15–16px

Secondary text:
13–14px

Timeline labels:
13–14px

Metadata:
12–14px depending on importance

Do not blindly apply these values.

Adapt them to the existing design.

IMPORTANT:

The current visual identity MUST remain intact.

Do not turn the app into a completely different design.

============================================================
P0-10. SPACING SYSTEM
============================================================

Audit all spacing.

Prefer a consistent scale such as:

4
8
12
16
24
32
40
48
64

Remove random spacing values when practical.

Pay special attention to:

- app header
- month title
- AI Quick Add
- AI input
- categories
- date selector
- timeline
- reminder cards
- empty state
- floating action button
- bottom navigation
- dialogs
- notification-related UI

Text must never feel:

- cramped
- floating awkwardly
- too close to icons
- too far from its heading
- vertically misaligned

============================================================
P0-11. VISUAL PLACEMENT
============================================================

Check every major screen visually.

For each section ask:

"Does this element appear exactly where the user expects it?"

Pay attention to:

- baseline alignment
- icon/text alignment
- card padding
- button label centering
- input vertical centering
- title positioning
- date positioning
- timeline alignment
- FAB position
- modal alignment

Do not fix alignment by adding arbitrary margins everywhere.

Prefer:

- flexbox
- grid
- consistent padding
- proper line-height
- proper alignment properties

============================================================
P0-12. AI LOADING STATE
============================================================

When Gemini is processing:

Show a polished loading state.

Example:

AI is understanding your reminder...

Use a subtle animation.

The user must clearly understand:

- their request was received
- processing is happening
- they should not press the button repeatedly

Prevent duplicate API requests caused by repeated taps.

============================================================
P0-13. AI SUCCESS FLOW
============================================================

The ideal flow should be:

User taps AI Quick Add
        ↓
Input is EMPTY
        ↓
User types request
        ↓
User submits
        ↓
Loading state
        ↓
Gemini 3.5 Flash parses request
        ↓
Validate structured response
        ↓
Show parsed reminder preview
        ↓
User confirms/edits
        ↓
Reminder saved
        ↓
Success feedback
        ↓
Notification scheduled

Do NOT skip validation.

============================================================
P0-14. AI PARSING EXAMPLES
============================================================

Test at minimum:

"toll at 7:50 tomorrow morning"

"doctor next Monday at 10"

"meeting tomorrow at 3 PM"

"gym every Monday at 7 AM"

"study every weekday at 8 PM"

"call mom in 30 minutes"

"pay electricity bill on the 15th at 8 AM"

"submit assignment tomorrow evening"

"meeting Friday afternoon"

"doctor at 8"

"remind me about the project"

The final examples should trigger appropriate clarification instead
of unsafe guessing when information is missing.

============================================================
P0-15. PRESERVE THE EXISTING DESIGN
============================================================

DO NOT redesign the application.

KEEP:

- current dark visual identity
- current accent color
- current calendar
- current timeline
- current AI Quick Add concept
- current floating + button
- rounded cards
- current minimal aesthetic
- current general navigation
- existing design language

Improve it.

Do not replace it.

============================================================
SECONDARY PRIORITIES — AFTER ALL P0 ITEMS
============================================================

Once every P0 requirement is working, continue with the following.

============================================================
1. TYPOGRAPHY POLISH
============================================================

Improve:

- hierarchy
- contrast
- line height
- weight
- readability
- responsive sizing

Do not overuse bold text.

============================================================
2. RESPONSIVE UI
============================================================

Test:

320px
360px
375px
390px
414px
tablet
desktop

Check for:

- clipping
- overflow
- overlapping
- cramped controls
- broken calendar
- broken timeline
- incorrect FAB placement
- text wrapping problems

============================================================
3. ACCESSIBILITY
============================================================

Check:

- contrast
- focus states
- semantic labels
- screen-reader labels
- keyboard navigation where applicable
- touch targets ≥44px where practical
- icon-only buttons
- form labels
- color-independent states

============================================================
4. REMINDER EDITING
============================================================

Allow users to edit:

- title
- date
- time
- category
- recurrence
- notification
- notes if supported

============================================================
5. RECURRING REMINDERS
============================================================

Support where appropriate:

- daily
- weekdays
- weekends
- weekly
- selected weekdays
- monthly
- custom recurrence

============================================================
6. SEARCH
============================================================

Make the existing search functional.

Search:

- titles
- categories
- notes where applicable

Provide useful empty results.

============================================================
7. EMPTY STATES
============================================================

Every empty state should explain:

- what is empty
- why
- what the user should do next

============================================================
8. ERROR STATES
============================================================

Every failure should:

- stop loading
- preserve user input
- explain the problem
- provide recovery

Never expose raw technical errors.

============================================================
9. MICRO-INTERACTIONS
============================================================

Use subtle 150–250ms transitions for:

- category selection
- date switching
- reminder creation
- completion
- deletion
- theme switching
- dialogs
- AI states

Do not over-animate.

============================================================
10. PERFORMANCE
============================================================

Check:

- unnecessary re-renders
- unnecessary API requests
- repeated database/storage operations
- unused CSS
- unused JS
- excessive dependencies
- expensive calculations

============================================================
11. DATA SAFETY
============================================================

Never destroy existing reminder data.

If the data model changes:

- preserve existing data
- migrate safely
- handle missing fields

============================================================
12. SECURITY
============================================================

Audit:

- API key handling
- user input
- API requests
- storage
- injection
- unsafe HTML
- authentication/authorization if applicable

============================================================
FINAL QA CHECKLIST
============================================================

Before considering the task complete, manually verify:

AI QUICK ADD:

[ ] Clicking AI Quick Add clears predefined input text.
[ ] Actual input value is empty.
[ ] Example text is only placeholder text.
[ ] Input receives focus.
[ ] User can type immediately.
[ ] User text is preserved during API errors.
[ ] Gemini 3.5 Flash request works.
[ ] API errors are handled.
[ ] Invalid responses are handled.
[ ] Ambiguous requests require confirmation.
[ ] Loading state works.
[ ] Duplicate submissions are prevented.
[ ] Successful reminder creation works.

GEMINI:

[ ] Model ID is gemini-3.5-flash.
[ ] API key is not exposed client-side.
[ ] API request is validated.
[ ] API response is validated.
[ ] Timeout/error handling works.
[ ] Rate-limit handling works.
[ ] Network failure works.
[ ] Invalid response handling works.

NOTIFICATIONS:

[ ] Correct app icon/logo appears.
[ ] Main reminder content appears.
[ ] Reminder title is correct.
[ ] Date/time information is correct.
[ ] Notification opens correct reminder.
[ ] Complete works if supported.
[ ] Snooze works if supported.
[ ] Deleted reminders don't notify.
[ ] Edited reminders notify at the updated time.
[ ] Recurring reminders behave correctly.

TYPOGRAPHY:

[ ] Headings are readable.
[ ] Body text is readable.
[ ] Metadata isn't unnecessarily tiny.
[ ] Line heights are comfortable.
[ ] Text is vertically aligned.
[ ] Text/icon spacing is consistent.
[ ] Button text is centered.
[ ] Input text is properly positioned.
[ ] No text overlaps.
[ ] No awkward wrapping.

SPACING:

[ ] Header spacing is consistent.
[ ] AI section spacing is consistent.
[ ] Category spacing is consistent.
[ ] Calendar spacing is consistent.
[ ] Timeline spacing is consistent.
[ ] Reminder card padding is consistent.
[ ] Empty state spacing is balanced.
[ ] FAB is correctly positioned.

RESPONSIVE:

[ ] 320px works.
[ ] 360px works.
[ ] 375px works.
[ ] 390px works.
[ ] 414px works.
[ ] Tablet works.
[ ] Desktop works.
[ ] No horizontal overflow.

ACCESSIBILITY:

[ ] Focus states work.
[ ] Touch targets are usable.
[ ] Contrast is sufficient.
[ ] Icon-only buttons have labels.
[ ] Color is not the only status indicator.

============================================================
FINAL RULE
============================================================

Do not stop after implementing the first visible changes.

Inspect → plan → implement → test → fix → visually polish → retest.

Do not claim something works unless you actually verified it.

Most importantly:

DO NOT CHANGE THE CORE DESIGN.

Make the existing app feel like a highly polished version of itself.