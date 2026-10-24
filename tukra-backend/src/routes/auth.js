import express from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { z } from 'zod';
import { query, toCamelCase, getClient } from '../db.js';
import { validate } from '../middleware/validate.js';
import config from '../config.js';

const router = express.Router();

const registerSchema = z.object({
  email: z.string().email().toLowerCase(),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  name: z.string().min(2, 'Name must be at least 2 characters'),
  campusId: z.string().uuid().optional(),
  role: z.enum(['STUDENT', 'STAFF'], {
    errorMap: () => ({ message: 'Public registration allows STUDENT or STAFF roles only' })
  }).default('STUDENT'),
  dietPref: z.enum(['ANY', 'VEG', 'JAIN', 'EGG']).default('ANY'),
  hostel: z.string().optional()
});

const loginSchema = z.object({
  email: z.string().email().toLowerCase(),
  password: z.string().min(1, 'Password is required')
});

// POST /auth/register
router.post('/register', validate(registerSchema), async (req, res, next) => {
  const { email, password, name, role, dietPref, hostel } = req.body;
  let campusId = req.body.campusId;

  const client = await getClient();
  try {
    await client.query('BEGIN');

    // Default to first campus if campusId not provided
    if (!campusId) {
      const defaultCampus = await client.query('SELECT id FROM campuses LIMIT 1');
      if (defaultCampus.rows.length === 0) {
        throw new Error('No campus configured in database');
      }
      campusId = defaultCampus.rows[0].id;
    }

    // Check existing email
    const existing = await client.query('SELECT id FROM users WHERE email = $1', [email]);
    if (existing.rows.length > 0) {
      await client.query('ROLLBACK');
      return res.status(409).json({
        error: {
          code: 'ALREADY_EXISTS',
          message: 'An account with this email already exists'
        }
      });
    }

    const passwordHash = await bcrypt.hash(password, 10);

    const userRes = await client.query(
      `INSERT INTO users (campus_id, name, email, password_hash, role, hostel, diet_pref)
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING id, campus_id, name, email, role, hostel, diet_pref, created_at`,
      [campusId, name, email, passwordHash, role, hostel || null, dietPref]
    );
    const newUser = userRes.rows[0];

    // Create wallet with 0 balance
    await client.query(
      'INSERT INTO wallets (user_id, balance_paise) VALUES ($1, 0) ON CONFLICT DO NOTHING',
      [newUser.id]
    );

    await client.query('COMMIT');

    const token = jwt.sign(
      {
        id: newUser.id,
        role: newUser.role,
        campusId: newUser.campus_id
      },
      config.JWT_SECRET,
      { expiresIn: '12h' }
    );

    res.status(201).json({
      user: toCamelCase({ ...newUser, balancePaise: 0 }),
      token
    });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
});

// POST /auth/login
router.post('/login', validate(loginSchema), async (req, res, next) => {
  const { email, password } = req.body;

  try {
    const userRes = await query(
      `SELECT u.id, u.campus_id, u.name, u.email, u.password_hash, u.role, u.hostel, u.diet_pref,
              u.deleted_at, COALESCE(w.balance_paise, 0) as balance_paise
         FROM users u
    LEFT JOIN wallets w ON w.user_id = u.id
        WHERE u.email = $1`,
      [email]
    );

    if (userRes.rows.length === 0) {
      return res.status(401).json({
        error: {
          code: 'UNAUTHENTICATED',
          message: 'Invalid email or password'
        }
      });
    }

    const user = userRes.rows[0];

    if (user.deleted_at) {
      return res.status(401).json({
        error: {
          code: 'ACCOUNT_DELETED',
          message: 'This account has been deleted'
        }
      });
    }

    const isMatch = await bcrypt.compare(password, user.password_hash);
    if (!isMatch) {
      return res.status(401).json({
        error: {
          code: 'UNAUTHENTICATED',
          message: 'Invalid email or password'
        }
      });
    }

    // Resolve outletId for vendor/mess operator
    let outletId = null;
    if (user.role === 'VENDOR' || user.role === 'MESS_OPERATOR') {
      const outletRes = await query('SELECT id FROM outlets WHERE owner_user_id = $1 LIMIT 1', [user.id]);
      if (outletRes.rows.length > 0) {
        outletId = outletRes.rows[0].id;
      }
    }

    const tokenPayload = {
      id: user.id,
      role: user.role,
      campusId: user.campus_id,
      ...(outletId ? { outletId } : {})
    };

    const token = jwt.sign(tokenPayload, config.JWT_SECRET, { expiresIn: '12h' });

    const safeUser = {
      id: user.id,
      campusId: user.campus_id,
      name: user.name,
      email: user.email,
      role: user.role,
      hostel: user.hostel,
      dietPref: user.diet_pref,
      balancePaise: parseInt(user.balance_paise, 10),
      ...(outletId ? { outletId } : {})
    };

    res.json({
      user: safeUser,
      token
    });
  } catch (err) {
    next(err);
  }
});

export default router;
