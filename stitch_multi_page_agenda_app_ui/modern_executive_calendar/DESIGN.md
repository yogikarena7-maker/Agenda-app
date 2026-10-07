---
name: Modern Executive Calendar
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#434938'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#747966'
  outline-variant: '#c3c9b2'
  surface-tint: '#476800'
  primary: '#476800'
  on-primary: '#ffffff'
  primary-container: '#bef264'
  on-primary-container: '#4b6e00'
  inverse-primary: '#a4d64c'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#00668a'
  on-tertiary: '#ffffff'
  tertiary-container: '#c0e6ff'
  on-tertiary-container: '#006b91'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#bff365'
  primary-fixed-dim: '#a4d64c'
  on-primary-fixed: '#131f00'
  on-primary-fixed-variant: '#354e00'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#c4e7ff'
  tertiary-fixed-dim: '#7bd0ff'
  on-tertiary-fixed: '#001e2c'
  on-tertiary-fixed-variant: '#004c69'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 2.25rem
    fontWeight: '700'
    lineHeight: 2.75rem
    letterSpacing: -0.03em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 1.75rem
    fontWeight: '700'
    lineHeight: 2.25rem
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 1.75rem
    fontWeight: '600'
    lineHeight: 2.25rem
    letterSpacing: -0.025em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 1.375rem
    fontWeight: '600'
    lineHeight: 1.75rem
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.75rem
    letterSpacing: -0.02em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 1.0625rem
    fontWeight: '600'
    lineHeight: 1.5rem
    letterSpacing: -0.015em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.5rem
    letterSpacing: -0.01em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.375rem
    letterSpacing: -0.005em
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1.125rem
    letterSpacing: 0em
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.875rem
    fontWeight: '600'
    lineHeight: 1.25rem
    letterSpacing: -0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.75rem
    fontWeight: '600'
    lineHeight: 1rem
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.6875rem
    fontWeight: '700'
    lineHeight: 0.875rem
    letterSpacing: 0.04em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system blends executive minimalism with high-velocity product craft. Inspired by the quiet luxury of bespoke stationery, precision instruments, and elite digital tools, the atmosphere is airy, pristine, and luminous. 

The interface communicates calm control, temporal clarity, and frictionless efficiency. The design balances warm porcelain surfaces and crisp white planes with high-contrast obsidian typography. An electric lime signature accent punctuates key interactions, transforming routine scheduling into an intentional, high-end experience. Visual weight is articulated through hairline micro-borders, diffused ambient shadows, and frosted translucent viewports rather than heavy structural fills.

## Colors

The palette relies on nuanced tonality, contrast ratios, and deliberate accents:

- **Canvas & Backdrops:** Warm alabaster (`#F8F9FA`) serves as the foundational application canvas, paired with pure porcelain (`#FFFFFF`) for elevated card faces and event blocks.
- **Structural Fills & Tonal Dividers:** Warm off-white (`#F1F3F5`) and subtle slate (`#F4F5F7`) designate secondary containers, day-column tracks, and subtle inset wells.
- **Typography & Structural Inks:** Deep obsidian (`#0F172A`) commands headings, primary metrics, and active states with crisp legibility. Graphite (`#1E293B`) defines subheaders, while refined slate (`#64748B`) manages secondary metadata, time-rail indicators, and structural icons.
- **Signature Accent:** Refined electric lime / vibrant chartreuse (`#BEF264` / `#D4FF32`) provides an energetic, modern focal point. It is reserved for high-intent triggers (primary CTA, active timeline needle, real-time status pulses, focus badges) and is anchored against deep obsidian text for readability.
- **Calendar Event Accents:** Temporal events and tags employ luminous, muted tints with crisp slate strokes—electric cyan (`#38BDF8`), delicate rose (`#FB7185`), amber gold (`#FBBF24`), and soft violet (`#A78BFA`).
- **Hairlines:** Crisp, semi-transparent slate boundaries (`#E2E8F0` at 80% opacity or `#EAECF0`) replace heavy shadows, creating clear spatial definition.

## Typography

Plus Jakarta Sans governs the typography across all surfaces, bringing geometric precision, open counters, and contemporary legibility. 

- **Display & Headings:** Use tight negative letter-spacing (`-0.03em` to `-0.015em`) with medium to bold weights to establish an editorial feel across dates, agenda titles, and month names.
- **Numbers & Time Metrics:** Time axes, durations, and calendar dates feature tabular figures (`font-variant-numeric: tabular-nums`) to maintain alignment across views and grids.
- **Labels & Micro-copy:** Micro-badges, category tags, and status indicators leverage uppercase tracking (`0.02em` to `0.04em`) with heavy weights (`600` and `700`) to guarantee legibility at scale.

## Layout & Spacing

The layout is built around an architectural, fluid grid with fixed temporal rails:

