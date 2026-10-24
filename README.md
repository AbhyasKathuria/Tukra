# Tukra (टुकड़ा)
> *Take your tukra. Pay for your tukra.*

**Package:** `com.xsparks.tukra`  
**Team:** Xsparks  
**Hackathon:** Swim with TinyFish Hackathon, Presidency University, Bengaluru (Fri 30 Oct 2026)  
**Platform:** Android (Kotlin, Jetpack Compose, Material 3, minSdk 26, targetSdk 37) + Node.js Backend (Express, PostgreSQL, TinyFish Client)

---

## 1. Overview & Problem Statement
Campus canteens and hostel messes face two chronic, daily crises:
1. **The 20-Minute Rush Hour Stampede:** During 15-to-20 minute class breaks, hundreds of students rush to canteen counters simultaneously, causing overcrowding, lost class time, and kitchen chaos.
2. **The "Hostel Mess Lock-In" & Food Waste:** Students pay mandatory flat-rate fees for mess food they frequently skip, while messes prepare static, unforecasted volumes that result in staggering daily food waste.

**Tukra** solves both problems with:
- **QueueLess 5-Minute Pickup Slots:** Orders are scheduled into capacity-metered 5-minute slots. Students order before break, walk up during their slot, and collect instantly via single-use dynamic QR codes.
- **ChoicePay:** Pay item-by-item rather than rigid mandatory meal packages.
- **TinyFish Campus Dining Intelligence:** Autonomous web agents scan canteen menus and noticeboards, extract prices/dishes, diff them against database inventory, and present proposals for one-tap vendor approval.
- **MessFlex Counter Mode & Demand Forecasting:** Hostel messes run landscape high-contrast counter touchscreens for simulated smart plate billing, paired with 4-week rolling demand forecasting.

---

## 2. Test Credentials Card

For immediate testing, four pre-configured role accounts are seeded in the database. (In debug builds of the Android app, one-tap demo chips on the login screen automatically fill these credentials).

| Role | Email | Password | Pre-seeded Context / Features |
|---|---|---|---|
| **Student** | `student@tukra.local` | `Password123!` | ₹500 wallet balance, active orders, break banner alerts, pickup QR codes |
| **Vendor** | `vendor@tukra.local` | `Password123!` | South Tiffin outlet, live order queue with sound alerts, menu availability switches, TinyFish sync |
| **Mess Operator** | `mess@tukra.local` | `Password123!` | North Mess, landscape serving counter mode, dish tiles, portion forecasts |
| **Campus Admin** | `admin@tukra.local` | `Password123!` | Presidency University command, orders-by-hour bar chart, food surplus waste audit, sync monitor |

---

## 3. Monorepo Architecture

