import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import * as XLSX from 'xlsx';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const dataDir = path.join(__dirname, '..', 'data');

if (!fs.existsSync(dataDir)) {
  fs.mkdirSync(dataDir, { recursive: true });
}

// ==========================================
// 1. PRESIDENCY UNIVERSITY STUDENTS DEMO DATA
// ==========================================
const students = [
  {
    roll_number: 'PU2023CS0101',
    full_name: 'Aarav Sharma',
    college_email: 'aarav@presidency.ac.in',
    phone: '+91 98765 43210',
    department: 'Computer Science & Engineering',
    degree: 'B.Tech',
    year_semester: '3rd Year (Sem 5)',
    hostel_block: 'Nilgiri Block A',
    room_number: 'Room 304',
    mess_plan: 'Gold Full-Board (3 Meals/Day)',
    diet_preference: 'VEG',
    wallet_balance_rupees: 500,
    clean_plate_streak_days: 14,
    food_waste_saved_kg: 4.2,
    green_credits: 420,
    rfid_card_id: 'PU-RFID-883921'
  },
  {
    roll_number: 'PU2023CS0102',
    full_name: 'Priya Patel',
    college_email: 'priya@presidency.ac.in',
    phone: '+91 98765 43211',
    department: 'Computer Science & Engineering',
    degree: 'B.Tech',
    year_semester: '3rd Year (Sem 5)',
    hostel_block: 'Cauvery Block B',
    room_number: 'Room 212',
    mess_plan: 'Silver (Lunch & Dinner)',
    diet_preference: 'VEG',
    wallet_balance_rupees: 0,
    clean_plate_streak_days: 9,
    food_waste_saved_kg: 2.8,
    green_credits: 280,
    rfid_card_id: 'PU-RFID-883922'
  },
  {
    roll_number: 'PU2022EC0045',
    full_name: 'Rohan Deshmukh',
    college_email: 'rohan.d@presidency.ac.in',
    phone: '+91 98765 43218',
    department: 'Electronics & Communication',
    degree: 'B.Tech',
    year_semester: '4th Year (Sem 7)',
    hostel_block: 'Krishna Block C',
    room_number: 'Room 408',
    mess_plan: 'Gold Full-Board (3 Meals/Day)',
    diet_preference: 'NON-VEG',
    wallet_balance_rupees: 350,
    clean_plate_streak_days: 21,
    food_waste_saved_kg: 6.5,
    green_credits: 650,
    rfid_card_id: 'PU-RFID-883923'
  },
  {
    roll_number: 'PU2023AI0021',
    full_name: 'Ananya Iyer',
    college_email: 'ananya.iyer@presidency.ac.in',
    phone: '+91 98765 43219',
    department: 'Artificial Intelligence & Data Science',
    degree: 'B.Tech',
    year_semester: '3rd Year (Sem 5)',
    hostel_block: 'Cauvery Block B',
    room_number: 'Room 105',
    mess_plan: 'Flexible Pay-Per-Meal',
    diet_preference: 'JAIN',
    wallet_balance_rupees: 420,
    clean_plate_streak_days: 18,
    food_waste_saved_kg: 5.1,
    green_credits: 510,
    rfid_card_id: 'PU-RFID-883924'
  },
  {
    roll_number: 'PU2024ME0012',
    full_name: 'Kabir Verma',
    college_email: 'kabir.v@presidency.ac.in',
    phone: '+91 98765 43220',
    department: 'Mechanical Engineering',
    degree: 'B.Tech',
    year_semester: '2nd Year (Sem 3)',
    hostel_block: 'Narmada Block D',
    room_number: 'Room 118',
    mess_plan: 'Gold Full-Board (3 Meals/Day)',
    diet_preference: 'NON-VEG',
    wallet_balance_rupees: 650,
    clean_plate_streak_days: 11,
    food_waste_saved_kg: 3.4,
    green_credits: 340,
    rfid_card_id: 'PU-RFID-883925'
  },
  {
    roll_number: 'PU2023BT0033',
    full_name: 'Sneha Mukhopadhyay',
    college_email: 'sneha.m@presidency.ac.in',
    phone: '+91 98765 43221',
    department: 'Biotechnology',
    degree: 'B.Tech',
    year_semester: '3rd Year (Sem 5)',
    hostel_block: 'Cauvery Block B',
    room_number: 'Room 310',
    mess_plan: 'Silver (Lunch & Dinner)',
    diet_preference: 'EGG',
    wallet_balance_rupees: 280,
    clean_plate_streak_days: 15,
    food_waste_saved_kg: 4.6,
    green_credits: 460,
    rfid_card_id: 'PU-RFID-883926'
  },
  {
    roll_number: 'PU2023MB0088',
    full_name: 'Vikram Malhotra',
    college_email: 'vikram.m@presidency.ac.in',
    phone: '+91 98765 43222',
    department: 'School of Management',
    degree: 'MBA',
    year_semester: '2nd Year (Sem 3)',
    hostel_block: 'Day Scholar (Koramangala)',
    room_number: 'N/A',
    mess_plan: 'Flexible Pay-Per-Meal',
    diet_preference: 'NON-VEG',
    wallet_balance_rupees: 890,
    clean_plate_streak_days: 8,
    food_waste_saved_kg: 2.2,
    green_credits: 220,
    rfid_card_id: 'PU-RFID-883927'
  },
  {
    roll_number: 'PU2024LW0019',
    full_name: 'Diya Sengupta',
    college_email: 'diya.s@presidency.ac.in',
    phone: '+91 98765 43223',
    department: 'School of Law',
    degree: 'BA LLB',
    year_semester: '2nd Year (Sem 3)',
    hostel_block: 'Narmada Block D',
    room_number: 'Room 202',
    mess_plan: 'Gold Full-Board (3 Meals/Day)',
    diet_preference: 'VEG',
    wallet_balance_rupees: 150,
    clean_plate_streak_days: 12,
    food_waste_saved_kg: 3.7,
    green_credits: 370,
    rfid_card_id: 'PU-RFID-883928'
  },
  {
    roll_number: 'PU2023CS0109',
    full_name: 'Arjun Nambiar',
    college_email: 'arjun.n@presidency.ac.in',
    phone: '+91 98765 43224',
    department: 'Computer Science & Engineering',
    degree: 'B.Tech',
    year_semester: '3rd Year (Sem 5)',
    hostel_block: 'Nilgiri Block A',
    room_number: 'Room 102',
    mess_plan: 'Gold Full-Board (3 Meals/Day)',
    diet_preference: 'NON-VEG',
    wallet_balance_rupees: 400,
    clean_plate_streak_days: 25,
    food_waste_saved_kg: 7.9,
    green_credits: 790,
    rfid_card_id: 'PU-RFID-883929'
  },
  {
    roll_number: 'PU2024CS0210',
    full_name: 'Tanvi Chawla',
    college_email: 'tanvi.c@presidency.ac.in',
    phone: '+91 98765 43225',
    department: 'Information Science & Tech',
    degree: 'B.Tech',
    year_semester: '2nd Year (Sem 3)',
    hostel_block: 'Cauvery Block B',
    room_number: 'Room 414',
    mess_plan: 'Silver (Lunch & Dinner)',
    diet_preference: 'VEG',
    wallet_balance_rupees: 520,
    clean_plate_streak_days: 16,
    food_waste_saved_kg: 4.9,
    green_credits: 490,
    rfid_card_id: 'PU-RFID-883930'
  }
];

