# Tukra (टुकड़ा)
> *Take your tukra. Pay for your tukra.*

**Package:** `com.xsparks.tukra`  
**Team:** Xsparks  
**Hackathon:** Swim with TinyFish Hackathon, Presidency University, Bengaluru (Fri 30 Oct 2026)  
**Platform:** Android (Kotlin, Jetpack Compose, Material 3, minSdk 26, targetSdk 37) + Node.js Backend (Express, PostgreSQL, TinyFish Client)  
**Live Production API:** `https://tukra.vercel.app`

---

## 1. Overview & Problem Statement
Campus canteens and hostel messes face two chronic, daily crises:
1. **The 20-Minute Rush Hour Stampede:** During 15-to-20 minute class breaks, hundreds of students rush to canteen counters simultaneously, causing overcrowding, lost class time, and kitchen chaos.
2. **The "Hostel Mess Lock-In" & Food Waste:** Students pay mandatory flat-rate fees for mess food they frequently skip, while messes prepare static, unforecasted volumes that result in staggering daily food waste.

**Tukra** solves both problems with:
- **QueueLess 5-Minute Pickup Slots:** Orders are scheduled into capacity-metered 5-minute slots. Students order before break, walk up during their slot, and collect instantly via single-use dynamic QR codes.
- **ChoicePay:** Pay item-by-item rather than rigid mandatory meal packages.
- **TinyFish Campus Dining Intelligence:** Autonomous web agents scan canteen menus, diff them against database inventory, scrape Swiggy/Zomato/EatSure to rescue students from disliked meals, benchmark competitor prices, and autofill vendor onboarding.
- **MessFlex Counter Mode & Demand Forecasting:** Hostel messes run landscape high-contrast counter touchscreens for simulated smart plate billing, paired with 4-week rolling demand forecasting.

---

## 2. Capabilities by User Profile

### 🎓 Student Profile
- **Browse & Pre-order:** View live menus from all campus canteens and stalls with Veg/Non-Veg filters and Quick Bites.
- **5-Minute Slot Booking:** Pick an exact 5-minute pickup time slot to skip long canteen queues.
- **Single-Use Pickup QR:** Receive a dynamic, replay-protected QR code to show at the counter.
- **Campus Wallet:** Instant payment via In-App Wallet (UPI, Card, Net Banking) with instant automated refunds on cancellation.
- **🐟 TinyFish Food Rescue:** If food is disliked or unavailable, parallel AI agents search Swiggy, Zomato, and EatSure to find the cheapest alternative delivered to Gate 2.
- **Auto-Location Detection:** Automatically detects campus presence ($<600\text{ m}$) and maps delivery to hostel drop-off gates.
- **Smart Plate Scan:** Scans mess plates and logs meals against the student's official university email.

### 👨‍🍳 Vendor Profile (Canteen Stall)
- **Live Kitchen Queue:** Instant dashboard for incoming orders (`New` $\rightarrow$ `Accepted` $\rightarrow$ `Cooking` $\rightarrow$ `Ready`).
- **Loud Sound Alerts:** Audio chimes notify the cook immediately when a new student order arrives.
- **Built-in QR Camera Scanner:** 1-tap camera scanner verifies student pickup QR codes and guards against replay.
- **Live Stock Controls:** Toggle dishes in-stock or sold-out and adjust prices with one tap.
- **🐟 TinyFish Menu Sync:** AI reads the stall's public menu page and proposes additions and price changes for approval.
- **🐟 Price Benchmark:** Compares stall prices against local Swiggy/Zomato market rates to help vendors price competitively.
- **Capacity Controls:** Limit maximum orders per 5-minute slot to stop kitchen overwhelm during peak breaks.

### 🍱 Mess Operator Profile (Hostel Mess)
- **Weekly Mess Menu:** Publishes breakfast, lunch, high-tea, and dinner menus for hostel residents.
- **Live Plate & Meal Counter:** Tracks how many students have eaten in real-time versus booked reservations.
- **Demand Forecasting & Waste Prevention:** Monitors reservation cancellations ahead of cut-off times to reduce food waste.
- **🐟 Dish Doctor:** Identifies the week's most-disliked mess items with student reasons and competitor price benchmarks to help decide whether to improve or replace recipes.

### 🏛️ Admin Profile (University Management)
- **Campus Food Command Center:** Real-time metrics across all canteens, revenue, active slots, and crowd density.
- **🐟 1-Click Vendor Onboarding:** Paste any stall's public web link; TinyFish auto-extracts the stall name, address, hours, and dishes.
- **🐟 Menu Freshness Guard:** Nightly automated audit flags stale menus, sold-out items, and $>20\%$ price jumps.
- **🐟 TinyFish Agent Console:** Full transparency dashboard showing live agent execution, streaming traces, durations, and credit quotas.
- **Sustainability & Waste Analytics:** University-wide reports on food waste saved across hostel messes.

