-- Tukra Database Schema (Part E)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS campuses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  city TEXT NOT NULL,
  break_times JSONB NOT NULL DEFAULT '[["10:30","10:50"],["13:00","13:45"]]'   -- IST, used by the Home break banner
);

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  campus_id UUID NOT NULL REFERENCES campuses(id),
  name TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  phone TEXT,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('STUDENT','STAFF','VENDOR','MESS_OPERATOR','ADMIN')),
  hostel TEXT,
  diet_pref TEXT NOT NULL DEFAULT 'ANY' CHECK (diet_pref IN ('ANY','VEG','JAIN','EGG')),
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS outlets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  campus_id UUID NOT NULL REFERENCES campuses(id),
  owner_user_id UUID REFERENCES users(id),
  name TEXT NOT NULL,
  type TEXT NOT NULL CHECK (type IN ('CANTEEN','MESS','CAFE','JUICE','SNACKS')),
  location_note TEXT,
  phone TEXT,
  cover_image_url TEXT,
  source_menu_url TEXT,
  is_open BOOLEAN NOT NULL DEFAULT true,
  slot_minutes INT NOT NULL DEFAULT 5,
  slot_capacity INT NOT NULL DEFAULT 12,
  opens_at TIME NOT NULL DEFAULT '08:00',
  accepts_orders_until TIME NOT NULL DEFAULT '17:00',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_outlets_campus ON outlets(campus_id);

CREATE TABLE IF NOT EXISTS categories (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS menu_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
  category_id UUID REFERENCES categories(id),
  name TEXT NOT NULL,
  description TEXT,
  price_paise INT NOT NULL CHECK (price_paise >= 0),
  is_veg BOOLEAN NOT NULL DEFAULT true,
  image_url TEXT,
  prep_seconds INT NOT NULL DEFAULT 180,
  prep_units INT NOT NULL DEFAULT 1 CHECK (prep_units > 0),
  is_available BOOLEAN NOT NULL DEFAULT true,
  spice_level INT CHECK (spice_level BETWEEN 0 AND 3),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_menu_items_outlet ON menu_items(outlet_id);

CREATE TABLE IF NOT EXISTS slots (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL,
  capacity INT NOT NULL,
  used INT NOT NULL DEFAULT 0 CHECK (used >= 0),
  UNIQUE (outlet_id, starts_at)
);
CREATE INDEX IF NOT EXISTS idx_slots_outlet_start ON slots(outlet_id, starts_at);

CREATE TABLE IF NOT EXISTS orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code TEXT UNIQUE NOT NULL,                       -- 'TK-4821'
  user_id UUID NOT NULL REFERENCES users(id),
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  slot_id UUID NOT NULL REFERENCES slots(id),
  status TEXT NOT NULL DEFAULT 'PLACED' CHECK (status IN
    ('PLACED','ACCEPTED','PREPARING','READY','COLLECTED','CANCELLED','REJECTED','NO_SHOW')),
  subtotal_paise INT NOT NULL,
  total_paise INT NOT NULL,
  prep_units INT NOT NULL,                         -- Needed to release slot capacity on cancel/reject
  payment_method TEXT NOT NULL CHECK (payment_method IN ('WALLET','UPI','DEMO','MESS_PLAN')),
  note TEXT,
  qr_token TEXT UNIQUE NOT NULL,
  reject_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ready_at TIMESTAMPTZ,
  collected_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_outlet_status ON orders(outlet_id, status);

CREATE TABLE IF NOT EXISTS order_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  menu_item_id UUID NOT NULL REFERENCES menu_items(id),
  name_snapshot TEXT NOT NULL,
  unit_price_paise INT NOT NULL,
  qty INT NOT NULL CHECK (qty > 0)
);

CREATE TABLE IF NOT EXISTS wallets (
  user_id UUID PRIMARY KEY REFERENCES users(id),
  balance_paise INT NOT NULL DEFAULT 0 CHECK (balance_paise >= 0)
);

CREATE TABLE IF NOT EXISTS wallet_txns (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  type TEXT NOT NULL CHECK (type IN ('TOPUP','ORDER','REFUND','ADJUST','PLATE')),
  amount_paise INT NOT NULL,                       -- + credit, − debit
  ref_order_id UUID REFERENCES orders(id),
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  razorpay_order_id TEXT UNIQUE NOT NULL,
  razorpay_payment_id TEXT,
  amount_paise INT NOT NULL,
  status TEXT NOT NULL DEFAULT 'CREATED' CHECK (status IN ('CREATED','PAID','FAILED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS mess_menus (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  service_date DATE NOT NULL,
  meal TEXT NOT NULL CHECK (meal IN ('BREAKFAST','LUNCH','SNACKS','DINNER')),
  menu_item_id UUID NOT NULL REFERENCES menu_items(id),
  planned_qty INT,
  UNIQUE (outlet_id, service_date, meal, menu_item_id)
);

CREATE TABLE IF NOT EXISTS mess_reservations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  mess_menu_id UUID NOT NULL REFERENCES mess_menus(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, mess_menu_id)
);

CREATE TABLE IF NOT EXISTS plate_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  plate_code TEXT NOT NULL,
  user_id UUID REFERENCES users(id),
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  status TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','BILLED','PAID')),
  total_paise INT NOT NULL DEFAULT 0,
  opened_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  paid_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_plate_open ON plate_sessions(plate_code) WHERE status IN ('OPEN','BILLED');

CREATE TABLE IF NOT EXISTS plate_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  plate_session_id UUID NOT NULL REFERENCES plate_sessions(id) ON DELETE CASCADE,
  menu_item_id UUID NOT NULL REFERENCES menu_items(id),
  qty INT NOT NULL DEFAULT 1,
  unit_price_paise INT NOT NULL,
  served_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS menu_sync_jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  tinyfish_run_id TEXT,
  status TEXT NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING','COMPLETED','FAILED')),
  streaming_url TEXT,
  progress TEXT,
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  finished_at TIMESTAMPTZ,
  num_steps INT,
  error_message TEXT
);

CREATE TABLE IF NOT EXISTS menu_proposals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  job_id UUID NOT NULL REFERENCES menu_sync_jobs(id) ON DELETE CASCADE,
  outlet_id UUID NOT NULL REFERENCES outlets(id),
  change_type TEXT NOT NULL CHECK (change_type IN ('NEW','PRICE_CHANGE','REMOVED','AVAILABILITY')),
  menu_item_id UUID REFERENCES menu_items(id),
  item_name TEXT NOT NULL,
  old_price_paise INT,
  new_price_paise INT,
  flagged BOOLEAN NOT NULL DEFAULT false,          -- Sanity check flag ("Check this price")
  status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
  decided_by UUID REFERENCES users(id),
  decided_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS device_tokens (
  user_id UUID NOT NULL REFERENCES users(id),
  token TEXT NOT NULL,
  PRIMARY KEY (user_id, token)
);
