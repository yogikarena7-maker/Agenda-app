MASTER PROMPT — REMINDER APP UX, UI & FUNCTIONALITY POLISH

You are a senior product engineer, UX designer, frontend engineer, and QA engineer.

You are working on my existing reminder/calendar application.

IMPORTANT:
This is an IMPROVEMENT task, not a redesign.

Do NOT replace the existing visual identity or core interaction model.
Do NOT rebuild the app from scratch.
Do NOT introduce unnecessary frameworks or dependencies.
Do NOT make the interface more complicated just to add features.

Your goal is to take the EXISTING application and bring its UI/UX, functionality, accessibility, reliability, and edge-case handling to a highly polished production-like level.

==================================================
1. FIRST: UNDERSTAND THE EXISTING PROJECT
==================================================

Before modifying anything:

1. Inspect the complete project structure.
2. Identify:
   - frontend framework/libraries
   - state management
   - routing
   - database/storage
   - notification/reminder system
   - calendar implementation
   - AI/natural-language parsing implementation
   - theme system
   - reusable components
3. Read the existing code before making changes.
4. Identify existing components that should be reused.
5. Do NOT duplicate functionality that already exists.
6. Do NOT remove working functionality.
7. Preserve existing data and storage formats unless a migration is genuinely required.

Create a mental map of:

- screens
- components
- data flow
- reminder lifecycle
- AI parsing flow
- theme system
- navigation

==================================================
2. PRESERVE THE EXISTING CORE DESIGN
==================================================

The existing visual identity is intentional.

KEEP:

- dark-first visual style
- existing accent color
- existing calendar/timeline concept
- rounded cards
- existing glass/soft-card treatment
- AI Quick Add concept
- floating + button
- minimal modern appearance
- existing overall layout philosophy

DO NOT:

- replace the design with a generic dashboard
- introduce a completely new color palette
- convert it into a standard Material UI app
- add excessive gradients
- add excessive animations
- add unnecessary cards
- make every element glow
- turn the UI into a crowded productivity dashboard

The result should look like:

"the same app, but professionally refined"

NOT:

"a completely different application."

==================================================
3. TYPOGRAPHY IMPROVEMENT
==================================================

Audit every screen.

Create a consistent typography hierarchy.

Target approximately:

- Page title: 22–24px
- Section title: 17–18px
- Reminder title: 16px
- Body text: 15–16px
- Secondary text: 13–14px
- Timeline labels: 13–14px
- Tiny metadata: never unnecessarily smaller than ~12px

Requirements:

- improve readability
- improve line-height
- prevent cramped text
- maintain visual hierarchy
- avoid excessive font weights
- ensure important information stands out

Do NOT simply increase every font size.

Improve hierarchy rather than size alone.

==================================================
4. SPACING SYSTEM
==================================================

Audit all margins and paddings.

Use a consistent spacing scale such as:

4
8
12
16
24
32
40
48
64

Remove arbitrary spacing where practical.

Pay special attention to:

- top header
- AI Quick Add
- category chips
- date selector
- timeline
- reminder cards
- empty states
- modal/dialog spacing
- bottom navigation
- floating action button

The interface should breathe without becoming unnecessarily large.

==================================================
5. TOP SECTION HIERARCHY
==================================================

Improve the visual hierarchy of the upper portion of the main screen.

The user should immediately understand:

1. current month/date
2. what they can do
3. categories/filter
4. selected date
5. today's reminders/timeline

Reduce visual competition between elements.

Do NOT remove useful functionality merely to reduce height.

==================================================
6. CATEGORY CHIPS
==================================================

Improve category chips.

Requirements:

- obvious selected state
- readable unselected state
- consistent height
- minimum comfortable touch target
- horizontal scrolling where necessary
- smooth scrolling
- no accidental clipping
- keyboard/focus accessibility where applicable

Do not rely on color alone to communicate selection.

==================================================
7. FLOATING ACTION BUTTON
==================================================

Keep the existing + button.

Improve:

- touch target
- hover state
- active state
- focus state
- subtle animation
- accessibility label

If the application already supports multiple creation types, expose them cleanly.

Do NOT create an unnecessary action menu if the current single-action behavior is better.

==================================================
8. AI QUICK ADD — MAJOR IMPROVEMENT
==================================================

This is one of the application's core features.

Make natural-language reminder creation robust.

Support inputs such as:

"toll at 7:50 tomorrow morning"

"doctor next Monday at 10"

"gym every Monday at 7 AM"

"submit assignment tomorrow evening"

"call mom in 30 minutes"

"meeting every Friday at 5 PM"

"pay electricity bill on the 15th at 8 AM"

Parse where possible:

- title
- date
- time
- AM/PM
- relative date
- recurrence
- category
- reminder time

==================================================
9. NEVER SILENTLY GUESS AMBIGUOUS INPUT
==================================================

If the input is ambiguous, show a confirmation step.

