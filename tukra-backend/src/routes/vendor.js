import express from 'express';
import { z } from 'zod';
import { auth } from '../middleware/auth.js';
import { validate } from '../middleware/validate.js';
import { query, toCamelCase } from '../db.js';
import { AppError } from '../middleware/error.js';
import { updateOrderStatus, verifyQrToken } from '../services/orderService.js';

const router = express.Router();

const updateStatusSchema = z.object({
  status: z.enum(['ACCEPTED', 'PREPARING', 'READY', 'COLLECTED', 'REJECTED']),
  rejectReason: z.string().max(250).optional()
});

const verifyQrSchema = z.object({
  token: z.string().min(1, 'QR token is required')
});

const updateItemSchema = z.object({
  isAvailable: z.boolean().optional(),
  pricePaise: z.number().int().min(0).optional()
});

const addItemSchema = z.object({
  name: z.string().min(2),
  pricePaise: z.number().int().min(0),
  isVeg: z.boolean().default(true),
  categoryId: z.string().uuid().optional(),
  description: z.string().max(500).optional(),
  prepSeconds: z.number().int().min(30).default(180),
  prepUnits: z.number().int().min(1).max(10).default(1),
  spiceLevel: z.number().int().min(0).max(3).optional()
});

const updateOutletSchema = z.object({
  isOpen: z.boolean().optional(),
  slotCapacity: z.number().int().min(1).max(100).optional(),
  acceptsOrdersUntil: z.string().regex(/^\d{2}:\d{2}$/, 'Must be in HH:MM format').optional()
});

// Helper to resolve vendor's outlet ID
async function getVendorOutletId(user) {
  if (user.outletId) return user.outletId;
  const res = await query('SELECT id FROM outlets WHERE owner_user_id = $1 LIMIT 1', [user.id]);
  if (res.rows.length === 0) {
    throw new AppError('FORBIDDEN', 'No outlet registered for this vendor account', 403);
  }
  return res.rows[0].id;
}

// GET /vendor/orders?status=
// Privacy rule: first name + code only; NEVER return student email or phone
router.get('/orders', auth(['VENDOR', 'ADMIN']), async (req, res, next) => {
  const { status } = req.query;

  try {
    const outletId = await getVendorOutletId(req.user);

    let sql = `
      SELECT o.id, o.code, o.status, o.total_paise, o.created_at, o.ready_at,
             o.note, s.starts_at as slot_starts_at,
             SPLIT_PART(u.name, ' ', 1) as student_first_name
        FROM orders o
        JOIN slots s ON s.id = o.slot_id
        JOIN users u ON u.id = o.user_id
       WHERE o.outlet_id = $1
    `;
    const params = [outletId];

    if (status) {
      sql += ' AND o.status = $2';
      params.push(status);
    } else {
      sql += " AND o.status IN ('PLACED', 'ACCEPTED', 'PREPARING', 'READY')";
    }

    sql += ' ORDER BY s.starts_at ASC, o.created_at ASC';

    const { rows: orders } = await query(sql, params);

    // Fetch items for each order
    const orderIds = orders.map((o) => o.id);
    const itemsByOrder = new Map();

    if (orderIds.length > 0) {
      const itemsRes = await query(
        `SELECT order_id, name_snapshot as name, unit_price_paise, qty
           FROM order_items
          WHERE order_id = ANY($1)`,
        [orderIds]
      );
      for (const item of itemsRes.rows) {
        if (!itemsByOrder.has(item.order_id)) {
          itemsByOrder.set(item.order_id, []);
        }
        itemsByOrder.get(item.order_id).push({
          name: item.name,
          qty: item.qty,
          unitPricePaise: item.unit_price_paise
        });
      }
    }

    const queue = orders.map((o) => ({
      id: o.id,
      code: o.code,
      studentFirstName: o.student_first_name,
      status: o.status,
      totalPaise: parseInt(o.total_paise, 10),
      slotStartsAt: new Date(o.slot_starts_at).toISOString(),
      readyAt: o.ready_at ? new Date(o.ready_at).toISOString() : null,
      note: o.note || null,
      createdAt: new Date(o.created_at).toISOString(),
      items: itemsByOrder.get(o.id) || []
    }));

    res.json(queue);
  } catch (err) {
    next(err);
  }
});

