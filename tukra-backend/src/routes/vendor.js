import express from 'express';
import { z } from 'zod';
import { auth } from '../middleware/auth.js';
import { validate } from '../middleware/validate.js';
import { query, toCamelCase, getClient } from '../db.js';
import { AppError } from '../middleware/error.js';
import { updateOrderStatus, verifyQrToken } from '../services/orderService.js';
import { runTinyFishSync } from '../services/tinyfish.js';
import { diffMenu } from '../services/menuDiff.js';

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

const proposalDecisionSchema = z.object({
  approve: z.boolean()
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

// GET /vendor/outlet
router.get('/outlet', auth(['VENDOR', 'ADMIN']), async (req, res, next) => {
  try {
    const outletId = await getVendorOutletId(req.user);
    const result = await query('SELECT * FROM outlets WHERE id = $1', [outletId]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Outlet not found' } });
    }
    res.json(toCamelCase(result.rows[0]));
  } catch (err) {
    next(err);
  }
});

// GET /vendor/items
router.get('/items', auth(['VENDOR', 'ADMIN']), async (req, res, next) => {
  try {
    const outletId = await getVendorOutletId(req.user);
    const sql = `
      SELECT m.id, m.outlet_id, m.category_id, m.name, m.description,
             m.price_paise, m.is_veg, m.image_url, m.prep_seconds,
             m.prep_units, m.is_available, m.spice_level,
             c.name as category_name
        FROM menu_items m
   LEFT JOIN categories c ON c.id = m.category_id
       WHERE m.outlet_id = $1
       ORDER BY m.name ASC
    `;
    const { rows } = await query(sql, [outletId]);
    res.json(toCamelCase(rows));
  } catch (err) {
    next(err);
  }
});

// GET /vendor/orders?status=
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

// PATCH /vendor/items/:id
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

// POST /vendor/items
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

// PATCH /vendor/outlet
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

// -------------------------------------------------------------
// TINYFISH AGENT MENU SYNC & PROPOSALS
// -------------------------------------------------------------

// POST /vendor/sync (Starts background TinyFish menu sync -> 202 { jobId })
router.post('/sync', auth(['VENDOR', 'ADMIN']), async (req, res, next) => {
  try {
    const outletId = await getVendorOutletId(req.user);

    const outletRes = await query('SELECT id, name, source_menu_url FROM outlets WHERE id = $1', [outletId]);
    const outlet = outletRes.rows[0];

    if (!outlet.source_menu_url) {
      return res.status(400).json({
        error: { code: 'NO_SOURCE_URL', message: 'No public menu URL configured for this outlet' }
      });
    }

    // 1. Insert RUNNING sync job
    const jobRes = await query(
      `INSERT INTO menu_sync_jobs (outlet_id, status, progress)
       VALUES ($1, 'RUNNING', 'Initializing TinyFish Web Agent...')
       RETURNING id`,
      [outletId]
    );
    const jobId = jobRes.rows[0].id;

    // 2. Respond immediately with 202 Accepted
    res.status(202).json({ jobId, status: 'RUNNING' });

    // 3. Execute in background
    runTinyFishSync(outlet.source_menu_url, {
      onStarted: async (runId) => {
        await query('UPDATE menu_sync_jobs SET tinyfish_run_id = $1, progress = $2 WHERE id = $3', [
          runId,
          'Agent started browser session...',
          jobId
        ]);
      },
      onStreamingUrl: async (streamingUrl, runId) => {
        await query('UPDATE menu_sync_jobs SET streaming_url = $1, tinyfish_run_id = $2 WHERE id = $3', [
          streamingUrl,
          runId,
          jobId
        ]);
      },
      onProgress: async (progressText) => {
        await query('UPDATE menu_sync_jobs SET progress = $1 WHERE id = $2', [progressText, jobId]);
      },
      onComplete: async (items, runId) => {
        try {
          const currentRes = await query(
            'SELECT id, name, price_paise, is_veg, is_available FROM menu_items WHERE outlet_id = $1',
            [outletId]
          );
          const proposals = diffMenu(items, currentRes.rows);

          const client = await getClient();
          try {
            await client.query('BEGIN');

            for (const p of proposals) {
              await client.query(
                `INSERT INTO menu_proposals (job_id, outlet_id, change_type, menu_item_id, item_name, old_price_paise, new_price_paise, flagged, status)
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 'PENDING')`,
                [jobId, outletId, p.changeType, p.menuItemId, p.itemName, p.oldPricePaise, p.newPricePaise, p.flagged]
              );
            }

            await client.query(
              `UPDATE menu_sync_jobs
                  SET status = 'COMPLETED',
                      progress = 'Sync complete. Review proposed changes.',
                      finished_at = now(),
                      tinyfish_run_id = $1,
                      num_steps = $2
                WHERE id = $3`,
              [runId, items.length, jobId]
            );

            await client.query('COMMIT');
          } catch (dbErr) {
            await client.query('ROLLBACK');
            throw dbErr;
          } finally {
            client.release();
          }
        } catch (err) {
          console.error('[SYNC COMPLETE ERROR]', err);
          await query('UPDATE menu_sync_jobs SET status = \'FAILED\', error_message = $1 WHERE id = $2', [err.message, jobId]);
        }
      },
      onError: async (err) => {
        console.error('[SYNC JOB FAILED]', err.message);
        await query('UPDATE menu_sync_jobs SET status = \'FAILED\', error_message = $1, finished_at = now() WHERE id = $2', [
          err.message,
          jobId
        ]);
      }
    });
  } catch (err) {
    next(err);
  }
});

// GET /vendor/sync/:jobId (Poll sync status & live streaming URL)
router.get('/sync/:jobId', auth(['VENDOR', 'ADMIN']), async (req, res, next) => {
  try {
    const jobRes = await query(
      `SELECT j.*, COUNT(p.id) as proposal_count
         FROM menu_sync_jobs j
    LEFT JOIN menu_proposals p ON p.job_id = j.id
        WHERE j.id = $1
        GROUP BY j.id`,
      [req.params.jobId]
    );

    if (jobRes.rows.length === 0) {
      return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Sync job not found' } });
    }

    const job = jobRes.rows[0];
    res.json({
      id: job.id,
      status: job.status,
      progress: job.progress,
      streamingUrl: job.streaming_url,
      numSteps: job.num_steps,
      proposalCount: parseInt(job.proposal_count, 10),
      errorMessage: job.error_message
    });
  } catch (err) {
    next(err);
  }
});

// GET /vendor/proposals (Pending AI extracted changes awaiting human approval)
router.get('/proposals', auth(['VENDOR', 'ADMIN']), async (req, res, next) => {
  try {
    const outletId = await getVendorOutletId(req.user);

    const { rows } = await query(
      `SELECT p.id, p.job_id, p.change_type, p.menu_item_id, p.item_name,
              p.old_price_paise, p.new_price_paise, p.flagged, p.status,
              p.decided_at, m.image_url
         FROM menu_proposals p
    LEFT JOIN menu_items m ON m.id = p.menu_item_id
        WHERE p.outlet_id = $1 AND p.status = 'PENDING'
        ORDER BY p.flagged DESC, p.item_name ASC`,
      [outletId]
    );

    res.json(toCamelCase(rows));
  } catch (err) {
    next(err);
  }
});

// POST /vendor/proposals/:id/decision (Human Vendor Review: Approve or Reject)
router.post('/proposals/:id/decision', auth(['VENDOR', 'ADMIN']), validate(proposalDecisionSchema), async (req, res, next) => {
  const { id } = req.params;
  const { approve } = req.body;
  const userId = req.user.id;

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const propRes = await client.query(
      'SELECT * FROM menu_proposals WHERE id = $1 FOR UPDATE',
      [id]
    );

    if (propRes.rows.length === 0) {
      throw new AppError('NOT_FOUND', 'Proposal not found', 404);
    }

    const proposal = propRes.rows[0];
    if (proposal.status !== 'PENDING') {
      throw new AppError('ALREADY_HANDLED', 'Proposal already handled', 409);
    }

    if (approve) {
      if (proposal.change_type === 'NEW') {
        // Insert new item
        await client.query(
          `INSERT INTO menu_items (outlet_id, name, price_paise, is_veg, is_available)
           VALUES ($1, $2, $3, true, true)`,
          [proposal.outlet_id, proposal.item_name, proposal.new_price_paise || 0]
        );
      } else if (proposal.change_type === 'PRICE_CHANGE') {
        await client.query(
          `UPDATE menu_items
              SET price_paise = $1,
                  updated_at = now()
            WHERE id = $2`,
          [proposal.new_price_paise, proposal.menu_item_id]
        );
      } else if (proposal.change_type === 'REMOVED') {
        await client.query(
          `UPDATE menu_items
              SET is_available = false,
                  updated_at = now()
            WHERE id = $1`,
          [proposal.menu_item_id]
        );
      } else if (proposal.change_type === 'AVAILABILITY') {
        await client.query(
          `UPDATE menu_items
              SET is_available = NOT is_available,
                  updated_at = now()
            WHERE id = $1`,
          [proposal.menu_item_id]
        );
      }

      await client.query(
        `UPDATE menu_proposals
            SET status = 'APPROVED',
                decided_by = $1,
                decided_at = now()
          WHERE id = $2`,
        [userId, id]
      );
    } else {
      await client.query(
        `UPDATE menu_proposals
            SET status = 'REJECTED',
                decided_by = $1,
                decided_at = now()
          WHERE id = $2`,
        [userId, id]
      );
    }

    await client.query('COMMIT');
    res.json({ id, status: approve ? 'APPROVED' : 'REJECTED' });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
});

// POST /vendor/proposals/approve-all
router.post('/proposals/approve-all', auth(['VENDOR', 'ADMIN']), async (req, res, next) => {
  const userId = req.user.id;
  const client = await getClient();
  try {
    await client.query('BEGIN');

    const outletId = await getVendorOutletId(req.user);

    const pendingRes = await client.query(
      "SELECT * FROM menu_proposals WHERE outlet_id = $1 AND status = 'PENDING' FOR UPDATE",
      [outletId]
    );

    let approvedCount = 0;
    for (const proposal of pendingRes.rows) {
      if (proposal.change_type === 'NEW') {
        await client.query(
          `INSERT INTO menu_items (outlet_id, name, price_paise, is_veg, is_available)
           VALUES ($1, $2, $3, true, true)`,
          [proposal.outlet_id, proposal.item_name, proposal.new_price_paise || 0]
        );
      } else if (proposal.change_type === 'PRICE_CHANGE') {
        await client.query(
          'UPDATE menu_items SET price_paise = $1, updated_at = now() WHERE id = $2',
          [proposal.new_price_paise, proposal.menu_item_id]
        );
      } else if (proposal.change_type === 'REMOVED') {
        await client.query(
          'UPDATE menu_items SET is_available = false, updated_at = now() WHERE id = $1',
          [proposal.menu_item_id]
        );
      } else if (proposal.change_type === 'AVAILABILITY') {
        await client.query(
          'UPDATE menu_items SET is_available = NOT is_available, updated_at = now() WHERE id = $1',
          [proposal.menu_item_id]
        );
      }

      await client.query(
        "UPDATE menu_proposals SET status = 'APPROVED', decided_by = $1, decided_at = now() WHERE id = $2",
        [userId, proposal.id]
      );
      approvedCount++;
    }

    await client.query('COMMIT');
    res.json({ success: true, approvedCount });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
});

export default router;
