# TUKRA — MASTER BUILD PROMPT (for Antigravity)

> Drop this file in the project root. Then tell Antigravity:
> **"Read TUKRA_MASTER_PROMPT.md completely, then begin Stage 0. Stop at every CHECKPOINT and wait for me to reply CONTINUE."**

---

# PART A — HOW YOU (THE AGENT) MUST WORK

You are the lead engineer building **Tukra**, a production-grade Android app plus a Node.js backend, from this single specification. This file overrides any other document in the repo. If the original PDF ("Tukra — Complete Build Documentation") is present, treat it as background only; where it disagrees with this file, this file wins (see Part O for the known corrections).

## A1. Execution rules
1. **Work stage by stage** (Part M). Never start a stage before the previous CHECKPOINT is confirmed by the user typing `CONTINUE`.
2. **Every checkpoint report** must contain: (a) what you built, (b) exact commands you ran and their results, (c) how I can verify it myself in under 2 minutes, (d) anything you could not do and why, (e) the next stage's name. Then stop.
3. **Guided steps for anything I must do by hand** (creating accounts, pasting keys, installing SDKs, Play Console): give numbered steps, one stage at a time, and ask me to confirm each before continuing.
4. **Full file replacements.** When you change an existing file, rewrite the whole file. Do not give partial diffs or "…rest unchanged" placeholders.
5. **Run it, don't assume it.** After each stage run the relevant build/test: `./gradlew assembleDebug testDebugUnitTest` for Android, `npm test` and `npm run lint` for backend. Fix failures before reporting. Never report "should work".
6. **Never invent secrets.** Never write a real key into any file that is committed. All secrets live in `tukra-backend/.env` (git-ignored) or `local.properties` (git-ignored). Provide `.env.example` and `local.properties.example` with placeholder values.
7. **Never put a secret in the Android project.** The TinyFish key, Razorpay secret, JWT secret and Firebase service account exist only on the backend.
8. **Verify external APIs before coding against them.** Before Stage 3, open `https://docs.tinyfish.ai` (Agent API reference, Authentication, SSE events) and confirm endpoint paths, header name, request fields, event names and run statuses. If anything differs from Part G, follow the live docs and tell me what changed. Keep all TinyFish calls isolated in `src/services/tinyfish.js` so a change touches one file.
9. **Library versions:** use a Gradle version catalog and the latest stable versions compatible with the installed Android Studio. Do not copy version numbers from this file.
10. **Money is integer paise everywhere.** Time is stored in UTC and shown in IST (`Asia/Kolkata`). IDs are UUID strings.
11. **No dead features.** Anything visible in the UI must work end to end, or be clearly marked "Coming soon" with the control disabled. No fake buttons.
12. **Honesty about simulation.** The smart plate, weighing and camera recognition are *simulated* by a tap on the serving-counter tablet. The UI and README must say "simulated" where relevant. Payments run in Razorpay **test mode** until the user switches keys.
13. If a requirement is ambiguous, pick the safest reasonable option, state it in the checkpoint report, and move on. Ask a question only when a wrong guess is expensive.

## A2. Definition of "ready to launch"
- Backend deployable with one command or one click, fails fast on missing config, exposes `/health`, has rate limiting, validation, parameterised SQL, and tests on money and slot logic.
- Android release build compiles with R8 on, signed via local config, no cleartext traffic in release, no secrets in the APK.
- Seed data rebuilds the entire demo in seconds (`npm run db:reset`).
- README lets a new teammate run everything in 15 minutes.
- Play-Store-readiness checklist completed (Stage 11), with a privacy policy page and in-app account deletion.

---

# PART B — PRODUCT

**Name:** Tukra (टुकड़ा, "a piece"). **Tagline:** *Take your tukra. Pay for your tukra.*
**Package:** `com.xsparks.tukra`. **Platform:** Android only, Kotlin, Jetpack Compose, Material 3, min SDK 26.
**Team:** Xsparks. **Event:** Swim with TinyFish hackathon, Presidency University, Bengaluru, Fri 30 Oct 2026.

**What it is:** Indian campus students, staff and faculty pick individual dishes, pay only for those dishes, choose a 5-minute pickup slot, and collect with a QR, without queueing.

**Three pillars + one layer**
1. **ChoicePay** — item-level menus, custom plate, itemised GST-inclusive bill, wallet (Razorpay test-mode top-up).
2. **QueueLess** — order ahead, capacity-controlled pickup slots, live status, QR pickup, cancel/refund before cutoff.
3. **MessFlex** — hostel mess: daily menu, reservations, QR-plate simulation at the serving counter, simple demand forecast.
4. **Campus Command** — admin dashboard (orders, revenue, demand, waste estimate) and the **TinyFish Campus Dining Intelligence Agent** that keeps outlet menus fresh, **always behind human approval**.

**Roles:** `STUDENT`, `STAFF`, `VENDOR`, `MESS_OPERATOR`, `ADMIN`. The role in the JWT decides which navigation graph appears after login.

**Personas to test against:** Aarav (hostel student, wants one dish, price/speed), Dr. Meena (25-minute break, orders from the lab), Ramesh bhaiya (stall owner, big buttons, Hindi labels, loud alert), Ms. Kaur (admin, wants numbers).

**Real vs simulated**
| Real, working software | Simulated / future |
|---|---|
| Ordering, slots, wallet, vendor queue, QR pickup, admin analytics, TinyFish menu sync with approval, plate-session billing | Physical smart plate, scales, camera recognition, real university fee integration, real vendor settlement |

**Billing models supported:** pay-per-item (canteens, MVP), prepaid-plan tracking, hybrid allowance (Phase 2), pure pay-per-item mess (needs university approval). The institution decides the fee model; Tukra supplies the technology.

---

# PART C — DESIGN SYSTEM (WHITE THEME + CUSTOM PALETTE)

The app is a **light, white-first** product. **Dark mode is out of scope**: set `android:forceDarkAllowed="false"`, do not implement a dark scheme, and disable dynamic colour.

## C1. Brand palette (supplied by the user — use exactly these)
| Token | Hex | Role |
|---|---|---|
| `Tukra Pink` (primary) | `#FE81D4` | Primary buttons, selected chips, active nav indicator, key highlights |
| `Rose` (secondary) | `#FAACBF` | Secondary buttons/fills, progress, illustrations, slot "fill meter" |
| `Blush` (tertiary) | `#FBC3C1` | Soft panels, chips background, empty-state art, pressed tints |
| `Butter` (accent) | `#FFEABB` | Banners (break banner), "Earliest" tag, info callouts, highlight rows |
| `White` | `#FFFFFF` | App background and all cards/sheets |

## C2. Supporting neutrals and functional colours (necessary additions)
The four brand colours cannot carry body text or status meaning on their own (white on `#FE81D4` is only ~2.3:1, which fails accessibility). Add these and nothing else:

| Token | Hex | Use |
|---|---|---|
| `Ink` | `#2B1A27` | All primary text, and text/icons **on** every brand colour (≈7:1 on `#FE81D4`) |
| `InkSoft` | `#6B5666` | Secondary text, captions |
| `Outline` | `#F3DCE5` | 1 dp card borders, dividers |
| `PinkDeep` | `#B0246F` | Text links and small accent text on white (verify ≥ 4.5:1; darken if not) |
| `Success` | `#1F9D55` | Ready status, veg mark, "approved" |
| `Danger` | `#D32F4E` | Errors, non-veg mark, cancel/reject, "sold out" |
| `Warning` | use `Butter` fill + `Ink` text + amber icon `#B7791F` | Preparing, low stock |

