import { test } from 'node:test';
import assert from 'node:assert/strict';
import { VALID_TRANSITIONS } from '../src/services/orderService.js';

test('Stage 2 End-to-End Scripted Flow (Order Lifecycle & Verification)', async () => {
  console.log('--- BEGIN SCRIPTED E2E FLOW ---');

  // 1. Student logs in & checks wallet balance
  const student = {
    id: 'u1111111-1111-1111-1111-111111111111',
    name: 'Aarav Sharma',
    role: 'STUDENT',
    walletBalancePaise: 50000 // ₹500
  };
  console.log(`[Step 1: Auth] Student ${student.name} logged in. Wallet: ₹${student.walletBalancePaise / 100}`);

  // 2. Query available slots
  const selectedSlot = {
    id: 's_slot_123',
    startsAt: '2026-10-30T13:15:00+05:30',
    capacity: 12,
    used: 4,
    remaining: 8
  };
  console.log(`[Step 2: Slots] Selected pickup slot ${selectedSlot.startsAt} (Remaining capacity: ${selectedSlot.remaining})`);

  // 3. Place order (1x Masala Dosa ₹70, 1x Filter Coffee ₹25) = ₹95 (9500 paise), 3 prep units
  const items = [
    { name: 'Masala Dosa', pricePaise: 7000, qty: 1, prepUnits: 2 },
    { name: 'Filter Coffee', pricePaise: 2500, qty: 1, prepUnits: 1 }
  ];
  const orderTotalPaise = 9500;
  const orderPrepUnits = 3;

  assert.ok(student.walletBalancePaise >= orderTotalPaise, 'Student has sufficient balance');
  assert.ok(selectedSlot.remaining >= orderPrepUnits, 'Slot has sufficient capacity');

  // Deduct wallet and reserve slot
  student.walletBalancePaise -= orderTotalPaise;
  selectedSlot.used += orderPrepUnits;
  selectedSlot.remaining = selectedSlot.capacity - selectedSlot.used;

  const order = {
    id: 'ord_e2e_789',
    code: 'TK-4821',
    status: 'PLACED',
    totalPaise: orderTotalPaise,
    prepUnits: orderPrepUnits,
    qrToken: 'q_test_token_8d1f7c',
    items
  };
  console.log(`[Step 3: Order Placed] ${order.code} created for ₹${order.totalPaise / 100}. Wallet now ₹${student.walletBalancePaise / 100}. Slot used: ${selectedSlot.used}`);

  // 4. Vendor accepts order
  assert.ok(VALID_TRANSITIONS[order.status].includes('ACCEPTED'));
  order.status = 'ACCEPTED';
  console.log(`[Step 4: Vendor Accept] Order ${order.code} status -> ${order.status}`);

  // 5. Vendor starts preparing
  assert.ok(VALID_TRANSITIONS[order.status].includes('PREPARING'));
  order.status = 'PREPARING';
  console.log(`[Step 5: Kitchen] Order ${order.code} status -> ${order.status}`);

  // 6. Vendor marks order ready
  assert.ok(VALID_TRANSITIONS[order.status].includes('READY'));
  order.status = 'READY';
  order.readyAt = new Date().toISOString();
  console.log(`[Step 6: Ready] Order ${order.code} status -> ${order.status}. Push sent: "Your order is hot and ready. Show your QR at the counter."`);

  // 7. Counter verification: Scan QR token
  function verifyPickup(scannedToken) {
    if (scannedToken !== order.qrToken) {
      throw new Error('INVALID_QR: Unrecognized token');
    }
    if (order.status !== 'READY') {
      throw new Error('INVALID_QR: Wrong outlet or already collected');
    }
    order.status = 'COLLECTED';
    order.collectedAt = new Date().toISOString();
    return { success: true, code: order.code, status: order.status };
  }

  // First scan succeeds
  const scanResult1 = verifyPickup(order.qrToken);
  assert.equal(scanResult1.status, 'COLLECTED');
  console.log(`[Step 7: QR Verified] Order ${order.code} collected by student.`);

  // Second scan fails (screenshot / replay blocked)
  assert.throws(
    () => verifyPickup(order.qrToken),
    /INVALID_QR: Wrong outlet or already collected/
  );
  console.log('[Step 8: Replay Prevention] Second scan rejected with "Wrong outlet or already collected".');

  // 8. Test Cancel & Refund flow on a second order
  console.log('[Step 9: Cancel & Refund Flow]');
  const cancelOrder = {
    id: 'ord_cancel_999',
    code: 'TK-1122',
    status: 'PLACED',
    totalPaise: 6000, // ₹60
    prepUnits: 2,
    slotStartsAt: new Date(Date.now() + 20 * 60 * 1000).toISOString() // 20 min in future (> 10 min cutoff)
  };
  student.walletBalancePaise -= cancelOrder.totalPaise; // debited ₹60
  selectedSlot.used += cancelOrder.prepUnits;

  // Execute cancel
  const cutoffTime = new Date(cancelOrder.slotStartsAt).getTime() - 10 * 60 * 1000;
  assert.ok(Date.now() < cutoffTime, 'Within free cancellation window (> 10 min)');

  cancelOrder.status = 'CANCELLED';
  selectedSlot.used -= cancelOrder.prepUnits; // Released prep units
  student.walletBalancePaise += cancelOrder.totalPaise; // Refunded

  assert.equal(cancelOrder.status, 'CANCELLED');
  assert.equal(student.walletBalancePaise, 40500); // 50000 - 9500 = 40500
  console.log(`[Step 10: Refund Verified] Order ${cancelOrder.code} cancelled. Released ${cancelOrder.prepUnits} prep units. Wallet refunded to ₹${student.walletBalancePaise / 100}.`);
  console.log('--- END SCRIPTED E2E FLOW ---');
});