```
/
├── app/                                 Android Client Application
│   ├── src/main/java/com/xsparks/tukra/
│   │   ├── core/                        Design system, network client, DTOs, TokenStore
│   │   │   ├── design/                  Theme (Blush palette), Color, Type, Spacing, Elevation
│   │   │   ├── components/              Buttons, Skeletons, Steppers, Empty states, Sound alerts
│   │   │   ├── network/                 Retrofit, OkHttp, API interfaces, DTO models
│   │   │   └── util/                    Indian rupee formatter (₹1,24,000), Date/time helpers
│   │   ├── feature/
│   │   │   ├── auth/                    Splash, Onboarding, Login/Register with debug chips
│   │   │   ├── home/                    Break banner, category chips, outlet cards, search
│   │   │   ├── outlet/                  Collapsing menu, dish bottom sheet, veg badges
│   │   │   ├── cart/                    Cart manager, 5-minute slot picker bottom sheet
│   │   │   ├── checkout/                Itemized bill, wallet balance, demo pay, Razorpay
│   │   │   ├── tracking/                Live order stepper, pickup QR display, cancel/reorder
│   │   │   ├── wallet/                  Display balance, fast top-up chips, transaction ledger
│   │   │   ├── scan/                    CameraX + ML Kit QR scanner for simulated mess plates
│   │   │   ├── profile/                 Student profile, diet filters, in-app account deletion
│   │   │   ├── vendor/                  Live order queue, audio alerts, menu manager, TinyFish sync
│   │   │   ├── mess/                    Mess menu, forecast demand table, landscape counter mode
│   │   │   └── admin/                   Overview KPIs, hourly bar chart, waste audit, sync monitor
│   │   └── navigation/                  Type-safe Compose navigation routes and role switchers
│   └── src/main/res/                    Vector drawables, English + Hindi strings (`values-hi`)
│
├── tukra-backend/                       Node.js Backend Service
│   ├── src/
│   │   ├── index.js                     Express server entry point, graceful shutdown, cron jobs
│   │   ├── config.js                    Zod-validated environment schema with production guards
│   │   ├── db.js                        PostgreSQL connection pool
│   │   ├── middleware/                  JWT auth, ownership check, error handling, rate limiting
│   │   ├── routes/                      Modular routes (auth, me, campus, outlets, orders,
│   │   │                                wallet, vendor, mess, plate, admin)
│   │   └── services/                    TinyFish agent client, menu diffing, Razorpay verification
│   ├── sql/                             Database schema (17 tables) and initial seed data
│   ├── scripts/                         Database reset script, plate QR generator (`PLT-0001`...)
│   └── test/                            36 unit and integration tests (`node:test`)
│
├── demo-menu-site/                      Public static canteen page used for TinyFish live sync
├── docs/
│   ├── API.md                           Comprehensive API documentation for all endpoints
│   ├── DEMO_SCRIPT.md                   90-second pitch script & 16-point pre-flight checklist
│   ├── LAUNCH_CHECKLIST.md              Google Play Console release & Data Safety guide
│   └── PRIVACY_POLICY.md                Privacy policy & in-app account deletion documentation
├── render.yaml                          1-Click Render blueprint deployment specification
└── local.properties.example             Template for developer-specific local configuration
```

---

## 4. 15-Minute Quick Start Guide

### Prerequisites
- Node.js ≥ 20.0.0
- Android Studio Ladybug (or later) with Android SDK 35/37
- Java JDK 17 or JDK 21 (bundled with Android Studio)
- PostgreSQL database (local instance or cloud like Supabase/Neon)

### Step 1: Start the Backend
```bash
cd tukra-backend

# Copy environment variables
cp .env.example .env

# Edit .env with your DATABASE_URL and JWT_SECRET
# Example:
# DATABASE_URL=postgres://postgres:password@localhost:5432/tukra
# JWT_SECRET=super_secret_jwt_key_at_least_16_chars

npm install
npm run db:reset    # Creates schema and seeds demo accounts/outlets/menus
npm start
```

Verify backend health at `http://localhost:3000/health`. The response should display:
```json
{
  "ok": true,
  "features": {
    "tinyfish": false,
    "razorpay": false,
    "push": false,
    "demoPay": true
  }
}
```