const hostelPlans = [
  {
    plan_name: 'Gold Full-Board',
    monthly_cost_rupees: 5200,
    meals_covered: 'Breakfast, Lunch, Evening Snacks, Dinner',
    dining_hall: 'Central Dining Hall',
    guest_meal_allowed: true,
    cancellation_cutoff_minutes: 10
  },
  {
    plan_name: 'Silver Two-Meal',
    monthly_cost_rupees: 3800,
    meals_covered: 'Lunch & Dinner',
    dining_hall: 'Central Dining Hall',
    guest_meal_allowed: false,
    cancellation_cutoff_minutes: 10
  },
  {
    plan_name: 'Flexible Campus Pay-Per-Meal',
    monthly_cost_rupees: 0,
    meals_covered: 'Pay as you dine with Tukra Wallet',
    dining_hall: 'All Campus Outlets & Mess',
    guest_meal_allowed: true,
    cancellation_cutoff_minutes: 10
  }
];

const campusDepartments = [
  { name: 'Computer Science & Engineering', block: 'Academic Block 1', students_enrolled: 1200 },
  { name: 'Artificial Intelligence & Data Science', block: 'Tech Block 3', students_enrolled: 640 },
  { name: 'Electronics & Communication', block: 'Academic Block 2', students_enrolled: 820 },
  { name: 'Mechanical & Civil Engineering', block: 'Workshop Block', students_enrolled: 450 },
  { name: 'School of Management (MBA)', block: 'Management Tower', students_enrolled: 580 },
  { name: 'School of Law', block: 'Justice Block', students_enrolled: 420 },
  { name: 'Biotechnology & Life Sciences', block: 'Science Complex', students_enrolled: 310 }
];

