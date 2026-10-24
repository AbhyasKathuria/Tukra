import { test } from 'node:test';
import assert from 'node:assert/strict';
import { VALID_TRANSITIONS } from '../src/services/orderService.js';

test('state machine: allows sequential progression PLACED -> ACCEPTED -> PREPARING -> READY -> COLLECTED', () => {
  assert.ok(VALID_TRANSITIONS.PLACED.includes('ACCEPTED'));
  assert.ok(VALID_TRANSITIONS.ACCEPTED.includes('PREPARING'));
  assert.ok(VALID_TRANSITIONS.PREPARING.includes('READY'));
  assert.ok(VALID_TRANSITIONS.READY.includes('COLLECTED'));
});

test('state machine: allows rejection from PLACED and ACCEPTED', () => {
  assert.ok(VALID_TRANSITIONS.PLACED.includes('REJECTED'));
  assert.ok(VALID_TRANSITIONS.ACCEPTED.includes('REJECTED'));
});

test('state machine: rejects skipping steps (e.g. PLACED -> READY)', () => {
  assert.equal(VALID_TRANSITIONS.PLACED.includes('READY'), false);
  assert.equal(VALID_TRANSITIONS.PLACED.includes('COLLECTED'), false);
  assert.equal(VALID_TRANSITIONS.ACCEPTED.includes('COLLECTED'), false);
  assert.equal(VALID_TRANSITIONS.PREPARING.includes('COLLECTED'), false);
});

test('state machine: terminal states allow no further transitions', () => {
  assert.equal(VALID_TRANSITIONS.COLLECTED.length, 0);
  assert.equal(VALID_TRANSITIONS.CANCELLED.length, 0);
  assert.equal(VALID_TRANSITIONS.REJECTED.length, 0);
  assert.equal(VALID_TRANSITIONS.NO_SHOW.length, 0);
});
