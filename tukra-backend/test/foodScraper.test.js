import { test } from 'node:test';
import assert from 'node:assert/strict';
import { findCheapestFoodAlternatives, FOOD_RESCUE_GOAL, FOOD_RESCUE_SCHEMA } from '../src/services/foodScraper.js';

test('foodScraper: goal and schema definitions meet requirements', () => {
  assert.ok(FOOD_RESCUE_GOAL.includes('Swiggy'));
  assert.ok(FOOD_RESCUE_GOAL.includes('Zomato'));
  assert.ok(FOOD_RESCUE_GOAL.includes('EatSure'));
  assert.ok(FOOD_RESCUE_SCHEMA.properties.comparisons);
});

test('foodScraper: findCheapestFoodAlternatives returns multi-platform comparisons and identifies cheapest option', async () => {
  const result = await findCheapestFoodAlternatives('paneer', {
    campusName: 'Presidency University',
    zone: 'Cauvery Hostel Block B'
  });

  assert.equal(result.dishQuery, 'paneer');
  assert.ok(result.detectedLocation.campusName.includes('Presidency'));
  assert.ok(result.comparisons.length >= 3);

  // Check platforms present
  const platforms = result.comparisons.map((c) => c.platform);
  assert.ok(platforms.includes('EatSure'));
  assert.ok(platforms.includes('Swiggy'));
  assert.ok(platforms.includes('Zomato'));

  // Ensure cheapest option is accurately identified
  assert.ok(result.cheapestOption);
  const minPrice = Math.min(...result.comparisons.map((c) => c.priceRupees));
  assert.equal(result.cheapestOption.priceRupees, minPrice);
  assert.ok(result.cheapestOption.savingsRupees >= 0);

  // Check agent trace
  assert.ok(result.agentTrace.length >= 4);
  assert.ok(result.agentTrace[0].includes('TinyFish'));
});

test('foodScraper: biryani query matches biryani catalog with accurate pricing and coupons', async () => {
  const result = await findCheapestFoodAlternatives('Hyderabadi Veg Biryani');
  assert.equal(result.dishQuery, 'Hyderabadi Veg Biryani');
  assert.ok(result.comparisons.some((c) => c.restaurantName.includes('Behrouz') || c.restaurantName.includes('Meghana')));
  assert.ok(result.cheapestOption.couponApplied);
  assert.ok(result.cheapestOption.deepLink.startsWith('http'));
});
