import express from 'express';
import { z } from 'zod';
import { auth } from '../middleware/auth.js';
import { validate } from '../middleware/validate.js';
import { query, toCamelCase } from '../db.js';
import { AppError } from '../middleware/error.js';
import { createOrder, cancelOrder } from '../services/orderService.js';

const router = express.Router();

const createOrderSchema = z.object({
  outletId: z.string().uuid(),
  slotId: z.string().uuid(),
  items: z.array(
    z.object({
      menuItemId: z.string().uuid(),
      qty: z.number().int().min(1).max(10)
    })
  ).min(1, 'At least one item is required'),
  paymentMethod: z.enum(['WALLET', 'DEMO']),
  note: z.string().max(250).optional()
});

// POST /orders (Create + pay)
router.post('/', auth(['STUDENT', 'STAFF']), validate(createOrderSchema), async (req, res, next) => {
  try {
    const order = await createOrder({
      userId: req.user.id,
      ...req.body
    });
    res.status(201).json(order);
  } catch (err) {
    next(err);
  }
});

// GET /orders?status=active|past
router.get('/', auth(['STUDENT', 'STAFF']), async (req, res, next) => {
  const { status } = req.query;
  const userId = req.user.id;

  try {
    let sql = `
      SELECT o.id, o.code, o.status, o.total_paise, o.qr_token, o.created_at,
             o.note, s.starts_at as slot_starts_at, outl.name as outlet_name
        FROM orders o
        JOIN slots s ON s.id = o.slot_id
        JOIN outlets outl ON outl.id = o.outlet_id
       WHERE o.user_id = $1
    `;
    const params = [userId];

    if (status === 'active') {
      sql += " AND o.status IN ('PLACED', 'ACCEPTED', 'PREPARING', 'READY')";
    } else if (status === 'past') {
      sql += " AND o.status IN ('COLLECTED', 'CANCELLED', 'REJECTED', 'NO_SHOW')";
    }

    sql += ' ORDER BY o.created_at DESC LIMIT 50';

    const { rows: orders } = await query(sql, params);

    // Fetch items for each order
    const orderIds = orders.map((o) => o.id);
    let itemsByOrder = new Map();

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

    const flatOrders = orders.map((o) => ({
      id: o.id,
      code: o.code,
      status: o.status,
      totalPaise: parseInt(o.total_paise, 10),
      qrToken: o.qr_token,
      slotStartsAt: new Date(o.slot_starts_at).toISOString(),
      outletName: o.outlet_name,
      note: o.note || null,
      createdAt: new Date(o.created_at).toISOString(),
      items: itemsByOrder.get(o.id) || []
    }));

    res.json(flatOrders);
  } catch (err) {
    next(err);
  }
});

// GET /orders/:id (Flat shape per F2)
router.get('/:id', auth(), async (req, res, next) => {
  const { id } = req.params;
  const user = req.user;

  try {
    const orderRes = await query(
      `SELECT o.id, o.code, o.user_id, o.outlet_id, o.status, o.total_paise,
              o.qr_token, o.note, o.created_at, s.starts_at as slot_starts_at,
              outl.name as outlet_name, outl.owner_user_id
         FROM orders o
         JOIN slots s ON s.id = o.slot_id
         JOIN outlets outl ON outl.id = o.outlet_id
        WHERE o.id = $1`,
      [id]
    );

    if (orderRes.rows.length === 0) {
      return res.status(404).json({
        error: { code: 'NOT_FOUND', message: 'Order not found' }
      });
    }

    const o = orderRes.rows[0];

    // Ownership check: must be the student who placed it, or the vendor owning the outlet, or an admin
    const isOwner = o.user_id === user.id;
    const isVendor = o.owner_user_id === user.id;
    const isAdmin = user.role === 'ADMIN';

    if (!isOwner && !isVendor && !isAdmin) {
      return res.status(403).json({
        error: { code: 'FORBIDDEN', message: 'You do not have access to this order' }
      });
    }

    const itemsRes = await query(
      `SELECT name_snapshot as name, unit_price_paise, qty
         FROM order_items
        WHERE order_id = $1`,
      [o.id]
    );

    res.json({
      id: o.id,
      code: o.code,
      status: o.status,
      totalPaise: parseInt(o.total_paise, 10),
      qrToken: o.qr_token,
      slotStartsAt: new Date(o.slot_starts_at).toISOString(),
      outletName: o.outlet_name,
      note: o.note || null,
      createdAt: new Date(o.created_at).toISOString(),
      items: itemsRes.rows.map((i) => ({
        name: i.name,
        qty: i.qty,
        unitPricePaise: parseInt(i.unit_price_paise, 10)
      }))
    });
  } catch (err) {
    next(err);
  }
});

// POST /orders/:id/cancel
router.post('/:id/cancel', auth(['STUDENT', 'STAFF']), async (req, res, next) => {
  try {
    const result = await cancelOrder(req.params.id, req.user.id);
    res.json(result);
  } catch (err) {
    next(err);
  }
});

export default router;