**Hard rules**
- Never use `#FE81D4`/`#FAACBF` as text colour on white. They are **fills**; text on them is always `Ink`.
- Never use white text on any brand colour.
- Status is never conveyed by colour alone: always add an icon and a label.
- Colour ratio per screen ≈ 80% white, 12% ink/neutral, 6% pink family, 2% butter/other.
- Veg (green) and non-veg (red) square marks are an Indian regulatory convention and are the only places `Success`/`Danger` are used decoratively.

## C3. Compose theme (implement exactly)
```kotlin
val TukraPink = Color(0xFFFE81D4); val Rose = Color(0xFFFAACBF)
val Blush = Color(0xFFFBC3C1);     val Butter = Color(0xFFFFEABB)
val Ink = Color(0xFF2B1A27);       val InkSoft = Color(0xFF6B5666)
val OutlineWarm = Color(0xFFF3DCE5); val PinkDeep = Color(0xFFB0246F)
val Success = Color(0xFF1F9D55);   val Danger = Color(0xFFD32F4E)

private val LightColors = lightColorScheme(
    primary = TukraPink, onPrimary = Ink,
    secondary = Rose, onSecondary = Ink,
    tertiary = Blush, onTertiary = Ink,
    primaryContainer = Blush, onPrimaryContainer = Ink,
    secondaryContainer = Butter, onSecondaryContainer = Ink,
    background = Color.White, onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Color(0xFFFFF6F9), onSurfaceVariant = InkSoft,
    outline = OutlineWarm, error = Danger, onError = Color.White
)
```
`TukraTheme` always uses `LightColors`; status bar and navigation bar are white with dark icons (`enableEdgeToEdge` with light system bars).

## C4. Typography (bundle fonts in `res/font`, lowercase file names, so it works offline)
| Style | Font | Size/line | Weight | Use |
|---|---|---|---|---|
| Display | Sora | 32/40 | Bold | Wallet balance, splash |
| Title L | Sora | 22/28 | SemiBold | Screen titles |
| Title M | Sora | 18/24 | SemiBold | Section headers, outlet names |
| Body L | Inter | 16/24 | Regular | Main text |
| Body M | Inter | 14/20 | Regular | Dish descriptions |
| Label | Inter | 12/16 | Medium | Chips, captions, status tags |
| Price | Sora | 16/20 | SemiBold | Prices, tabular numerals |

Download Sora (SemiBold, Bold) and Inter (Regular, Medium) from Google Fonts. Vendor mode adds +2 sp to every style.

## C5. Shape, spacing, elevation
Spacing scale (dp): 4, 8, 12, 16, 20, 24, 32; side padding 16. Radii: 12 (chips/inputs), 20 (cards), 28 (sheet top corners), 16 (buttons). Elevation: **two levels only**: cards = 1 dp `Outline` border, no shadow; sheets and cart bar = one soft shadow. Minimum touch target 48×48 dp.

## C6. Components (build each as a reusable composable with a `@Preview` for every state)
| Component | Spec |
|---|---|
| Outlet card | 16:9 photo (blush placeholder with outlet initial if none), name (Title M), type tag, open/closed pill, wait chip "~6 min" or "Busy", location note |
| Dish row | 72 dp photo, veg mark, name, 1-line description, price, round **+** button (Tukra Pink, Ink icon) that morphs into **−  1  +** stepper |
| Cart bar | Sticky, white, soft shadow, item count, total, pink "View cart" 56 dp button; slides up with slight overshoot (250 ms) |
| Slot chip | Time label, tiny fill meter in `Rose`, disabled when full, first chip tagged "Earliest" on `Butter` |
| Status stepper | Placed → Preparing → Ready → Collected; current dot pink with pulsing ring; Ready draws a green check (600 ms); icon + label on each step |
| QR card | White card, 220 dp QR, order code beneath, raise screen brightness while visible |
| Primary button | `#FE81D4` fill, `Ink` text, 56 dp, 16 dp radius, pressed = 8% darker |
| Secondary button | White fill, 1.5 dp `Ink` outline |
| Bottom sheet | 28 dp top corners, drag handle |
| Bill row | Name left, qty centre, price right in tabular figures; dashed divider above bold total |
| Skeleton | Blush-tinted shimmer matching the final layout, 1.2 s sweep (never a bare spinner) |
| Empty state | Flat illustration from brand colours only + one sentence + one action. Needed: empty cart, no orders, no internet, outlet closed |
| Snackbar | Ink background, white text, above the cart bar |

## C7. Motion (respect system "remove animations": all durations → 0)
Add to cart: stepper morph + total count-up 180 ms, haptic tick, <100 ms response. Screen change: shared-axis horizontal slide + fade 300 ms. Order ready: ring pulse + self-drawing check 600 ms. No confetti.

## C8. Iconography and imagery
Material Symbols Rounded only, 24 dp; filled variant only for the selected bottom-nav item. **No emoji as icons.** Food photos: square or 16:9, loaded with Coil, blush placeholder. Photos are optional in seed data; ship a tasteful initial-letter placeholder composable. Leave a `seed/images/` folder with a README explaining where the user drops real campus-dish photos.

## C9. Microcopy (use verbatim)
| Situation | Copy |
|---|---|
| Empty cart | "Nothing here yet. Pick your tukra." |
| Slot picker title | "When do you want it?" |
| Order placed | "Done! {dish} will be ready by {time}." |
| Order ready | "Your order is hot and ready. Show your QR at the counter." |
| Slot full | "That time just filled up. Try the next slot." |
| No internet | "No connection. Your cart is safe, we will wait." |
| Low balance | "₹{n} short. Add money to place this order." |
| Onboarding 1 | "Choose only what you eat" — "Build your plate dish by dish. No more paying for dal you skip." |
| Onboarding 2 | "Skip the queue" — "Order before the break, pick a time, walk in and collect." |
| Onboarding 3 | "See where your money goes" — "Every bill is itemised and every order is saved." |

## C10. Accessibility (acceptance criteria)
Text contrast ≥ 4.5:1 (Ink on every brand fill passes); every icon button has `contentDescription`; font scaling to 200% must not clip; TalkBack reads a dish row as "name, veg/non-veg, price, quantity in cart"; touch targets ≥ 48 dp.

## C11. Role variations (same system)
Vendor: +2 sp type, big status buttons, loud alert sound on new order, optional Hindi labels (`strings-hi`). Admin: denser layout, charts use `Rose` for the main series and `Tukra Pink` for the highlight series. Mess counter: landscape, very large tiles, high contrast.

---

# PART D — REPOSITORY, STACK, CONVENTIONS

## D1. Repository layout (monorepo at the Android project root)
```
/                         (Android Studio project root, already created by the user)
  app/                    Android app, package com.xsparks.tukra
  gradle/libs.versions.toml
  tukra-backend/          Node.js API
  demo-menu-site/         public static menu page the TinyFish agent reads
  seed/images/            optional dish photos
  docs/                   README.md, API.md, DEMO_SCRIPT.md, PRIVACY_POLICY.md, LAUNCH_CHECKLIST.md
  TUKRA_MASTER_PROMPT.md
  .gitignore              (include .env, local.properties, *.jks, google-services.json?, service accounts)
```
`google-services.json` is **not secret** but is user-specific: git-ignore it, and make the Firebase Gradle plugin **conditional** (apply only if the file exists) so the project builds without it.