---

## 3. TinyFish Autonomous Agent Architecture

> *“The most TinyFish work happens in Dislike Rescue (three parallel agents per dislike) and Menu Sync (every outlet, every night). Everything else reuses the same agent layer.”*

### TinyFish Usage Map (For Judges)

| Where in Tukra | Who | Status | What TinyFish Does | Why It Needs an Agent |
| :--- | :--- | :--- | :--- | :--- |
| **Dislike Rescue** | Student | **BUILT** | Compares Swiggy, Zomato, and EatSure in parallel for a disliked dish and ranks the 3 cheapest alternatives with delivery time & fees. | Delivery platforms have no open public API. |
| **Menu Sync** | Vendor | **BUILT** | Scrapes the outlet's public menu page and proposes dish additions, price changes, and sold-out toggles. | Menus change daily and manual updates lead to stale data. |
| **Vendor Onboarding** | Vendor | **BUILT** | Paste any public stall link to auto-fill outlet name, hours, address, and menu items with veg/non-veg tags. | Reduces onboarding friction; stalls have no structured API. |
| **Menu Freshness Guard**| Admin | **BUILT** | Nightly cron (`02:00 IST`) checks menus $>24$h old; flags stale data, sold-out items, and $>20\%$ price jumps. | Proactively prevents students from ordering stale or unavailable dishes. |
| **Nearby Discovery** | Off-Campus Student | **BUILT** | Surfaces local eateries, sample dishes, and prices when GPS confirms the student is outside campus ($>600\text{ m}$). | PG students eat off campus with no central food database. |
| **Vendor Price Benchmark**| Vendor | **BUILT** | Queries live market prices for a dish to show "your price vs. area market median". | Stalls need market intelligence to price food fairly. |
| **Dish Doctor** | Mess & Admin | **BUILT** | Correlates weekly disliked campus food with competitor prices and ratings. | Turns subjective student complaints into clear data-backed fix-or-drop actions. |
| **Agent Console** | Admin / Judges | **BUILT** | Inspects all agent runs, duration, source status, and live SSE streaming logs. | Provides complete transparency, cost control, and credit observability. |

### Strict Safety Boundary
- **What TinyFish is NEVER used for:** Payments, bill calculations, slot allocations, authentication/login, and allergen advice. Those operations require atomic $<50\text{ ms}$ deterministic guarantees in normal code.

---

## 4. Test Credentials Card

For immediate testing, four pre-configured role accounts are seeded in the database. (In debug builds of the Android app, one-tap demo chips on the login screen automatically fill these credentials).

| Role | Email | Password | Pre-seeded Context / Features |
|---|---|---|---|
| **Student** | `aarav@presidency.ac.in` | `demo1234` | ₹500 wallet balance, active orders, break banner alerts, pickup QR codes |
| **Vendor** | `ramesh@southtiffin.com` | `demo1234` | South Tiffin outlet, live order queue with sound alerts, menu availability switches, TinyFish sync |
| **Mess Operator** | `mess@presidency.ac.in` | `demo1234` | North Mess, landscape serving counter mode, dish tiles, portion forecasts |
| **Campus Admin** | `admin@presidency.ac.in` | `demo1234` | Presidency University command, orders-by-hour bar chart, food surplus waste audit, sync monitor |

*(Aliases like `student@tukra.local`, `vendor@tukra.local`, `mess@tukra.local`, and `admin@tukra.local` also work interchangeably with password `demo1234`).*

---

## 5. Monorepo Architecture

