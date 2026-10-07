---
name: Kinetic Obsidian
colors:
  surface: '#121316'
  surface-dim: '#121316'
  surface-bright: '#38393c'
  surface-container-lowest: '#0d0e11'
  surface-container-low: '#1b1b1f'
  surface-container: '#1f1f23'
  surface-container-high: '#292a2d'
  surface-container-highest: '#343538'
  on-surface: '#e3e2e6'
  on-surface-variant: '#c5c9ad'
  inverse-surface: '#e3e2e6'
  inverse-on-surface: '#303034'
  outline: '#8f937a'
  outline-variant: '#444934'
  surface-tint: '#aed500'
  primary: '#ffffff'
  on-primary: '#293500'
  primary-container: '#c8f322'
  on-primary-container: '#576c00'
  inverse-primary: '#526600'
  secondary: '#fff9e9'
  on-secondary: '#373100'
  secondary-container: '#f5df00'
  on-secondary-container: '#6c6200'
  tertiary: '#ffffff'
  on-tertiary: '#2e3035'
  tertiary-container: '#e2e2e9'
  on-tertiary-container: '#63646a'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c8f322'
  primary-fixed-dim: '#aed500'
  on-primary-fixed: '#171e00'
  on-primary-fixed-variant: '#3d4d00'
  secondary-fixed: '#fbe40b'
  secondary-fixed-dim: '#dcc800'
  on-secondary-fixed: '#201c00'
  on-secondary-fixed-variant: '#4f4700'
  tertiary-fixed: '#e2e2e9'
  tertiary-fixed-dim: '#c5c6cd'
  on-tertiary-fixed: '#191c20'
  on-tertiary-fixed-variant: '#45474c'
  background: '#121316'
  on-background: '#e3e2e6'
  surface-variant: '#343538'
typography:
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '500'
    lineHeight: 22px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 12px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.75rem
  gutter-desktop: 1.25rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system embodies high-focus executive productivity, combining ultra-refined dark minimalism with electric kinetic energy. Built specifically for modern time-management, calendar scheduling, and tactical agendas, the visual language balances stealth obsidian surfaces with high-visibility electric lime accents.

The target audience comprises digital natives, founders, designers, and high-output professionals who view their agenda not as a chore, but as a dynamic control deck. The emotional tone is deliberate, sharp, rhythmic, and distraction-free.

Key stylistic principles include:
- **Stealth Architecture:** Pure black and deep charcoal bedrock surfaces eliminate visual glare and reduce cognitive load during heavy planning sessions.
- **Electric Precision:** High-voltage lime and neon citrus hues function strictly as tactical wayfinders—pinpointing current time markers, active dates, and imminent commitments.
- **Pill Geometry & Modular Pods:** Tactile floating control pills, rounded day trackers, and soft container carousels provide immediate touch targets and ergonomic one-handed operation.

## Colors

The palette operates on a high-contrast dark foundation engineered for crisp legibility and optical punch:

- **Primary (`#D4FF32`):** Kinetic Lime. Used for active state indicators, primary calendar blocks, the live time needle, and critical CTA highlights.
- **Secondary (`#FFE814`):** Neon Canary. Secondary priority event containers, active focus tasks, and urgent schedule highlights.
- **Tertiary (`#24262B`):** Deep Slate Container. The default card and grouped element background, elevating modules from base surfaces.
- **Neutral Surface Hierarchy:**
  - Canvas Base: `#0D0E11` (Deep Obsidian)
  - Surface Raised / Grid Tile: `#17181D` (Muted Gunmetal)
  - Surface Subdued: `#1E2026`
  - High Contrast Text: `#FFFFFF` (Primary titles, active day numerals)
  - Muted Text / Inactive: `#8E929E` (Secondary meta-data, upcoming inactive hours)
  - Grid Line Stroke: `#1E2229` with time marker rule `#D4FF32`

## Typography

Plus Jakarta Sans serves as the sole typographic engine, providing clean modernist geometric proportions, open apertures, and crisp readability on deep OLED screens.

- **Numerics & Timeline:** Numerical weights (`fontWeight: 700`) are optimized for scanning dense grids, month matrices, and duration blocks.
- **Hierarchy:** Month headings (`headline-lg-mobile`) stay compact and bold, pinned alongside quick-action icons. Micro-labels (`label-md` and `label-sm`) feature elevated tracking (+0.02em) for day-of-week monograms (M, T, W, T, F, S, S) and small tag indicators.
- **Contrast Rules:** When typography sits atop primary `#D4FF32` or `#FFE814` cards, text color flips strictly to obsidian `#0D0E11` at semi-bold or bold weights for maximum legibility.

