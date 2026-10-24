import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { pool, query } from '../src/db.js';
import { generateDailySlots } from '../src/services/slotService.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function resetDb() {
  console.log('🔄 [RESET DB] Starting complete database recreation...');

  const schemaPath = path.join(__dirname, '..', 'sql', 'schema.sql');
  const seedPath = path.join(__dirname, '..', 'sql', 'seed.sql');

  const schemaSql = fs.readFileSync(schemaPath, 'utf8');
  const seedSql = fs.readFileSync(seedPath, 'utf8');

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    console.log('🧹 [RESET DB] Dropping all existing Tukra tables...');
    await client.query(`
      DROP TABLE IF EXISTS device_tokens CASCADE;
      DROP TABLE IF EXISTS menu_proposals CASCADE;
      DROP TABLE IF EXISTS menu_sync_jobs CASCADE;
      DROP TABLE IF EXISTS plate_items CASCADE;
      DROP TABLE IF EXISTS plate_sessions CASCADE;
      DROP TABLE IF EXISTS mess_reservations CASCADE;
      DROP TABLE IF EXISTS mess_menus CASCADE;
      DROP TABLE IF EXISTS payments CASCADE;
      DROP TABLE IF EXISTS wallet_txns CASCADE;
      DROP TABLE IF EXISTS wallets CASCADE;
      DROP TABLE IF EXISTS order_items CASCADE;
      DROP TABLE IF EXISTS orders CASCADE;
      DROP TABLE IF EXISTS slots CASCADE;
      DROP TABLE IF EXISTS menu_items CASCADE;
      DROP TABLE IF EXISTS categories CASCADE;
      DROP TABLE IF EXISTS outlets CASCADE;
      DROP TABLE IF EXISTS users CASCADE;
      DROP TABLE IF EXISTS campuses CASCADE;
    `);

    console.log('🏗️  [RESET DB] Applying schema.sql...');
    await client.query(schemaSql);

    console.log('🌱 [RESET DB] Applying seed.sql...');
    await client.query(seedSql);

    await client.query('COMMIT');
    console.log('✅ [RESET DB] Schema & Seed applied successfully.');

    console.log('⏰ [RESET DB] Generating today\'s initial pickup slots (Asia/Kolkata)...');
    const slotCount = await generateDailySlots(client);
    console.log(`✅ [RESET DB] Generated ${slotCount} slots for today.`);

    console.log('\n========================================================================================');
    console.log('                     TUKRA DEMO CREDENTIALS TABLE (Password: demo1234)                   ');
    console.log('========================================================================================');
    console.table([
      { Role: 'STUDENT', Name: 'Aarav Sharma', Email: 'aarav@presidency.ac.in', Wallet: '₹500.00', Notes: 'Primary Student Demo' },
      { Role: 'STUDENT', Name: 'Priya Patel', Email: 'priya@presidency.ac.in', Wallet: '₹0.00', Notes: 'Low Balance / Topup Flow' },
      { Role: 'STAFF', Name: 'Dr. Meena Rao', Email: 'meena.sharma@presidency.ac.in', Wallet: '₹1,000.00', Notes: '25-min break persona' },
      { Role: 'VENDOR', Name: 'Ramesh Bhaiya', Email: 'ramesh@southtiffin.com', Wallet: 'N/A', Notes: 'South Tiffin (Canteen)' },
      { Role: 'VENDOR', Name: 'Suresh Gupta', Email: 'suresh@chaatcorner.com', Wallet: 'N/A', Notes: 'Chaat Corner (Snacks)' },
      { Role: 'VENDOR', Name: 'Imran Khan', Email: 'imran@juicejunction.com', Wallet: 'N/A', Notes: 'Juice Junction (Beverages)' },
      { Role: 'MESS_OPERATOR', Name: 'Rajesh Nair', Email: 'mess@presidency.ac.in', Wallet: 'N/A', Notes: 'Central Mess Counter' },
      { Role: 'ADMIN', Name: 'Ms. Kaur', Email: 'admin@presidency.ac.in', Wallet: 'N/A', Notes: 'Campus Analytics & Review' }
    ]);
    console.log('========================================================================================\n');

  } catch (err) {
    await client.query('ROLLBACK');
    console.error('❌ [RESET DB] Failed to reset database:', err);
    process.exit(1);
  } finally {
    client.release();
    await pool.end();
  }
}

resetDb();
