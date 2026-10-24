# Tukra Play Store Launch Checklist & Submission Guide

This document details the complete end-to-end launch workflow for publishing the **Tukra (टुकड़ा)** Android client to the Google Play Store.

---

## 1. App Identity & Store Listing Metadata

| Field | Production Value | Constraints |
|---|---|---|
| **App Name** | Tukra (टुकड़ा) | Max 30 chars |
| **Short Description** | Skip university dining queues. Pick your tukra, schedule pickup, tap and go. | Max 80 chars |
| **Package Name** | `com.xsparks.tukra` | Immutable once registered |
| **Default Language** | English (en-US) | Localization: Hindi (hi-IN) supported |
| **Category** | Food & Drink | Secondary: Campus Life / Productivity |
| **Tags** | Food delivery, Canteen ordering, Smart campus, QR code, Meal scheduling | Max 5 tags |

### Full Description (Up to 4,000 characters)
```text
Tukra (टुकड़ा) — "Take your tukra. Pay for your tukra."

Tukra is the next-generation campus dining platform built for university students, faculty, and canteen operators. Stop standing in crowded 20-minute canteen lines between classes or paying for fixed hostel mess meals you never ate.

KEY FEATURES:

1. ⚡ QueueLess 5-Minute Pickup Slots
Schedule your food collection within dynamic 5-minute capacity-controlled slots. Order your Chole Bhature or Cold Coffee before class ends, walk up to the counter when your slot starts, and flash your dynamic single-use QR code for instant pickup.

2. 🥗 ChoicePay: Pay for What You Eat
Say goodbye to mandatory fixed hostel mess plans. Select individual rotis, sabjis, and bowls with transparent itemized pricing.

3. 🤖 TinyFish Campus Dining Intelligence
Canteen menus change daily. With integrated TinyFish autonomous web agents, Tukra extracts updated specials and live prices from public canteen noticeboards, diffs them against current inventory, and lets vendors approve updates with a single tap.

4. 🍽️ MessFlex Smart Counter Mode
Hostel mess operators use high-contrast serving counter interfaces to log plate sessions in real-time, link student plates via QR scan, and forecast next-day cooking quantities to drastically eliminate food waste.

5. 👛 In-App Wallet & Fast Top-Ups
Enjoy instant single-tap checkouts backed by an in-app student dining wallet. Top up securely in seconds via UPI, debit card, or net banking powered by Razorpay.

6. 🔔 Real-Time Order Tracking & Haptic Feedback
Track your order from Accepted to Preparing and Ready with live countdowns and push alerts. Designed with WCAG AA compliant contrast, TalkBack screen reader support, and dual-vibration vendor kitchen notifications.

PRIVACY & SAFETY FIRST:
- Canteen vendors only see your first name and order code—never your email, phone, or hostel room.
- Camera access is used solely for on-device QR scanning (no photos or videos are ever stored or uploaded).
- One-tap "Delete my account" in your profile permanently erases your personal data.

Built by Team Xsparks. Experience queue-free university dining today!
```

---

## 2. Graphic Assets & Design Specifications

- [ ] **App Icon:**
  - Format: 32-bit PNG with alpha channel.
  - Dimensions: 512 px by 512 px.
  - Max file size: 1024 KB.
  - Design: Blush pink circular emblem `#FE81D4` featuring the Sora Bold "tukra" wordmark with the signature pink bite mark.
- [ ] **Feature Graphic:**
  - Format: JPEG or 24-bit PNG (no alpha).
  - Dimensions: 1024 px width by 500 px height.
  - Design: Clean brand banner on soft `#FFF9FC` blush canvas showcasing the Tukra phone mockup, break banner countdown, and pickup QR card.
- [ ] **Screenshots (Minimum 4 phone screenshots, 1080 x 1920 or 1080 x 2400):**
  1. *Screenshot 1 (Home & Break Banner):* "Break in 18 min. Order now and skip the line."
  2. *Screenshot 2 (Menu & Dish Sheet):* "Full customization, spice levels & allergen tags."
  3. *Screenshot 3 (Capacity Slot Picker):* "Choose your 5-minute pickup window."
  4. *Screenshot 4 (Live Order Tracking & QR):* "Dynamic single-use pickup verification."
  5. *Screenshot 5 (Vendor Queue & Kitchen Mode):* "Instant status updates with audio alerts."
  6. *Screenshot 6 (Mess Counter & Plate Session):* "Real-time itemized plate billing."