## D2. Android stack
Kotlin 2.x, Compose + Material 3, MVVM + Repository with unidirectional data flow, Coroutines + Flow, Hilt, Navigation Compose (type-safe routes), Retrofit + OkHttp + kotlinx.serialization, Room + DataStore, Coil, CameraX + ML Kit Barcode Scanning, ZXing core (QR generation), Firebase Cloud Messaging (optional), Razorpay Android SDK (test mode), Vico or custom Canvas charts, JUnit + MockK + Turbine + Compose UI test.

**Folder structure (by feature):**
```
com.xsparks.tukra/
  TukraApp.kt  MainActivity.kt
  core/designsystem/ (Color, Type, Theme, components/)
  core/network/ (TukraApi, Dtos, AuthInterceptor, TokenStore)
  core/database/ (TukraDatabase, DAOs, entities)
  core/util/ (Money.kt, TimeFormat.kt, Resource.kt)
  core/navigation/ (Routes.kt, TukraNavHost.kt)
  feature/ auth home menu cart slots checkout orders wallet scan mess vendor admin profile sync
  di/ (Hilt modules)
```
State rules: each screen exposes one `UiState` as `StateFlow` (`Loading | Success | Error`). `CartManager` is a Hilt `@Singleton`, persisted to DataStore. Menus and orders cached in Room; orders/payments are **never** created offline. The server is the source of truth for prices, slot capacity and wallet balance.

## D3. Backend stack
Node 20+, Express, `pg`, Zod, `jsonwebtoken`, `bcryptjs` (cost ≥ 10), `cors`, `helmet`, `express-rate-limit`, `dotenv`, `node-cron`, `razorpay`, `firebase-admin` (optional), `qrcode` (dev script), `node:test` for tests. ES modules. Postgres on Supabase/Neon/Render.

Backend layout:
```
tukra-backend/
  src/index.js  src/app.js  src/config.js (env validation)  src/db.js
  src/middleware/ auth.js validate.js error.js
  src/routes/ auth outlets menu slots orders wallet vendor mess plate admin sync me
  src/services/ orderService slotService walletService tinyfish menuDiff push demand
  src/jobs/cron.js
  sql/schema.sql  sql/seed.sql  sql/migrations/
  scripts/ makePlateQrs.js  tinyfish-smoke.js  resetDb.js
  test/ *.test.js
  .env.example  package.json  render.yaml (or railway.json)
```
`config.js` validates env with Zod at boot and **exits with a readable list of what is missing**. Required: `DATABASE_URL`, `JWT_SECRET`. Optional with feature flags reported on `/health`: `TINYFISH_API_KEY` (menu sync), `RAZORPAY_KEY_ID/SECRET` (top-up), `FIREBASE_SERVICE_ACCOUNT` (push), `ENABLE_DEMO_PAY` (demo top-up; must default to `false` and be refused when `NODE_ENV=production` unless explicitly set).

`.env.example`:
```
PORT=3000
NODE_ENV=development
DATABASE_URL=postgres://user:pass@host:5432/tukra
JWT_SECRET=change-me-to-a-long-random-string
TINYFISH_API_KEY=
RAZORPAY_KEY_ID=
RAZORPAY_KEY_SECRET=
FIREBASE_SERVICE_ACCOUNT=
ENABLE_DEMO_PAY=true
DEMO_MENU_URL=https://your-demo-menu-page.example.com
```

## D4. API conventions
- JSON camelCase on the wire; convert snake_case DB columns in one helper.
- Every route except `/auth/*` and `/health` requires `Authorization: Bearer <JWT>`.
- JWT payload: `{ id, role, campusId, outletId? }` (`outletId` resolved from `outlets.owner_user_id` for vendors), 12 h expiry.
- Uniform error body: `{ "error": { "code": "SLOT_FULL", "message": "..." } }`.
- Status codes: 200/201 OK; 400 `VALIDATION_FAILED`; 401 `UNAUTHENTICATED`; 402 `INSUFFICIENT_BALANCE`; 403 `FORBIDDEN`; 404 `NOT_FOUND`; 409 `SLOT_FULL` / `ITEM_UNAVAILABLE` / `OUTLET_CLOSED` / `ALREADY_PROCESSED`; 500 `INTERNAL`.
- Never log tokens, passwords, emails, or API keys. Redact.
- Rate limit: 10 login attempts / 15 min / IP; general 120 req/min/IP.
- Cache `GET /outlets` for 15 s.

---

# PART E — DATABASE (run in this order; all money in paise)

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE campuses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL, city TEXT NOT NULL,
  break_times JSONB NOT NULL DEFAULT '[["10:30","10:50"],["13:00","13:45"]]'   -- IST, used by the Home break banner
);

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  campus_id UUID NOT NULL REFERENCES campuses(id),
  name TEXT NOT NULL, email TEXT UNIQUE NOT NULL, phone TEXT,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('STUDENT','STAFF','VENDOR','MESS_OPERATOR','ADMIN')),
  hostel TEXT,
  diet_pref TEXT NOT NULL DEFAULT 'ANY' CHECK (diet_pref IN ('ANY','VEG','JAIN','EGG')),
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE outlets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  campus_id UUID NOT NULL REFERENCES campuses(id),
  owner_user_id UUID REFERENCES users(id),
  name TEXT NOT NULL,
  type TEXT NOT NULL CHECK (type IN ('CANTEEN','MESS','CAFE','JUICE','SNACKS')),
  location_note TEXT, phone TEXT, cover_image_url TEXT,
  source_menu_url TEXT,
  is_open BOOLEAN NOT NULL DEFAULT true,
  slot_minutes INT NOT NULL DEFAULT 5,
  slot_capacity INT NOT NULL DEFAULT 12,
  opens_at TIME NOT NULL DEFAULT '08:00',
  accepts_orders_until TIME NOT NULL DEFAULT '17:00',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_outlets_campus ON outlets(campus_id);

CREATE TABLE categories (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
  name TEXT NOT NULL, sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE menu_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
  category_id UUID REFERENCES categories(id),
  name TEXT NOT NULL, description TEXT,
  price_paise INT NOT NULL CHECK (price_paise >= 0),
  is_veg BOOLEAN NOT NULL DEFAULT true,
  image_url TEXT,
  prep_seconds INT NOT NULL DEFAULT 180,
  prep_units INT NOT NULL DEFAULT 1 CHECK (prep_units > 0),
  is_available BOOLEAN NOT NULL DEFAULT true,
  spice_level INT CHECK (spice_level BETWEEN 0 AND 3),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_menu_items_outlet ON menu_items(outlet_id);

CREATE TABLE slots (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
  starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ NOT NULL,
  capacity INT NOT NULL, used INT NOT NULL DEFAULT 0 CHECK (used >= 0),
  UNIQUE (outlet_id, starts_at)
);
CREATE INDEX idx_slots_outlet_start ON slots(outlet_id, starts_at);

CREATE TABLE orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code TEXT UNIQUE NOT NULL,                       -- 'TK-4821'
  user_id UUID NOT NULL REFERENCES users(id),
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  slot_id UUID NOT NULL REFERENCES slots(id),
  status TEXT NOT NULL DEFAULT 'PLACED' CHECK (status IN
    ('PLACED','ACCEPTED','PREPARING','READY','COLLECTED','CANCELLED','REJECTED','NO_SHOW')),
  subtotal_paise INT NOT NULL, total_paise INT NOT NULL,
  prep_units INT NOT NULL,                         -- ADDED: needed to release slot capacity on cancel/reject
  payment_method TEXT NOT NULL CHECK (payment_method IN ('WALLET','UPI','DEMO','MESS_PLAN')),
  note TEXT,
  qr_token TEXT UNIQUE NOT NULL,
  reject_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ready_at TIMESTAMPTZ, collected_at TIMESTAMPTZ
);
CREATE INDEX idx_orders_user ON orders(user_id, created_at DESC);
CREATE INDEX idx_orders_outlet_status ON orders(outlet_id, status);