Example:

User:
"meeting Friday afternoon"

Show:

I understood:

Meeting
Friday
Afternoon

Please confirm the time.

[Cancel] [Edit] [Confirm]

Another example:

User:
"doctor at 8"

Show:

I understood:

Doctor
8:00 AM

Is this correct?

[Edit] [Confirm]

The user must be able to correct the interpretation easily.

==================================================
10. AI FAILURE HANDLING
==================================================

If natural-language parsing fails:

DO NOT silently create an incorrect reminder.

Instead show:

"I couldn't determine the date/time."

Then provide:

- retry
- manual entry
- example prompts

For example:

Try:
"Meeting tomorrow at 3 PM"

==================================================
11. REMINDER EDITING
==================================================

Every created reminder should be editable.

Allow editing:

- title
- date
- time
- category
- recurrence
- notification
- notes if supported

Editing should reuse the existing visual language.

Do NOT create an ugly generic form.

==================================================
12. REMINDER LIFECYCLE
==================================================

Support clear states where applicable:

- upcoming
- completed
- missed
- cancelled

Use subtle visual differences.

Do not rely solely on colors.

Example:

Completed:
- reduced emphasis
- check icon
- optional strikethrough

Missed:
- clear text/state indicator

==================================================
13. RECURRING REMINDERS
==================================================

Add recurrence support if the architecture allows it.

Support:

- every day
- weekdays
- weekends
- every week
- selected weekdays
- monthly
- custom recurrence

Natural-language examples should work where possible:

"gym every Monday at 7 AM"

"study every weekday at 8 PM"

"rent on the 1st of every month"

==================================================
14. SNOOZE
==================================================

If reminders have notification behavior, provide practical snooze options:

- 10 minutes
- 30 minutes
- 1 hour
- tomorrow
- custom

Keep this interface simple.

==================================================
15. NOTIFICATION / REMINDER RELIABILITY
==================================================

Audit the reminder/notification implementation.

Verify:

- correct timezone
- correct date
- correct time
- recurring reminders
- completed reminders don't unnecessarily trigger
- cancelled reminders don't trigger
- edited reminders update correctly
- deleted reminders don't trigger
- app restart does not lose scheduled reminders

If the platform has notification limitations, handle them gracefully rather than pretending notifications are guaranteed.

==================================================
16. CALENDAR / DATE HANDLING
==================================================

Audit all date logic.

Test:

- today
- tomorrow
- yesterday
- next week
- month changes
- year changes
- month-end
- leap year
- timezone changes where relevant

Make sure:

"today"

means the user's actual local date.

Avoid off-by-one date bugs.

==================================================
17. SEARCH
==================================================

If search already exists, make it functional and useful.

Search should find:

- reminder titles
- notes if supported
- categories
- dates where practical

Include:

- empty result state
- clear search action
- loading state if necessary

Do not leave decorative search controls that don't work.

==================================================
18. EMPTY STATES
==================================================

Audit every empty state.

Each empty state should explain:

1. what is empty
2. why it may be empty
3. what the user can do next

Examples:

"No reminders scheduled"

"Your day is clear."

"Tap + or use Quick Add to create one."

Keep empty states visually minimal.

==================================================
19. LOADING STATES
==================================================

Every asynchronous operation should have appropriate feedback.

Examples:

- AI parsing
- saving reminder
- loading reminders
- deleting
- updating
- searching

Use subtle skeletons/spinners where appropriate.

Avoid freezing the UI.

==================================================
20. ERROR STATES
==================================================

Create clear error handling.

Never expose raw technical errors to normal users.

Instead:

"Couldn't save reminder."

[Try Again]

Provide technical logging in development mode if appropriate.

==================================================
21. CONFIRMATION / FEEDBACK
==================================================

Keep the existing successful reminder confirmation style.

Improve feedback for:

- create
- edit
- delete
- complete
- restore
- snooze

Use subtle toast/snackbar/inline feedback.

Avoid excessive popups.

==================================================
22. DELETE SAFETY
==================================================

For destructive actions:

- provide confirmation when appropriate
- or provide a short undo window

Example:

"Reminder deleted."

[Undo]

Avoid unnecessary confirmation dialogs for every minor action.

==================================================
23. ACCESSIBILITY
==================================================

Perform a full accessibility pass.

Check:

- text contrast
- button contrast
- touch targets ≥44×44px where practical
- keyboard navigation
- visible focus states
- semantic labels
- screen-reader labels
- form labels
- icon-only buttons
- modal focus behavior
- color-independent status communication

Do not sacrifice the visual design.

==================================================
24. DARK THEME
==================================================

Preserve the current dark theme.

Audit:

- text contrast
- borders
- cards
- disabled states
- input fields
- dialogs
- notifications
- hover states
- selected states

Avoid pure black backgrounds everywhere unless already intentional.

Maintain depth and hierarchy.

