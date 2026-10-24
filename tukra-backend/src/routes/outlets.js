import express from 'express';
import { query, toCamelCase } from '../db.js';
import { auth } from '../middleware/auth.js';
import { getFeasibleSlots } from '../services/slotService.js';

const router = express.Router();

// 15-second in-memory cache for outlets
let outletsCache = null;
let outletsCacheExpiry = 0;

// GET /outlets
router.get('/', auth(), async (req, res, next) => {
  const campusId = req.user?.campusId;
  const now = Date.now();

  if (outletsCache && outletsCacheExpiry > now) {
    return res.json(outletsCache);
  }

  try {
    let sql = `
      SELECT o.id, o.campus_id, o.owner_user_id, o.name, o.type, o.location_note,
             o.phone, o.cover_image_url, o.is_open, o.opens_at, o.accepts_orders_until
        FROM outlets o
    `;
    const params = [];
    if (campusId) {
      sql += ' WHERE o.campus_id = $1';
      params.push(campusId);
    }
    sql += ' ORDER BY o.is_open DESC, o.name ASC';

    const { rows: outlets } = await query(sql, params);

    // Compute waitMinutes for each outlet
    const enrichedOutlets = await Promise.all(
      outlets.map(async (outlet) => {
        if (!outlet.is_open) {
          return { ...toCamelCase(outlet), waitMinutes: null };
        }

        // Find earliest available slot starting today at or after now
        const slotRes = await query(
          `SELECT starts_at
             FROM slots
            WHERE outlet_id = $1
              AND starts_at >= now()
              AND capacity - used > 0
              AND starts_at >= (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata')
              AND starts_at <  (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata') + interval '1 day'
            ORDER BY starts_at ASC
            LIMIT 1`,
          [outlet.id]
        );

        let waitMinutes = null;
        if (slotRes.rows.length > 0) {
          const slotStart = new Date(slotRes.rows[0].starts_at).getTime();
          waitMinutes = Math.max(0, Math.round((slotStart - Date.now()) / 60000));
        }

        return {
          ...toCamelCase(outlet),
          waitMinutes
        };
      })
    );

    outletsCache = enrichedOutlets;
    outletsCacheExpiry = now + 15000; // 15-second TTL cache

    res.json(enrichedOutlets);
  } catch (err) {
    next(err);
  }
});

// GET /outlets/:id/menu?diet=VEG|JAIN|EGG
router.get('/:id/menu', auth(), async (req, res, next) => {
  const { id } = req.params;
  const { diet } = req.query;

  try {
    let sql = `
      SELECT m.id, m.outlet_id, m.category_id, m.name, m.description,
             m.price_paise, m.is_veg, m.image_url, m.prep_seconds,
             m.prep_units, m.is_available, m.spice_level,
             c.name as category_name
        FROM menu_items m
   LEFT JOIN categories c ON c.id = m.category_id
       WHERE m.outlet_id = $1
    `;
    const params = [id];

    if (diet === 'VEG' || diet === 'JAIN') {
      sql += ' AND m.is_veg = true';
    }

    sql += ' ORDER BY c.sort_order ASC NULLS LAST, m.name ASC';

    const { rows } = await query(sql, params);

    res.json({
      items: toCamelCase(rows)
    });
  } catch (err) {
    next(err);
  }
});

// GET /outlets/:id/slots?units=N&longestPrepSeconds=S
router.get('/:id/slots', auth(), async (req, res, next) => {
  const { id } = req.params;
  const { units, longestPrepSeconds } = req.query;

  try {
    const result = await getFeasibleSlots(id, units, longestPrepSeconds);
    if (!result.outlet) {
      return res.status(404).json({
        error: { code: 'NOT_FOUND', message: 'Outlet not found' }
      });
    }

    if (!result.outlet.is_open) {
      return res.status(409).json({
        error: { code: 'OUTLET_CLOSED', message: 'Outlet is currently closed' }
      });
    }

    res.json({
      outletId: id,
      slots: result.slots
    });
  } catch (err) {
    next(err);
  }
});

export default router;
