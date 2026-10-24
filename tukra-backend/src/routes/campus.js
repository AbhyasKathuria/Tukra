import express from 'express';
import { query, toCamelCase } from '../db.js';
import { auth } from '../middleware/auth.js';

const router = express.Router();

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