==================================================
25. LIGHT THEME
==================================================

If a light theme exists, make it feel intentionally designed.

Do not simply invert the dark theme.

Check:

- background hierarchy
- card contrast
- border visibility
- text contrast
- accent color readability
- shadows
- disabled controls

Both themes should feel like the same product.

==================================================
26. RESPONSIVE DESIGN
==================================================

Test at:

- 320px
- 360px
- 375px
- 390px
- 414px
- tablet
- desktop

Check:

- no horizontal overflow
- no clipped buttons
- no overlapping cards
- readable timeline
- usable calendar
- usable AI input
- correct floating button position
- comfortable touch targets

Do not simply shrink everything.

Reflow the layout intelligently.

==================================================
27. MICRO-INTERACTIONS
==================================================

Use subtle animations for:

- category selection
- reminder creation
- completion
- deletion
- date switching
- theme switching
- modal opening
- AI parsing

Prefer approximately 150–250ms transitions.

Avoid:

- excessive bouncing
- constant pulsing
- distracting parallax
- unnecessary animation everywhere

Motion should communicate state, not decorate everything.

==================================================
28. PERFORMANCE
==================================================

Audit:

- unnecessary re-renders
- large dependencies
- unnecessary network calls
- repeated database queries
- image sizes
- unused CSS
- unused JavaScript
- excessive animation
- expensive calendar calculations

Do not optimize prematurely.

Only make changes that provide real value.

==================================================
29. DATA SAFETY
==================================================

Do not break existing reminder data.

Before modifying the data model:

- understand the current schema
- preserve existing records
- provide migrations if necessary
- handle missing fields safely

Existing users should not lose their reminders.

==================================================
30. SECURITY
==================================================

Audit all user-controlled input.

Check:

- validation
- sanitization
- authentication
- authorization
- database queries
- injection risks
- unsafe HTML rendering
- stored user content

Do not weaken existing security for convenience.

==================================================
31. CODE QUALITY
==================================================

Improve code where useful.

Requirements:

- reuse components
- avoid duplicated logic
- meaningful variable names
- small focused functions
- clear state management
- centralized constants
- consistent error handling
- no dead code
- no unnecessary abstractions

Do not refactor the entire project simply for stylistic reasons.

Only refactor when it improves maintainability or prevents bugs.

==================================================
32. TESTING
==================================================

After implementing changes, test all major flows.

Create a mental test matrix covering:

CREATE
- normal reminder
- AI reminder
- manual reminder

EDIT
- title
- date
- time
- category
- recurrence

DELETE
- delete
- undo if supported

DATE
- today
- tomorrow
- future date
- month change

AI
- clear input
- ambiguous input
- invalid input
- recurring input
- relative date input

UI
- dark mode
- light mode
- mobile
- tablet
- desktop

ERRORS
- storage/database failure
- notification failure
- parsing failure
- invalid input

==================================================
33. DO NOT ADD FEATURES JUST FOR A HIGHER FEATURE COUNT
==================================================

This is critical.

Do not add:

- social features
- unnecessary analytics
- gamification
- excessive dashboards
- complicated statistics
- random productivity features

unless they genuinely improve the reminder product.

Quality > feature count.

==================================================
34. FINAL UI POLISH PASS
==================================================

After all functional work:

Perform one final visual review of every screen.

Check:

- alignment
- spacing
- typography
- colors
- borders
- shadows
- radii
- icons
- button sizes
- empty states
- loading states
- error states
- dark mode
- light mode
- mobile layout

The final result should feel cohesive.

==================================================
35. IMPORTANT DESIGN PRINCIPLE
==================================================

DO NOT make the application look "more professional" by making it more complicated.

Professional means:

- clear
- predictable
- accessible
- fast
- consistent
- reliable
- visually balanced

The existing design should remain recognizable.

==================================================
36. FINAL DELIVERABLE
==================================================

After making changes:

1. Verify that the application still starts correctly.
2. Verify existing functionality.
3. Verify new functionality.
4. Fix console errors.
5. Fix broken imports.
6. Fix broken routes.
7. Fix responsive issues.
8. Fix accessibility issues.
9. Fix obvious visual inconsistencies.

Then provide a concise summary containing:

### Changed
List the important improvements.

### Fixed
List bugs/UX problems fixed.

### Added
List genuinely useful functionality added.

### Tested
List the major flows tested.

### Remaining
Only mention genuinely remaining issues.

Do NOT claim something was tested if it was not actually tested.

==================================================
FINAL GOAL
==================================================

Transform the current application from a polished prototype into a highly reliable, production-like reminder application while preserving its existing visual identity and core design.

Target qualities:

UI/UX: 9.5+
Functionality: 9.5+
Accessibility: 9+
Reliability: 9.5+
Code quality: 9+
Performance: 9+
Overall product quality: 9.5+

Do not chase the score.

Make the application genuinely better.