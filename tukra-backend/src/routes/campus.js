import express from 'express';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';
import * as XLSX from 'xlsx';
import { query, toCamelCase } from '../db.js';
import { auth } from '../middleware/auth.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const dataDir = path.join(__dirname, '..', '..', 'data');

const router = express.Router();

// GET /campus/export/students-excel
router.get('/export/students-excel', async (req, res, next) => {
  const filePath = path.join(dataDir, 'Presidency_University_Students_Demo_Data.xlsx');
  if (fs.existsSync(filePath)) {
    res.setHeader('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    res.setHeader('Content-Disposition', 'attachment; filename="Presidency_University_Students_Demo_Data.xlsx"');
    return res.sendFile(filePath);
  }
  try {
    const { rows } = await query(
      `SELECT roll_number, name, email, phone, department, degree_year, hostel, room_number, diet_pref, rfid_card_id, COALESCE(green_credits, 420) as green_credits
         FROM users WHERE role IN ('STUDENT', 'STAFF') ORDER BY roll_number`
    );
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(rows);
    XLSX.utils.book_append_sheet(wb, ws, 'Students');
    const buf = XLSX.write(wb, { type: 'buffer', bookType: 'xlsx' });
    res.setHeader('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    res.setHeader('Content-Disposition', 'attachment; filename="Presidency_University_Students_Demo_Data.xlsx"');
    res.send(buf);
  } catch (err) {
    next(err);
  }
});

// GET /campus/export/students-csv
router.get('/export/students-csv', async (req, res, next) => {
  const filePath = path.join(dataDir, 'Presidency_University_Students_Demo_Data.csv');
  if (fs.existsSync(filePath)) {
    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', 'attachment; filename="Presidency_University_Students_Demo_Data.csv"');
    return res.sendFile(filePath);
  }
  try {
    const { rows } = await query(
      `SELECT roll_number, name, email, phone, department, degree_year, hostel, room_number, diet_pref, rfid_card_id, COALESCE(green_credits, 420) as green_credits
         FROM users WHERE role IN ('STUDENT', 'STAFF') ORDER BY roll_number`
    );
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(rows);
    const csv = XLSX.utils.sheet_to_csv(ws);
    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', 'attachment; filename="Presidency_University_Students_Demo_Data.csv"');
    res.send(csv);
  } catch (err) {
    next(err);
  }
});

// GET /campus/demo-data (JSON representation of university student demo records)
router.get('/demo-data', async (req, res, next) => {
  try {
    const { rows } = await query(
      `SELECT u.id, u.name, u.email, u.phone, u.role, u.hostel, u.diet_pref,
              u.roll_number, u.department, u.degree_year, u.room_number, u.rfid_card_id,
              u.green_credits, u.clean_streak_days, u.food_saved_kg,
              COALESCE(w.balance_paise, 0) as balance_paise
         FROM users u
    LEFT JOIN wallets w ON w.user_id = u.id
        WHERE u.role IN ('STUDENT', 'STAFF')
        ORDER BY u.role, u.name`
    );
    res.json({
      campus: 'Presidency University, Bengaluru',
      totalRecords: rows.length,
      students: toCamelCase(rows)
    });
  } catch (err) {
    next(err);
  }
});

// GET /campus
router.get('/', auth(), async (req, res, next) => {
  const campusId = req.user?.campusId;

  try {
    let sql = 'SELECT id, name, city, break_times FROM campuses';
    const params = [];
    if (campusId) {
      sql += ' WHERE id = $1';
      params.push(campusId);
    } else {
      sql += ' LIMIT 1';
    }

    const { rows } = await query(sql, params);
    if (rows.length === 0) {
      return res.status(404).json({
        error: { code: 'NOT_FOUND', message: 'Campus not found' }
      });
    }

    res.json(toCamelCase(rows[0]));
  } catch (err) {
    next(err);
  }
});

export default router;
