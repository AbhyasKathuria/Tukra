import express from 'express';
import { z } from 'zod';
import { auth } from '../middleware/auth.js';
import { validate } from '../middleware/validate.js';
import { query, toCamelCase, getClient } from '../db.js';
import { AppError } from '../middleware/error.js';

const router = express.Router();

const openPlateSchema = z.object({
  plateCode: z.string().min(3).max(20),
  outletId: z.string().uuid()
});

const linkPlateSchema = z.object({
  plateCode: z.string().min(3).max(20)
});

const addPlateItemSchema = z.object({
  menuItemId: z.string().uuid(),
  qty: z.number().int().min(1).default(1)
});

// POST /plate/open (Operator starts plate session at counter)
router.post('/open', auth(['MESS_OPERATOR', 'VENDOR', 'ADMIN']), validate(openPlateSchema), async (req, res, next) => {
  const { plateCode, outletId } = req.body;

  try {
    const existing = await query(
      "SELECT id FROM plate_sessions WHERE plate_code = $1 AND status IN ('OPEN', 'BILLED')",
      [plateCode]
    );

    if (existing.rows.length > 0) {
      return res.status(409).json({
        error: {
          code: 'PLATE_BUSY',
          message: `Plate ${plateCode} already has an active session. Please finish or close the previous session.`
        }
      });
    }

    const insertRes = await query(
      `INSERT INTO plate_sessions (plate_code, outlet_id, status, total_paise)
       VALUES ($1, $2, 'OPEN', 0)
       RETURNING id, plate_code, status, total_paise, opened_at`,
      [plateCode, outletId]
    );

    const session = insertRes.rows[0];
    res.status(201).json({
      sessionId: session.id,
      plateCode: session.plate_code,
      status: session.status,
      totalPaise: 0,
      openedAt: new Date(session.opened_at).toISOString(),
      footerNotice: 'Simulated smart plate'
    });
  } catch (err) {
    if (err.code === '23505') {
      return res.status(409).json({
        error: { code: 'PLATE_BUSY', message: 'Plate already has an active session' }
      });
    }
    next(err);
  }
});

// POST /plate/link (Student scans plate QR to link account)
router.post('/link', auth(['STUDENT', 'STAFF']), validate(linkPlateSchema), async (req, res, next) => {
  const { plateCode } = req.body;
  const userId = req.user.id;

  try {
    const sessionRes = await query(
      "SELECT id, status, user_id FROM plate_sessions WHERE plate_code = $1 AND status IN ('OPEN', 'BILLED')",
      [plateCode]
    );

    if (sessionRes.rows.length === 0) {
      return res.status(404).json({
        error: {
          code: 'NOT_FOUND',
          message: `No active plate session found for QR ${plateCode}. Ask the counter operator to start a session first.`
        }
      });
    }

    const session = sessionRes.rows[0];
    await query('UPDATE plate_sessions SET user_id = $1 WHERE id = $2', [userId, session.id]);

    res.json({
      sessionId: session.id,
      plateCode,
      status: session.status,
      linked: true
    });
  } catch (err) {
    next(err);
  }
});

// POST /plate/:id/items (Operator taps dish tile to record food served)
router.post('/:id/items', auth(['MESS_OPERATOR', 'VENDOR', 'ADMIN']), validate(addPlateItemSchema), async (req, res, next) => {
  const { id } = req.params;
  const { menuItemId, qty } = req.body;

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const sessionRes = await client.query(
      'SELECT id, status, outlet_id, total_paise FROM plate_sessions WHERE id = $1 FOR UPDATE',
      [id]
    );

    if (sessionRes.rows.length === 0) {
      throw new AppError('NOT_FOUND', 'Plate session not found', 404);
    }

    const session = sessionRes.rows[0];
    if (session.status !== 'OPEN') {
      throw new AppError('SESSION_LOCKED', 'Cannot add items: session is already billed or paid', 409);
    }

    // Fetch dish price from DB
    const itemRes = await client.query(
      'SELECT id, name, price_paise FROM menu_items WHERE id = $1 AND outlet_id = $2',
      [menuItemId, session.outlet_id]
    );

    if (itemRes.rows.length === 0) {
      throw new AppError('NOT_FOUND', 'Dish not found on this mess/outlet menu', 404);
    }

    const menuItem = itemRes.rows[0];
    const unitPricePaise = menuItem.price_paise;
    const itemTotalPaise = unitPricePaise * qty;

    await client.query(
      `INSERT INTO plate_items (plate_session_id, menu_item_id, qty, unit_price_paise)
       VALUES ($1, $2, $3, $4)`,
      [session.id, menuItem.id, qty, unitPricePaise]
    );

    const updateRes = await client.query(
      'UPDATE plate_sessions SET total_paise = total_paise + $1 WHERE id = $2 RETURNING total_paise',
      [itemTotalPaise, session.id]
    );

    await client.query('COMMIT');

    res.json({
      plateSessionId: session.id,
      totalPaise: parseInt(updateRes.rows[0].total_paise, 10),
      addedItem: {
        name: menuItem.name,
        qty,
        unitPricePaise
      }
    });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
});