CREATE TABLE order_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  menu_item_id UUID NOT NULL REFERENCES menu_items(id),
  name_snapshot TEXT NOT NULL,
  unit_price_paise INT NOT NULL,
  qty INT NOT NULL CHECK (qty > 0)
);

CREATE TABLE wallets (
  user_id UUID PRIMARY KEY REFERENCES users(id),
  balance_paise INT NOT NULL DEFAULT 0 CHECK (balance_paise >= 0)
);

CREATE TABLE wallet_txns (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  type TEXT NOT NULL CHECK (type IN ('TOPUP','ORDER','REFUND','ADJUST','PLATE')),
  amount_paise INT NOT NULL,                       -- + credit, − debit
  ref_order_id UUID REFERENCES orders(id),
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  razorpay_order_id TEXT UNIQUE NOT NULL,
  razorpay_payment_id TEXT,
  amount_paise INT NOT NULL,
  status TEXT NOT NULL DEFAULT 'CREATED' CHECK (status IN ('CREATED','PAID','FAILED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE mess_menus (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  service_date DATE NOT NULL,
  meal TEXT NOT NULL CHECK (meal IN ('BREAKFAST','LUNCH','SNACKS','DINNER')),
  menu_item_id UUID NOT NULL REFERENCES menu_items(id),
  planned_qty INT,
  UNIQUE (outlet_id, service_date, meal, menu_item_id)
);

CREATE TABLE mess_reservations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  mess_menu_id UUID NOT NULL REFERENCES mess_menus(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, mess_menu_id)
);

CREATE TABLE plate_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  plate_code TEXT NOT NULL,
  user_id UUID REFERENCES users(id),
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  status TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','BILLED','PAID')),
  total_paise INT NOT NULL DEFAULT 0,
  opened_at TIMESTAMPTZ NOT NULL DEFAULT now(), paid_at TIMESTAMPTZ
);
-- ADDED: a plate code can have only one live session
CREATE UNIQUE INDEX uq_plate_open ON plate_sessions(plate_code) WHERE status IN ('OPEN','BILLED');

CREATE TABLE plate_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  plate_session_id UUID NOT NULL REFERENCES plate_sessions(id) ON DELETE CASCADE,
  menu_item_id UUID NOT NULL REFERENCES menu_items(id),
  qty INT NOT NULL DEFAULT 1, unit_price_paise INT NOT NULL,
  served_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE menu_sync_jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  tinyfish_run_id TEXT,
  status TEXT NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING','COMPLETED','FAILED')),
  streaming_url TEXT, progress TEXT,               -- merged in from the doc's ALTER TABLE
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(), finished_at TIMESTAMPTZ,
  num_steps INT, error_message TEXT
);

CREATE TABLE menu_proposals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  job_id UUID NOT NULL REFERENCES menu_sync_jobs(id) ON DELETE CASCADE,
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  change_type TEXT NOT NULL CHECK (change_type IN ('NEW','PRICE_CHANGE','REMOVED','AVAILABILITY')),
  menu_item_id UUID REFERENCES menu_items(id),
  item_name TEXT NOT NULL,
  old_price_paise INT, new_price_paise INT,
  flagged BOOLEAN NOT NULL DEFAULT false,          -- ADDED: sanity-check flag ("Check this price")
  status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
  decided_by UUID REFERENCES users(id), decided_at TIMESTAMPTZ
);

CREATE TABLE device_tokens (
  user_id UUID NOT NULL REFERENCES users(id),
  token TEXT NOT NULL, PRIMARY KEY (user_id, token)
);
```
**DB-enforced rules:** wallet can never go negative; slot `used` is incremented inside the order transaction; prices are frozen in `order_items`; every wallet movement writes a `wallet_txns` row in the same transaction. Pool config: add `ssl: { rejectUnauthorized: false }` when `DATABASE_URL` points to a hosted DB.

**Seed (`sql/seed.sql`, idempotent, realistic):** campus "Presidency University, Bengaluru"; 4 outlets — **South Tiffin** (CANTEEN, `source_menu_url = DEMO_MENU_URL`), **Chaat Corner** (SNACKS), **Juice Junction** (JUICE), **Hostel Mess** (MESS); 8–12 real Indian dishes each with believable ₹ prices and varied `prep_units` (lassi=1, chole bhature=3) and `prep_seconds`; users: Aarav (STUDENT, wallet ₹500), a second student (wallet ₹0), Dr. Meena (STAFF), one VENDOR per outlet, one MESS_OPERATOR, one ADMIN; password for all demo accounts `demo1234` (bcrypt-hashed in the seed; print the credentials table at the end of `db:reset`). `npm run db:reset` drops, recreates, seeds, and generates today's slots.

---

# PART F — BACKEND SPECIFICATION

## F1. Endpoints
| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | /auth/register | public | Create STUDENT/STAFF only (roles above that are seed/admin-created) |
| POST | /auth/login | public | JWT + user profile |
| GET | /me | any | Current user |
| POST | /me/device-token | any | Save FCM token |
| DELETE | /me | any | Delete account: anonymise PII, keep aggregate order history |
| PATCH | /me | any | Name, hostel, diet preference |
| GET | /campus | any | Campus name + break times |
| GET | /outlets | any | Outlets on caller's campus with `isOpen`, `waitMinutes` (first slot with free capacity minus now; `null` = Busy) |
| GET | /outlets/:id/menu | any | `{ items: [...] }`, each item has `category` name; honour `diet_pref` only as an optional `?diet=` filter |
| GET | /outlets/:id/slots?units=N&longestPrepSeconds=S | any | Feasible slots (see F3) |
| POST | /orders | student/staff | Create + pay (WALLET or DEMO) |
| GET | /orders | student/staff | My orders newest first (`?status=active|past`) |
| GET | /orders/:id | owner or outlet's vendor | One order (flat shape, see F2) |
| POST | /orders/:id/cancel | owner | Cancel + refund (F4) |
| GET | /wallet | student/staff | Balance + last 50 txns |
| POST | /wallet/topup/order | student/staff | Create Razorpay order (₹10–₹5,000) |
| POST | /wallet/topup/verify | student/staff | Verify HMAC signature, credit once |
| POST | /wallet/topup/demo | student/staff | Only when `ENABLE_DEMO_PAY=true` and not production |
| GET | /vendor/orders?status= | vendor | Live queue (first name + code only; never email/phone) |
| PATCH | /vendor/orders/:id/status | vendor | ACCEPTED, PREPARING, READY, REJECTED (state machine enforced server-side) |
| POST | /vendor/orders/verify-qr | vendor | `{ token }` → COLLECTED |
| PATCH | /vendor/items/:id | vendor | Toggle availability, edit price |
| POST | /vendor/items | vendor | Add item |
| PATCH | /vendor/outlet | vendor | Open/close, slot capacity, last-order time |
| POST | /vendor/sync | vendor/admin | Start TinyFish sync → `202 { jobId }` |
| GET | /vendor/sync/:jobId | vendor/admin | `{ status, progress, streamingUrl, proposalCount }` |
| GET | /vendor/proposals | vendor/admin | Pending changes |
| POST | /vendor/proposals/:id/decision | vendor/admin | `{ approve: bool }` |
| GET | /mess/menu?date=&meal= | any | Mess menu + reservation counts |
| POST | /mess/menu | mess | Publish/edit menu |
| POST | /mess/reservations | student | Reserve dishes |
| GET | /mess/forecast | mess/admin | Suggested prep quantity per dish |
| POST | /plate/open | mess | `{ plateCode, outletId }` → session |
| POST | /plate/link | student | `{ plateCode }` → attaches caller to the open session |
| POST | /plate/:id/items | mess | `{ menuItemId, qty }` (price from DB) |
| POST | /plate/:id/finish | mess | OPEN → BILLED |
| GET | /plate/:id | mess or owner | Itemised bill |
| POST | /plate/:id/pay | owner | Wallet debit, BILLED → PAID, in one transaction |
| GET | /admin/overview | admin | Orders today, revenue, avg wait, active outlets, orders-by-hour |
| GET | /admin/demand | admin | Items sold per day and hour; waste estimate |
| GET | /admin/outlets | admin | Outlets with counts + last sync |
| GET | /admin/sync-jobs | admin | Recent TinyFish runs |
| POST | /admin/slots/generate | admin | Manual slot generation |
| GET | /health | public | `{ ok, time, features: { tinyfish, razorpay, push, demoPay } }` |

## F2. Order response shape (flat; the Android DTO must match)
```json
{ "id":"…","code":"TK-4821","status":"PLACED","totalPaise":14500,
  "qrToken":"q_8d1f7c…","slotStartsAt":"2026-10-30T07:15:00Z",
  "outletName":"South Tiffin","note":"Less spicy please",
  "items":[{"name":"Chole Bhature","qty":1,"unitPricePaise":6500}] }