- **Grid Architecture:** Desktop views use an asymmetric layout—a fixed navigation/utility rail (260px), a flexible primary calendar grid spanning 12 adaptive columns, and a contextual collapsible inspector drawer (340px). 
- **Time Rails & Intervals:** The vertical time axis adheres to an 8px base rhythm. Standard 1-hour slots map to 64px vertical increments, subdividing cleanly into 15-minute 16px micro-increments.
- **Responsive Adaptations:**
  - **Desktop (>= 1024px):** Multi-column daily and weekly views with full time-rail expansion, dual navigation rails, and edge gutters of `2.5rem`.
  - **Tablet (768px - 1023px):** Side navigation collapses into an icon-driven floating dock; calendar shifts to 3-day or compact 7-day views with `1.5rem` gutters.
  - **Mobile (< 768px):** Single-column agenda view or 1-day temporal timeline with swipe navigation. Bottom navigation docks as a frosted pill floating `1rem` above the screen edge.

## Elevation & Depth

Visual hierarchy is communicated through structural layering, frosted light refractions, and diffused ambient occlusion rather than heavy drop shadows:

- **Level 0 (Base Canvas):** Flat, un-elevated warm alabaster (`#F8F9FA`).
- **Level 1 (Cards, Grid Tiles, Schedule Panels):** Crisp pure white (`#FFFFFF`) bound by a single 1px hairline border in semi-transparent slate (`rgba(226, 232, 240, 0.85)`), layered over an ultra-soft ambient shadow: `0 1px 3px rgba(15, 23, 42, 0.03), 0 8px 24px -4px rgba(15, 23, 42, 0.04)`.
- **Level 2 (Popovers, Event Detail Modals, Menus):** Elevated white surfaces accompanied by a crisp dual border: an outer 1px hairline border (`#E2E8F0`) and an inner subtle highlight border (`inset 0 1px 0 rgba(255, 255, 255, 0.8)`). Supported by depth shadows: `0 12px 32px -6px rgba(15, 23, 42, 0.08), 0 4px 12px -2px rgba(15, 23, 42, 0.03)`.
- **Level 3 (Floating Overlays, Command Palette, Floating Dock):** Frosted glass surfaces utilizing `backdrop-filter: blur(20px) saturate(180%)`, a 75% translucent white base (`rgba(255, 255, 255, 0.75)`), an edge sheen stroke (`rgba(255, 255, 255, 0.4)`), and an expansive glow shadow: `0 20px 48px -8px rgba(15, 23, 42, 0.12)`.

## Shapes

The design language pairs structured outer containers with tactile, pill-shaped interactive components:

- **Floating Navigation, Quick Actions & Pills:** Interactive switches, segment controllers, filter chips, and navigation docks use full pill shapes (`roundedness: 3` / `9999px`) to create an organic, tactile aesthetic.
- **Cards, Panels & Modal Containers:** Use progressive radii (`rounded-xl` / 1.5rem to 2rem) with continuous squircle corners.
- **Calendar Event Blocks:** Event tiles use a softened 0.5rem to 0.75rem corner radius, fitting comfortably within the time-grid's geometric boundaries.

## Components

### Buttons & Quick Action Triggers
- **Primary Action:** Solid electric lime (`#BEF264`) pill fill paired with deep obsidian typography (`#0F172A`, `label-md`). Hover transitions introduce a subtle brightness increase and a focused upward translation (-1px) backed by a soft lime ambient glow (`0 8px 20px -4px rgba(190, 242, 100, 0.45)`).
- **Secondary Action:** Frosted white porcelain surface with a hairline slate border (`#E2E8F0`). Text is obsidian with a soft hover transition to `#F8F9FA`.
- **Tertiary & Icon Buttons:** Circular or pill-shaped buttons with no initial background or border; hover triggers a soft slate tint (`#F1F3F5`).

### Calendar Event Blocks
- Rendered in crisp white or delicate event tints (e.g., cyan fill `#F0F9FF` with `#0284C7` accent; amber fill `#FFFBEB` with `#D97706` accent).
- Left edge features an integrated 3px solid accent bar indicating project status or calendar origin.
- Micro-labels inside the block display start/end times in tabular format alongside attendees' micro-avatars.

### Floating Navigation Dock
- A centered, floating pill bar with frosted glass styling (`rgba(255, 255, 255, 0.8)`, `backdrop-filter: blur(20px)`).
- Bound by a hairline border (`rgba(226, 232, 240, 0.8)`) and lifted by ambient shadows.
- Active tabs feature a solid obsidian (`#0F172A`) pill with crisp white text, while inactive tabs use muted slate icons that transition on hover.

### Segmented Controls & Day Switchers
- Inset pill-shaped track in warm off-white (`#F1F3F5`).
- Active segment transitions smoothly using an elevated pure white pill container with a hairline border and subtle lift shadow (`0 2px 6px rgba(15, 23, 42, 0.05)`).

### Form Inputs & Date Pickers
- Borderless inputs set inside a warm alabaster fill (`#F8F9FA`) with an ambient 1px hairline border (`#E2E8F0`).
- Focus state reveals an obsidian border ring alongside a subtle electric lime focus glow (`0 0 0 3px rgba(190, 242, 100, 0.35)`).
- Placeholder text is set in refined slate (`#94A3B8`).

### Time Indicator Needle (Now Line)
- A high-visibility, 1.5px continuous horizontal rule rendered in electric lime (`#BEF264`) or saturated chartreuse (`#A3E635`).
- Anchored at the left axis by a solid lime circular pulse badge with a soft animated ambient ping.