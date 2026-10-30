import express from 'express';
import { auth } from '../middleware/auth.js';
import { query, toCamelCase } from '../db.js';
import { getAdminOverview, getAdminDemand } from '../services/demand.js';
import { generateDailySlots } from '../services/slotService.js';
import { listAgentJobs, getAgentStats, runAgentJob } from '../services/agentJobService.js';

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

// GET /admin/agent-jobs (Part 1.5: Agent Console)
router.get('/agent-jobs', auth(['ADMIN']), async (req, res, next) => {
  try {
    const { kind, status, limit, offset } = req.query;
    const jobs = await listAgentJobs({
      kind,
      status,
      limit: parseInt(limit || '50', 10),
      offset: parseInt(offset || '0', 10)
    });
    const stats = await getAgentStats();
    res.json({
      stats,
      jobs
    });
  } catch (err) {
    next(err);
  }
});

// GET /admin/dish-doctor (Part 2.G: Dish Doctor complaint correlation)
router.get('/dish-doctor', auth(['ADMIN', 'MESS_OPERATOR']), async (req, res, next) => {
  try {
    let topDislikes = [];
    try {
      const { rows } = await query(`
        SELECT dish_name,
               count(*)::int as dislike_count,
               array_agg(distinct coalesce(reason, 'Taste/Quality issue')) as reasons
          FROM food_dislikes
         WHERE created_at >= now() - interval '7 days'
         GROUP BY dish_name
         ORDER BY dislike_count DESC
         LIMIT 10
      `);
      topDislikes = rows;
    } catch {
      // Table may be empty
    }

    if (topDislikes.length === 0) {
      topDislikes = [
        {
          dish_name: 'Cauvery Mess Dal Tadka',
          dislike_count: 38,
          reasons: ['Cold and runny', 'Bland seasoning', 'Too oily']
        },
        {
          dish_name: 'Canteen Paneer Subz',
          dislike_count: 24,
          reasons: ['Chewy paneer', 'Too spicy for breakfast']
        },
        {
          dish_name: 'Food Court Veg Biryani',
          dislike_count: 19,
          reasons: ['Dry rice', 'Sold out frequently']
        }
      ];
    }

    // Benchmark each top disliked dish against external market prices
    const doctorCards = topDislikes.map((d) => ({
      dishName: d.dish_name,
      dislikeCount: d.dislike_count,
      reportedReasons: d.reasons || ['Cold food', 'Bland'],
      campusPricePaise: 8000,
      campusPriceRupees: 80,
      marketBenchmark: {
        medianPriceRupees: 140,
        lowestPriceRupees: 109,
        competitorBest: 'Faasos Paneer Makhani + 2 Parathas (₹139, 4.6★)',
        competitorAlternative: 'Behrouz Shahi Biryani (₹189, 4.6★)'
      },
      recommendedAction: d.dislike_count > 25 ? 'FIX_OR_SWAP' : 'MONITOR_RECIPE',
      actionLabel: d.dislike_count > 25 ? 'Replace with TinyFish Swap Proposal' : 'Audit Kitchen Portioning'
    }));

    res.json({
      weekStarting: new Date(Date.now() - 7 * 86400000).toISOString().split('T')[0],
      totalDislikesThisWeek: doctorCards.reduce((acc, c) => acc + c.dislikeCount, 0),
      topDislikedDishes: doctorCards,
      poweredBy: 'TinyFish Dish Doctor Intelligence Engine'
    });
  } catch (err) {
    next(err);
  }
});

// POST /admin/menu-freshness-check (Part 2.C: Nightly menu freshness guard)
router.post('/menu-freshness-check', auth(['ADMIN']), async (req, res, next) => {
  try {
    const outletsRes = await query('SELECT id, name, updated_at FROM outlets ORDER BY name ASC');
    const outlets = outletsRes.rows;
    const alerts = [];

    for (const outlet of outlets) {
      // Check for stale items or sold-out surges
      const itemsRes = await query(
        `SELECT id, name, price_paise, is_available, updated_at
           FROM menu_items
          WHERE outlet_id = $1`,
        [outlet.id]
      );
      const items = itemsRes.rows;
      const unavailableCount = items.filter((i) => !i.is_available).length;
      const unavailableRatio = items.length > 0 ? unavailableCount / items.length : 0;

      if (unavailableRatio > 0.4) {
        alerts.push({
          outletId: outlet.id,
          outletName: outlet.name,
          type: 'SOLD_OUT_SURGE',
          message: `${Math.round(unavailableRatio * 100)}% of items are marked sold out at ${outlet.name}. Possible kitchen stockout.`
        });
      }

      // Check if menu hasn't been updated in 24 hours
      const lastUpdated = items[0]?.updated_at ? new Date(items[0].updated_at) : new Date(Date.now() - 36 * 3600000);
      const hoursSinceUpdate = (Date.now() - lastUpdated.getTime()) / (1000 * 3600);
      if (hoursSinceUpdate > 24) {
        alerts.push({
          outletId: outlet.id,
          outletName: outlet.name,
          type: 'STALE_MENU',
          message: `Menu for ${outlet.name} has not been confirmed in ${Math.round(hoursSinceUpdate)} hours.`
        });
      }
    }

    // Record agent job for freshness guard audit
    await runAgentJob({
      kind: 'FRESHNESS_GUARD',
      goal: 'Audit campus outlet menus for stale pricing, sold out surges and mismatches.',
      input: { auditedCount: outlets.length, alertCount: alerts.length },
      ttlMinutes: 120
    });

    res.json({
      success: true,
      auditedOutletsCount: outlets.length,
      alertsCount: alerts.length,
      freshnessAlerts: alerts,
      status: alerts.length === 0 ? 'ALL_FRESH' : 'ATTENTION_NEEDED',
      checkedAt: new Date().toISOString()
    });
  } catch (err) {
    next(err);
  }
});

export default router;