```

## F3. Slots
- Generate daily at 05:00 IST and on demand: from `outlets.opens_at` to `accepts_orders_until` in `slot_minutes` steps, `ON CONFLICT DO NOTHING`, using `+05:30` explicitly.
- Offer a slot only if: `starts_at >= now + longestPrepSeconds + 120 s`; `capacity − used >= units`; it falls within **today in IST** (the original doc used a UTC day boundary — do not); outlet is open and before last-order time. Return up to 24, ordered, with `remaining`.
- Unit limits: max qty per dish 10, max prep units per order 20.

```js
// today in IST, as UTC bounds
const { rows } = await client.query(
 `SELECT id, starts_at, ends_at, capacity - used AS remaining
    FROM slots
   WHERE outlet_id=$1 AND starts_at >= $2 AND capacity - used >= $3
     AND starts_at >= (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata')
     AND starts_at <  (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata') + interval '1 day'
   ORDER BY starts_at LIMIT 24`, [outletId, earliest, units]);
```

## F4. Create order (atomic; the most important function)
In **one transaction**: (1) outlet must be open → else `OUTLET_CLOSED`; (2) price every line **from the database** (never from the app) and reject unavailable items → `ITEM_UNAVAILABLE`; enforce qty/units limits; (3) reserve slot: `UPDATE slots SET used = used + $units WHERE id=$slot AND outlet_id=$outlet AND used + $units <= capacity AND starts_at > now() RETURNING id` → else `SLOT_FULL`; (4) if `WALLET`: `UPDATE wallets SET balance_paise = balance_paise − $total WHERE user_id=$u AND balance_paise >= $total` → else `INSUFFICIENT_BALANCE` (402); if `DEMO`: allowed only when `ENABLE_DEMO_PAY`; (5) insert order (store `prep_units`), generate `TK-####` and **retry up to 5 times on a unique violation**, `qr_token = 'q_' + 12 random bytes hex`; (6) insert `order_items` with frozen name/price; (7) insert `wallet_txns` (`ORDER`, negative). Rollback on any error. Totals are GST-inclusive (`total = subtotal`), shown as "Inclusive of taxes".

**Cancel/refund:** allowed only if status ∈ {PLACED, ACCEPTED} and now < slot start − 10 min. In a transaction: status `CANCELLED`, `slots.used -= orders.prep_units`, wallet credit, `wallet_txns` `REFUND`. **Reject** by vendor: same refund, slot released, `reject_reason` stored, push to student. **NO_SHOW:** READY orders with `ready_at` older than 20 min, no refund.

**Status state machine (server-enforced, also drives which button the vendor app shows):** `PLACED → ACCEPTED → PREPARING → READY → COLLECTED`; `PLACED/ACCEPTED → REJECTED`. Vendor may not skip steps. On `READY` set `ready_at` and push "Your order is ready".

## F5. QR pickup
`POST /vendor/orders/verify-qr {token}` → token exists, order belongs to the vendor's outlet, status is READY → set COLLECTED + `collected_at`, return items. Otherwise 409 with "Wrong outlet or already collected". A second scan must fail (blocks screenshot reuse).

## F6. Wallet top-up (Razorpay, test mode)
`/topup/order` creates the Razorpay order and a `payments` row (CREATED). `/topup/verify` computes `HMAC-SHA256(orderId + "|" + paymentId, KEY_SECRET)`, compares with `timingSafeEqual` (check equal length first), then **in one transaction** moves the payment `CREATED → PAID` (409 `ALREADY_PROCESSED` if already done), upserts wallet, writes `TOPUP` txn. The app never decides the balance. If Razorpay keys are absent, `/health` reports `razorpay:false` and the app shows only Demo top-up when `demoPay:true`.

**Checkout "UPI" button = top up then pay.** Orders are paid from the wallet. If balance is short, the button reads "Add ₹N and pay": it opens Razorpay for the shortfall (rounded up to a ₹10 multiple, min ₹10), verifies, then retries the order automatically.

## F7. Jobs (`node-cron`, Asia/Kolkata)
`generateSlots` 05:00 daily; `markNoShow` every minute; `closePlates` every 10 min (flag OPEN plates older than 90 min); `menuSync` nightly for outlets with `source_menu_url` **only if TinyFish is configured**. Jobs must be idempotent and wrapped in try/catch with a logged summary.

## F8. Mess, plates, forecast
- **Plate flow:** mess operator scans plate QR (`PLT-0001`…) → `/plate/open`; student scans the same plate QR with their phone → `/plate/link` (sets `user_id`); operator taps dish tiles → `/plate/:id/items` (server adds price × qty to running total); student's Scan screen polls `GET /plate/:id` every 3 s; operator taps Finish → BILLED; student taps Pay → wallet debit in a transaction (`wallet_txns` type `PLATE`), PAID, plate code freed. The plate QR identifies the **plate session only**; the food is recorded by operator tap (simulation). Say this in the UI footer of the counter screen: "Simulated smart plate".
- **Forecast (MVP):** per dish, tomorrow's demand = average of the same weekday over the last 4 weeks (`COLLECTED`/`READY` orders and mess plate items) + reservations already made. Labelled "Estimate" in the UI.
- `scripts/makePlateQrs.js` generates `PLT-0001`…`PLT-0010` PNGs into `plates/` (git-ignored) for printing.

## F9. Security checklist (all must be true)
Parameterised SQL everywhere; Zod on every body (reject unknown keys, cap string lengths); `auth(roles)` plus ownership checks; `helmet`; CORS limited to configured origins; bcrypt cost ≥ 10; no secret in logs; server recomputes every amount; single-use QR tokens; Razorpay signature verified; idempotent credit; `app.disable('x-powered-by')`; graceful shutdown on SIGTERM; unhandled-rejection handler that logs and exits.