// POST /plate/:id/finish (Operator completes plate -> marks BILLED)
router.post('/:id/finish', auth(['MESS_OPERATOR', 'VENDOR', 'ADMIN']), async (req, res, next) => {
  const { id } = req.params;

  try {
    const result = await query(
      "UPDATE plate_sessions SET status = 'BILLED' WHERE id = $1 AND status = 'OPEN' RETURNING id, plate_code, status, total_paise",
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(409).json({
        error: { code: 'INVALID_STATUS', message: 'Session is not in OPEN status or does not exist' }
      });
    }

    const s = result.rows[0];
    res.json({
      sessionId: s.id,
      plateCode: s.plate_code,
      status: s.status,
      totalPaise: parseInt(s.total_paise, 10),
      footerNotice: 'Simulated smart plate'
    });
  } catch (err) {
    next(err);
  }
});

// GET /plate/:id (Student Scan screen polls every 3s)
router.get('/:id', auth(), async (req, res, next) => {
  const { id } = req.params;

  try {
    const sessionRes = await query(
      `SELECT ps.id, ps.plate_code, ps.status, ps.total_paise, ps.opened_at, ps.paid_at,
              outl.name as outlet_name, u.name as student_name
         FROM plate_sessions ps
         JOIN outlets outl ON outl.id = ps.outlet_id
    LEFT JOIN users u ON u.id = ps.user_id
        WHERE ps.id = $1`,
      [id]
    );

    if (sessionRes.rows.length === 0) {
      return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Plate session not found' } });
    }

    const s = sessionRes.rows[0];

    const itemsRes = await query(
      `SELECT mi.name, pi.qty, pi.unit_price_paise, pi.served_at
         FROM plate_items pi
         JOIN menu_items mi ON mi.id = pi.menu_item_id
        WHERE pi.plate_session_id = $1
        ORDER BY pi.served_at ASC`,
      [s.id]
    );

    res.json({
      id: s.id,
      plateCode: s.plate_code,
      status: s.status,
      totalPaise: parseInt(s.total_paise, 10),
      outletName: s.outlet_name,
      studentName: s.student_name || null,
      openedAt: new Date(s.opened_at).toISOString(),
      paidAt: s.paid_at ? new Date(s.paid_at).toISOString() : null,
      items: toCamelCase(itemsRes.rows),
      footerNotice: 'Simulated smart plate'
    });
  } catch (err) {
    next(err);
  }
});

// POST /plate/:id/pay (Student pays bill via in-app wallet)
router.post('/:id/pay', auth(['STUDENT', 'STAFF']), async (req, res, next) => {
  const { id } = req.params;
  const userId = req.user.id;

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const sessionRes = await client.query(
      `SELECT ps.id, ps.plate_code, ps.status, ps.total_paise, ps.outlet_id,
              outl.name as outlet_name
         FROM plate_sessions ps
         JOIN outlets outl ON outl.id = ps.outlet_id
        WHERE ps.id = $1 AND ps.user_id = $2
        FOR UPDATE`,
      [id, userId]
    );

    if (sessionRes.rows.length === 0) {
      throw new AppError('NOT_FOUND', 'Plate session not found for your account', 404);
    }

    const session = sessionRes.rows[0];
    if (session.status !== 'BILLED') {
      throw new AppError('INVALID_STATUS', `Plate session cannot be paid in status ${session.status}`, 409);
    }

    const totalPaise = parseInt(session.total_paise, 10);
    let remainingBalance = 0;

    if (totalPaise > 0) {
      const walletRes = await client.query(
        `UPDATE wallets
            SET balance_paise = balance_paise - $1
          WHERE user_id = $2 AND balance_paise >= $1
          RETURNING balance_paise`,
        [totalPaise, userId]
      );

      if (walletRes.rows.length === 0) {
        throw new AppError('INSUFFICIENT_BALANCE', 'Insufficient wallet balance to pay for this plate', 402);
      }
      remainingBalance = parseInt(walletRes.rows[0].balance_paise, 10);

      await client.query(
        `INSERT INTO wallet_txns (user_id, type, amount_paise, note)
         VALUES ($1, 'PLATE', $2, $3)`,
        [userId, -totalPaise, `Plate ${session.plate_code} at ${session.outlet_name}`]
      );
    }

    const updateRes = await client.query(
      "UPDATE plate_sessions SET status = 'PAID', paid_at = now() WHERE id = $1 RETURNING paid_at",
      [session.id]
    );

    await client.query('COMMIT');

    res.json({
      success: true,
      status: 'PAID',
      plateCode: session.plate_code,
      totalPaise,
      paidAt: new Date(updateRes.rows[0].paid_at).toISOString(),
      balancePaise: remainingBalance,
      footerNotice: 'Simulated smart plate'
    });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
});

export default router;
