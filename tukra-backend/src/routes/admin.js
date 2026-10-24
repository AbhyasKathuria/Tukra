import express from 'express';
import { auth } from '../middleware/auth.js';
import { query, toCamelCase } from '../db.js';
import { getAdminOverview, getAdminDemand } from '../services/demand.js';
import { generateDailySlots } from '../services/slotService.js';

const router = express.Router();

// Helper to resolve admin's campus
async function getAdminCampusId(req) {
  if (req.user?.campusId) return req.user.campusId;
  const campusRes = await query('SELECT id FROM campuses LIMIT 1');
  return campusRes.rows[0]?.id;
}

// GET /admin/overview
router.get('/overview', auth(['ADMIN']), async (req, res, next) => {
  try {
    const campusId = await getAdminCampusId(req);
    const overview = await getAdminOverview(campusId);
    res.json(overview);
  } catch (err) {
    next(err);
  }
});

// GET /admin/demand
router.get('/demand', auth(['ADMIN']), async (req, res, next) => {
  try {
    const campusId = await getAdminCampusId(req);
    const demand = await getAdminDemand(campusId, req.query.date);
    res.json(demand);
  } catch (err) {
    next(err);
  }
});

// GET /admin/outlets
router.get('/outlets', auth(['ADMIN']), async (req, res, next) => {
  try {
    const campusId = await getAdminCampusId(req);

    const { rows } = await query(
      `SELECT o.id, o.name, o.type, o.is_open, o.slot_capacity, o.opens_at, o.accepts_orders_until,
              u.name as owner_name,
              COUNT(ord.id) as total_orders,
              (SELECT status FROM menu_sync_jobs WHERE outlet_id = o.id ORDER BY started_at DESC LIMIT 1) as last_sync_status,
              (SELECT finished_at FROM menu_sync_jobs WHERE outlet_id = o.id ORDER BY started_at DESC LIMIT 1) as last_sync_at
         FROM outlets o
    LEFT JOIN users u ON u.id = o.owner_user_id
    LEFT JOIN orders ord ON ord.outlet_id = o.id
        WHERE o.campus_id = $1
        GROUP BY o.id, u.name
        ORDER BY o.name ASC`,
      [campusId]
    );

    res.json(toCamelCase(rows));
  } catch (err) {
    next(err);
  }
});

// GET /admin/sync-jobs
router.get('/sync-jobs', auth(['ADMIN']), async (req, res, next) => {
  try {
    const { rows } = await query(
      `SELECT j.*, o.name as outlet_name,
              COUNT(p.id) as proposal_count
         FROM menu_sync_jobs j
         JOIN outlets o ON o.id = j.outlet_id
    LEFT JOIN menu_proposals p ON p.job_id = j.id
        GROUP BY j.id, o.name
        ORDER BY j.started_at DESC
        LIMIT 25`
    );

    res.json(toCamelCase(rows));
  } catch (err) {
    next(err);
  }
});

// POST /admin/slots/generate
router.post('/slots/generate', auth(['ADMIN']), async (req, res, next) => {
  try {
    const count = await generateDailySlots();
    res.json({
      success: true,
      message: `Generated ${count} pickup slots for all active outlets today`
    });
  } catch (err) {
    next(err);
  }
});

export default router;
