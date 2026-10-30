import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const seedPath = path.join(__dirname, '..', 'sql', 'seed.sql');

let content = fs.readFileSync(seedPath, 'utf8');

// Replace users table insertion
const oldUserSql = `INSERT INTO users (id, campus_id, name, email, phone, password_hash, role, hostel, diet_pref)`;
const newUserSql = `INSERT INTO users (id, campus_id, name, email, phone, password_hash, role, hostel, diet_pref, roll_number, department, degree_year, room_number, rfid_card_id, green_credits, clean_streak_days, food_saved_kg)`;

if (!content.includes('roll_number, department')) {
  content = content.replace(oldUserSql, newUserSql);
}

// Update Aarav Sharma
const oldAarav = "('a1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111',\n   'Aarav Sharma', 'aarav@presidency.ac.in', '+919876543210',\n   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STUDENT', 'Nilgiri Block A', 'ANY'),";
const newAarav = "('a1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111',\n   'Aarav Sharma', 'aarav@presidency.ac.in', '+919876543210',\n   '$2b$10$/jFET/R8NwkcGI.2h3crieUhjPsAALilSARmznQqDvoanB7lwmKNy', 'STUDENT', 'Nilgiri Block A', 'ANY',\n   'PU2023CS0101', 'Computer Science & Engineering', 'B.Tech (3rd Year, Sem 5)', 'Room 304', 'PU-RFID-883921', 420, 14, 4.20),";

content = content.replace(oldAarav, newAarav);

// Update ON CONFLICT for users
const oldConflict = "ON CONFLICT (id) DO UPDATE SET\n  name = EXCLUDED.name,\n  password_hash = EXCLUDED.password_hash,\n  role = EXCLUDED.role;";
const newConflict = `ON CONFLICT (id) DO UPDATE SET
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

content = content.replace(oldConflict, newConflict);

// Add Non-Veg & Quick Bite menu items
if (!content.includes('Chicken Frankie Roll')) {
  const target = "('b2222222-2222-2222-2222-222222222222', 'c0022222-2222-2222-2222-222222222222', 'Masala Maggi'";
  const insertion = "('b2222222-2222-2222-2222-222222222222', 'c0022222-2222-2222-2222-222222222222', 'Chicken Frankie Roll', 'Juicy spiced chicken tikka strips wrapped in egg-layered paratha', 8500, false, 180, 1, true, 2),\n  ('b2222222-2222-2222-2222-222222222222', 'c0022222-2222-2222-2222-222222222222', 'Egg Bhurji Pav', 'Double egg spiced scramble tossed with onions, chillies and butter pavs', 6500, false, 150, 1, true, 2),\n  " + target;
  content = content.replace(target, insertion);
}

if (!content.includes('Butter Chicken Rice Bowl')) {
  const targetMess = "('b4444444-4444-4444-4444-444444444444', 'c0044444-4444-4444-4444-444444444442', 'Chicken Biryani (Special)'";
  const insertionMess = "('b4444444-4444-4444-4444-444444444444', 'c0044444-4444-4444-4444-444444444442', 'Butter Chicken Rice Bowl', 'Boneless tandoori chicken cooked in rich makhani gravy over jeera rice', 12000, false, 240, 2, true, 1),\n  " + targetMess;
  content = content.replace(targetMess, insertionMess);
}

fs.writeFileSync(seedPath, content, 'utf8');
console.log('Seed SQL updated successfully!');
