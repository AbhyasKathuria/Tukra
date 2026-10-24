# Tukra (टुकड़ा)
> *Take your tukra. Pay for your tukra.*

**Package:** `com.xsparks.tukra`  
**Team:** Xsparks  
**Hackathon Event:** Swim with TinyFish hackathon, Presidency University, Bengaluru (Fri 30 Oct 2026)  
**Platform:** Android (Kotlin, Jetpack Compose, Material 3, minSdk 26) + Node.js (Express, PostgreSQL)

---

## 1. Overview
Tukra eliminates university canteen and hostel mess queues:
- **ChoicePay:** Pay item-by-item rather than rigid mandatory meal charges.
- **QueueLess:** Schedule pickup within 5-minute capacity-governed slots; collect via single-use dynamic QR codes.
- **MessFlex:** Hostel mess menus, pre-order dish forecasts, and simulated counter serving sessions.
- **Campus Command:** Real-time demand analytics and the **TinyFish Campus Dining Intelligence Agent** for automated, vendor-approved menu extraction.

---

## 2. Monorepo Architecture
```
/                         Project root
  app/                    Android app (Jetpack Compose, Material 3, Hilt, Room, CameraX)
  gradle/libs.versions.toml Gradle Version Catalog
  tukra-backend/          Node.js API (Express, pg, Zod, node-cron, TinyFish client)
  demo-menu-site/         Public static canteen menu page for TinyFish web intelligence
  seed/images/            Optional dish photography
  docs/                   API, Demo script, Privacy policy, Launch checklist
```

---

## 3. Quick Start (15-Minute Setup)

### Prerequisites
- Node.js ≥ 20
- Java JDK 17 or 21 (or Android Studio bundled JDK)
- Android Studio Ladybug / Meerkat or later
- PostgreSQL database (Supabase, Neon, or local PostgreSQL)

### Backend Setup
```bash
cd tukra-backend
cp .env.example .env
# Edit .env with your DATABASE_URL and JWT_SECRET
npm install
npm run db:reset
npm start
```
Verify the backend at: `http://localhost:3000/health`

### Android App Setup
1. Open the repository root in Android Studio.
2. Ensure `local.properties` points to your Android SDK and has `API_BASE_URL=http://10.0.2.2:3000/` (for Android emulator).
3. Run `./gradlew assembleDebug` or click **Run 'app'** in Android Studio.

---

## 4. Simulation vs Real Working Software
| Real, Working Software | Simulated / Future Work |
|---|---|
| Item-level ordering & customized plates | Physical smart plates, load cells & scales |
| Capacity-controlled pickup slots | Camera food recognition AI |
| In-app wallet & Razorpay test-mode top-up | Real university ERP tuition fee billing |
| Single-use QR pickup verification | Real vendor bank settlement & payouts |
| TinyFish autonomous web menu extraction & diffing | Automated sync without human review (always requires vendor sign-off) |
| Live counter plate sessions | Hardware NFC/RFID trays |

---

## 5. Roadmap & Future Scope
- Hardware smart-plate integration (weighing sensors & NFC)
- University ERP fee ledger synchronization
- Direct bank payouts to campus vendor accounts
- Multi-outlet grouped single-cart checkouts
- Real UPI intent deep-linking with automated webhooks in production
