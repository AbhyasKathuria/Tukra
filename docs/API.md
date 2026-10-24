# Tukra API Specification

**Base URL:**
- Local Development (Emulator): `http://10.0.2.2:3000/`
- Local Machine: `http://localhost:3000/`
- Production (Render): `https://tukra-backend.onrender.com/`

All requests expecting or sending JSON must provide `Content-Type: application/json`.  
Authenticated requests must supply the HTTP Header:
```http
Authorization: Bearer <jwt_token>
```

---

## 1. Authentication & System Health

### `POST /auth/register`
Creates a new student or staff account. (Vendor, mess, and admin roles are provisioned by campus administrators or seeds).
- **Access:** Public
- **Request Body:**
```json
{
  "name": "Aarav Sharma",
  "email": "aarav@presidency.edu.in",
  "password": "Password123!",
  "role": "STUDENT",
  "campusId": "d0f8a848-158a-4467-bc18-289569faeb01"
}
```
- **Response `201 Created`:**
```json
{
  "token": "eyJhbGciOi...",
  "user": {
    "id": "e2a3...",
    "email": "aarav@presidency.edu.in",
    "name": "Aarav Sharma",
    "role": "STUDENT",
    "campusId": "d0f8a848-158a-4467-bc18-289569faeb01",
    "campusName": "Presidency University"
  }
}
```

### `POST /auth/login`
Authenticates existing users and returns a signed JWT.
- **Access:** Public
- **Request Body:**
```json
{
  "email": "student@tukra.local",
  "password": "Password123!"
}
```
- **Response `200 OK`:** Same structure as `/auth/register`.

### `GET /health`
System diagnostics and runtime feature flags.
- **Access:** Public
- **Response `200 OK`:**
```json
{
  "ok": true,
  "time": "2026-10-24T23:20:00.000Z",
  "features": {
    "tinyfish": true,
    "razorpay": false,
    "push": false,
    "demoPay": true
  }
}
```

---

## 2. User Profile & Device Tokens

### `GET /me`
Returns caller's full profile and campus info.
- **Access:** Authenticated (Any role)

### `PATCH /me`
Updates caller's mutable profile fields.
- **Request Body:**
```json
{
  "name": "Aarav S.",
  "hostel": "Block B, Room 304",
  "dietPreference": "VEG"
}
```

### `DELETE /me`
Performs in-app account deletion under privacy compliance. Immediately anonymizes all personal identifiable information (PII) while preserving disassociated order aggregates for university auditing.
- **Response `200 OK`:**
```json
{
  "ok": true,
  "message": "Account successfully anonymized and deleted"
}
```

### `POST /me/device-token`
Saves or updates the caller's Firebase Cloud Messaging (FCM) registration token for push alerts.
- **Request Body:**
```json
{
  "token": "fcm_token_string..."
}
```

---

## 3. Campus, Outlets & Menus

### `GET /campus`
Returns active campus metadata, operating hours, and scheduled break periods.
- **Response `200 OK`:**
```json
{
  "campus": {
    "id": "d0f8a848-158a-4467-bc18-289569faeb01",
    "name": "Presidency University",
    "code": "PU-BLR",
    "breakTimes": [
      { "name": "Morning Break", "start": "11:15", "end": "11:35" },
      { "name": "Lunch Break", "start": "13:00", "end": "14:00" },
      { "name": "Evening Tea", "start": "16:15", "end": "16:45" }
    ]
  }
}
```

### `GET /outlets`
Returns all dining outlets and messes on the caller's campus, with live open status, slot capacities, and estimated queue wait in minutes (`null` = Busy/Full).

### `GET /outlets/:id/menu?diet=`
Returns menu items for an outlet grouped with category names. Supports optional `?diet=VEG|NON_VEG|EGG|VEGAN` filter.

### `GET /outlets/:id/slots?units=N&longestPrepSeconds=S`
Returns up to 24 feasible 5-minute pickup slots within today (IST). Evaluates kitchen prep time, open hours, and slot remaining capacity.

---

## 4. Ordering & Pickups

### `POST /orders`
Creates a prepaid dining order in an atomic database transaction.
- **Access:** `STUDENT`, `STAFF`
- **Request Body:**
```json
{
  "outletId": "3b2c1a00-...",
  "slotId": "f7d8c9...",
  "items": [
    { "menuItemId": "001122...", "qty": 1, "notes": "Extra chutney please" }
  ],
  "note": "Less spicy",
  "paymentMethod": "WALLET"
}
```
- **Response `201 Created` (Flat Order DTO):**
```json
{
  "id": "8f9a0b1c-...",
  "code": "TK-4821",
  "status": "PLACED",
  "totalPaise": 8500,
  "qrToken": "q_7a8b9c0d1e2f3a4b",
  "slotStartsAt": "2026-10-30T07:15:00.000Z",
  "outletName": "South Tiffin",
  "note": "Less spicy",
  "items": [
    { "name": "Masala Dosa", "qty": 1, "unitPricePaise": 8500 }
  ]
}
```

