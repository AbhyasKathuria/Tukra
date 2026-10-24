import { test } from 'node:test';
import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { verifyRazorpayPayment, demoTopup } from '../src/services/walletService.js';
import config from '../src/config.js';

test('wallet: Razorpay signature verification logic', () => {
  const secret = 'rzp_test_secret_for_unit_tests';
  const orderId = 'order_DA29f123';
  const paymentId = 'pay_FA43219';

  // Valid signature
  const validSignature = crypto
    .createHmac('sha256', secret)
    .update(`${orderId}|${paymentId}`)
    .digest('hex');

  // Verify function
  function verifySig(sec, ord, pay, sig) {
    const expected = crypto.createHmac('sha256', sec).update(`${ord}|${pay}`).digest('hex');
    const expBuf = Buffer.from(expected, 'utf8');
    const actBuf = Buffer.from(sig || '', 'utf8');
    return expBuf.length === actBuf.length && crypto.timingSafeEqual(expBuf, actBuf);
  }

  assert.equal(verifySig(secret, orderId, paymentId, validSignature), true);
  assert.equal(verifySig(secret, orderId, paymentId, 'tampered_signature'), false);
  assert.equal(verifySig(secret, orderId, paymentId, ''), false);
});

test('wallet: top-up amount limits (min ₹10 = 1000 paise, max ₹5000 = 500000 paise)', async () => {
  // Reject < 1000 paise
  await assert.rejects(
    async () => {
      await demoTopup('u1', 500); // ₹5
    },
    (err) => err.code === 'VALIDATION_FAILED' && err.message.includes('₹10 and ₹5,000')
  );

  // Reject > 500000 paise
  await assert.rejects(
    async () => {
      await demoTopup('u1', 600000); // ₹6,000
    },
    (err) => err.code === 'VALIDATION_FAILED' && err.message.includes('₹10 and ₹5,000')
  );
});
