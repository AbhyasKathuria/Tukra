import { query, toCamelCase } from '../db.js';

/**
 * Returns the current date in Asia/Kolkata (IST) as 'YYYY-MM-DD'.
 */
export function getTodayISTDateString() {
  const formatter = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Kolkata',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
  return formatter.format(new Date()); // Formats as YYYY-MM-DD
}

/**
 * Generates slots for a specific outlet for a specific date (defaults to today in IST).
 */
export async function generateSlotsForOutlet(outletId, dateStr = getTodayISTDateString(), client = null) {
  const runner = client || { query };
  const outletRes = await runner.query(
    'SELECT id, opens_at, accepts_orders_until, slot_minutes, slot_capacity, is_open FROM outlets WHERE id = $1',
    [outletId]
  );
  if (outletRes.rows.length === 0) return 0;

  const outlet = outletRes.rows[0];
  const slotMinutes = outlet.slot_minutes || 5;
  const slotCapacity = outlet.slot_capacity || 12;

  const [openHour, openMin] = outlet.opens_at.split(':').map(Number);
  const [closeHour, closeMin] = outlet.accepts_orders_until.split(':').map(Number);

  const startTotalMinutes = openHour * 60 + openMin;
  const endTotalMinutes = closeHour * 60 + closeMin;

  let createdCount = 0;
  for (let m = startTotalMinutes; m < endTotalMinutes; m += slotMinutes) {
    const curHour = String(Math.floor(m / 60)).padStart(2, '0');
    const curMin = String(m % 60).padStart(2, '0');
    const endM = m + slotMinutes;
    const endHour = String(Math.floor(endM / 60)).padStart(2, '0');
    const endMin = String(endM % 60).padStart(2, '0');

    // Specify explicit +05:30 offset
    const startsAt = `${dateStr}T${curHour}:${curMin}:00+05:30`;
    const endsAt = `${dateStr}T${endHour}:${endMin}:00+05:30`;

    await runner.query(
      `INSERT INTO slots (outlet_id, starts_at, ends_at, capacity, used)
       VALUES ($1, $2, $3, $4, 0)
       ON CONFLICT (outlet_id, starts_at) DO NOTHING`,
      [outletId, startsAt, endsAt, slotCapacity]
    );
    createdCount++;
  }
  return createdCount;
}

/**
 * Generates slots for all open outlets for today in IST.
 */
export async function generateDailySlots(client = null) {
  const runner = client || { query };
  const outletsRes = await runner.query('SELECT id FROM outlets WHERE is_open = true');
  const dateStr = getTodayISTDateString();
  let totalSlots = 0;
  for (const outlet of outletsRes.rows) {
    const count = await generateSlotsForOutlet(outlet.id, dateStr, runner);
    totalSlots += count;
  }
  return totalSlots;
}

/**
 * Returns feasible slots for an order per Part F3:
 * - starts_at >= now + longestPrepSeconds + 120s buffer
 * - capacity - used >= units
 * - within today in IST
 * - outlet is open and before last-order time
 */
export async function getFeasibleSlots(outletId, units = 1, longestPrepSeconds = 180) {
  const sanitizedUnits = Math.max(1, Math.min(Number(units) || 1, 20));
  const prepSeconds = Math.max(0, Number(longestPrepSeconds) || 0);

  // Check outlet status
  const outletRes = await query(
    'SELECT is_open, opens_at, accepts_orders_until FROM outlets WHERE id = $1',
    [outletId]
  );
  if (outletRes.rows.length === 0) {
    return { outlet: null, slots: [] };
  }
  const outlet = outletRes.rows[0];
  if (!outlet.is_open) {
    return { outlet, slots: [] };
  }

  // Earliest feasible slot start time
  const bufferSeconds = 120;
  const earliestTime = new Date(Date.now() + (prepSeconds + bufferSeconds) * 1000);

  const sql = `
    SELECT id, starts_at, ends_at, capacity, used, (capacity - used) AS remaining
      FROM slots
     WHERE outlet_id = $1
       AND starts_at >= $2
       AND capacity - used >= $3
       AND starts_at >= (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata')
       AND starts_at <  (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata') + interval '1 day'
     ORDER BY starts_at ASC
     LIMIT 24
  `;

  const { rows } = await query(sql, [outletId, earliestTime.toISOString(), sanitizedUnits]);

  return {
    outlet,
    slots: toCamelCase(rows)
  };
}

export default {
  getTodayISTDateString,
  generateSlotsForOutlet,
  generateDailySlots,
  getFeasibleSlots
};
