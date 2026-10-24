import { test } from 'node:test';
import assert from 'node:assert/strict';
import { z } from 'zod';
import { toCamelCase } from '../src/db.js';
import { validate } from '../src/middleware/validate.js';

test('toCamelCase: converts snake_case keys in objects and arrays', () => {
  const input = {
    outlet_id: '123',
    price_paise: 7000,
    is_veg: true,
    user_details: {
      first_name: 'Aarav',
      home_address: {
        pin_code: '560064'
      }
    },
    menu_items: [
      { item_name: 'Dosa', prep_seconds: 180 },
      { item_name: 'Idli', prep_seconds: 120 }
    ]
  };

  const output = toCamelCase(input);

  assert.equal(output.outletId, '123');
  assert.equal(output.pricePaise, 7000);
  assert.equal(output.isVeg, true);
  assert.equal(output.userDetails.firstName, 'Aarav');
  assert.equal(output.userDetails.homeAddress.pinCode, '560064');
  assert.equal(output.menuItems[0].itemName, 'Dosa');
  assert.equal(output.menuItems[0].prepSeconds, 180);
});

test('validate middleware: passes valid body and assigns parsed data', () => {
  const schema = z.object({
    qty: z.string().transform(Number)
  });

  const req = { body: { qty: '5' } };
  const res = {};
  let nextCalled = false;

  validate(schema)(req, res, () => { nextCalled = true; });

  assert.equal(nextCalled, true);
  assert.equal(req.body.qty, 5);
});

test('validate middleware: returns 400 VALIDATION_FAILED on invalid input', () => {
  const schema = z.object({
    email: z.string().email()
  });

  const req = { body: { email: 'not-an-email' } };
  let statusCode = null;
  let responseBody = null;
  const res = {
    status(code) {
      statusCode = code;
      return this;
    },
    json(body) {
      responseBody = body;
      return this;
    }
  };
  let nextCalled = false;

  validate(schema)(req, res, () => { nextCalled = true; });

  assert.equal(nextCalled, false);
  assert.equal(statusCode, 400);
  assert.equal(responseBody.error.code, 'VALIDATION_FAILED');
});