### Step 2: Run the Android Client
1. Open the project root in Android Studio.
2. In `local.properties`, ensure:
   ```properties
   API_BASE_URL=http://10.0.2.2:3000/
   ```
   *(For physical devices over local Wi-Fi, use your laptop's LAN IP: `http://192.168.1.X:3000/`)*.
3. Select an Android Emulator running API 26 or higher.
4. Click **Run 'app'** or run:
   ```bash
   .\gradlew.bat installDebug
   ```
5. On the login screen, tap the **Student** chip to instantly log in and start ordering.

---

## 5. Verification & Testing

### Running Android Unit Tests
```bash
.\gradlew.bat testDebugUnitTest
```
Tests pass 100% across:
- `MoneyFormatterTest`: Indian digit grouping verification (`₹1,24,000`).
- `CartManagerTest`: Cart quantity caps, item replacement, multi-outlet conflict guards.
- `VendorQueueFilterTest`: Kitchen queue status filtering.
- `MessAndAdminLogicTest`: Forecast calculations, surplus waste classification, plate session billing.
- `AccessibilityAndMicrocopyTest`: WCAG AA contrast ratio compliance, TalkBack accessibility descriptions, C9 microcopy assertions.

### Running Backend Unit & Integration Tests
```bash
cd tukra-backend
npm test
```
All **36 tests** pass natively via `node:test` covering:
- Atomic order transactions & balance rollback on failures.
- Capacity slot IST day boundary reservations.
- TinyFish menu diffing & extreme price deviation sanity flags (> 3× / < 0.33×).
- Single-use QR verification and anti-replay guards.
- Razorpay HMAC-SHA256 signature verification & idempotency.
- Role-based authorization & ownership barriers.

---

## 6. Real Software vs. Simulated Feature Transparency

In accordance with hackathon evaluation standards, Tukra clearly delineates working production software from conceptual hardware simulations:

| Feature Area | Working, Production Software | Simulated / Out-of-Scope (Hackathon Demo) |
|---|---|---|
| **Ordering & Pickup** | Live slot allocation, atomic database reservations, dynamic QR codes, vendor status machine | Hardware barcode scanner gun (simulated via Android CameraX + ML Kit) |
| **Dining Wallet** | In-app ledger, balance deductions, refunds, Razorpay test-mode HMAC verification | Real student university ERP tuition ledger sync |
| **Vendor Kitchen** | Real-time queue polling, sound & vibration alerts, menu manager, capacity controls | Physical kitchen ticket thermal printers |
| **Hostel Mess** | Serving counter mode, itemized dish taps, 4-week forecast engine, plate linking | Physical smart plates with built-in RFID/NFC/scales |
| **TinyFish AI** | Autonomous browser agent, SSE stream consumption, structured diffing, human sign-off | Unsupervised autonomous menu changes (human vendor approval is strictly required) |

---

## 7. 90-Second Demo & Pitch Flow

Refer to [docs/DEMO_SCRIPT.md](file:///docs/DEMO_SCRIPT.md) for the exact step-by-step 90-second judge presentation script and 16-point pre-flight checklist.

**Key Pitch Highlight (The "TinyFish Moment"):**
1. Vendor opens the South Tiffin Menu Manager in the Tukra app.
2. The demo canteen web page has prices updated (Masala Dosa changed from ₹70 to ₹80, and Rava Idli added).
3. Vendor taps **"Sync menu"**.
4. Tukra launches TinyFish Campus Dining Intelligence, showing live SSE progress and the streaming browser session.
5. In 15 seconds, 2 proposals appear with clear diff highlights.
6. The vendor taps **"Approve All"**.
7. The student app immediately reflects the updated prices and new items.  
*Key Takeaway: "Zero manual data entry by busy canteen cooks. The AI discovers changes; the human retains full authority."*

---

## 8. Demo Backup Plan

During high-stakes hackathon demos, connectivity or hardware hiccups can occur. Follow this backup protocol:
1. **Primary Plan:** Android phone on local Wi-Fi connected to local backend server, with TinyFish API key active.
2. **Wi-Fi Backup:** Mobile phone 5G hotspot shared between developer laptop and testing phone.
3. **Hardware Backup:** Android Emulator running inside Android Studio connected to `http://10.0.2.2:3000/`.
4. **Offline / Video Fallback:** High-resolution 90-second MP4 screencast recorded and stored locally on the laptop desktop.
5. **Pre-printed Plates:** Physical QR codes `PLT-0001` through `PLT-0005` pre-printed and laminated for the mess counter demonstration.

---

## 9. Deployment to Production

### 1-Click Backend Deployment (Render)
1. Fork or push this repository to GitHub.
2. Log into [Render.com](https://render.com) and click **New > Blueprint**.
3. Select your repository. Render automatically reads `render.yaml` and provisions:
   - A managed PostgreSQL instance.
   - The Node.js web service running `tukra-backend`.
4. Set your `TINYFISH_API_KEY` and optional `RAZORPAY_KEY_ID` in the Render dashboard.

### Building Release APK & Android App Bundle (.aab)
```bash
# Build signed minified release APK
.\gradlew.bat assembleRelease

# Build Google Play release App Bundle
.\gradlew.bat bundleRelease
```
Artifacts are generated in:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

---

## 10. Roadmap & Future Scope
- Hardware smart-plate integration (IoT load cells and RFID trays).
- University ERP single-sign-on (SSO) and semester fee ledger integration.
- Direct automated bank payouts to canteen vendors.
- Multi-canteen grouped carts with cross-campus pickup routing.
- Native iOS client (SwiftUI).

---

## 11. Team & Acknowledgments
Built with ❤️ by **Team Xsparks**:
- **Abhyas Kathuria** — Lead Engineer & Architect
- Special thanks to the **TinyFish** team for their autonomous web agent infrastructure and **Presidency University, Bengaluru** for hosting the hackathon.
