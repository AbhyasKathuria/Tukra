import express from 'express';
import { z } from 'zod';
import { query, toCamelCase } from '../db.js';
import { auth } from '../middleware/auth.js';
import { validate } from '../middleware/validate.js';

const router = express.Router();

const updateMeSchema = z.object({
  name: z.string().min(2).optional(),
  hostel: z.string().nullable().optional(),
  dietPref: z.enum(['ANY', 'VEG', 'JAIN', 'EGG']).optional()
});

const deviceTokenSchema = z.object({
  token: z.string().min(10, 'Device token must be a valid FCM token string')
});

// GET /me
router.get('/', auth(), async (req, res, next) => {
  try {
    const userRes = await query(
      `SELECT u.id, u.campus_id, u.name, u.email, u.phone, u.role, u.hostel, u.diet_pref,
              COALESCE(w.balance_paise, 0) as balance_paise
         FROM users u
    LEFT JOIN wallets w ON w.user_id = u.id
        WHERE u.id = $1 AND u.deleted_at IS NULL`,
      [req.user.id]
    );

    if (userRes.rows.length === 0) {
      return res.status(404).json({
        error: { code: 'NOT_FOUND', message: 'User not found' }
      });
    }

    res.json(toCamelCase(userRes.rows[0]));
  } catch (err) {
    next(err);
  }
});

// PATCH /me
router.patch('/', auth(), validate(updateMeSchema), async (req, res, next) => {
  const { name, hostel, dietPref } = req.body;
  const updates = [];
  const values = [];
  let idx = 1;

  if (name !== undefined) {
    updates.push(`name = $${idx++}`);
    values.push(name);
  }
  if (hostel !== undefined) {
    updates.push(`hostel = $${idx++}`);
    values.push(hostel);
  }
  if (dietPref !== undefined) {
    updates.push(`diet_pref = $${idx++}`);
    values.push(dietPref);
  }

  if (updates.length === 0) {
    return res.status(400).json({
      error: { code: 'VALIDATION_FAILED', message: 'No fields to update' }
    });
  }

  values.push(req.user.id);
  const sql = `UPDATE users SET ${updates.join(', ')} WHERE id = $${idx} AND deleted_at IS NULL RETURNING id, name, hostel, diet_pref`;

  try {
    const result = await query(sql, values);
    if (result.rows.length === 0) {
      return res.status(404).json({
        error: { code: 'NOT_FOUND', message: 'User not found' }
      });
    }
    res.json(toCamelCase(result.rows[0]));
  } catch (err) {
    next(err);
  }
});

// DELETE /me (Right to be forgotten - anonymize PII)
router.delete('/', auth(), async (req, res, next) => {
  const userId = req.user.id;
  const anonymizedEmail = `deleted_${userId}@tukra.local`;

  try {
    await query(
      `UPDATE users
          SET name = 'Deleted User',
              email = $1,
              phone = NULL,
              hostel = NULL,
              deleted_at = now()
        WHERE id = $2`,
      [anonymizedEmail, userId]
    );

    // Remove device tokens
    await query('DELETE FROM device_tokens WHERE user_id = $1', [userId]);

    res.json({
      success: true,
      message: 'Account deleted and personal information anonymized successfully'
    });
  } catch (err) {
    next(err);
  }
});

// POST /me/device-token
router.post('/device-token', auth(), validate(deviceTokenSchema), async (req, res, next) => {
  const { token } = req.body;
  try {
    await query(
      `INSERT INTO device_tokens (user_id, token)
       VALUES ($1, $2)
       ON CONFLICT (user_id, token) DO NOTHING`,
      [req.user.id, token]
    );

    res.json({ success: true, message: 'Device token registered' });
  } catch (err) {
    next(err);
  }
});

export default router;
