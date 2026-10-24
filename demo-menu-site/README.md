# South Tiffin Demo Menu Site

This directory contains a clean, fast public menu page representing a campus canteen's official webpage.
The **TinyFish Campus Dining Intelligence Agent** scans this page autonomously to extract food items and prices for vendor review.

---

## 1. Hosting Instructions (Free & Instant)

### Option A: GitHub Pages
1. In your GitHub repository settings for `Tukra`, navigate to **Settings > Pages**.
2. Under **Build and deployment > Branch**, select `main` and set folder to `/demo-menu-site`.
3. Save. Your live public URL will be:
   `https://<your-username>.github.io/Tukra/`
4. Set this URL as `source_menu_url` for South Tiffin in your database or `.env`:
   ```env
   DEMO_MENU_URL=https://<your-username>.github.io/Tukra/
   ```

### Option B: Vercel / Netlify
1. Drag and drop the `demo-menu-site` folder into [Vercel](https://vercel.com) or [Netlify Drop](https://app.netlify.com/drop).
2. Copy the resulting production `.vercel.app` or `.netlify.app` URL into `DEMO_MENU_URL`.

---

## 2. The 60-Second "TinyFish Moment" (Live Pitch Demo)

Follow these exact steps during your hackathon presentation:

1. **Show the Live Canteen Page:**
   Open the demo page in a browser showing *Masala Dosa* at `₹70`.
2. **Make the Live Edit on the Website:**
   Edit `demo-menu-site/index.html`:
   * Change *Masala Dosa* price from `₹70` to `₹80`.
   * Add a new dish under *Tiffin & Mains*:
     ```html
     <div class="menu-card">
       <div class="item-info">
         <div class="item-title-row">
           <span class="veg-mark"><span class="veg-dot"></span></span>
           <span class="item-name">Rava Idli</span>
         </div>
         <p class="item-desc">Steamed semolina cakes spiced with mustard, cashews and ginger</p>
       </div>
       <div class="price-col">
         <div class="price">₹50</div>
       </div>
     </div>
     ```
   * Commit & push, or re-deploy.
3. **Trigger TinyFish Agent in Tukra:**
   In the Vendor app, tap **Sync menu** (or `POST /vendor/sync`).
4. **Watch Live Execution:**
   Tap **Watch live** to stream the TinyFish browser in real time.
5. **Human Approval Gate:**
   Tukra detects:
   * `PRICE_CHANGE: Masala Dosa ₹70 ➔ ₹80`
   * `NEW: Rava Idli ₹50`
   Vendor taps **Approve all**.
6. **Student Verification:**
   Student pulls to refresh their Tukra app. The new price and new dish appear immediately.
   *Pitch line: "No vendor spent time typing or configuring menus. The TinyFish agent extracted the update from the canteen's website, and human approval kept the menu 100% safe."*
