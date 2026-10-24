import express from 'express';
import { z } from 'zod';
import { auth } from '../middleware/auth.js';
import { validate } from '../middleware/validate.js';
import { query, toCamelCase } from '../db.js';
import { AppError } from '../middleware/error.js';
import { getDemandForecast } from '../services/demand.js';

const router = express.Router();

const postMessMenuSchema = z.object({
  outletId: z.string().uuid(),
  serviceDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Must be YYYY-MM-DD format'),
  meal: z.enum(['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER']),
  menuItemId: z.string().uuid(),
  plannedQty: z.number().int().min(1).optional()
});

const makeReservationSchema = z.object({
  messMenuId: z.string().uuid()
});

// GET /mess/menu?date=&meal=
router.get('/menu', auth(), async (req, res, next) => {
  const { date, meal, outletId } = req.query;
  const targetDate = date || new Date().toISOString().slice(0, 10);

  try {
    let sql = `
      SELECT mm.id, mm.outlet_id, mm.service_date, mm.meal, mm.planned_qty,
             mi.id as menu_item_id, mi.name as dish_name, mi.price_paise, mi.is_veg,
             mi.description, mi.image_url,
             COUNT(mr.id) as reservation_count
        FROM mess_menus mm
        JOIN menu_items mi ON mi.id = mm.menu_item_id
   LEFT JOIN mess_reservations mr ON mr.mess_menu_id = mm.id
       WHERE mm.service_date = $1
    `;
    const params = [targetDate];
    let idx = 2;

    if (meal) {
      sql += ` AND mm.meal = $${idx++}`;
      params.push(meal.toUpperCase());
    }
    if (outletId) {
      sql += ` AND mm.outlet_id = $${idx++}`;
      params.push(outletId);
    }

    sql += ' GROUP BY mm.id, mi.id ORDER BY mm.meal, mi.name';

    const { rows } = await query(sql, params);

    res.json({
      serviceDate: targetDate,
      items: rows.map((r) => ({
        messMenuId: r.id,
        outletId: r.outlet_id,
        meal: r.meal,
        menuItemId: r.menu_item_id,
        dishName: r.dish_name,
        pricePaise: parseInt(r.price_paise, 10),
        isVeg: r.is_veg,
        description: r.description,
        imageUrl: r.image_url,
        plannedQty: r.planned_qty ? parseInt(r.planned_qty, 10) : null,
        reservationCount: parseInt(r.reservation_count, 10)
      }))
    });
  } catch (err) {
    next(err);
  }
});

// POST /mess/menu (Operator publishes/edits meal offering)
router.post('/menu', auth(['MESS_OPERATOR', 'ADMIN']), validate(postMessMenuSchema), async (req, res, next) => {
  const { outletId, serviceDate, meal, menuItemId, plannedQty } = req.body;

  try {
    const result = await query(
      `INSERT INTO mess_menus (outlet_id, service_date, meal, menu_item_id, planned_qty)
       VALUES ($1, $2, $3, $4, $5)
       ON CONFLICT (outlet_id, service_date, meal, menu_item_id)
       DO UPDATE SET planned_qty = EXCLUDED.planned_qty
       RETURNING *`,
      [outletId, serviceDate, meal, menuItemId, plannedQty || null]
    );

    res.status(201).json(toCamelCase(result.rows[0]));
  } catch (err) {
    next(err);
  }
});

// POST /mess/reservations (Student pre-reserves a dish)
router.post('/reservations', auth(['STUDENT', 'STAFF']), validate(makeReservationSchema), async (req, res, next) => {
  const { messMenuId } = req.body;
  const userId = req.user.id;

  try {
    const checkRes = await query('SELECT id FROM mess_menus WHERE id = $1', [messMenuId]);
    if (checkRes.rows.length === 0) {
      return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Mess menu meal not found' } });
    }

    await query(
      `INSERT INTO mess_reservations (user_id, mess_menu_id)
       VALUES ($1, $2)
       ON CONFLICT (user_id, mess_menu_id) DO NOTHING`,
      [userId, messMenuId]
    );

    res.status(201).json({
      success: true,
      message: 'Meal reserved successfully'
    });
  } catch (err) {
    next(err);
  }
});

// GET /mess/forecast (Operator/Admin simple 4-week weekday demand forecast)
router.get('/forecast', auth(['MESS_OPERATOR', 'ADMIN']), async (req, res, next) => {
  const { outletId, date } = req.query;

  try {
    let targetOutletId = outletId;
    if (!targetOutletId) {
      const defaultOutlet = await query("SELECT id FROM outlets WHERE type = 'MESS' LIMIT 1");
      if (defaultOutlet.rows.length === 0) {
        throw new AppError('NOT_FOUND', 'No mess outlet configured', 404);
      }
      targetOutletId = defaultOutlet.rows[0].id;
    }

    const forecast = await getDemandForecast(targetOutletId, date);
    res.json(forecast);
  } catch (err) {
    next(err);
  }
});

export default router;