// Write Student Workbook
const studentWb = XLSX.utils.book_new();
const studentWs = XLSX.utils.json_to_sheet(students);
const plansWs = XLSX.utils.json_to_sheet(hostelPlans);
const deptsWs = XLSX.utils.json_to_sheet(campusDepartments);

XLSX.utils.book_append_sheet(studentWb, studentWs, 'Students');
XLSX.utils.book_append_sheet(studentWb, plansWs, 'Hostel_Mess_Plans');
XLSX.utils.book_append_sheet(studentWb, deptsWs, 'Campus_Departments');

const studentXlsxPath = path.join(dataDir, 'Presidency_University_Students_Demo_Data.xlsx');
const studentCsvPath = path.join(dataDir, 'Presidency_University_Students_Demo_Data.csv');

XLSX.writeFile(studentWb, studentXlsxPath);
fs.writeFileSync(studentCsvPath, XLSX.utils.sheet_to_csv(studentWs));
console.log('Saved:', studentXlsxPath);
console.log('Saved:', studentCsvPath);

// ==========================================
// 2. PRESIDENCY UNIVERSITY CANTEEN DEMO DATA
// ==========================================
const canteenOutlets = [
  {
    outlet_id: 'b1111111-1111-1111-1111-111111111111',
    name: 'South Tiffin',
    type: 'CANTEEN',
    location: 'Ground Floor, Academic Block 1',
    operating_hours: '08:00 AM - 05:00 PM',
    slot_minutes: 5,
    slot_capacity: 12,
    has_veg: true,
    has_non_veg: false,
    has_quick_bites: true,
    tinyfish_sync_status: 'SYNCED_LIVE'
  },
  {
    outlet_id: 'b2222222-2222-2222-2222-222222222222',
    name: 'Chaat Corner',
    type: 'SNACKS',
    location: 'Student Centre Amphitheatre',
    operating_hours: '11:00 AM - 07:00 PM',
    slot_minutes: 5,
    slot_capacity: 15,
    has_veg: true,
    has_non_veg: true,
    has_quick_bites: true,
    tinyfish_sync_status: 'SYNCED_LIVE'
  },
  {
    outlet_id: 'b3333333-3333-3333-3333-333333333333',
    name: 'Juice Junction',
    type: 'JUICE',
    location: 'Near Sports Complex',
    operating_hours: '09:00 AM - 06:00 PM',
    slot_minutes: 5,
    slot_capacity: 10,
    has_veg: true,
    has_non_veg: false,
    has_quick_bites: true,
    tinyfish_sync_status: 'SYNCED_LIVE'
  },
  {
    outlet_id: 'b4444444-4444-4444-4444-444444444444',
    name: 'Hostel Mess & Dining',
    type: 'MESS',
    location: 'Central Dining Hall (Near Hostels)',
    operating_hours: '07:30 AM - 09:00 PM',
    slot_minutes: 5,
    slot_capacity: 20,
    has_veg: true,
    has_non_veg: true,
    has_quick_bites: false,
    tinyfish_sync_status: 'SYNCED_LIVE'
  },
  {
    outlet_id: 'b5555555-5555-5555-5555-555555555555',
    name: 'Presidency Campus Diner',
    type: 'CANTEEN',
    location: 'Food Court, Block 2',
    operating_hours: '10:00 AM - 08:30 PM',
    slot_minutes: 5,
    slot_capacity: 18,
    has_veg: true,
    has_non_veg: true,
    has_quick_bites: true,
    tinyfish_sync_status: 'SYNCED_LIVE'
  }
];