## Layout & Spacing

The layout is built around a rhythm optimized for touch ergonomics, persistent accessibility, and vertical agenda velocity:

- **Grid & Safe Zones:** Mobile displays use a fluid grid with a strict `1rem` (16px) margin and `0.75rem` (12px) gutter between multi-column time slots and monthly matrix cells.
- **Timeline Rhythm:** The vertical daily view relies on fixed 60-minute increments spaced at 64px vertical blocks, divided by thin baseline rules.
- **Floating Controls:** A persistent floating action dock (view switcher: list, month, agenda) is centered at the bottom viewport boundary, elevated 24px above the system home bar.
- **Form Factor Adaptability:**
  - **Mobile (<640px):** Single-column timeline or compact 7-column calendar matrix. Day header selector collapses to a horizontal week strip.
  - **Tablet & Desktop (≥640px):** Expands to a multi-pane split: left persistent month matrix and right scrollable agenda stream, adjusting outer margins to `2rem`.

## Elevation & Depth

Visual hierarchy uses a stack of low-luminance tonal layers combined with subtle border illumination rather than heavy drop shadows:

- **Level 0 (Canvas Base):** Deepest black `#0D0E11`.
- **Level 1 (Structural Trackers & Month Columns):** Layered at `#17181D` with subtle 1px border stroke `rgba(255, 255, 255, 0.05)`.
- **Level 2 (Interactive Rows & Input Groups):** `#1E2026` backdrop with border stroke `rgba(255, 255, 255, 0.08)`.
- **Level 3 (Floating Navigation Pills & Overlays):** `#24262B` with 70% backdrop blur (`backdrop-filter: blur(16px)`), finished with a fine top-edge highlight (`border: 1px solid rgba(255, 255, 255, 0.14)`) and a diffused atmospheric shadow (`0px 12px 32px rgba(0, 0, 0, 0.65)`).
- **Active Event Glow:** Urgent time-blocks in neon lime/yellow cast a faint contextual halo (`box-shadow: 0px 4px 20px rgba(212, 255, 50, 0.15)`).

## Shapes

The interface embraces a unified squircle and pill geometry:

- **Base Radius (`0.5rem` / 8px):** Applied to micro tags, small status chips, and internal time pills.
- **Large Radius (`1rem` / 16px):** Standard for agenda event blocks, day date pods, and modal settings rows.
- **Extra Large Radius (`1.5rem` / 24px):** Applied to month view calendar tiles and full module containers.
- **Capsule / Pill (`9999px`):** Used universally for the floating dock controller, top week-scroller date active indicator, avatar rings, and primary action buttons.

## Components

### Floating Navigation Pill
- **Structure:** Floating capsule dock centered horizontally at the bottom of the screen.
- **Style:** Background `#24262B` with 16px backdrop blur, bordered by `1px solid rgba(255, 255, 255, 0.12)`.
- **Items:** Icon toggles (agenda, month grid, task list) spaced with `space-md`. Active item is rendered inside an illuminated mini-pill or inverted color state.

### Calendar Day Pill & Week Trackers
- **Inactive Days:** Slate dark background (`#17181D`), white numeric label, muted uppercase day label.
- **Active / Selected Day:** Solid `#D4FF32` filled container, dark obsidian `#0D0E11` text, bold weight.
- **Event Dots:** 4px micro-indicators rendered in lime or canary beneath date numerals to signify density.

### Event Agenda Cards
- **High Priority / Active:** Solid fill in `#D4FF32` or `#FFE814`. Dark text `#0D0E11`, pill-shaped internal tags with semi-translucent dark tint (`rgba(0, 0, 0, 0.1)`), stacked circular avatar badges with dark outline borders.
- **Standard / Background:** `#1E2026` surface, white primary title, muted timestamp, fine 1px side accent bar in Primary Lime indicating category.

### Form Inputs & Modal Rows
- **List Rows:** Grouped rounded containers (`#17181D`) with subtle row dividers (`rgba(255, 255, 255, 0.06)`).
- **Interactive Affordance:** Left iconography, bold title, and right-aligned chevron or toggle.
- **Toggles:** Deep charcoal track when inactive; vibrant `#D4FF32` track when active with a pure white thumb.

### Timeline Indicator (Live Needle)
- Horizontal 2px rule spanning the full schedule width in `#D4FF32`.
- Left-anchored diamond or pill containing the live digital timestamp in bold micro typography.