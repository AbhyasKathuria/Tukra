import { test } from 'node:test';
import assert from 'node:assert/strict';

test('plate simulation: complete counter session lifecycle', () => {
  // Simulated state database
  const activeSessions = new Map();
  const studentWallet = { balancePaise: 50000 }; // ₹500

  // 1. Operator opens plate session
  function openPlate(plateCode, outletId) {
    if (activeSessions.has(plateCode) && ['OPEN', 'BILLED'].includes(activeSessions.get(plateCode).status)) {
      throw new Error('PLATE_BUSY: Plate already has an active session');
    }
    const session = {
      id: `ps_${Date.now()}`,
      plateCode,
      outletId,
      userId: null,
      status: 'OPEN',
      totalPaise: 0,
      items: []
    };
    activeSessions.set(plateCode, session);
    return session;
  }

  // 2. Student links plate QR
  function linkPlate(plateCode, userId) {
    const session = activeSessions.get(plateCode);
    if (!session || !['OPEN', 'BILLED'].includes(session.status)) {
      throw new Error('NOT_FOUND: Active plate session not found');
    }
    session.userId = userId;
    return { success: true, linked: true };
  }

  // 3. Operator adds served dishes
  function addPlateItem(plateCode, { name, pricePaise, qty }) {
    const session = activeSessions.get(plateCode);
    if (session.status !== 'OPEN') {
      throw new Error('SESSION_LOCKED: Cannot add items to billed or closed session');
    }
    session.items.push({ name, pricePaise, qty });
    session.totalPaise += pricePaise * qty;
    return session.totalPaise;
  }

  // 4. Operator finishes and bills
  function finishPlate(plateCode) {
    const session = activeSessions.get(plateCode);
    session.status = 'BILLED';
    return session;
  }

  // 5. Student pays via wallet
  function payPlate(plateCode) {
    const session = activeSessions.get(plateCode);
    if (session.status !== 'BILLED') {
      throw new Error('INVALID_STATUS: Plate is not billed');
    }
    if (studentWallet.balancePaise < session.totalPaise) {
      throw new Error('INSUFFICIENT_BALANCE');
    }
    studentWallet.balancePaise -= session.totalPaise;
    session.status = 'PAID';
    session.paidAt = new Date().toISOString();
    return { success: true, remainingWalletPaise: studentWallet.balancePaise };
  }

  // Step 1: Open
  const session = openPlate('PLT-0001', 'o_mess_1');
  assert.equal(session.status, 'OPEN');
  assert.equal(session.totalPaise, 0);

  // Step 2: Prevent duplicate open while active
  assert.throws(() => openPlate('PLT-0001', 'o_mess_1'), /PLATE_BUSY/);

  // Step 3: Link
  linkPlate('PLT-0001', 'u_student_aarav');
  assert.equal(session.userId, 'u_student_aarav');

  // Step 4: Add dishes (Deluxe Thali ₹95 + Gulab Jamun ₹35 = ₹130 = 13000 paise)
  addPlateItem('PLT-0001', { name: 'Deluxe Lunch Thali', pricePaise: 9500, qty: 1 });
  addPlateItem('PLT-0001', { name: 'Gulab Jamun (2 pcs)', pricePaise: 3500, qty: 1 });
  assert.equal(session.totalPaise, 13000);

  // Step 5: Finish
  finishPlate('PLT-0001');
  assert.equal(session.status, 'BILLED');

  // Attempting to add item to BILLED session fails
  assert.throws(
    () => addPlateItem('PLT-0001', { name: 'Roti', pricePaise: 1000, qty: 1 }),
    /SESSION_LOCKED/
  );

  // Step 6: Pay
  const payResult = payPlate('PLT-0001');
  assert.equal(payResult.success, true);
  assert.equal(session.status, 'PAID');
  assert.equal(payResult.remainingWalletPaise, 37000); // 50000 - 13000 = 37000 (₹370)

  // Step 7: Plate code is freed for a new session now that previous is PAID
  const newSession = openPlate('PLT-0001', 'o_mess_1');
  assert.equal(newSession.status, 'OPEN');
  assert.equal(newSession.totalPaise, 0);
});
