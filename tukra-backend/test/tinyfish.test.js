import { test } from 'node:test';
import assert from 'node:assert/strict';
import { extractItems, MENU_GOAL, OUTPUT_SCHEMA } from '../src/services/tinyfish.js';

test('tinyfish: schema and prompt goal definitions exist and meet requirements', () => {
  assert.ok(MENU_GOAL.includes('canteen menu page'));
  assert.ok(MENU_GOAL.includes('rupees'));
  assert.ok(OUTPUT_SCHEMA.properties.items);
});

test('tinyfish extractItems: handles result.items array with rupee and string conversions', () => {
  const payload = {
    items: [
      { name: 'Masala Dosa', price_rupees: 80, category: 'Mains', is_veg: true },
      { name: 'Filter Coffee', price_rupees: '₹30.00', category: 'Drinks' },
      { name: 'Chicken Curry', price_rupees: 140, is_veg: false, sold_out: true }
    ]
  };

  const extracted = extractItems(payload);
  assert.equal(extracted.length, 3);

  // Masala Dosa
  assert.equal(extracted[0].name, 'Masala Dosa');
  assert.equal(extracted[0].priceRupees, 80);
  assert.equal(extracted[0].pricePaise, 8000);
  assert.equal(extracted[0].isVeg, true);

  // Filter Coffee (string parsing check)
  assert.equal(extracted[1].name, 'Filter Coffee');
  assert.equal(extracted[1].priceRupees, 30);
  assert.equal(extracted[1].pricePaise, 3000);
  assert.equal(extracted[1].category, 'Drinks');

  // Chicken Curry (non-veg & sold out check)
  assert.equal(extracted[2].name, 'Chicken Curry');
  assert.equal(extracted[2].isVeg, false);
  assert.equal(extracted[2].soldOut, true);
});

test('tinyfish extractItems: handles bare result array and drops non-numeric prices', () => {
  const payload = {
    result: [
      { name: 'Valid Item', price: 45 },
      { name: 'Invalid Price', price: 'Free' },
      { name: 'Missing Price' }
    ]
  };

  const extracted = extractItems(payload);
  assert.equal(extracted.length, 1);
  assert.equal(extracted[0].name, 'Valid Item');
  assert.equal(extracted[0].pricePaise, 4500);
});
