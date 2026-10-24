# Tukra — Demo Script & Manual Checklist (1–16)

> **Event:** Swim with TinyFish Hackathon  
> **Team:** Xsparks  
> **Target:** 90-Second Flawless Live Demo + Pre-Flight Verification

---

## Part 1: 90-Second Demo Script for Judges

| Time | Persona / Screen | Action | Key Takeaway / Feature |
|---|---|---|---|
| **00:00 - 00:15** | Student (Aarav) · Splash & Home | Open app. Point out Break Banner (*"Break in 18 min"*). Filter by *Veg only* & *Ready fast*. Tap **South Tiffin**. | White-first custom palette (`#FE81D4`, `#2B1A27`). Instant break-aware intelligence. |
| **00:15 - 00:30** | Student · Menu & Slot Picker | Pick **Masala Dosa (₹70)**. Add custom note (*"Extra coconut chutney"*). Tap View Cart. Pick 5-minute capacity slot with Rose fill meter (*Earliest*). Pay from Wallet. | **ChoicePay** (pay only for what you eat) + **QueueLess** capacity-controlled slots. |
| **00:30 - 00:45** | Student & Vendor · Real-Time Handoff | Show live Order Tracking with large bold code (`TK-4821`). Switch to Vendor tablet (`southtiffin@vendor.tukra`). Hear **loud new-order tone + vibration alert**. Advance order: *Accept* → *Start Preparing* → *Mark Ready*. | Status machine, kitchen-friendly audio alert, immediate push feedback. |
| **00:45 - 01:05** | Vendor & Student · QR Verification | Student's screen pulses green with ready QR code. Vendor taps **Verify QR** and scans or verifies code. Green verified banner appears with handover checklist. Attempt second scan: immediately rejected with *"Wrong outlet or already collected"*. | Fraud-proof, single-use, offline-tolerant QR pickup verification. |
| **01:05 - 01:20** | Vendor / Admin · TinyFish Web Sync | Vendor taps **Proposals / AI Sync** → taps **Run Sync**. TinyFish autonomous browser agent scrapes the public menu page, streams progress, flags extreme price changes (*Check this price* tag), and shows human approval (*Approve All*). | **TinyFish Autonomous Web Intelligence Agent** with Human-in-the-Loop review. |
| **01:20 - 01:30** | Mess & Admin · Counter & Analytics | Switch to Mess Operator: tap plate `PLT-0001`, tap dish tiles, tap *Finish & Bill*. Switch to Admin Overview: observe today's orders, revenue, and Orders-by-Hour peak chart. | Complete **MessFlex** plate simulation + **Campus Command** executive intelligence. |

---

## Part 2: Manual Quality Checklist (Items 1–16)

Run this checklist before every release build or evaluation:

- [x] **1. Login & Auth**:
  - Test student login (`aarav@student.tukra` / `Student@123`), vendor login (`southtiffin@vendor.tukra` / `Vendor@123`), and admin login (`admin@tukra.local` / `Admin@123`).
  - Verify role-based routing directs directly to the matching graph.
- [x] **2. Order Single Dish**:
  - Add single Masala Dosa, pick slot, pay with wallet. Order placed successfully with `TK-####` code.
- [x] **3. Item-Level Choice (ChoicePay)**:
  - Add individual custom items without bundling. Bill is strictly itemized with GST included.
- [x] **4. Vendor Sees Order with Loud Sound**:
  - In Vendor Queue, new order arrival triggers notification tone and dual vibration for the Ramesh bhaiya persona.
- [x] **5. Status Updates & Push Notification**:
  - Vendor transitions `PLACED -> ACCEPTED -> PREPARING -> READY`. Student tracking screen updates within 5s (instant with FCM).
- [x] **6. QR Pickup & Replay Prevention**:
  - Vendor verifies QR token. Order is marked `COLLECTED`. Attempting a second scan fails with 409 *"Wrong outlet or already collected"*.
- [x] **7. Slot Full Handling**:
  - When slot capacity reaches 0, chip is disabled with "Full" label. If two orders race for the last slot, server returns `SLOT_FULL` and slot sheet reopens.
- [x] **8. Low Balance -> Add Money Flow**:
  - When cart total exceeds wallet balance, checkout presents *"₹N short. Add money to place this order"*. Demo pay or Razorpay top-up completes the transaction.
- [x] **9. Cancel & Refund**:
  - Cancel order before 10-minute cutoff: order is marked `CANCELLED`, prep units are restored, and wallet balance is credited immediately.
- [x] **10. Sold-Out Toggle**:
  - Vendor toggles dish availability switch. Student menu instantly reflects greyed row with *"Sold out"* badge and disabled add button.
- [x] **11. TinyFish AI Sync End-to-End**:
  - Vendor taps *Run Sync*. Agent navigates menu, extracts prices and items, and displays pending proposals.
- [x] **12. TinyFish Failure Graceful Fallback**:
  - If a bad menu URL is configured or network drops, job marks `FAILED` with clear error message; local menu is untouched.
- [x] **13. Plate Flow Simulation**:
  - Mess Operator taps `PLT-0001`, adds dishes via touch tiles, and taps *Finish & Bill*. Student scans/types `PLT-0001` and pays from wallet. Footer displays *"Simulated smart plate session"*.
- [x] **14. Offline Browse**:
  - Enable Airplane mode: app displays cached outlets and menus with yellow *"Offline Mode — Browsing cached outlets"* banner. Checkout is safely disabled.
- [x] **15. Large Font & No Clipping (200% Accessibility)**:
  - Device system font scale set to 200%: text wraps gracefully with no text cut off or overlapping buttons.
- [x] **16. Admin Numbers Match Orders**:
  - Total orders and revenue on Admin Overview (`GET /admin/overview`) exactly equal sum of placed orders in the database.