---

# PART G — TINYFISH INTEGRATION (the sponsor feature; make it excellent)

**Purpose:** a *Campus Dining Intelligence Agent*. It reads an outlet's public menu page, extracts dishes and prices as structured JSON, the backend diffs against the database, and **the vendor approves or rejects each change**. The agent never writes to the live menu. Payments, totals and slot allocation never use AI.

**API (confirm against `docs.tinyfish.ai` first, per rule A1.8):** base `https://agent.tinyfish.ai/v1/automation/`; auth header `X-API-Key`; endpoints `POST /run` (blocking), `POST /run-async` (returns `run_id`; poll `GET /v1/runs/{id}`), `POST /run-sse` (stream: `STARTED`, `STREAMING_URL`, `PROGRESS`, `HEARTBEAT`, `COMPLETE`). Use **run-sse as primary** and polling as the recovery path if the stream drops.

**Request body:**
```json
{ "url": "<outlet.source_menu_url>",
  "goal": "<MENU_GOAL>",
  "output_schema": { "type":"object", "properties": { "items": { "type":"array", "items": {
      "type":"object", "properties": {
        "name":{"type":"string"}, "price_rupees":{"type":"number"},
        "category":{"type":"string"}, "is_veg":{"type":"boolean"}, "sold_out":{"type":"boolean"} },
      "required":["name","price_rupees"] } } }, "required":["items"] },
  "browser_profile": "lite",
  "agent_config": { "max_duration_seconds": 180 } }
```
**`MENU_GOAL`:** "Open this canteen menu page. Extract every food and drink item that is listed. For each item return its name, its price in rupees as a number, its category heading if there is one, whether it is vegetarian (true or false) and whether it is marked sold out. Ignore ads, opening hours and contact details. Do not click any order, pay or login buttons."

If the account rejects `output_schema` (only a JSON-Schema subset is supported), drop it and describe the exact JSON shape inside the goal text. `extractItems` must accept both `result.items` and a bare list under `result.result`, drop rows with non-numeric price, convert to paise with `Math.round(rupees*100)`, default `is_veg` to true.

**Client behaviour (`src/services/tinyfish.js`):** parse SSE by splitting on blank lines and reading `data: ` lines; forward each event to a callback; capture `run_id`; on stream end without `COMPLETE`, call `pollRun(runId)` every 3 s up to 180 s; map errors — 401 → "Menu sync is not available right now" (log server-side, mark job FAILED, manual editing still works); 429 → retry once after 10 s; `FAILED` runs → store `error.message`; zero items → FAILED "No menu items found on the page". Never log the key.

**Sync flow:**
1. `POST /vendor/sync {outletId}` → insert `menu_sync_jobs` (RUNNING) → respond `202 { jobId }` immediately → run in background.
2. Events: `STREAMING_URL` → save `streaming_url` + `tinyfish_run_id`; `PROGRESS` → save `progress` text.
3. On `COMPLETE`/`COMPLETED`: `extractItems` → `diffMenu(scraped, current)` → insert `menu_proposals` (PENDING) and set job COMPLETED with `num_steps`.
4. Vendor app polls `GET /vendor/sync/:jobId` every 2 s, shows live progress, "Watch live" (opens `streamingUrl` in a WebView), then "N changes found → Review changes".
5. Approve applies: `NEW` → insert item; `PRICE_CHANGE` → update price; `REMOVED` → `is_available=false`; `AVAILABILITY` → toggle. All inside a transaction with `FOR UPDATE` on the proposal; double-approve returns "Proposal already handled."

**`diffMenu`:** normalise names (lower-case, strip punctuation, collapse spaces); `NEW` if unseen; `PRICE_CHANGE` if price differs; `AVAILABILITY` if `soldOut === cur.is_available`; `REMOVED` if in DB but missing from page. **`sanityFlag`:** a price change with ratio > 3× or < 0.33× sets `flagged=true` → red "Check this price" tag in the UI, approve button requires a second tap.

**Demo target page (`/demo-menu-site/index.html`):** a clean, public, fast static page styled with the Tukra palette showing "South Tiffin — Menu" with 8–10 dishes, prices, veg marks, category headings (Mains, Snacks, Drinks). Add `README` for deploying to GitHub Pages or Vercel and for the **live-demo edit**: change Masala Dosa ₹70 → ₹80 and add Rava Idli ₹50. Only point the agent at pages the team owns. Write `scripts/tinyfish-smoke.js`: runs one real agent call against `DEMO_MENU_URL` and prints items, so the user can confirm the key works.

**Responsible use:** only owned/consented pages; human approval for every change; agent told never to click order/pay/login; max run duration; extracted data validated before storage; all proposals auditable (`decided_by`, `decided_at`).

---

# PART H — ANDROID SCREEN SPECIFICATION

Build Compose previews for each state (loading, empty, error, offline, success) before wiring data.