---

## 3. Data Safety & Privacy Form Answers

Use these exact answers in the Google Play Console **Data Safety** section (derived from `docs/PRIVACY_POLICY.md`):

### Data Collection & Sharing Summary:
- **Does the app collect or share user data?** Yes.
- **Is all data encrypted in transit?** Yes (all network traffic uses TLS 1.3 / HTTPS).
- **Does the app provide a way for users to request data deletion?** Yes (in-app "Delete my account" under Profile, calling `DELETE /me`).

### Specific Data Types:
1. **Personal Information:**
   - **Name:** Collected (App functionality, Account management). Not shared with third parties.
   - **Email Address:** Collected (Account authentication, Receipts). Not shared with third parties.
2. **Financial Information:**
   - **Purchase History:** Collected (In-app dining wallet transactions, order history). Not shared with third parties.
   - **Payment Info:** Not collected by app (handled externally by PCI-DSS compliant Razorpay SDK).
3. **Location:** None collected.
4. **Photos and Videos:** None collected.
5. **Camera:** Ephemeral processing only. Used strictly for barcode/QR scanning on-device via ML Kit. No frames or images are saved or transmitted off-device.
6. **Device or Other Identifiers:**
   - **FCM Device Token:** Collected (App functionality, Push notifications for order status). Encrypted in transit.

---

## 4. Content Rating (IARC)

Complete the IARC questionnaire in Google Play Console with the following responses:
- **Category:** Utility, Productivity, Communication, or Other (Food & Beverage Ordering).
- **Violence:** No.
- **Sexuality / Adult Content:** No.
- **Language / Profanity:** No.
- **Controlled Substances:** No (app orders canteen food and beverages only; no alcohol or tobacco).
- **User-to-User Interactions:** No (no public chat forums or unmoderated messaging).
- **Physical Location Sharing:** No.
- **Digital Purchases:** Yes (digital food ordering and dining wallet top-ups).
- **Expected Rating:** Everyone (PEGI 3 / ESRB Everyone).

---

## 5. Release Signing & Production Keystore Setup

### Step 1: Generate Release Keystore (Run once on secure developer workstation)
```bash
keytool -genkeypair -v \
  -keystore tukra-release.jks \
  -alias tukra-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass YOUR_SECURE_STORE_PASSWORD \
  -keypass YOUR_SECURE_KEY_PASSWORD \
  -dname "CN=Tukra Release, OU=Mobile, O=Xsparks, L=Bengaluru, ST=Karnataka, C=IN"
```

> [!CAUTION]
> **NEVER commit `tukra-release.jks` to git.** It must remain in your private local directory or CI/CD secret manager (e.g. GitHub Actions Encrypted Secrets).

### Step 2: Configure `local.properties` (or CI environment variables)
Add the following to your private `local.properties`:
```properties
RELEASE_STORE_FILE=C\:\\path\\to\\tukra-release.jks
RELEASE_STORE_PASSWORD=YOUR_SECURE_STORE_PASSWORD
RELEASE_KEY_ALIAS=tukra-key
RELEASE_KEY_PASSWORD=YOUR_SECURE_KEY_PASSWORD
API_BASE_URL=https://tukra-backend.onrender.com/
RAZORPAY_KEY_ID=rzp_live_YOUR_LIVE_KEY_ID
```

### Step 3: Build the Android App Bundle (.aab)
```bash
.\gradlew.bat bundleRelease
```
The production bundle will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

---

## 6. Pre-Submission Verification Steps

- [ ] `versionCode` and `versionName` incremented in `app/build.gradle.kts`.
- [ ] `API_BASE_URL` points to production HTTPS endpoint (e.g. `https://tukra-backend.onrender.com/`).
- [ ] `isMinifyEnabled = true` and `isShrinkResources = true` verified with clean R8 pass.
- [ ] ProGuard rules in `app/proguard-rules.pro` verified without runtime reflection crashes.
- [ ] `android:forceDarkAllowed="false"` verified in `AndroidManifest.xml`.
- [ ] Camera and notification runtime permission rationale dialogs display before requesting system prompts.
- [ ] Privacy Policy URL hosted and accessible publicly (e.g. GitHub Pages or raw repository URL).
- [ ] Internal Testing Track: Add 5 tester emails and install via Google Play Internal App Sharing.
- [ ] Execute 16-point manual test script (`docs/DEMO_SCRIPT.md`) on a physical Android device.
