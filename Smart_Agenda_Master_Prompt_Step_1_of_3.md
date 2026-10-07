# SMART AGENDA — MASTER PROMPT 1/3
## FOUNDATION + UI/UX POLISH

### ROLE
Act as a senior Android engineer, senior product designer, UX engineer, and QA engineer.

You are improving the existing Smart Agenda Android app from approximately 7.5–8/10 toward 9–9.5/10. This first step focuses on **understanding the current implementation and making the visual/UX foundation polished and coherent**.

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


## STEP 1 OBJECTIVE
Do not implement the entire project in one pass. In this step, establish a stable baseline and polish the core visual experience without breaking functionality.

## EXECUTION ORDER

1. Inspect the entire existing project before editing.
2. Identify screens, reusable Compose components, navigation routes, ViewModels, repositories, Room entities/DAOs, notification scheduling, date/time picker implementation, theme/colors/typography, clickable elements, TODOs/placeholders/dead code, and build configuration.
3. Build the existing project first and record the baseline.
4. Preserve the existing visual identity: **premium + warm + restrained + modern**.
5. Polish the Agenda/Home screen so the hierarchy is immediately understandable:
   - current date/context
   - date navigation
   - today's tasks
   - task state
   - add action
6. Fix the top search bar interaction if it is visually present but not functional.
7. Fix the horizontal date row so every date item is genuinely tappable and selection updates the agenda.
8. Improve the date picker without replacing its architecture:
   - clear calendar grid
   - obvious selected date
   - clear today state
   - good spacing
   - easy month navigation
   - subtle depth/layered transition is acceptable
   - no exaggerated 3D/gimmicky effect
9. Improve the time picker:
   - clear hour/minute/AM-PM
   - obvious selection
   - comfortable touch targets
   - coherent date → time flow
10. Polish typography, cards, filters, spacing, touch targets, empty states, and restrained micro-interactions.
11. Audit visible buttons and interactive elements for visual affordance and consistency.
12. Keep AI/email UI secondary and visually consistent; do not expand their functionality.

## DESIGN RULE
The result should feel like a **finished human productivity app**, not a student prototype and not an “AI-generated” interface.

Avoid excessive:
- animation
- gradients
- glassmorphism
- shadows
- 3D
- decorative elements
- oversized controls
- feature clutter

## STEP 1 ACCEPTANCE TEST
Before moving to Step 2:
- project builds successfully
- app launches
- Agenda is visually coherent
- search interaction is verified
- horizontal date selection is verified
- date picker is usable
- time picker is usable
- no obvious clipping/overlap
- no critical navigation regression
- security was not modified

Do NOT claim the whole app is release-ready yet. Report only what Step 1 actually completed.
