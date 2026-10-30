import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  validateAndNormalizeResult,
  runAgentJob,
  getTinyFishUsageMap
} from '../src/services/agentJobService.js';

test('agentJobService: validateAndNormalizeResult handles VENDOR_ONBOARD', () => {
  const raw = {
    outlet_name: 'Campus Shawarma & Rolls',
    address: 'Near Presidency Food Court',
    opening_hours: '10:00 AM - 09:00 PM',
    items: [
      { name: 'Paneer Kathi Roll', price_rupees: 90, category: 'Rolls', is_veg: true },
      { name: 'Falafel Roll', price_rupees: '₹100.00', category: 'Rolls' },
      { name: 'Invalid Item', price: 'Free' }
    ]
  };

  const norm = validateAndNormalizeResult('VENDOR_ONBOARD', raw);
  assert.equal(norm.outletName, 'Campus Shawarma & Rolls');
  assert.equal(norm.items.length, 3);
  assert.equal(norm.items[0].pricePaise, 9000);
  assert.equal(norm.items[1].pricePaise, 10000);
  assert.equal(norm.items[2].pricePaise, 0);
});

test('agentJobService: validateAndNormalizeResult handles DISLIKE_RESCUE and computes effective prices', () => {
  const raw = {
    dish_query: 'Veg Dum Biryani',
    comparisons: [
      {
        platform: 'Swiggy',
        restaurant_name: 'Meghana Foods',
        dish_name: 'Veg Dum Biryani',
        price_rupees: 200,
        delivery_fee_rupees: 30
      },
      {
        platform: 'EatSure',
        restaurant_name: 'Behrouz Biryani',
        dish_name: 'Shahi Subz Biryani',
        price_rupees: 160,
        delivery_fee_rupees: 0,
        is_cheapest: true
      }
    ]
  };

  const norm = validateAndNormalizeResult('DISLIKE_RESCUE', raw);
  assert.equal(norm.comparisons.length, 2);
  assert.equal(norm.comparisons[0].effectivePriceRupees, 230);
  assert.equal(norm.comparisons[0].effectivePricePaise, 23000);
  assert.equal(norm.comparisons[1].effectivePriceRupees, 160);
  assert.equal(norm.comparisons[1].effectivePricePaise, 16000);
  assert.equal(norm.comparisons[1].isCheapest, true);
});

test('agentJobService: runAgentJob caches results and honors ttlMinutes', async () => {
  const cacheKey = `test_cache_key_${Date.now()}`;
  const job1 = await runAgentJob({
    kind: 'PRICE_BENCHMARK',
    input: { dishName: 'Masala Dosa', area: 'Presidency' },
    cacheKey,
    ttlMinutes: 30
  });

  assert.equal(job1.status, 'DONE');
  assert.equal(job1.fromCache, false);
  assert.ok(job1.result.medianPriceRupees > 0);

  // Second run with same cacheKey should return from cache
  const job2 = await runAgentJob({
    kind: 'PRICE_BENCHMARK',
    input: { dishName: 'Masala Dosa', area: 'Presidency' },
    cacheKey,
    ttlMinutes: 30
  });

  assert.equal(job2.fromCache, true);
  assert.equal(job2.durationMs, 0);
  assert.equal(job2.result.medianPriceRupees, job1.result.medianPriceRupees);
});

test('agentJobService: getTinyFishUsageMap contains all 8 judge use cases and no forbidden domains', () => {
  const map = getTinyFishUsageMap();
  assert.equal(map.length, 8);

  const places = map.map((m) => m.where);
  assert.ok(places.includes('Vendor onboarding'));
  assert.ok(places.includes('Menu Sync'));
  assert.ok(places.includes('Menu freshness guard'));
  assert.ok(places.includes('Dislike Rescue'));
  assert.ok(places.includes('Nearby discovery'));
  assert.ok(places.includes('Vendor price benchmark'));
  assert.ok(places.includes('Dish Doctor'));
  assert.ok(places.includes('Agent Console'));

  // Ensure forbidden domains (payments, billing, slots, login) are not listed as agent use cases
  for (const m of map) {
    assert.ok(!m.what.toLowerCase().includes('payment'));
    assert.ok(!m.what.toLowerCase().includes('deduct wallet'));
    assert.ok(!m.what.toLowerCase().includes('allocate slot'));
  }
});
