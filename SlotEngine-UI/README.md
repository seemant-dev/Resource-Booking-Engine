# BookEngine — Resource Booking Engine (Frontend)

A production-quality Next.js frontend implementing the approved UI/UX design for a
concurrency-safe resource booking engine. This is the frontend foundation a team would
hand off **before backend integration begins** — every screen is built and navigable,
but all data is static/local and no network calls are made.

> **Scope note:** this repository is frontend-only by design. There is no backend, no
> API client, no authentication, and no real data persistence. See
> [Backend Integration](#9-backend-integration) for how that gets layered in later.

---

## 1. Project overview

The application has two experiences, gated by route group rather than real
authorization (since there's no auth yet):

- **User** — browse resources, view a resource's available time slots, book a slot,
  manage your own bookings, edit basic account details.
- **Admin** — view workspace-wide metrics, manage resources, generate bookable slots in
  bulk, view and cancel any user's booking.

Every screen maps 1:1 to a screen in the approved design board. Nothing was redesigned,
simplified, or extended beyond what the approved design and project requirements called
for.

## 2. Tech stack

| Concern         | Choice                                       | Why                                                                                                                               |
| --------------- | -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| Framework       | **Next.js 16** (App Router)                  | Current stable, file-based routing matches the screen-per-route structure cleanly, server components by default.                  |
| Language        | **TypeScript**                               | Type safety across domain models, component props, and mock data.                                                                 |
| Styling         | **Tailwind CSS v4**                          | Utility-first, CSS-variable-based theme tokens map directly onto the approved design's design tokens (see `src/app/globals.css`). |
| Icons           | **lucide-react**                             | Lightweight, tree-shakeable icon set used in the sidebar and empty states.                                                        |
| Class utilities | **clsx**                                     | Conditional Tailwind class composition without string concatenation.                                                              |
| Linting         | **ESLint** (`eslint-config-next`)            | Next.js's recommended ruleset.                                                                                                    |
| Formatting      | **Prettier** + `prettier-plugin-tailwindcss` | Consistent formatting and automatic Tailwind class ordering.                                                                      |

No state management library, API client, or auth library is included — see
[Important constraints](#3-important-constraints-this-build-honors).

## 3. Important constraints this build honors

This project deliberately does **not** include:

- A backend or any backend logic
- Real API integration (no `fetch`/`axios` calls to any endpoint)
- Mock backend logic (no `msw`, no fake server, no simulated network delay)
- Authentication or session logic (login/register forms are static; no JWT, no cookies)
- State management for _real_ data (no Redux/Zustand/React Query — local `useState` is
  used only to drive UI interactions like slot selection or which modal is open)
- Placeholder API service files that pretend to call endpoints

Where the approved design implies a backend behavior (booking a slot, cancelling,
generating slots), the UI shows the _result_ of that action using local component
state, so the screen is fully interactive and demonstrable without a server.

## 4. Folder structure

```
src/
├── app/                          # Next.js App Router — one folder per route
│   ├── (auth)/                   # Route group: no sidebar/topbar chrome
│   │   ├── layout.tsx            #   Centered card layout
│   │   ├── login/page.tsx        #   Screen 01
│   │   └── register/page.tsx     #   Screen 02
│   ├── (user)/                   # Route group: user shell (sidebar + topbar)
│   │   ├── layout.tsx
│   │   ├── page.tsx              #   Screen 03 — Home (route "/")
│   │   ├── resources/
│   │   │   ├── page.tsx          #   Screen 04 — Resources list
│   │   │   └── [resourceId]/
│   │   │       └── page.tsx      #   Screen 05 — Resource detail + booking flow
│   │   ├── bookings/page.tsx     #   Screen 06 — My bookings
│   │   └── settings/page.tsx     #   Screen 07 — Account settings
│   ├── (admin)/                  # Route group: admin shell (sidebar + topbar)
│   │   ├── layout.tsx
│   │   └── admin/
│   │       ├── page.tsx          #   Screen 08 — Admin home
│   │       ├── resources/
│   │       │   ├── page.tsx      #   Screen 09 — Admin resources list
│   │       │   └── [resourceId]/
│   │       │       ├── page.tsx  #   Screen 10 — Admin resource detail
│   │       │       └── slots/new/page.tsx  # Screen 11 — Generate slots
│   │       ├── bookings/page.tsx #   Screen 12 — All bookings (+ force-cancel)
│   │       └── settings/page.tsx
│   ├── forbidden/page.tsx        # State C — 403 not-authorized page
│   ├── not-found.tsx             # App-wide 404
│   ├── layout.tsx                # Root layout (fonts, metadata)
│   └── globals.css               # Design tokens, mapped from the approved board
│
├── components/
│   ├── ui/                       # Generic, design-system-level primitives
│   │   ├── Badge.tsx              #   Status pills (confirmed/locked/cancelled/etc.)
│   │   ├── Button.tsx             #   primary / primaryAdmin / ghost / danger variants
│   │   ├── Card.tsx
│   │   ├── EmptyState.tsx         #   Shared pattern for empty + error states
│   │   ├── Field.tsx              #   Form-field-shaped display container
│   │   ├── MetricTile.tsx         #   Count-only summary tile (no charts)
│   │   ├── Modal.tsx              #   Portal-based overlay + panel
│   │   ├── Table.tsx
│   │   └── Toast.tsx
│   ├── layout/                    # App chrome: sidebar, topbar, shell
│   │   ├── AppShell.tsx
│   │   ├── Sidebar.tsx            #   user/admin variants, active-route highlighting
│   │   ├── Topbar.tsx
│   │   ├── BreadcrumbContext.tsx  #   lets pages set their own topbar breadcrumb
│   │   └── SetBreadcrumb.tsx
│   ├── resources/                 # Resource + slot domain components
│   │   ├── ResourceCard.tsx
│   │   └── SlotGrid.tsx
│   ├── bookings/                  # Booking domain components (shared user/admin)
│   │   ├── BookingsTable.tsx      #   one table, "self" | "admin" mode
│   │   ├── BookingConfirmModal.tsx
│   │   ├── BookingConflictModal.tsx
│   │   └── AdminCancelModal.tsx
│   └── admin/
│       └── AdminResourcesTable.tsx
│
├── data/                          # All mock/static data — see below
│   ├── resources.ts
│   ├── slots.ts
│   ├── bookings.ts
│   ├── users.ts
│   └── index.ts                   # Barrel export
│
├── types/
│   └── index.ts                   # Domain types: Resource, Slot, Booking, User, etc.
│
└── lib/
    └── cn.ts                      # Tailwind class-merging helper
```

**Why this structure:** components are grouped by what they're _for_ (generic `ui/`
vs. layout chrome vs. domain-specific `resources/` / `bookings/` / `admin/`), not by
which page uses them. `BookingsTable`, for example, lives in `components/bookings/`
once and is used by both the user "My bookings" screen and the admin "All bookings"
screen via a `mode` prop — this avoids the kind of duplication a tutorial-style project
tends to accumulate.

## 5. Where mock data lives

All static data is in `src/data/`, one file per domain entity, each shaped to mirror
what the corresponding real endpoint is expected to return:

| File           | Mirrors                                           | Used by                                          |
| -------------- | ------------------------------------------------- | ------------------------------------------------ |
| `resources.ts` | `GET /api/resources`                              | Resources list, Resource detail, Admin resources |
| `slots.ts`     | `GET /api/resources/{id}/slots?date=`             | Resource detail / booking flow                   |
| `bookings.ts`  | `GET /api/bookings/me`, `GET /api/admin/bookings` | My bookings, Admin all bookings                  |
| `users.ts`     | The decoded JWT/session payload                   | Sidebar, Home, Settings                          |

Components never import raw arrays directly from these files in an ad-hoc way — they
go through small accessor functions (`getResourceById`, `getSlotsForResource`,
`getBookingsForUser`) so swapping the implementation later (see below) only requires
changing the function body, not every call site.

## 6. Installation

Requires **Node.js 20+** and npm.

```bash
git clone <repository-url>
cd booking-engine-frontend
npm install
```

## 7. Running the project

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000). The app does not redirect anywhere
on its own — start at `/login` or `/register` to see the auth screens, or go straight to
`/` for the user Home, or `/admin` for the admin Home.

Other scripts:

```bash
npm run build          # Production build
npm run start          # Serve the production build
npm run lint           # ESLint
npm run format         # Prettier — writes formatting fixes
npm run format:check   # Prettier — checks only, no writes
npm run typecheck      # TypeScript, no emit
```

## 8. Development workflow

- **Adding a new mock entity or field:** add/extend the type in `src/types/index.ts`
  first, then update the corresponding file in `src/data/`. TypeScript will flag every
  component that needs updating.
- **Adding a new screen:** create the route under the matching route group
  (`(user)`, `(admin)`, or `(auth)`), reuse primitives from `components/ui/` wherever
  the design calls for a button, badge, field, table, or modal rather than writing new
  markup for it.
- **Changing the visual design:** design tokens (colors) live in `src/app/globals.css`
  as CSS variables, consumed via Tailwind's `@theme inline` block. Changing a token
  there updates every component that uses it.
- **Formatting/linting:** run `npm run format` and `npm run lint` before committing;
  there's no pre-commit hook configured, so this is currently a manual step.

## 9. Backend integration

This frontend is structured so backend integration is additive, not a rewrite:

1. **Replace `src/data/*` accessor functions with real fetches.** Each function
   (`getResourceById`, `getSlotsForResource`, `getBookingsForUser`, etc.) has a single,
   well-defined return type already. Swap the body from returning a static array to an
   `async` function that calls the real endpoint, and update the few call sites that
   currently call them synchronously to handle a promise (e.g. via a Server Component
   `fetch` or a data-fetching hook).
2. **Introduce an API client.** A `src/lib/api/` folder (not present in this build, per
   the "no placeholder API services" constraint) is the natural place for a typed fetch
   wrapper once there's a real base URL and real endpoints to call.
3. **Add real authentication.** The `(auth)` route group's forms are currently static.
   Wiring them up means adding form state, a submit handler that calls
   `POST /api/auth/login` or `/register`, and middleware (Next.js `middleware.ts`) to
   redirect unauthenticated requests away from the `(user)` and `(admin)` groups —
   which is also where the `/forbidden` page becomes a real redirect target instead of
   a directly-linked demo route.
4. **Wire up the modals' confirm actions.** `BookingConfirmModal`, `AdminCancelModal`,
   and the cancel actions in `BookingsTable` currently update local React state only.
   Each `onConfirm`/`onCancelClick` callback is already isolated to one call site per
   page, so adding the corresponding `POST`/`DELETE` call means editing the handler
   function in the page component, not the shared component itself.
5. **Replace `mockCurrentUser` / `mockCurrentAdmin`.** Once there's a real session, the
   sidebar/topbar's `userLabel` and the Settings page's fields should come from the
   decoded session rather than `src/data/users.ts`.

No component in `src/components/` needs to change shape to support this — they all
already accept data via props/typed objects rather than reaching into mock data
themselves (with the narrow exception of the page-level files in `src/app/`, which is
exactly where data-fetching is expected to live in the App Router model).

## 10. Known limitations of this build

- Date pickers, search fields, and form inputs in flows like Generate Slots or the
  booking search bar are visually present but not functionally wired (no validation, no
  filtering) — they exist to match the approved design's layout, not to demonstrate
  business logic that depends on a backend.
- The "Simulate conflict" action on the Resource detail screen is a deliberate addition
  for this static build only: it's the only way to make the approved design's
  slot-conflict (409) state reachable and demonstrable without an actual concurrent
  request. It should be removed once the real booking endpoint exists and can produce
  that state itself.
- `npm audit` reports a moderate-severity advisory for `postcss`, but it's a transitive
  dependency bundled inside `next` itself (`next > postcss@8.4.31`), not a package this
  project depends on directly. The suggested `npm audit fix --force` would downgrade
  Next.js to version 9, which is not a real fix — this is expected to be resolved in a
  future Next.js patch release upstream.
