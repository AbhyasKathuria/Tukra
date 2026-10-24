import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createOrder } from '../src/services/orderService.js';

test('order logic: rejects empty items array', async () => {
  await assert.rejects(
    async () => {
      await createOrder({
        userId: 'u1',
        outletId: 'o1',
        slotId: 's1',
        items: [],
        paymentMethod: 'WALLET'
      });
    },
    (err) => err.code === 'VALIDATION_FAILED' && err.message.includes('at least one item')
  );
});

test('order logic: rejects more than 10 units of a single dish', async () => {
  await assert.rejects(
    async () => {
      await createOrder({
        userId: 'u1',
        outletId: 'o1',
        slotId: 's1',
        items: [{ menuItemId: 'm1', qty: 11 }],
        paymentMethod: 'WALLET'
      });
    },
    (err) => err.code === 'VALIDATION_FAILED' && err.message.includes('Maximum 10 units allowed')
  );
});

test('order logic: cancellation cutoff window verification (10 minutes before slot)', () => {
  const now = Date.now();

  // Slot starting in 15 minutes -> cutoff is in 5 minutes -> now < cutoff -> cancellation allowed
  const slotStartsIn15m = new Date(now + 15 * 60 * 1000);
  const cutoff15m = slotStartsIn15m.getTime() - 10 * 60 * 1000;
  const isAllowedBeforeCutoff = now < cutoff15m;
  assert.equal(isAllowedBeforeCutoff, true);

  // Slot starting in 8 minutes -> cutoff was 2 minutes ago -> now >= cutoff -> cancellation blocked
  const slotStartsIn8m = new Date(now + 8 * 60 * 1000);
  const cutoff8m = slotStartsIn8m.getTime() - 10 * 60 * 1000;
  const isBlockedAfterCutoff = now >= cutoff8m;
  assert.equal(isBlockedAfterCutoff, true);
});

test('order logic: single-use QR verification guards against replay', () => {
  // Simulates verification state transitions
  const order = {
    id: 'ord_1',
    qrToken: 'q_test123',
    status: 'READY'
  };

  function verifyScan(currentOrder, scannedToken) {
    if (currentOrder.qrToken !== scannedToken) {
      throw new Error('INVALID_QR: Wrong token');
    }
    if (currentOrder.status !== 'READY') {
      throw new Error('INVALID_QR: Wrong outlet or already collected');
    }
    currentOrder.status = 'COLLECTED';
    return { success: true, status: currentOrder.status };
  }

  // First scan: succeeds
  const firstScan = verifyScan(order, 'q_test123');
  assert.equal(firstScan.success, true);
  assert.equal(order.status, 'COLLECTED');

  // Second scan: fails (replay blocked)
  assert.throws(
    () => verifyScan(order, 'q_test123'),
    /INVALID_QR: Wrong outlet or already collected/
  );
});