## H1. Student/staff (bottom bar: Home, Orders, Scan, Wallet, Profile)
- **S1 Splash:** white, wordmark in Sora Bold with a small pink "bite" mark replacing the dot on the "u"; session check < 1.2 s → Home or Onboarding/Login.
- **S2 Onboarding (3 pages, once):** flat brand-colour illustration, dots, pink "Next", "Skip" top right, last page "Get started". Copy in C9.
- **S3 Login/Register:** email, password (+ name, campus on register), inline validation, error under field in `Danger`, primary "Log in", link "New here? Create account". **Debug builds only:** three chips fill Student / Vendor / Admin demo credentials.
- **S4 Home:** greeting + campus + wallet pill; **break banner** on `Butter` ("Break in 18 min. Order now and skip the line.") using `/campus` break times (or the student's own timetable if set); search across dishes; filter chips (Veg only, Under ₹100, Ready fast, Open now); outlet cards (closed ones last, greyed); "Popular right now" row. States: 3 skeleton cards / "No outlets match" / retry / offline label with cached data.
- **S5 Outlet menu:** collapsing header (photo, name, hours, wait), sticky category tabs synced with scroll, dish rows with stepper, "Sold out" greyed rows, cart bar after first add.
- **S6 Dish sheet:** big photo, veg mark, description, spice dots, notes field, qty stepper, button "Add for ₹65".
- **S7 Cart:** one outlet at a time (adding from another outlet asks before clearing), swipe-to-remove, estimated prep time, itemised subtotal/total, "Choose pickup time". Cart persisted in DataStore.
- **S8 Slot picker (sheet):** "When do you want it?", chips, fill meters, full = disabled, first tagged "Earliest", "Continue" enabled on selection.
- **S9 Checkout:** summary card, itemised bill, wallet balance, "Add ₹N and pay" when short, Demo Pay (debug/flag only), note "Free cancellation until 10 minutes before pickup", button "Pay ₹145". Handles `SLOT_FULL` (reopen sheet + snackbar), `INSUFFICIENT_BALANCE`, `ITEM_UNAVAILABLE`, `OUTLET_CLOSED`.
- **S10 Tracking:** status stepper, countdown to slot, large order code, QR card (always accessible; highlighted on READY), Cancel (while allowed), Call outlet, Reorder after collection. Poll every 5 s; FCM makes it instant. Request notification permission at the first order, with a one-line reason.
- **S11 History:** Active / Past tabs, order cards, month summary ("This month: ₹1,240 spent, 17 orders") with a small bar sparkline.
- **S12 Wallet:** balance in Display type, "Add money" with ₹100/₹200/₹500 quick amounts, transaction list (top-ups, orders, refunds tagged).
- **S13 Scan/Plate:** CameraX + ML Kit, rounded-square viewfinder, hint "Scan the QR on your plate", camera-permission gate with explanation, vibrate once on success, then live growing bill (poll 3 s) and "Pay" when BILLED.
- **S14 Profile:** name, email, hostel/department, diet preference, notification settings, break times, privacy policy link, **Delete my account**, log out.

## H2. Vendor (bottom bar: Queue, Menu, Capacity, Proposals; big type, loud new-order alert)
V1 Queue (tabs New/Preparing/Ready, countdowns, sound on new order, Accept / Reject with reason / Start preparing / Mark ready) · V2 Order detail (items + notes in large type, first name, slot, next-status button, Scan QR) · V3 Menu manager (search, availability switch, price edit, add item, **Sync menu** button) · V4 Capacity & hours (open/closed, capacity slider, last-order time) · V5 Proposals (NEW / PRICE_CHANGE / REMOVED / AVAILABILITY with old→new, flagged tag, Approve, Reject, Approve all) · V6 QR verify (green success; red "Wrong outlet or already collected").

## H3. Mess operator (bottom bar: Menu, Reservations, Counter)
M1 Mess menu (meal tabs, reservation counts, publish, planned qty) · M2 Reservations (per-dish totals, **Suggested quantity** column labelled "Estimate", export list) · M3 Serving counter (landscape, huge tiles, shows plate code + student name, Scan plate, tap tile to add, Finish and bill, footer "Simulated smart plate").

## H4. Admin (bottom bar: Overview, Outlets, Analytics, Sync)
A1 Overview (KPI cards: orders today, revenue, average wait, active outlets; orders-by-hour chart) · A2 Outlets (open/closed, order count, last sync, add/edit capacity) · A3 Demand analytics (top-dishes bar chart, hourly heatmap, waste estimate table; filter outlet/date) · A4 Sync monitor (recent TinyFish runs, status, steps, changes found, "Run sync now").

## H5. Navigation
After login, read the role and open the matching graph: student/staff → Home graph; vendor → Queue; mess → Menu; admin → Overview. Student flow: Home → Outlet menu → (Dish sheet) → Cart → Slot sheet → Checkout → Tracking. Type-safe routes with `@Serializable` objects/data classes.

## H6. Android implementation notes
- **Network:** Retrofit + kotlinx.serialization (`ignoreUnknownKeys`, `explicitNulls=false`); `AuthInterceptor` adds the bearer token; `HttpLoggingInterceptor` BODY in debug only, NONE in release; base URL and Razorpay **key id** from `local.properties` into `BuildConfig`; base URL must end with `/`. Cleartext only through a **debug-only** network security config; release is HTTPS-only. Emulator → laptop backend is `http://10.0.2.2:3000/`.
- **Result wrapper:** `Resource<T>`; `safeCall` maps `HttpException` to the server's `{error:{code,message}}` and `IOException` to `OFFLINE`. ViewModels never throw into UI.
- **Errors → friendly copy** per code (OFFLINE, SLOT_FULL, ITEM_UNAVAILABLE, OUTLET_CLOSED, INSUFFICIENT_BALANCE, UNAUTHENTICATED → clear token and go to Login).
- **Room cache** of outlets, menus and recent orders; cached data shown immediately with an "Offline" label; checkout/top-up disabled offline with an explanation.
- **Money helper** `Int.toRupees()` with Indian digit grouping (`₹1,24,000`); test it as an **instrumented** test if JVM locale data lacks lakh grouping.
- **QR:** ZXing renders the pickup QR from `qrToken`; ML Kit scans; `unbindAll()` before `bindToLifecycle`; honour `rotationDegrees`.
- **FCM:** create the `orders` channel at high importance; vendor channel plays a distinct sound; upload token on login via `/me/device-token`; everything optional if `google-services.json` is absent.
- **Razorpay:** `Checkout` with key id from BuildConfig, `PaymentResultWithDataListener` in `MainActivity` forwarding into a `PaymentEvents` shared flow; the wallet ViewModel collects it and calls `/wallet/topup/verify`.
- **Manifest:** `INTERNET`, `CAMERA`, `POST_NOTIFICATIONS`; camera `required="false"`; `android:allowBackup="false"` with data-extraction rules excluding the session store; `android:forceDarkAllowed="false"`.
- **Release:** `isMinifyEnabled=true`, `isShrinkResources=true`, keep rules for serialization models, signing config from `local.properties`/env (keystore never in repo), `versionCode`/`versionName` set, build `.aab`.
- Performance targets: cold start < 2 s, 60 fps menu scroll, add-to-cart < 100 ms, APK < 25 MB (release).

---

# PART I — TESTING (write as you go; stages are not done until tests pass)

**Backend (`node:test`):** `diffMenu` (price change, new item, removed, availability); `sanityFlag`; slot filter and IST day bounds; order transaction (insufficient balance → nothing persisted; slot full → nothing persisted; success → totals, ledger row, `used` incremented); cancel/refund releases exactly `prep_units`; QR verify (wrong outlet, second scan); Razorpay signature accept/reject and idempotency; status state machine rejects skips; role/ownership 403s; env validation exits clearly.
**Android:** `Money` formatting; `CartManager` (add, other-outlet rejection, remove last empties cart, cap at 10); `CheckoutViewModel` with a fake repository + Turbine (success clears cart; `SLOT_FULL` and `INSUFFICIENT_BALANCE` states); `AuthViewModel` validation; one Compose UI test for add-dish → cart bar visible.
**Manual checklist (`docs/DEMO_SCRIPT.md`, run before every demo build):** 1 login · 2 order one dish · 3 item-level choice · 4 vendor sees order with sound · 5 status updates + push · 6 QR pickup, second scan fails · 7 slot full · 8 low balance → add money · 9 cancel + refund · 10 sold-out toggle · 11 TinyFish sync end to end · 12 TinyFish failure (bad URL) falls back gracefully · 13 plate flow · 14 offline browse · 15 large font/no clipping · 16 admin numbers match orders.

---

# PART J — DEMO & PITCH SUPPORT (generate these files in the repo)

`docs/DEMO_SCRIPT.md` containing the 60-second **TinyFish moment**: show the demo page (Masala Dosa ₹70) → edit page (₹80, add Rava Idli ₹50) → vendor taps Sync menu → live progress → optional Watch live → "2 changes found" → approve both → student pull-to-refresh shows new price and dish → line: "No vendor typed anything. The agent found the change, and a human approved it." Also: 5-minute pitch outline (problem 0:00–0:30, idea –1:00, live demo –3:00, TinyFish –4:00, plate simulation –4:30, impact/roadmap –5:00), a "what is real vs simulated" slide text, likely judge Q&A (fee model, plate recognition, why TinyFish vs a scraper, no-website stalls, fraud, waste claims, revenue), and the mini-study method (5 queue vs 5 app users; report averages with sample size; improvement = (T_queue − T_tukra)/T_queue × 100%). Never claim queues or waste are eliminated; report measured numbers.

---

# PART K — PRIVACY & COMPLIANCE
Collect only name, email, campus, optional hostel, diet preference, order history. Diet preference is never shown to vendors. Vendors see first name + order code only. Camera and notification permissions are requested at point of use with a short explanation, never at launch. In-app **Delete my account** (anonymise PII, keep aggregates). Admin analytics show counts and totals, not individuals. Generate `docs/PRIVACY_POLICY.md` (host it as a static page for the Play listing).

---

# PART L — OUT OF SCOPE (do not build)
Real university fee integration; real vendor settlement; hardware plate/scales/camera recognition; iOS; dark mode; multi-outlet grouped carts; refund to original UPI source; OTP login; ratings. List these in the README under "Roadmap" so the pitch can say they are future work.

---

# PART M — STAGES AND CHECKPOINTS

Each stage ends with a CHECKPOINT report (see A1.2). **Stop and wait for `CONTINUE`.**

**Stage 0 — Environment and scaffold.**
Verify Android Studio project builds (`./gradlew assembleDebug`), JDK is Android Studio's bundled JDK, Node ≥ 20, Git. Create folder layout (D1), `.gitignore`, version catalog, `README` skeleton, `tukra-backend` with `package.json` and `.env.example`, `local.properties.example`. Guide me, step by step with confirmations, through: creating a Postgres database (Supabase/Neon) and pasting `DATABASE_URL` into `tukra-backend/.env`; where to paste my TinyFish key (`TINYFISH_API_KEY` in `.env` only — **tell me never to paste it into chat or any committed file**). ✅ *Checkpoint.*

**Stage 1 — Backend core.** `config.js` env validation, `db.js`, schema + seed + `db:reset`, auth (register/login/me/PATCH/DELETE), outlets with wait estimate, menu, slots generation + availability, health, error/validation middleware, rate limiting, tests. ✅ *Checkpoint:* show `curl` for login, outlets, menu, slots; print the demo credentials table.

**Stage 2 — Orders, wallet, vendor, jobs.** Order transaction, cancel/refund, status machine, QR verify, vendor routes, wallet + Razorpay + demo top-up, device tokens + optional FCM push, cron jobs, tests for all money paths. ✅ *Checkpoint:* scripted end-to-end API run (login → slots → order → vendor accept/ready → verify QR → second scan fails → cancel path refunds).

**Stage 3 — TinyFish.** First read the live TinyFish docs and report any deviation from Part G. Build `tinyfish.js`, `menuDiff.js`, sync routes, proposals, status endpoint, `demo-menu-site`, `tinyfish-smoke.js`. Ask me to run the smoke test with my key and confirm the output. ✅ *Checkpoint:* a real run against the demo page produces proposals after I edit the page.

**Stage 4 — Mess, plates, admin, forecast.** Plate routes + unique-open index, mess menu/reservations, forecast, admin overview/demand/outlets/sync-jobs, `makePlateQrs.js`. ✅ *Checkpoint:* API-level plate walkthrough and admin numbers.

**Stage 5 — Android foundation.** Theme (Part C exactly), fonts, components with previews, DTOs, Retrofit/Hilt, TokenStore, `Resource`/`safeCall`, navigation skeleton, splash, onboarding, login/register with role routing. ✅ *Checkpoint:* app runs on emulator, logs in as student against the local backend, theme matches palette.

**Stage 6 — Student ordering flow.** Home, outlet menu, dish sheet, cart (persisted), slot picker, checkout, tracking with QR, Room cache, offline states. ✅ *Checkpoint:* place a real order from the app and see it in the DB.

**Stage 7 — Wallet, history, scan, profile.** Wallet + Razorpay + Demo top-up, history + month summary, plate scan screen + live bill + pay, profile + delete account, notification permission + FCM. ✅ *Checkpoint.*

**Stage 8 — Vendor app.** Queue with sound, order detail, menu manager, capacity, QR verify, **Sync screen (live progress, Watch live) and Proposals**. ✅ *Checkpoint:* full TinyFish moment on device.

**Stage 9 — Mess and Admin.** Mess menu, reservations + forecast, landscape serving counter, admin overview/outlets/analytics/sync monitor with charts. ✅ *Checkpoint.*

**Stage 10 — Polish and hardening.** Skeletons, empty states, haptics, motion (with reduced-motion handling), 200% font check, TalkBack labels, contrast audit of every brand-colour pairing, Hindi labels for the vendor app, performance pass, full unit/UI test run, manual checklist 1–16. ✅ *Checkpoint* with test results.

**Stage 11 — Deploy and launch.**
1. Backend: deploy (Render/Railway; generate `render.yaml`), set env vars, run schema/seed, confirm `/health` shows the right `features`, set an uptime ping.
2. Point the app's `API_BASE_URL` at the HTTPS URL; build a signed release `.aab` and a debug APK; install on a real phone and rerun the manual checklist.
3. Razorpay: switch from test to live keys **only when I confirm KYC is complete**.
4. Play Store readiness: `docs/LAUNCH_CHECKLIST.md` (developer account, privacy policy URL, screenshots, short/full description, content rating, data-safety form answers derived from Part K, versionCode/versionName, keystore backup).
5. Hand-off package: README, API.md, APK link instructions, demo credentials card, 90-second recording script, backup plan (hotspot, preinstalled APK on two phones, emulator, recorded flow).
✅ *Final checkpoint:* "Launch report" listing what is live, what is simulated, what is test mode, and known limitations.

---

# PART N — PRIORITY IF TIME RUNS SHORT (hackathon cut order, keep from the top)
1 Student order flow with slots and wallet · 2 Vendor queue with status updates · 3 TinyFish menu sync with approval · 4 QR pickup verification · 5 Plate simulation · 6 Admin analytics · 7 Animations, push notifications, Hindi labels. Freeze features 45 minutes before submission; after that, blockers only.

---

# PART O — CORRECTIONS TO THE SOURCE DOCUMENT (already applied above; do not regress)
1. TinyFish endpoints are hyphenated: `run-async`, `run-sse` (the PDF text lost the hyphens).
2. Slot availability used a UTC day boundary; it must use the **IST** day.
3. Cancellation released capacity using `units` that were never stored → added `orders.prep_units`.
4. Checkout offered UPI as an order payment method but `createOrder` only handled the wallet → orders are paid from the wallet; "UPI" means top up first, then pay.
5. Order DTO expected `slotStartsAt` but the API example nested `slot.startsAt` → the response is flat (F2).
6. `menu_sync_jobs.streaming_url/progress` were a late `ALTER TABLE` → merged into the base schema; added `menu_proposals.flagged`.
7. Order code collisions → retry on unique violation.
8. Two conflicting plate-open designs (`studentId` in open vs `/plate/link`) → one design: operator opens, student links; unique-open-plate index added; `/plate/finish` and `/plate/pay` specified; `PLATE` wallet txn type added.
9. `MENU_SCHEMA` was a placeholder in the PDF → fully specified in Part G.
10. Missing from the endpoint list: `/campus`, `/me` PATCH/DELETE, mess publish/forecast, admin outlets/slot generation, vendor add-item → added.
11. Break-banner "campus default break times" had no storage → `campuses.break_times`.
12. Vendor JWT needs `outletId` → resolved from `outlets.owner_user_id`.
13. Demo-pay safety: gated by `ENABLE_DEMO_PAY` and refused in production by default.
14. The PDF's table count is "18"; the schema has 17 tables plus helper `device_tokens` — build what Part E lists.
15. The PDF's green-and-yellow dark-capable theme is replaced by the **white + pink palette, light only** (Part C).
16. Firebase/Razorpay/TinyFish are optional at boot with feature flags so the app never crashes for a missing key.

---

# FIRST ACTION
Acknowledge you have read this file in full by replying with: (1) a 10-line summary of what you will build, (2) any ambiguity you found, (3) the exact Stage 0 steps you are about to run. Then begin Stage 0 and stop at its checkpoint.
