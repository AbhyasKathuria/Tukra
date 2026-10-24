import { test } from 'node:test';
import assert from 'node:assert/strict';
import { getTodayISTDateString, generateSlotsForOutlet } from '../src/services/slotService.js';

test('getTodayISTDateString: formats valid YYYY-MM-DD for IST', () => {
  const dateStr = getTodayISTDateString();
  assert.match(dateStr, /^\d{4}-\d{2}-\d{2}$/);
});

test('generateSlotsForOutlet: creates exact 5-minute slots with +05:30 timezone offset', async () => {
  const mockInsertedSlots = [];

  const mockClient = {
    async query(sql, params) {
      if (sql.includes('SELECT id, opens_at')) {
        return {
          rows: [
            {
              id: 'o1111111-1111-1111-1111-111111111111',
              opens_at: '08:00',
              accepts_orders_until: '08:30', // 30 minutes = 6 slots
              slot_minutes: 5,
              slot_capacity: 12,
              is_open: true
            }
          ]
        };
      }
      if (sql.includes('INSERT INTO slots')) {
        mockInsertedSlots.push({
          outletId: params[0],
          startsAt: params[1],
          endsAt: params[2],
          capacity: params[3]
        });
        return { rows: [] };
      }
      return { rows: [] };
    }
  };

  const count = await generateSlotsForOutlet('o1111111-1111-1111-1111-111111111111', '2026-10-30', mockClient);

  assert.equal(count, 6);
  assert.equal(mockInsertedSlots.length, 6);
  assert.equal(mockInsertedSlots[0].startsAt, '2026-10-30T08:00:00+05:30');
  assert.equal(mockInsertedSlots[0].endsAt, '2026-10-30T08:05:00+05:30');
  assert.equal(mockInsertedSlots[5].startsAt, '2026-10-30T08:25:00+05:30');
  assert.equal(mockInsertedSlots[5].endsAt, '2026-10-30T08:30:00+05:30');
  assert.equal(mockInsertedSlots[0].capacity, 12);
});
