import { test } from 'node:test';
import assert from 'node:assert/strict';
import { normalizeName, diffMenu } from '../src/services/menuDiff.js';

test('normalizeName: trims, lowercases, strips punctuation and collapses whitespace', () => {
  assert.equal(normalizeName('  Masala Dosa (Special)!  '), 'masala dosa special');
  assert.equal(normalizeName('Chole-Bhature... '), 'cholebhature');
  assert.equal(normalizeName('Filter   Coffee'), 'filter coffee');
});

test('diffMenu: detects NEW items on the scraped page', () => {
  const scraped = [
    { name: 'Rava Idli', priceRupees: 50, isVeg: true, category: 'Tiffin' }
  ];
  const currentDb = [
    { id: '1', name: 'Masala Dosa', price_paise: 7000, is_veg: true, is_available: true }
  ];

  const proposals = diffMenu(scraped, currentDb);
  const newItems = proposals.filter((p) => p.changeType === 'NEW');

  assert.equal(newItems.length, 1);
  assert.equal(newItems[0].itemName, 'Rava Idli');
  assert.equal(newItems[0].newPricePaise, 5000);
});

test('diffMenu: detects PRICE_CHANGE and calculates sanity flag for extreme jumps', () => {
  const scraped = [
    { name: 'Masala Dosa', priceRupees: 80 }, // Normal change: ₹70 -> ₹80 (1.14x)
    { name: 'Filter Coffee', priceRupees: 90 }  // Extreme change: ₹25 -> ₹90 (3.6x > 3.0x threshold!)
  ];
  const currentDb = [
    { id: '1', name: 'Masala Dosa', price_paise: 7000, is_veg: true, is_available: true },
    { id: '2', name: 'Filter Coffee', price_paise: 2500, is_veg: true, is_available: true }
  ];

  const proposals = diffMenu(scraped, currentDb);

  const dosaChange = proposals.find((p) => p.itemName === 'Masala Dosa');
  assert.ok(dosaChange);
  assert.equal(dosaChange.changeType, 'PRICE_CHANGE');
  assert.equal(dosaChange.oldPricePaise, 7000);
  assert.equal(dosaChange.newPricePaise, 8000);
  assert.equal(dosaChange.flagged, false); // Normal jump

  const coffeeChange = proposals.find((p) => p.itemName === 'Filter Coffee');
  assert.ok(coffeeChange);
  assert.equal(coffeeChange.changeType, 'PRICE_CHANGE');
  assert.equal(coffeeChange.oldPricePaise, 2500);
  assert.equal(coffeeChange.newPricePaise, 9000);
  assert.equal(coffeeChange.flagged, true); // > 3x ratio flagged!
});

test('diffMenu: detects AVAILABILITY toggles when page marks items sold out', () => {
  const scraped = [
    { name: 'Masala Dosa', priceRupees: 70, soldOut: true }
  ];
  const currentDb = [
    { id: '1', name: 'Masala Dosa', price_paise: 7000, is_veg: true, is_available: true }
  ];

  const proposals = diffMenu(scraped, currentDb);
  const availChanges = proposals.filter((p) => p.changeType === 'AVAILABILITY');

  assert.equal(availChanges.length, 1);
  assert.equal(availChanges[0].isAvailable, false);
});

test('diffMenu: detects REMOVED items when DB items are missing from scraped page', () => {
  const scraped = [
    { name: 'Masala Dosa', priceRupees: 70 }
  ];
  const currentDb = [
    { id: '1', name: 'Masala Dosa', price_paise: 7000, is_veg: true, is_available: true },
    { id: '2', name: 'Idli Vada Combo', price_paise: 5500, is_veg: true, is_available: true }
  ];

  const proposals = diffMenu(scraped, currentDb);
  const removed = proposals.find((p) => p.changeType === 'REMOVED');

  assert.ok(removed);
  assert.equal(removed.itemName, 'Idli Vada Combo');
  assert.equal(removed.menuItemId, '2');
});