```
/
├── app/                                 Android Client Application (Jetpack Compose, Material 3)
│   ├── src/main/java/com/xsparks/tukra/
│   │   ├── core/                        Design system, network client, DTOs, TokenStore
│   │   │   ├── designsystem/            Theme (Blush/Butter/TukraPink palette), Color, Typography
│   │   │   ├── components/              TinyFishBadge, TinyFishInfoBottomSheet, TinyFishLiveProgress
│   │   │   ├── location/                CampusLocationManager (Fused GPS, Geocoder, 600m boundary)
│   │   │   ├── network/                 Retrofit, OkHttp, AuthInterceptor, TukraApi, DTOs
│   │   │   └── util/                    MoneyFormatter (₹1,24,000), VendorSoundAlert
│   │   ├── feature/
│   │   │   ├── auth/                    Splash, Onboarding, Login/Register with 1-tap demo chips
│   │   │   ├── home/                    Break banner, category chips, outlet cards, search
│   │   │   ├── rescue/                  TinyFish AI Food Rescue (Swiggy, Zomato, EatSure scraping)
│   │   │   ├── agent/                   AgentConsoleScreen, AboutTinyFishScreen (for judges)
│   │   │   ├── cart/                    Cart manager, 5-minute slot picker bottom sheet
│   │   │   ├── checkout/                Itemized bill, wallet balance, demo pay, Razorpay
│   │   │   ├── tracking/                Live order stepper, pickup QR display, cancel/reorder
│   │   │   ├── wallet/                  Display balance, fast top-up chips, transaction ledger
│   │   │   ├── scan/                    CameraX + ML Kit QR scanner for simulated mess plates
│   │   │   ├── profile/                 Student profile, diet filters, in-app account deletion
│   │   │   ├── vendor/                  Live kitchen queue, sound alerts, menu manager, TinyFish sync
│   │   │   ├── mess/                    Mess menu, forecast demand table, landscape counter mode
│   │   │   └── admin/                   Overview KPIs, hourly bar chart, waste audit, sync monitor
│   │   └── navigation/                  Type-safe Compose navigation routes and role switchers
│   └── src/main/res/                    Vector drawables, English + Hindi strings (`values-hi`)
│
├── tukra-backend/                       Node.js Backend Service (Express + PostgreSQL)
│   ├── src/
│   │   ├── index.js                     Express server entry point, graceful shutdown
│   │   ├── config.js                    Zod-validated config with TINYFISH_API_KEY + backup failover
│   │   ├── db.js                        PostgreSQL connection pool
│   │   ├── middleware/                  JWT auth, ownership check, error handling, rate limiting
│   │   ├── routes/                      auth, campus, outlets, orders, wallet, vendor, mess, agent, admin
│   │   ├── services/                    agentJobService, tinyfish, foodScraper, menuDiff, orderService
│   │   └── jobs/                        Nightly node-cron Menu Freshness Guard (02:00 IST)
│   ├── sql/                             Database schema (001_init, 002_plate_sessions, 003_agent_jobs)
│   └── test/                            43 unit and integration tests (`node:test`)
```

---

## 6. Quick Start Guide

### Prerequisites
- Node.js ≥ 20.0.0
- Android Studio Ladybug (or later) with Android SDK 35/37
- Java JDK 17 or JDK 21
- PostgreSQL database (Neon or local)

### Step 1: Start the Backend
```bash
cd tukra-backend

# Copy environment variables
cp .env.example .env

# Install dependencies & run tests
npm install
npm test            # Runs all 43 tests in < 1 second

# Start dev server
npm start
```

### Step 2: Run the Android Client
1. Open the project root in Android Studio.
2. In `local.properties`:
   ```properties
   API_BASE_URL=https://tukra.vercel.app/
   ```
   *(Or use your laptop's LAN IP `http://192.168.1.X:3000/` for local testing)*.
3. Select an Android Device or Emulator running API 26+.
4. Click **Run 'app'** or install directly via CLI:
   ```bash
   .\gradlew.bat installDebug
   ```

---

## 7. Verification & Automated Testing

### Backend Unit & Integration Tests (43 Passing)
```bash
cd tukra-backend
npm test
```
Covers:
- Shared Agent Layer (`validateAndNormalizeResult`, 30-min caching, rate limiting, usage map).
- End-to-End Order Lifecycle (`PLACED` $\rightarrow$ `ACCEPTED` $\rightarrow$ `PREPARING` $\rightarrow$ `READY` $\rightarrow$ `COLLECTED`).
- Replay-protected single-use QR verification and cutoff cancellation refunds.
- Multi-source parallel scraping and coupon calculations.
- Menu diffing (`NEW`, `PRICE_CHANGE`, `SOLD_OUT`, `REMOVED`).
- Razorpay signature verification & wallet bounds.

### Android Unit Tests
```bash
.\gradlew.bat testDebugUnitTest
```
Covers:
- `MoneyFormatterTest`: Indian digit grouping verification (`₹1,24,000`).
- `CartManagerTest`: Cart quantity caps, item replacement, multi-outlet conflict guards.
- `VendorQueueFilterTest`: Kitchen queue status filtering.
- `MessAndAdminLogicTest`: Forecast calculations, surplus waste classification, plate session billing.

---

## 8. Built Artifacts

- **Compiled Debug APK:** `app/build/outputs/apk/debug/app-debug.apk` (25.0 MB)
- **Live Vercel Backend:** `https://tukra.vercel.app`
- **Judge Usage API:** `https://tukra.vercel.app/meta/tinyfish-usage`
- **Agent Monitor API:** `https://tukra.vercel.app/admin/agent-jobs`

---

## 9. Team & Acknowledgments
Built with ❤️ by **Team Xsparks**:
- **Abhyas Kathuria** — Lead Engineer & Architect
- Special thanks to the **TinyFish** team for their autonomous web agent infrastructure and **Presidency University, Bengaluru** for hosting the hackathon.
