import crypto from 'node:crypto';
import Razorpay from 'razorpay';
import { getClient, query, toCamelCase } from '../db.js';
import { AppError } from '../middleware/error.js';
import config from '../config.js';

let razorpayClient = null;
if (config.RAZORPAY_KEY_ID && config.RAZORPAY_KEY_SECRET) {
  razorpayClient = new Razorpay({
    key_id: config.RAZORPAY_KEY_ID,
    key_secret: config.RAZORPAY_KEY_SECRET
  });
}

/**
 * Retrieves wallet balance and last 50 transactions.
 */
export async function getWallet(userId) {
  const walletRes = await query(
    'SELECT balance_paise FROM wallets WHERE user_id = $1',
    [userId]
  );
  const balancePaise = walletRes.rows.length > 0 ? parseInt(walletRes.rows[0].balance_paise, 10) : 0;

  const txnsRes = await query(
    `SELECT id, type, amount_paise, ref_order_id, note, created_at
       FROM wallet_txns
      WHERE user_id = $1
      ORDER BY created_at DESC
      LIMIT 50`,
    [userId]
  );

  return {
    balancePaise,
    transactions: toCamelCase(txnsRes.rows)
  };
}

/**
 * Creates a Razorpay order in test mode and inserts a CREATED payments row.
 */
export async function createRazorpayOrder(userId, amountPaise) {
  if (amountPaise < 1000 || amountPaise > 500000) {
    throw new AppError('VALIDATION_FAILED', 'Top-up amount must be between ₹10 and ₹5,000', 400);
  }

  if (!razorpayClient) {
    throw new AppError('RAZORPAY_NOT_CONFIGURED', 'Razorpay is not configured on this server', 400);
  }

  const receipt = `rcpt_${userId.slice(0, 8)}_${Date.now()}`;
  const rzpOrder = await razorpayClient.orders.create({
    amount: amountPaise,
    currency: 'INR',
    receipt
  });

  await query(
    `INSERT INTO payments (user_id, razorpay_order_id, amount_paise, status)
     VALUES ($1, $2, $3, 'CREATED')`,
    [userId, rzpOrder.id, amountPaise]
  );

  return {
    razorpayOrderId: rzpOrder.id,
    amountPaise,
    currency: 'INR',
    keyId: config.RAZORPAY_KEY_ID
  };
}

/**
 * Verifies Razorpay HMAC signature and credits wallet atomically.
 */
export async function verifyRazorpayPayment(userId, { razorpayOrderId, razorpayPaymentId, razorpaySignature }) {
  if (!config.RAZORPAY_KEY_SECRET) {
    throw new AppError('RAZORPAY_NOT_CONFIGURED', 'Razorpay secret is not configured', 400);
  }

  // 1. Verify HMAC-SHA256 signature
  const text = `${razorpayOrderId}|${razorpayPaymentId}`;
  const expectedSignature = crypto
    .createHmac('sha256', config.RAZORPAY_KEY_SECRET)
    .update(text)
    .digest('hex');

  const expectedBuffer = Buffer.from(expectedSignature, 'utf8');
  const actualBuffer = Buffer.from(razorpaySignature || '', 'utf8');

  if (expectedBuffer.length !== actualBuffer.length || !crypto.timingSafeEqual(expectedBuffer, actualBuffer)) {
    throw new AppError('INVALID_SIGNATURE', 'Invalid Razorpay payment signature', 400);
  }

  // 2. In one transaction: update payment to PAID, upsert wallet, insert TOPUP txn
  const client = await getClient();
  try {
    await client.query('BEGIN');

    const paymentRes = await client.query(
      `SELECT id, amount_paise, status
         FROM payments
        WHERE razorpay_order_id = $1 AND user_id = $2
        FOR UPDATE`,
      [razorpayOrderId, userId]
    );

    if (paymentRes.rows.length === 0) {
      throw new AppError('NOT_FOUND', 'Payment order not found', 404);
    }

    const payment = paymentRes.rows[0];
    if (payment.status === 'PAID') {
      throw new AppError('ALREADY_PROCESSED', 'This payment has already been credited to your wallet', 409);
    }

    await client.query(
      `UPDATE payments
          SET status = 'PAID',
              razorpay_payment_id = $1
        WHERE id = $2`,
      [razorpayPaymentId, payment.id]
    );

    const walletRes = await client.query(
      `INSERT INTO wallets (user_id, balance_paise)
       VALUES ($1, $2)
       ON CONFLICT (user_id) DO UPDATE
       SET balance_paise = wallets.balance_paise + $2
       RETURNING balance_paise`,
      [userId, payment.amount_paise]
    );

    const newBalance = parseInt(walletRes.rows[0].balance_paise, 10);

    await client.query(
      `INSERT INTO wallet_txns (user_id, type, amount_paise, note)
       VALUES ($1, 'TOPUP', $2, $3)`,
      [userId, payment.amount_paise, `Razorpay top-up (${razorpayPaymentId})`]
    );

    await client.query('COMMIT');

    return {
      success: true,
      balancePaise: newBalance,
      creditedAmountPaise: payment.amount_paise
    };
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

/**
 * Demo wallet top-up for local testing/hackathon presentations.
 */
export async function demoTopup(userId, amountPaise) {
  if (!config.ENABLE_DEMO_PAY || config.NODE_ENV === 'production') {
    throw new AppError('FORBIDDEN', 'Demo top-up is disabled in this environment', 403);
  }

  if (amountPaise < 1000 || amountPaise > 500000) {
    throw new AppError('VALIDATION_FAILED', 'Top-up amount must be between ₹10 and ₹5,000', 400);
  }

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const walletRes = await client.query(
      `INSERT INTO wallets (user_id, balance_paise)
       VALUES ($1, $2)
       ON CONFLICT (user_id) DO UPDATE
       SET balance_paise = wallets.balance_paise + $2
       RETURNING balance_paise`,
      [userId, amountPaise]
    );

    const newBalance = parseInt(walletRes.rows[0].balance_paise, 10);

    await client.query(
      `INSERT INTO wallet_txns (user_id, type, amount_paise, note)
       VALUES ($1, 'TOPUP', $2, $3)`,
      [userId, amountPaise, 'Demo wallet credit']
    );

    await client.query('COMMIT');

    return {
      success: true,
      balancePaise: newBalance,
      creditedAmountPaise: amountPaise
    };
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

export default {
  getWallet,
  createRazorpayOrder,
  verifyRazorpayPayment,
  demoTopup
};