const menuCatalog = [
  // South Tiffin (Academic Block 1)
  {
    outlet_name: 'South Tiffin',
    dish_name: 'Masala Dosa',
    category: 'Tiffin & Dosas',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: false,
    price_rupees: 70,
    prep_time_mins: 3,
    prep_units: 2,
    spice_level: 1,
    live_stock_portions: 18,
    calories_approx: 320,
    description: 'Crispy fermented rice crepe loaded with tempered spiced potato mash, served with coconut chutney & drumstick sambar'
  },
  {
    outlet_name: 'South Tiffin',
    dish_name: 'Idli Vada Combo',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 55,
    prep_time_mins: 2,
    prep_units: 1,
    spice_level: 0,
    live_stock_portions: 24,
    calories_approx: 250,
    description: '2 fluffy steamed idlis + 1 crispy medu vada with hot sambar and fresh chutney'
  },
  {
    outlet_name: 'South Tiffin',
    dish_name: 'Onion Uttapam',
    category: 'Tiffin & Dosas',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: false,
    price_rupees: 65,
    prep_time_mins: 4,
    prep_units: 2,
    spice_level: 1,
    live_stock_portions: 10,
    calories_approx: 280,
    description: 'Thick savory pancake topped with caramelized shallots, green chillies, and coriander'
  },
  {
    outlet_name: 'South Tiffin',
    dish_name: 'Filter Coffee',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 25,
    prep_time_mins: 1,
    prep_units: 1,
    spice_level: 0,
    live_stock_portions: 45,
    calories_approx: 95,
    description: 'Authentic South Indian brass-filter decoction brewed with frothy whole milk'
  },
  {
    outlet_name: 'South Tiffin',
    dish_name: 'Curd Rice',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 50,
    prep_time_mins: 1,
    prep_units: 1,
    spice_level: 0,
    live_stock_portions: 15,
    calories_approx: 210,
    description: 'Tempered yogurt rice with mustard seeds, curry leaves, pomegranate seeds, and ginger'
  },

  // Chaat Corner (Student Centre)
  {
    outlet_name: 'Chaat Corner',
    dish_name: 'Pani Puri (6 pcs)',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 40,
    prep_time_mins: 1,
    prep_units: 1,
    spice_level: 2,
    live_stock_portions: 30,
    calories_approx: 180,
    description: 'Crisp hollow puris filled with spiced potato-chana mash and ice-chilled mint hing water'
  },
  {
    outlet_name: 'Chaat Corner',
    dish_name: 'Vada Pav',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 30,
    prep_time_mins: 1,
    prep_units: 1,
    spice_level: 2,
    live_stock_portions: 22,
    calories_approx: 290,
    description: 'Mumbai spiced potato fritter in butter-toasted pav with dry garlic coconut chutney'
  },
  {
    outlet_name: 'Chaat Corner',
    dish_name: 'Pav Bhaji',
    category: 'Hot Snacks',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: false,
    price_rupees: 80,
    prep_time_mins: 4,
    prep_units: 2,
    spice_level: 2,
    live_stock_portions: 16,
    calories_approx: 420,
    description: 'Slow-simmered butter-mashed spiced vegetable curry served with 2 warm butter pavs'
  },
  {
    outlet_name: 'Chaat Corner',
    dish_name: 'Chicken Frankie Roll',
    category: 'Non-Veg Specials',
    food_type: '🔴 NON-VEG',
    is_veg: false,
    is_quick_bite: true,
    price_rupees: 85,
    prep_time_mins: 3,
    prep_units: 1,
    spice_level: 2,
    live_stock_portions: 14,
    calories_approx: 380,
    description: 'Egg-layered paratha wrapped with juicy spiced chicken tikka strips, mint mayo, and onions'
  },
  {
    outlet_name: 'Chaat Corner',
    dish_name: 'Egg Bhurji Pav',
    category: 'Non-Veg Specials',
    food_type: '🔴 NON-VEG',
    is_veg: false,
    is_quick_bite: true,
    price_rupees: 65,
    prep_time_mins: 3,
    prep_units: 1,
    spice_level: 2,
    live_stock_portions: 12,
    calories_approx: 340,
    description: 'Double egg spiced scramble tossed with onions, green chillies, and tomatoes with 2 pavs'
  },
  {
    outlet_name: 'Chaat Corner',
    dish_name: 'Masala Maggi',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 45,
    prep_time_mins: 3,
    prep_units: 1,
    spice_level: 2,
    live_stock_portions: 35,
    calories_approx: 310,
    description: 'Campus comfort noodles tossed with sweet corn, capsicum, butter, and extra magic tastemaker'
  },

  // Juice Junction (Sports Complex)
  {
    outlet_name: 'Juice Junction',
    dish_name: 'Sweet Lime (Mosambi) Juice',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 50,
    prep_time_mins: 2,
    prep_units: 1,
    spice_level: 0,
    live_stock_portions: 25,
    calories_approx: 120,
    description: 'Freshly cold-pressed mosambi juice with rock salt and roasted jeera'
  },
  {
    outlet_name: 'Juice Junction',
    dish_name: 'Mango Lassi',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 60,
    prep_time_mins: 2,
    prep_units: 1,
    spice_level: 0,
    live_stock_portions: 20,
    calories_approx: 240,
    description: 'Creamy sweet curd blended with real Alphonso mango pulp and cardamom'
  },
  {
    outlet_name: 'Juice Junction',
    dish_name: 'Cold Coffee with Ice Cream',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 75,
    prep_time_mins: 2,
    prep_units: 2,
    spice_level: 0,
    live_stock_portions: 18,
    calories_approx: 280,
    description: 'Rich dark blended cold espresso topped with a generous scoop of vanilla ice cream'
  },

  // Central Mess & Dining (Hostel Dining Hall)
  {
    outlet_name: 'Hostel Mess & Dining',
    dish_name: 'Deluxe Lunch Thali',
    category: 'Meals & Thalis',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: false,
    price_rupees: 95,
    prep_time_mins: 2,
    prep_units: 2,
    spice_level: 1,
    live_stock_portions: 40,
    calories_approx: 680,
    description: 'Dal makhani, paneer butter masala, dry seasonal sabzi, 3 hot rotis, basmati rice, salad & gulab jamun'
  },
  {
    outlet_name: 'Hostel Mess & Dining',
    dish_name: 'Chicken Dum Biryani (Special)',
    category: 'Non-Veg Specials',
    food_type: '🔴 NON-VEG',
    is_veg: false,
    is_quick_bite: false,
    price_rupees: 130,
    prep_time_mins: 4,
    prep_units: 3,
    spice_level: 2,
    live_stock_portions: 25,
    calories_approx: 740,
    description: 'Aromatic layered basmati rice with tender spiced chicken, boiled egg, mirchi ka salan, and raita'
  },
  {
    outlet_name: 'Hostel Mess & Dining',
    dish_name: 'Egg Curry with Jeera Rice',
    category: 'Non-Veg Specials',
    food_type: '🔴 NON-VEG',
    is_veg: false,
    is_quick_bite: false,
    price_rupees: 80,
    prep_time_mins: 3,
    prep_units: 2,
    spice_level: 2,
    live_stock_portions: 18,
    calories_approx: 510,
    description: '2 golden shallow-fried eggs in robust homestyle onion-tomato gravy with fragrant cumin rice'
  },
  {
    outlet_name: 'Hostel Mess & Dining',
    dish_name: 'Rajma Chawal Bowl',
    category: 'Meals & Thalis',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: false,
    price_rupees: 65,
    prep_time_mins: 2,
    prep_units: 1,
    spice_level: 1,
    live_stock_portions: 22,
    calories_approx: 450,
    description: 'Jammu red kidney beans slow-cooked in rich ginger-tomato masala over steaming rice'
  },
  {
    outlet_name: 'Hostel Mess & Dining',
    dish_name: 'Aloo Paratha with Curd',
    category: 'Meals & Thalis',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: false,
    price_rupees: 50,
    prep_time_mins: 3,
    prep_units: 2,
    spice_level: 1,
    live_stock_portions: 20,
    calories_approx: 380,
    description: 'Tawa-toasted spiced potato stuffed whole wheat paratha with fresh set curd and mango pickle'
  },

  // Presidency Campus Diner (Food Court Block 2)
  {
    outlet_name: 'Presidency Campus Diner',
    dish_name: 'Butter Chicken Rice Bowl',
    category: 'Non-Veg Specials',
    food_type: '🔴 NON-VEG',
    is_veg: false,
    is_quick_bite: false,
    price_rupees: 120,
    prep_time_mins: 4,
    prep_units: 2,
    spice_level: 1,
    live_stock_portions: 15,
    calories_approx: 620,
    description: 'Boneless tandoori chicken cooked in creamy tomato butter gravy poured over fragrant jeera rice'
  },
  {
    outlet_name: 'Presidency Campus Diner',
    dish_name: 'Crispy Veg Burger',
    category: 'Quick Bites',
    food_type: '🟢 VEG',
    is_veg: true,
    is_quick_bite: true,
    price_rupees: 60,
    prep_time_mins: 3,
    prep_units: 1,
    spice_level: 1,
    live_stock_portions: 28,
    calories_approx: 390,
    description: 'Crunchy herb potato patty with lettuce, tomatoes, creamy house mayo in a toasted sesame bun'
  },
  {
    outlet_name: 'Presidency Campus Diner',
    dish_name: 'Chicken Popcorn (Box)',
    category: 'Quick Bites',
    food_type: '🔴 NON-VEG',
    is_veg: false,
    is_quick_bite: true,
    price_rupees: 75,
    prep_time_mins: 2,
    prep_units: 1,
    spice_level: 1,
    live_stock_portions: 20,
    calories_approx: 340,
    description: 'Bite-sized crispy spiced chicken chunks served with chili garlic dip'
  }
];

