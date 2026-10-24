# Tukra Privacy Policy

**Effective Date:** October 24, 2026  
**Last Updated:** October 24, 2026  
**Application Name:** Tukra (टुकड़ा)  
**Package Identifier:** `com.xsparks.tukra`  
**Developer / Organization:** Xsparks Team  
**Contact:** `privacy@tukra.app`  

---

## 1. Introduction & Scope
Tukra ("we", "us", or "our") provides a campus dining and hostel mess pre-ordering system designed to eliminate queue congestion, streamline food pickup via capacity-controlled time slots, and reduce mess food waste. This Privacy Policy describes how we collect, use, process, and protect your information when you use the Tukra Android application and associated backend services.

We are committed to data minimization: **we collect only what is strictly necessary to prepare and deliver your meal, process your pickup, and maintain your campus dining wallet.**

---

## 2. Information We Collect

### A. Information You Provide
1. **Account Registration & Profile:**
   - **Full Name & Email Address:** Required for account authentication, identification, and receipt delivery.
   - **Campus Affiliation:** Your enrolled university or college campus.
   - **Hostel / Department (Optional):** Used to customize break notifications and default pickup points.
   - **Dietary Preference:** (e.g., Vegetarian, Vegan, Jain, Non-Veg) Used solely on your personal device to highlight relevant menu items and filters. **Dietary preferences are never shared with canteen vendors.**

2. **Order and Dining Activity:**
   - Items ordered, quantities, customization notes, order totals, and scheduled pickup time slots.
   - Mess plate sessions (simulated smart plate checkouts).

3. **In-App Wallet & Payments:**
   - Wallet ledger balance and transaction history (credits, order debits, refunds).
   - Razorpay transaction IDs and verification payment IDs.  
   *(Note: Tukra never collects, handles, or stores credit/debit card numbers, CVVs, or UPI banking PINs. All payment processing is handled directly by Razorpay under PCI-DSS Level 1 compliance).*

### B. Information Collected Automatically
1. **Device Tokens (Push Notifications):**
   - Firebase Cloud Messaging (FCM) registration tokens are transmitted via `POST /me/device-token` so the server can push instantaneous notifications when your order status changes (e.g., "Accepted", "Preparing", "Ready for Pickup").
2. **Camera Access (Point of Use Only):**
   - We request runtime camera permissions exclusively for scanning dynamic QR codes (verifying your pickup order at the vendor counter or linking an open mess serving plate).
   - **Ephemeral Processing:** Frames are analyzed strictly in-memory using Google ML Kit Barcode Scanning on your device. **No photos, videos, or image buffers are ever saved to disk or transmitted to our servers.**

---

## 3. How We Use Your Information
We use your information exclusively to:
- Authenticate your identity and secure your dining account.
- Transmit orders to the selected campus canteen or mess operator.
- Schedule preparation slots within kitchen capacity limits.
- Deliver single-use pickup verification tokens (`TK-####` and dynamic QR codes).
- Send real-time order status updates via push notifications.
- Aggregate campus-level dining demand analytics and food waste estimates (e.g., portions prepared vs. consumed).

---

## 4. Vendor Privacy Firewall & Data Sharing
To protect student privacy on campus:
- **What Canteen Vendors See:** Canteen operators see **only** your first name, the items ordered, cooking notes, and the 4-digit order code (`TK-####`).
- **What Canteen Vendors NEVER See:** Vendors never receive your email address, phone number, hostel room, account balance, or personal dietary flags.
- **Third-Party Disclosures:** We do not sell, rent, monetize, or trade your personal information. Data is shared with third parties only as necessary for core operations:
  - **Razorpay Software Private Limited:** For payment gateway processing.
  - **Google Firebase:** For high-priority push notification dispatch.

---

## 5. In-App Account Deletion & Right to Erasure
You have full ownership of your data. You can permanently delete your Tukra account at any time directly inside the app:
1. Open the Tukra app.
2. Tap the **Profile** icon in the bottom navigation bar.
3. Scroll to the bottom and select **"Delete my account"**.
4. Confirm your selection.

### What happens on account deletion (`DELETE /me`):
- Your personal identifiable information (Full Name, Email address, FCM device tokens, and stored credentials) is **permanently purged and anonymized** in the database.
- Active login sessions are immediately revoked.
- Any unspent wallet balances are voided or refunded per university campus policy.
- Historical order line items are retained solely in an anonymized, disassociated state for statutory campus accounting and historical mess food waste auditing.

Alternatively, you can request manual account deletion by emailing `privacy@tukra.app` with the subject "Account Deletion Request".

---

## 6. Security Standards
We implement rigorous technical safeguards to secure your personal data:
- **TLS 1.3 Encryption:** All network traffic between the Tukra Android client and backend API is encrypted over HTTPS.
- **Password Protection:** Passwords are hashed using bcrypt with a minimum work factor of 10.
- **Database Safety:** All queries use parameterised SQL to prevent injection vulnerabilities.
- **Single-Use QR Tokens:** QR codes are single-use cryptographically random tokens (`q_<hex>`) that expire upon collection to prevent screenshot reuse or fraud.
- **No Secret Logging:** Authorization tokens, API keys, and passwords are never written to server console logs.

---

## 7. Children's Privacy
Tukra is built specifically for university students, faculty, and authorized campus dining staff. We do not knowingly collect personal data from children under the age of 13.

---

## 8. Policy Updates & Contact
We may update this Privacy Policy periodically to reflect campus dining feature enhancements. If changes are material, we will provide notice via in-app banner or email notification.

If you have questions, feedback, or grievance inquiries regarding this Privacy Policy, please contact our team:
- **Email:** `privacy@tukra.app`
- **Grievance Officer:** Team Xsparks, Presidency University Campus, Bengaluru, KA, India.
