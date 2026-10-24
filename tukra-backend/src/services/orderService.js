import crypto from 'node:crypto';
import { getClient, query, toCamelCase } from '../db.js';
import { AppError } from '../middleware/error.js';
import { sendPush } from './push.js';
import config from '../config.js';

// Server-enforced valid transitions
export const VALID_TRANSITIONS = {
  PLACED: ['ACCEPTED', 'REJECTED'],
  ACCEPTED: ['PREPARING', 'REJECTED'],
  PREPARING: ['READY'],
  READY: ['COLLECTED', 'NO_SHOW'],
  COLLECTED: [],
  CANCELLED: [],
  REJECTED: [],
  NO_SHOW: []
};

/**
 * Creates an order atomically in a single transaction.
 */
export async function createOrder({ userId, outletId, slotId, items, paymentMethod, note }) {
  if (!items || !Array.isArray(items) || items.length === 0) {
    throw new AppError('VALIDATION_FAILED', 'Order must contain at least one item', 400);
  }

  // Validate item quantities (max 10 per dish)
  for (const item of items) {
    if (!item.menuItemId || typeof item.qty !== 'number' || item.qty <= 0) {
      throw new AppError('VALIDATION_FAILED', 'Invalid item or quantity', 400);
    }
    if (item.qty > 10) {
      throw new AppError('VALIDATION_FAILED', `Maximum 10 units allowed per dish: ${item.menuItemId}`, 400);
    }
  }

  const client = await getClient();
  try {
    await client.query('BEGIN');

    // 1. Verify outlet is open
    const outletRes = await client.query(
      'SELECT id, name, is_open, opens_at, accepts_orders_until FROM outlets WHERE id = $1 FOR SHARE',
      [outletId]
    );
    if (outletRes.rows.length === 0) {
      throw new AppError('NOT_FOUND', 'Outlet not found', 404);
    }
    const outlet = outletRes.rows[0];
    if (!outlet.is_open) {
      throw new AppError('OUTLET_CLOSED', 'This outlet is currently closed', 409);
    }

    // 2. Fetch all menu items from database to price lines server-side
    const menuItemIds = items.map((i) => i.menuItemId);
    const dbItemsRes = await client.query(
      `SELECT id, name, price_paise, prep_units, is_veg, is_available
         FROM menu_items
        WHERE id = ANY($1) AND outlet_id = $2`,
      [menuItemIds, outletId]
    );

    const dbItemMap = new Map(dbItemsRes.rows.map((row) => [row.id, row]));

    let subtotalPaise = 0;
    let totalPrepUnits = 0;
    const orderLines = [];

    for (const item of items) {
      const dbItem = dbItemMap.get(item.menuItemId);
      if (!dbItem) {
        throw new AppError('ITEM_UNAVAILABLE', `Item ${item.menuItemId} is not on this outlet's menu`, 409);
      }
      if (!dbItem.is_available) {
        throw new AppError('ITEM_UNAVAILABLE', `Item "${dbItem.name}" is currently sold out`, 409);
      }

      const lineTotal = dbItem.price_paise * item.qty;
      subtotalPaise += lineTotal;
      totalPrepUnits += (dbItem.prep_units || 1) * item.qty;

      orderLines.push({
        menuItemId: dbItem.id,
        nameSnapshot: dbItem.name,
        unitPricePaise: dbItem.price_paise,
        qty: item.qty
      });
    }

    // Enforce max prep units per order (max 20 units)
    if (totalPrepUnits > 20) {
      throw new AppError('VALIDATION_FAILED', `Order exceeds maximum kitchen capacity limit of 20 prep units (requested ${totalPrepUnits})`, 400);
    }

    const totalPaise = subtotalPaise; // GST-inclusive

    // 3. Atomically reserve slot capacity
    const slotRes = await client.query(
      `UPDATE slots
          SET used = used + $1
        WHERE id = $2
          AND outlet_id = $3
          AND used + $1 <= capacity
          AND starts_at > now()
        RETURNING id, starts_at`,
      [totalPrepUnits, slotId, outletId]
    );

    if (slotRes.rows.length === 0) {
      throw new AppError('SLOT_FULL', 'That pickup slot just filled up. Please select the next available slot.', 409);
    }
    const slotStartsAt = slotRes.rows[0].starts_at;

    // 4. Process payment
    if (paymentMethod === 'WALLET') {
      const walletRes = await client.query(
        `UPDATE wallets
            SET balance_paise = balance_paise - $1
          WHERE user_id = $2 AND balance_paise >= $1
          RETURNING balance_paise`,
        [totalPaise, userId]
      );
      if (walletRes.rows.length === 0) {
        throw new AppError('INSUFFICIENT_BALANCE', 'Insufficient wallet balance for this order', 402);
      }
    } else if (paymentMethod === 'DEMO') {
      if (!config.ENABLE_DEMO_PAY) {
        throw new AppError('FORBIDDEN', 'Demo payment is disabled in this environment', 403);
      }
    } else {
      throw new AppError('VALIDATION_FAILED', `Payment method ${paymentMethod} is not supported directly for order checkout`, 400);
    }

    // 5. Generate unique TK-#### order code with retry up to 5 times
    let code = null;
    let orderRow = null;
    let attempts = 0;
    const qrToken = 'q_' + crypto.randomBytes(12).toString('hex');

    while (attempts < 5) {
      attempts++;
      const randomDigits = Math.floor(1000 + Math.random() * 9000);
      const testCode = `TK-${randomDigits}`;

      try {
        const insertOrderRes = await client.query(
          `INSERT INTO orders (code, user_id, outlet_id, slot_id, status, subtotal_paise, total_paise, prep_units, payment_method, note, qr_token)
           VALUES ($1, $2, $3, $4, 'PLACED', $5, $6, $7, $8, $9, $10)
           RETURNING id, code, created_at, status`,
          [testCode, userId, outletId, slotId, subtotalPaise, totalPaise, totalPrepUnits, paymentMethod, note || null, qrToken]
        );
        code = testCode;
        orderRow = insertOrderRes.rows[0];
        break;
      } catch (err) {
        if (err.code === '23505' && err.constraint?.includes('code')) {
          continue; // Unique code collision, retry with new random digits
        }
        throw err;
      }
    }

    if (!orderRow) {
      throw new AppError('INTERNAL', 'Failed to generate unique order identifier', 500);
    }

    // 6. Insert frozen order items
    for (const line of orderLines) {
      await client.query(
        `INSERT INTO order_items (order_id, menu_item_id, name_snapshot, unit_price_paise, qty)
         VALUES ($1, $2, $3, $4, $5)`,
        [orderRow.id, line.menuItemId, line.nameSnapshot, line.unitPricePaise, line.qty]
      );
    }

    // 7. Insert wallet ledger transaction (negative amount for order debit)
    if (paymentMethod === 'WALLET' || paymentMethod === 'DEMO') {
      await client.query(
        `INSERT INTO wallet_txns (user_id, type, amount_paise, ref_order_id, note)
         VALUES ($1, 'ORDER', $2, $3, $4)`,
        [userId, -totalPaise, orderRow.id, `Order ${code} at ${outlet.name}`]
      );
    }

    await client.query('COMMIT');

    // Notify vendor about new order
    if (outlet.owner_user_id) {
      sendPush(outlet.owner_user_id, {
        title: `New Order ${code}!`,
        body: `${orderLines.length} item(s) • ₹${(totalPaise / 100).toFixed(2)}`,
        data: { orderId: orderRow.id, code }
      }).catch(() => {});
    }

    // Return flat shape per Part F2
    return {
      id: orderRow.id,
      code: orderRow.code,
      status: orderRow.status,
      totalPaise,
      qrToken,
      slotStartsAt: new Date(slotStartsAt).toISOString(),
      outletName: outlet.name,
      note: note || null,
      items: orderLines.map((l) => ({
        name: l.nameSnapshot,
        qty: l.qty,
        unitPricePaise: l.unitPricePaise
      }))
    };
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

/**
 * Cancels an order and refunds payment (allowed only if PLACED/ACCEPTED and > 10 min before slot).
 */
export async function cancelOrder(orderId, userId) {
  const client = await getClient();
  try {
    await client.query('BEGIN');

    const orderRes = await client.query(
      `SELECT o.id, o.user_id, o.slot_id, o.status, o.total_paise, o.prep_units, o.code,
              o.payment_method, s.starts_at, outl.name as outlet_name
         FROM orders o
         JOIN slots s ON s.id = o.slot_id
         JOIN outlets outl ON outl.id = o.outlet_id
        WHERE o.id = $1 AND o.user_id = $2
        FOR UPDATE`,
      [orderId, userId]
    );

    if (orderRes.rows.length === 0) {
      throw new AppError('NOT_FOUND', 'Order not found', 404);
    }

    const order = orderRes.rows[0];

    if (!['PLACED', 'ACCEPTED'].includes(order.status)) {
      throw new AppError('CANNOT_CANCEL', `Order cannot be cancelled in status ${order.status}`, 409);
    }

    // Cutoff: up to 10 minutes before slot start
    const slotTime = new Date(order.starts_at).getTime();
    const cutoffTime = slotTime - 10 * 60 * 1000;
    if (Date.now() >= cutoffTime) {
      throw new AppError('CANCEL_WINDOW_EXPIRED', 'Free cancellation is only allowed up to 10 minutes before the pickup window', 409);
    }

    // 1. Update status
    await client.query("UPDATE orders SET status = 'CANCELLED' WHERE id = $1", [order.id]);

    // 2. Release slot capacity
    await client.query(
      'UPDATE slots SET used = GREATEST(0, used - $1) WHERE id = $2',
      [order.prep_units, order.slot_id]
    );

    // 3. Refund wallet
    if (order.payment_method === 'WALLET' || order.payment_method === 'DEMO') {
      await client.query(
        'UPDATE wallets SET balance_paise = balance_paise + $1 WHERE user_id = $2',
        [order.total_paise, order.user_id]
      );
      await client.query(
        `INSERT INTO wallet_txns (user_id, type, amount_paise, ref_order_id, note)
         VALUES ($1, 'REFUND', $2, $3, $4)`,
        [order.user_id, order.total_paise, order.id, `Refund for cancelled order ${order.code}`]
      );
    }

    await client.query('COMMIT');
    return { success: true, message: `Order ${order.code} cancelled and ₹${(order.total_paise / 100).toFixed(2)} refunded to wallet` };
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

/**
 * Transitions order status through the server-enforced state machine.
 */
export async function updateOrderStatus(orderId, nextStatus, vendorUserId, rejectReason = null) {
  const client = await getClient();
  try {
    await client.query('BEGIN');

    // Verify vendor owns this outlet
    const orderRes = await client.query(
      `SELECT o.id, o.user_id, o.outlet_id, o.slot_id, o.status, o.total_paise,
              o.prep_units, o.code, o.payment_method, outl.owner_user_id, outl.name as outlet_name
         FROM orders o
         JOIN outlets outl ON outl.id = o.outlet_id
        WHERE o.id = $1
        FOR UPDATE`,
      [orderId]
    );

    if (orderRes.rows.length === 0) {
      throw new AppError('NOT_FOUND', 'Order not found', 404);
    }

    const order = orderRes.rows[0];

    // Must be the owner of the outlet or an admin
    if (order.owner_user_id !== vendorUserId) {
      const adminCheck = await client.query("SELECT role FROM users WHERE id = $1 AND role = 'ADMIN'", [vendorUserId]);
      if (adminCheck.rows.length === 0) {
        throw new AppError('FORBIDDEN', 'You do not have permission to manage orders for this outlet', 403);
      }
    }

    const currentStatus = order.status;
    const allowedNext = VALID_TRANSITIONS[currentStatus] || [];
    if (!allowedNext.includes(nextStatus)) {
      throw new AppError('INVALID_STATE_TRANSITION', `Cannot transition order from ${currentStatus} to ${nextStatus}`, 400);
    }

    if (nextStatus === 'REJECTED') {
      // Rejection triggers refund and releases slot capacity
      await client.query(
        "UPDATE orders SET status = 'REJECTED', reject_reason = $1 WHERE id = $2",
        [rejectReason || 'Order rejected by vendor', order.id]
      );

      await client.query(
        'UPDATE slots SET used = GREATEST(0, used - $1) WHERE id = $2',
        [order.prep_units, order.slot_id]
      );

      if (order.payment_method === 'WALLET' || order.payment_method === 'DEMO') {
        await client.query(
          'UPDATE wallets SET balance_paise = balance_paise + $1 WHERE user_id = $2',
          [order.total_paise, order.user_id]
        );
        await client.query(
          `INSERT INTO wallet_txns (user_id, type, amount_paise, ref_order_id, note)
           VALUES ($1, 'REFUND', $2, $3, $4)`,
          [order.user_id, order.total_paise, order.id, `Refund for rejected order ${order.code}: ${rejectReason || 'Vendor rejected'}`]
        );
      }

      await client.query('COMMIT');

      sendPush(order.user_id, {
        title: `Order ${order.code} Rejected`,
        body: `Your order was rejected: ${rejectReason || 'Vendor unable to prepare'}. ₹${(order.total_paise / 100).toFixed(2)} refunded.`,
        data: { orderId: order.id, code: order.code, status: 'REJECTED' }
      }).catch(() => {});

      return { id: order.id, status: 'REJECTED', rejectReason };
    }

    const updates = ["status = $1"];
    const values = [nextStatus];
    let idx = 2;

    if (nextStatus === 'READY') {
      updates.push(`ready_at = now()`);
    } else if (nextStatus === 'COLLECTED') {
      updates.push(`collected_at = now()`);
    }

    values.push(order.id);
    await client.query(`UPDATE orders SET ${updates.join(', ')} WHERE id = $${idx}`, values);

    await client.query('COMMIT');

    // Push notification to student on READY
    if (nextStatus === 'READY') {
      sendPush(order.user_id, {
        title: `Order ${order.code} is Ready!`,
        body: 'Your order is hot and ready. Show your QR at the counter.',
        data: { orderId: order.id, code: order.code, status: 'READY' }
      }).catch(() => {});
    }

    return { id: order.id, status: nextStatus };
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

/**
 * Single-use QR verification at pickup counter.
 */
export async function verifyQrToken(qrToken, vendorUserId) {
  const client = await getClient();
  try {
    await client.query('BEGIN');

    // Find vendor's outlet
    const outletRes = await client.query('SELECT id, name FROM outlets WHERE owner_user_id = $1 LIMIT 1', [vendorUserId]);
    if (outletRes.rows.length === 0) {
      throw new AppError('FORBIDDEN', 'No outlet found registered to this vendor', 403);
    }
    const vendorOutletId = outletRes.rows[0].id;

    const orderRes = await client.query(
      `SELECT o.id, o.code, o.outlet_id, o.status, o.total_paise, o.user_id, u.name as student_name
         FROM orders o
         JOIN users u ON u.id = o.user_id
        WHERE o.qr_token = $1
        FOR UPDATE`,
      [qrToken]
    );

    if (orderRes.rows.length === 0) {
      throw new AppError('INVALID_QR', 'Invalid or unrecognised QR code', 409);
    }

    const order = orderRes.rows[0];

    // Must belong to this vendor's outlet
    if (order.outlet_id !== vendorOutletId) {
      throw new AppError('INVALID_QR', 'Wrong outlet or already collected', 409);
    }

    // Must be in READY state
    if (order.status !== 'READY') {
      throw new AppError('INVALID_QR', 'Wrong outlet or already collected', 409);
    }

    // Mark as COLLECTED
    await client.query("UPDATE orders SET status = 'COLLECTED', collected_at = now() WHERE id = $1", [order.id]);

    // Fetch line items for counter display
    const itemsRes = await client.query(
      'SELECT name_snapshot as name, unit_price_paise, qty FROM order_items WHERE order_id = $1',
      [order.id]
    );

    await client.query('COMMIT');

    sendPush(order.user_id, {
      title: `Order ${order.code} Collected`,
      body: 'Enjoy your meal! Take your tukra.',
      data: { orderId: order.id, code: order.code, status: 'COLLECTED' }
    }).catch(() => {});

    return {
      orderId: order.id,
      code: order.code,
      studentName: order.student_name,
      status: 'COLLECTED',
      totalPaise: order.total_paise,
      items: toCamelCase(itemsRes.rows)
    };
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

/**
 * Marks READY orders older than 20 minutes as NO_SHOW (no refund).
 */
export async function markNoShowOrders() {
  const result = await query(
    `UPDATE orders
        SET status = 'NO_SHOW'
      WHERE status = 'READY'
        AND ready_at < now() - interval '20 minutes'
      RETURNING id, code`
  );
  if (result.rows.length > 0) {
    console.log(`⏱️ [CRON] Marked ${result.rows.length} order(s) as NO_SHOW: ${result.rows.map((r) => r.code).join(', ')}`);
  }
  return result.rows.length;
}

export default {
  createOrder,
  cancelOrder,
  updateOrderStatus,
  verifyQrToken,
  markNoShowOrders,
  VALID_TRANSITIONS
};