const menuSwaps = [
  {
    swap_id: 'SWP-01',
    outlet_name: 'South Tiffin',
    action: 'LUNCH SPECIAL SWAP',
    outgoing_dish: 'Poori Sagu',
    incoming_dish: 'Bisi Bele Bath with Boondi',
    reason: 'Chef rotation for Thursday afternoon lunch peak',
    price_impact: '₹60 -> ₹65',
    tinyfish_verified: true
  },
  {
    swap_id: 'SWP-02',
    outlet_name: 'Hostel Mess & Dining',
    action: 'NON-VEG ROTATION',
    outgoing_dish: 'Egg Curry with Jeera Rice',
    incoming_dish: 'Chicken Korma with Tandoori Roti',
    reason: 'Weekend special dinner upgrade',
    price_impact: '₹80 -> ₹95',
    tinyfish_verified: true
  },
  {
    swap_id: 'SWP-03',
    outlet_name: 'Chaat Corner',
    action: 'QUICK BITE BOOST',
    outgoing_dish: 'Samosa Chaat',
    incoming_dish: 'Cheese Corn Momos (Steamed 6 pcs)',
    reason: 'High student demand for evening snack break',
    price_impact: '₹55 -> ₹65',
    tinyfish_verified: true
  }
];

// Write Canteen Workbook
const canteenWb = XLSX.utils.book_new();
const outletsWs = XLSX.utils.json_to_sheet(canteenOutlets);
const catalogWs = XLSX.utils.json_to_sheet(menuCatalog);
const swapsWs = XLSX.utils.json_to_sheet(menuSwaps);

XLSX.utils.book_append_sheet(canteenWb, outletsWs, 'Canteen_Outlets');
XLSX.utils.book_append_sheet(canteenWb, catalogWs, 'Menu_Catalog');
XLSX.utils.book_append_sheet(canteenWb, swapsWs, 'Live_Menu_Swaps');

const canteenXlsxPath = path.join(dataDir, 'Presidency_University_Canteen_Demo_Data.xlsx');
const canteenCsvPath = path.join(dataDir, 'Presidency_University_Canteen_Demo_Data.csv');

XLSX.writeFile(canteenWb, canteenXlsxPath);
fs.writeFileSync(canteenCsvPath, XLSX.utils.sheet_to_csv(catalogWs));
console.log('Saved:', canteenXlsxPath);
console.log('Saved:', canteenCsvPath);

console.log('--- All Excel and CSV Demo Datasets Generated Successfully! ---');