// PATCH /vendor/orders/:id/status
router.patch('/orders/:id/status', auth(['VENDOR', 'ADMIN']), validate(updateStatusSchema), async (req, res, next) => {
  try {
    const result = await updateOrderStatus(
      req.params.id,
      req.body.status,
      req.user.id,
      req.body.rejectReason
    );
    res.json(result);
  } catch (err) {
    next(err);
  }
});

// POST /vendor/orders/verify-qr
router.post('/orders/verify-qr', auth(['VENDOR', 'ADMIN']), validate(verifyQrSchema), async (req, res, next) => {
  try {
    const result = await verifyQrToken(req.body.token, req.user.id);
    res.json(result);
  } catch (err) {
    next(err);
  }
});

// PATCH /vendor/items/:id (toggle availability, edit price)
router.patch('/items/:id', auth(['VENDOR', 'ADMIN']), validate(updateItemSchema), async (req, res, next) => {
  const { id } = req.params;
  const { isAvailable, pricePaise } = req.body;

  try {
    const outletId = await getVendorOutletId(req.user);

    const updates = [];
    const values = [];
    let idx = 1;

    if (isAvailable !== undefined) {
      updates.push(`is_available = $${idx++}`);
      values.push(isAvailable);
    }
    if (pricePaise !== undefined) {
      updates.push(`price_paise = $${idx++}`);
      values.push(pricePaise);
    }

    if (updates.length === 0) {
      return res.status(400).json({
        error: { code: 'VALIDATION_FAILED', message: 'No fields provided to update' }
      });
    }

    updates.push(`updated_at = now()`);
    values.push(id, outletId);

    const sql = `UPDATE menu_items SET ${updates.join(', ')} WHERE id = $${idx++} AND outlet_id = $${idx} RETURNING *`;
    const result = await query(sql, values);

    if (result.rows.length === 0) {
      return res.status(404).json({
        error: { code: 'NOT_FOUND', message: 'Item not found in your outlet' }
      });
    }

    res.json(toCamelCase(result.rows[0]));
  } catch (err) {
    next(err);
  }
});

// POST /vendor/items (add item)
router.post('/items', auth(['VENDOR', 'ADMIN']), validate(addItemSchema), async (req, res, next) => {
  try {
    const outletId = await getVendorOutletId(req.user);
    const { name, pricePaise, isVeg, categoryId, description, prepSeconds, prepUnits, spiceLevel } = req.body;

    const result = await query(
      `INSERT INTO menu_items (outlet_id, category_id, name, description, price_paise, is_veg, prep_seconds, prep_units, spice_level, is_available)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, true)
       RETURNING *`,
      [outletId, categoryId || null, name, description || null, pricePaise, isVeg, prepSeconds, prepUnits, spiceLevel ?? null]
    );

    res.status(201).json(toCamelCase(result.rows[0]));
  } catch (err) {
    next(err);
  }
});

// PATCH /vendor/outlet (open/close, slot capacity, last-order time)
router.patch('/outlet', auth(['VENDOR', 'ADMIN']), validate(updateOutletSchema), async (req, res, next) => {
  const { isOpen, slotCapacity, acceptsOrdersUntil } = req.body;

  try {
    const outletId = await getVendorOutletId(req.user);

    const updates = [];
    const values = [];
    let idx = 1;

    if (isOpen !== undefined) {
      updates.push(`is_open = $${idx++}`);
      values.push(isOpen);
    }
    if (slotCapacity !== undefined) {
      updates.push(`slot_capacity = $${idx++}`);
      values.push(slotCapacity);
    }
    if (acceptsOrdersUntil !== undefined) {
      updates.push(`accepts_orders_until = $${idx++}`);
      values.push(acceptsOrdersUntil);
    }

    if (updates.length === 0) {
      return res.status(400).json({
        error: { code: 'VALIDATION_FAILED', message: 'No fields provided to update' }
      });
    }

    values.push(outletId);
    const sql = `UPDATE outlets SET ${updates.join(', ')} WHERE id = $${idx} RETURNING *`;
    const result = await query(sql, values);

    res.json(toCamelCase(result.rows[0]));
  } catch (err) {
    next(err);
  }
});

export default router;