### `GET /orders?status=active|past`
Returns caller's active or completed orders, newest first.

### `GET /orders/:id`
Retrieves a single order by ID (accessible by student owner or outlet vendor).

### `POST /orders/:id/cancel`
Cancels an order and immediately refunds the full amount to the student's in-app wallet.
- **Constraints:** Allowed only when status is `PLACED` or `ACCEPTED` and more than 10 minutes prior to slot start time.

---

## 5. Wallet & Razorpay

### `GET /wallet`
Returns student wallet balance in paise and the last 50 transactions (`TOPUP`, `ORDER`, `REFUND`, `PLATE`).

### `POST /wallet/topup/order`
Initiates a Razorpay payment order for wallet top-up (₹10 to ₹5,000).
- **Request Body:** `{ "amountPaise": 20000 }`
- **Response `200 OK`:** `{ "orderId": "order_...", "amountPaise": 20000, "currency": "INR", "keyId": "rzp_test_..." }`

### `POST /wallet/topup/verify`
Verifies cryptographic HMAC-SHA256 signature from Razorpay and credits wallet once.
- **Request Body:**
```json
{
  "razorpayOrderId": "order_...",
  "razorpayPaymentId": "pay_...",
  "razorpaySignature": "hex_signature..."
}
```

### `POST /wallet/topup/demo`
Direct test top-up for demo & evaluation. Allowed only when `ENABLE_DEMO_PAY=true`.
- **Request Body:** `{ "amountPaise": 50000 }`

---

## 6. Canteen Vendor Operations

### `GET /vendor/orders?status=`
Returns live queue for vendor's outlet filtered by status (`NEW`, `PREPARING`, `READY`).  
*Privacy Protection:* Student email and phone are omitted; only first name and `TK-####` code are returned.

### `PATCH /vendor/orders/:id/status`
Advances an order through the server-enforced state machine:
`PLACED` → `ACCEPTED` → `PREPARING` → `READY`.  
Or rejects an order (`PLACED`/`ACCEPTED` → `REJECTED` with `rejectReason`), which triggers an automatic wallet refund.

### `POST /vendor/orders/verify-qr`
Validates a student's pickup QR token.
- **Request Body:** `{ "token": "q_7a8b9c0d1e2f3a4b" }`
- **Response `200 OK`:** Transitions order to `COLLECTED` and sets `collected_at`. A second scan with the same token returns `409 Conflict`.

### `GET /vendor/items` & `PATCH /vendor/items/:id`
Lists and manages outlet inventory (instantly toggle `isAvailable` or update prices).

### `GET /vendor/outlet` & `PATCH /vendor/outlet`
Manages outlet operating status, 5-minute slot capacity (1–50 units), and order cutoff time.

---

## 7. TinyFish AI Menu Intelligence

### `POST /vendor/sync`
Initiates an autonomous TinyFish web agent run against the outlet's configured `source_menu_url`.
- **Response `202 Accepted`:** `{ "jobId": "uuid..." }`

### `GET /vendor/sync/:jobId`
Returns real-time execution status, progress message, streaming URL for the live browser session, and count of proposed diffs.

### `GET /vendor/proposals`
Lists pending menu change proposals (`NEW`, `PRICE_CHANGE`, `REMOVED`, `AVAILABILITY`). Extreme price variations (> 3× or < 0.33×) are tagged with `flagged: true`.

### `POST /vendor/proposals/:id/decision`
Approves or rejects a proposed menu diff. When approved, updates live menu within a single transaction.

---

## 8. Hostel Mess & Smart Plates

### `GET /mess/menu?date=&meal=` & `POST /mess/menu`
Views and publishes mess meals (*Breakfast*, *Lunch*, *Snacks*, *Dinner*) and planned portions.

### `POST /mess/reservations`
Allows students to pre-book meal items, driving accurate mess demand forecasting.

### `GET /mess/forecast`
Returns predictive preparation quantity per dish calculated using a 4-week same-weekday rolling average plus actual student reservations. Labeled `"Estimate"` in the UI.

### `POST /plate/open` & `POST /plate/link`
Opens a serving session for a physical plate (`PLT-0001`…`PLT-0010`) and links the student's phone.

### `POST /plate/:id/items` & `POST /plate/:id/finish`
Mess operator taps dish tiles to add items; finishing moves session from `OPEN` to `BILLED`.

### `POST /plate/:id/pay`
Student reviews itemized bill and pays via wallet debit in a single atomic transaction, transitioning session to `PAID`.

---

## 9. Campus Admin Command

### `GET /admin/overview`
High-level KPIs: Orders today, total revenue, active outlets, average wait time, and orders-by-hour bar chart data.

### `GET /admin/demand`
Itemized dish consumption breakdown and food waste audit table (`BALANCED` vs `HIGH_SURPLUS`).

### `GET /admin/outlets`
Campus outlet operational status, slot capacity utilization, and last TinyFish sync timestamps.

### `POST /admin/slots/generate`
Manual trigger to generate 5-minute capacity slots for all active outlets.
