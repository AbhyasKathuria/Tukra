import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const seedPath = path.join(__dirname, '..', 'sql', 'seed.sql');

let content = fs.readFileSync(seedPath, 'utf8');

const userSection = `INSERT INTO users (id, campus_id, name, email, phone, password_hash, role, hostel, diet_pref, roll_number, department, degree_year, room_number, rfid_card_id, green_credits, clean_streak_days, food_saved_kg)
VALUES
  -- Student with ₹500 wallet
  ('a1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111',
   'Aarav Sharma', 'aarav@presidency.ac.in', '+919876543210',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STUDENT', 'Nilgiri Block A', 'ANY',
   'PU2023CS0101', 'Computer Science & Engineering', 'B.Tech (3rd Year, Sem 5)', 'Room 304', 'PU-RFID-883921', 420, 14, 4.20),

  -- Student with ₹0 wallet
  ('a2222222-2222-2222-2222-222222222222', 'c1111111-1111-1111-1111-111111111111',
   'Priya Patel', 'priya@presidency.ac.in', '+919876543211',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STUDENT', 'Cauvery Block B', 'VEG',
   'PU2023CS0102', 'Computer Science & Engineering', 'B.Tech (3rd Year, Sem 5)', 'Room 212', 'PU-RFID-883922', 280, 9, 2.80),

  -- Staff with ₹1,000 wallet
  ('a3333333-3333-3333-3333-333333333333', 'c1111111-1111-1111-1111-111111111111',
   'Dr. Meena Rao', 'meena.sharma@presidency.ac.in', '+919876543212',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STAFF', 'Staff Quarters D-4', 'VEG',
   'PU-STAFF-104', 'Biotechnology Department', 'Professor & HOD', 'Office D-4', 'PU-RFID-771021', 600, 30, 8.50),

  -- Vendors
  ('a4444444-4444-4444-4444-444444444444', 'c1111111-1111-1111-1111-111111111111',
   'Ramesh Bhaiya', 'ramesh@southtiffin.com', '+919876543213',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'VENDOR', NULL, 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),

  ('a5555555-5555-5555-5555-555555555555', 'c1111111-1111-1111-1111-111111111111',
   'Suresh Gupta', 'suresh@chaatcorner.com', '+919876543214',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'VENDOR', NULL, 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),

  ('a6666666-6666-6666-6666-666666666666', 'c1111111-1111-1111-1111-111111111111',
   'Imran Khan', 'imran@juicejunction.com', '+919876543215',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'VENDOR', NULL, 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),

  -- Mess Operator
  ('a7777777-7777-7777-7777-777777777777', 'c1111111-1111-1111-1111-111111111111',
   'Rajesh Nair', 'mess@presidency.ac.in', '+919876543216',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'MESS_OPERATOR', 'Central Mess Office', 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),

  -- Admin
  ('a8888888-8888-8888-8888-888888888888', 'c1111111-1111-1111-1111-111111111111',
   'Ms. Kaur', 'admin@presidency.ac.in', '+919876543217',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'ADMIN', 'Dean Student Affairs', 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),

  -- Student Aliases
  ('a1111111-1111-1111-1111-111111111112', 'c1111111-1111-1111-1111-111111111111',
   'Aarav Sharma', 'aarav@presidency.edu.in', '+919876543210',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STUDENT', 'Nilgiri Block A', 'ANY',
   'PU2023CS0101', 'Computer Science & Engineering', 'B.Tech (3rd Year, Sem 5)', 'Room 304', 'PU-RFID-883921', 420, 14, 4.20),
  ('a1111111-1111-1111-1111-111111111113', 'c1111111-1111-1111-1111-111111111111',
   'Demo Student', 'student@tukra.local', '+919876543210',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STUDENT', 'Nilgiri Block A', 'ANY',
   'PU2023CS0101', 'Computer Science & Engineering', 'B.Tech (3rd Year, Sem 5)', 'Room 304', 'PU-RFID-883921', 420, 14, 4.20),

  -- Vendor Aliases
  ('a4444444-4444-4444-4444-444444444445', 'c1111111-1111-1111-1111-111111111111',
   'Ramesh Bhaiya', 'southtiffin@vendor.tukra', '+919876543213',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'VENDOR', NULL, 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),
  ('a4444444-4444-4444-4444-444444444446', 'c1111111-1111-1111-1111-111111111111',
   'Ramesh Bhaiya', 'vendor@tukra.local', '+919876543213',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'VENDOR', NULL, 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),

  -- Mess Aliases
  ('a7777777-7777-7777-7777-777777777778', 'c1111111-1111-1111-1111-111111111111',
   'Rajesh Nair', 'mess@presidency.edu.in', '+919876543216',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'MESS_OPERATOR', 'Central Mess Office', 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),
  ('a7777777-7777-7777-7777-777777777779', 'c1111111-1111-1111-1111-111111111111',
   'Rajesh Nair', 'mess@tukra.local', '+919876543216',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'MESS_OPERATOR', 'Central Mess Office', 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),

  -- Admin Aliases
  ('a8888888-8888-8888-8888-888888888889', 'c1111111-1111-1111-1111-111111111111',
   'Ms. Kaur', 'admin@presidency.edu.in', '+919876543217',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'ADMIN', 'Dean Student Affairs', 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0),
  ('a8888888-8888-8888-8888-888888888890', 'c1111111-1111-1111-1111-111111111111',
   'Ms. Kaur', 'admin@tukra.local', '+919876543217',
   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'ADMIN', 'Dean Student Affairs', 'ANY',
   NULL, NULL, NULL, NULL, NULL, 0, 0, 0)
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  password_hash = EXCLUDED.password_hash,
  role = EXCLUDED.role,
  roll_number = EXCLUDED.roll_number,
  department = EXCLUDED.department,
  degree_year = EXCLUDED.degree_year,
  room_number = EXCLUDED.room_number,
  rfid_card_id = EXCLUDED.rfid_card_id,
  green_credits = EXCLUDED.green_credits,
  clean_streak_days = EXCLUDED.clean_streak_days,
  food_saved_kg = EXCLUDED.food_saved_kg;`;

// Replace from INSERT INTO users up to ON CONFLICT...;
content = content.replace(/INSERT INTO users[\s\S]+?ON CONFLICT \(id\) DO UPDATE SET[\s\S]+?;/, userSection);

fs.writeFileSync(seedPath, content, 'utf8');
console.log('Fixed users seed successfully!');
