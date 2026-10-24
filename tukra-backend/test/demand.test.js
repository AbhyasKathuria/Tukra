import { test } from 'node:test';
import assert from 'node:assert/strict';

test('demand & waste: calculates food surplus percentage and status correctly', () => {
  function computeWaste(planned, consumed) {
    const estimatedSurplus = Math.max(0, planned - consumed);
    const wastePercent = planned > 0 ? Math.round((estimatedSurplus / planned) * 100) : 0;
    return {
      planned,
      consumed,
      estimatedSurplus,
      wastePercent,
      status: wastePercent > 20 ? 'HIGH_SURPLUS' : 'BALANCED'
    };
  }

  // 100 planned, 90 consumed -> 10 surplus (10% waste) -> BALANCED
  const audit1 = computeWaste(100, 90);
  assert.equal(audit1.wastePercent, 10);
  assert.equal(audit1.status, 'BALANCED');

  // 100 planned, 60 consumed -> 40 surplus (40% waste) -> HIGH_SURPLUS
  const audit2 = computeWaste(100, 60);
  assert.equal(audit2.wastePercent, 40);
  assert.equal(audit2.status, 'HIGH_SURPLUS');

  // 50 planned, 55 consumed -> 0 surplus -> BALANCED
  const audit3 = computeWaste(50, 55);
  assert.equal(audit3.estimatedSurplus, 0);
  assert.equal(audit3.status, 'BALANCED');
});

test('demand forecast: calculates weekday 4-week average with baseline minimum', () => {
  function calculateForecast(historicalSold4Weeks, currentReservations) {
    const fourWeekWeekdayAvg = Math.round(historicalSold4Weeks / 4);
    // Baseline minimum is 15 units
    const suggestedQty = Math.max(15, fourWeekWeekdayAvg + currentReservations);
    return {
      fourWeekWeekdayAvg,
      currentReservations,
      suggestedQty,
      confidenceLabel: 'Estimate'
    };
  }

  // 120 sold over 4 previous Mondays (avg 30) + 10 pre-reservations -> 40 suggested
  const f1 = calculateForecast(120, 10);
  assert.equal(f1.fourWeekWeekdayAvg, 30);
  assert.equal(f1.suggestedQty, 40);

  // New item with 0 past sales and 0 reservations -> baseline minimum 15
  const f2 = calculateForecast(0, 0);
  assert.equal(f2.suggestedQty, 15);
});
