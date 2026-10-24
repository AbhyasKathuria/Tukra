-- Idempotent Realistic Seed Data for Tukra
-- Campus: Presidency University, Bengaluru

BEGIN;

-- 1. Campus
INSERT INTO campuses (id, name, city, break_times)
VALUES (
  'c1111111-1111-1111-1111-111111111111',
  'Presidency University, Bengaluru',
  'Bengaluru',
  '[["10:30","10:50"],["13:00","13:45"]]'::jsonb
) ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  city = EXCLUDED.city,
  break_times = EXCLUDED.break_times;

-- 2. Users (Password for all demo users: demo1234)
-- Bcrypt hash: $2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy
INSERT INTO users (id, campus_id, name, email, phone, password_hash, role, hostel, diet_pref)
VALUES
  -- Student with ₹500 wallet
  ('u1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111',
   'Aarav Sharma', 'aarav@presidency.ac.in', '+919876543210',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STUDENT', 'Nilgiri Block A', 'ANY'),

  -- Student with ₹0 wallet
  ('u2222222-2222-2222-2222-222222222222', 'c1111111-1111-1111-1111-111111111111',
   'Priya Patel', 'priya@presidency.ac.in', '+919876543211',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STUDENT', 'Cauvery Block B', 'VEG'),

  -- Staff with ₹1,000 wallet
  ('u3333333-3333-3333-3333-333333333333', 'c1111111-1111-1111-1111-111111111111',
   'Dr. Meena Rao', 'meena.sharma@presidency.ac.in', '+919876543212',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STAFF', 'Staff Quarters D-4', 'VEG'),

  -- Vendors
  ('u4444444-4444-4444-4444-444444444444', 'c1111111-1111-1111-1111-111111111111',
   'Ramesh Bhaiya', 'ramesh@southtiffin.com', '+919876543213',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'VENDOR', NULL, 'ANY'),

  ('u5555555-5555-5555-5555-555555555555', 'c1111111-1111-1111-1111-111111111111',
   'Suresh Gupta', 'suresh@chaatcorner.com', '+919876543214',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'VENDOR', NULL, 'ANY'),

  ('u6666666-6666-6666-6666-666666666666', 'c1111111-1111-1111-1111-111111111111',
   'Imran Khan', 'imran@juicejunction.com', '+919876543215',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'VENDOR', NULL, 'ANY'),

  -- Mess Operator
  ('u7777777-7777-7777-7777-777777777777', 'c1111111-1111-1111-1111-111111111111',
   'Rajesh Nair', 'mess@presidency.ac.in', '+919876543216',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'MESS_OPERATOR', 'Central Mess Office', 'ANY'),

  -- Admin
  ('u8888888-8888-8888-8888-888888888888', 'c1111111-1111-1111-1111-111111111111',
   'Ms. Kaur', 'admin@presidency.ac.in', '+919876543217',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'ADMIN', 'Dean Student Affairs', 'ANY')
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  password_hash = EXCLUDED.password_hash,
  role = EXCLUDED.role;

-- 3. Wallets
INSERT INTO wallets (user_id, balance_paise)
VALUES
  ('u1111111-1111-1111-1111-111111111111', 50000), -- Aarav: ₹500
  ('u2222222-2222-2222-2222-222222222222', 0),     -- Priya: ₹0
  ('u3333333-3333-3333-3333-333333333333', 100000) -- Dr. Meena: ₹1,000
ON CONFLICT (user_id) DO UPDATE SET balance_paise = EXCLUDED.balance_paise;

-- Initial Top-up Transactions
INSERT INTO wallet_txns (user_id, type, amount_paise, note)
VALUES
  ('u1111111-1111-1111-1111-111111111111', 'TOPUP', 50000, 'Welcome student demo balance'),
  ('u3333333-3333-3333-3333-333333333333', 'TOPUP', 100000, 'Staff monthly dining credit')
ON CONFLICT DO NOTHING;

-- 4. Outlets
INSERT INTO outlets (id, campus_id, owner_user_id, name, type, location_note, is_open, slot_minutes, slot_capacity, opens_at, accepts_orders_until, source_menu_url)
VALUES
  ('o1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111', 'u4444444-4444-4444-4444-444444444444',
   'South Tiffin', 'CANTEEN', 'Ground Floor, Academic Block 1', true, 5, 12, '08:00', '17:00', 'https://south-tiffin-menu.example.com'),

  ('o2222222-2222-2222-2222-222222222222', 'c1111111-1111-1111-1111-111111111111', 'u5555555-5555-5555-5555-555555555555',
   'Chaat Corner', 'SNACKS', 'Student Centre Amphitheatre', true, 5, 15, '11:00', '19:00', NULL),

  ('o3333333-3333-3333-3333-333333333333', 'c1111111-1111-1111-1111-111111111111', 'u6666666-6666-6666-6666-666666666666',
   'Juice Junction', 'JUICE', 'Near Sports Complex', true, 5, 10, '09:00', '18:00', NULL),

  ('o4444444-4444-4444-4444-444444444444', 'c1111111-1111-1111-1111-111111111111', 'u7777777-7777-7777-7777-777777777777',
   'Hostel Mess', 'MESS', 'Central Dining Hall', true, 5, 20, '07:30', '21:00', NULL)
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  is_open = EXCLUDED.is_open,
  opens_at = EXCLUDED.opens_at,
  accepts_orders_until = EXCLUDED.accepts_orders_until;

-- 5. Categories
INSERT INTO categories (id, outlet_id, name, sort_order)
VALUES
  -- South Tiffin Categories
  ('cat11111-1111-1111-1111-111111111111', 'o1111111-1111-1111-1111-111111111111', 'Tiffin & Dosas', 1),
  ('cat11111-1111-1111-1111-111111111112', 'o1111111-1111-1111-1111-111111111111', 'Combos & Meals', 2),
  ('cat11111-1111-1111-1111-111111111113', 'o1111111-1111-1111-1111-111111111111', 'Beverages', 3),

  -- Chaat Corner Categories
  ('cat22222-2222-2222-2222-222222222221', 'o2222222-2222-2222-2222-222222222222', 'Crispy Chaat', 1),
  ('cat22222-2222-2222-2222-222222222222', 'o2222222-2222-2222-2222-222222222222', 'Hot Snacks', 2),

  -- Juice Junction Categories
  ('cat33333-3333-3333-3333-333333333331', 'o3333333-3333-3333-3333-333333333333', 'Fresh Fruit Juices', 1),
  ('cat33333-3333-3333-3333-333333333332', 'o3333333-3333-3333-3333-333333333333', 'Shakes & Lassi', 2),

  -- Hostel Mess Categories
  ('cat44444-4444-4444-4444-444444444441', 'o4444444-4444-4444-4444-444444444444', 'Thali & Meals', 1),
  ('cat44444-4444-4444-4444-444444444442', 'o4444444-4444-4444-4444-444444444444', 'A La Carte Dishes', 2),
  ('cat44444-4444-4444-4444-444444444443', 'o4444444-4444-4444-4444-444444444444', 'Desserts', 3)
ON CONFLICT (id) DO NOTHING;

-- 6. Menu Items
-- South Tiffin Items
INSERT INTO menu_items (outlet_id, category_id, name, description, price_paise, is_veg, prep_seconds, prep_units, is_available, spice_level)
VALUES
  ('o1111111-1111-1111-1111-111111111111', 'cat11111-1111-1111-1111-111111111111', 'Masala Dosa', 'Golden crispy crepe filled with spiced potato masala and served with 2 chutneys & sambar', 7000, true, 180, 2, true, 1),
  ('o1111111-1111-1111-1111-111111111111', 'cat11111-1111-1111-1111-111111111111', 'Idli Vada Combo', '2 steamed fluffy idlis and 1 crispy medu vada with coconut chutney and hot drumstick sambar', 5500, true, 120, 1, true, 0),
  ('o1111111-1111-1111-1111-111111111111', 'cat11111-1111-1111-1111-111111111111', 'Onion Uttapam', 'Thick pancake topped with caramelized red onions, curry leaves, and green chillies', 6500, true, 240, 2, true, 1),
  ('o1111111-1111-1111-1111-111111111111', 'cat11111-1111-1111-1111-111111111111', 'Poori Sagu', '3 golden puffed pooris served with mild Karnataka vegetable sagu', 6000, true, 180, 2, true, 1),
  ('o1111111-1111-1111-1111-111111111111', 'cat11111-1111-1111-1111-111111111112', 'Chole Bhature', '2 giant bhature served with rich Delhi-style pindi chole and pickled onions', 9000, true, 300, 3, true, 2),
  ('o1111111-1111-1111-1111-111111111111', 'cat11111-1111-1111-1111-111111111112', 'Curd Rice', 'Tempered yogurt rice with mustard seeds, pomegranate, and ginger', 5000, true, 60, 1, true, 0),
  ('o1111111-1111-1111-1111-111111111111', 'cat11111-1111-1111-1111-111111111113', 'Filter Coffee', 'Authentic South Indian chicory-brewed filter coffee with frothy hot milk', 2500, true, 60, 1, true, 0),
  ('o1111111-1111-1111-1111-111111111111', 'cat11111-1111-1111-1111-111111111113', 'Rava Kesari', 'Warm semolina pudding flavoured with saffron, ghee, and cashew nuts', 4000, true, 90, 1, true, 0)
ON CONFLICT DO NOTHING;

-- Chaat Corner Items
INSERT INTO menu_items (outlet_id, category_id, name, description, price_paise, is_veg, prep_seconds, prep_units, is_available, spice_level)
VALUES
  ('o2222222-2222-2222-2222-222222222222', 'cat22222-2222-2222-2222-222222222221', 'Pani Puri (6 pcs)', 'Crispy puris stuffed with potato-chickpea mix and spicy mint water', 4000, true, 90, 1, true, 2),
  ('o2222222-2222-2222-2222-222222222222', 'cat22222-2222-2222-2222-222222222221', 'Sev Puri', 'Flat puris loaded with diced potatoes, onions, trio of chutneys, and fine sev', 5000, true, 120, 1, true, 1),
  ('o2222222-2222-2222-2222-222222222222', 'cat22222-2222-2222-2222-222222222221', 'Dahi Puri', 'Puris filled with sweetened chilled yogurt, tamarind date chutney, and sev', 6000, true, 150, 2, true, 1),
  ('o2222222-2222-2222-2222-222222222222', 'cat22222-2222-2222-2222-222222222222', 'Pav Bhaji', 'Spiced buttery mashed vegetable curry with 2 butter-toasted soft pavs', 8000, true, 240, 2, true, 2),
  ('o2222222-2222-2222-2222-222222222222', 'cat22222-2222-2222-2222-222222222222', 'Vada Pav', 'Mumbai batata vada sandwiched in chutney pav with fried green chilli', 3000, true, 60, 1, true, 2),
  ('o2222222-2222-2222-2222-222222222222', 'cat22222-2222-2222-2222-222222222222', 'Samosa Chaat', 'Crushed samosas topped with hot chole gravy, sweetened yogurt, and sev', 5500, true, 120, 1, true, 2),
  ('o2222222-2222-2222-2222-222222222222', 'cat22222-2222-2222-2222-222222222222', 'Masala Maggi', 'Campus classic 2-minute noodles tossed with butter, onions, peas, and extra masala', 4500, true, 180, 1, true, 2)
ON CONFLICT DO NOTHING;

-- Juice Junction Items
INSERT INTO menu_items (outlet_id, category_id, name, description, price_paise, is_veg, prep_seconds, prep_units, is_available, spice_level)
VALUES
  ('o3333333-3333-3333-3333-333333333333', 'cat33333-3333-3333-3333-333333333331', 'Sweet Lime (Mosambi) Juice', 'Freshly pressed cold sweet lime juice with black salt and cumin hint', 5000, true, 120, 1, true, 0),
  ('o3333333-3333-3333-3333-333333333333', 'cat33333-3333-3333-3333-333333333331', 'Watermelon Juice', '100% natural pure chilled watermelon juice without added sugar', 4000, true, 60, 1, true, 0),
  ('o3333333-3333-3333-3333-333333333333', 'cat33333-3333-3333-3333-333333333332', 'Mango Lassi', 'Rich thick yogurt blended with Alphonso mango pulp and cardamom', 6000, true, 90, 1, true, 0),
  ('o3333333-3333-3333-3333-333333333333', 'cat33333-3333-3333-3333-333333333332', 'Cold Coffee with Ice Cream', 'Creamy blended cold espresso poured over a scoop of vanilla ice cream', 7500, true, 150, 2, true, 0),
  ('o3333333-3333-3333-3333-333333333333', 'cat33333-3333-3333-3333-333333333332', 'Oreo Shake', 'Vanilla ice cream and whole Oreo biscuits blended with chilled milk', 7000, true, 180, 2, true, 0),
  ('o3333333-3333-3333-3333-333333333333', 'cat33333-3333-3333-3333-333333333332', 'Badam Milk', 'Saffron-infused warm or chilled almond milk with crushed pistachios', 4500, true, 60, 1, true, 0)
ON CONFLICT DO NOTHING;

-- Hostel Mess Items
INSERT INTO menu_items (outlet_id, category_id, name, description, price_paise, is_veg, prep_seconds, prep_units, is_available, spice_level)
VALUES
  ('o4444444-4444-4444-4444-444444444444', 'cat44444-4444-4444-4444-444444444441', 'Deluxe Lunch Thali', 'Dal makhani, paneer subzi, seasonal dry vegetable, 3 rotis, steamed rice, salad & sweet', 9500, true, 120, 2, true, 1),
  ('o4444444-4444-4444-4444-444444444444', 'cat44444-4444-4444-4444-444444444441', 'Rajma Chawal Bowl', 'Slow-cooked Jammu rajma in rich tomato gravy served over fragrant basmati rice', 6500, true, 90, 1, true, 1),
  ('o4444444-4444-4444-4444-444444444444', 'cat44444-4444-4444-4444-444444444442', 'Paneer Butter Masala', 'Soft cottage cheese cubes in buttery creamy tomato gravy', 8500, true, 180, 2, true, 1),
  ('o4444444-4444-4444-4444-444444444444', 'cat44444-4444-4444-4444-444444444442', 'Egg Curry with Jeera Rice', '2 boiled fried eggs in homestyle onion-tomato gravy with jeera rice', 8000, false, 150, 2, true, 2),
  ('o4444444-4444-4444-4444-444444444444', 'cat44444-4444-4444-4444-444444444442', 'Chicken Biryani (Special)', 'Dum-cooked chicken biryani with spiced hard-boiled egg and mirchi ka salan', 13000, false, 240, 3, true, 2),
  ('o4444444-4444-4444-4444-444444444444', 'cat44444-4444-4444-4444-444444444442', 'Aloo Paratha with Curd', 'Whole wheat tandoori paratha stuffed with spicy potatoes, served with fresh curd & pickle', 5000, true, 150, 2, true, 1),
  ('o4444444-4444-4444-4444-444444444444', 'cat44444-4444-4444-4444-444444444443', 'Gulab Jamun (2 pcs)', 'Warm milk dumplings soaked in aromatic rose-cardamom sugar syrup', 3500, true, 30, 1, true, 0)
ON CONFLICT DO NOTHING;

COMMIT;
