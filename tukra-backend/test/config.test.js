import { test } from 'node:test';
import assert from 'node:assert/strict';
import { parseConfig } from '../src/config.js';

test('parseConfig: succeeds with valid required environment', () => {
  const env = {
    DATABASE_URL: 'postgres://user:pass@localhost:5432/tukra',
    JWT_SECRET: 'this_is_a_very_secure_long_secret_key',
    PORT: '4000',
    NODE_ENV: 'development'
  };

  const parsed = parseConfig(env);
  assert.equal(parsed.PORT, 4000);
  assert.equal(parsed.NODE_ENV, 'development');
  assert.equal(parsed.DATABASE_URL, 'postgres://user:pass@localhost:5432/tukra');
  assert.equal(parsed.ENABLE_DEMO_PAY, false);
});

test('parseConfig: fails if DATABASE_URL is missing', () => {
  const env = {
    JWT_SECRET: 'this_is_a_very_secure_long_secret_key'
  };

  assert.throws(() => parseConfig(env), /DATABASE_URL/);
});

test('parseConfig: fails if JWT_SECRET is too short', () => {
  const env = {
    DATABASE_URL: 'postgres://localhost:5432/tukra',
    JWT_SECRET: 'short'
  };

  assert.throws(() => parseConfig(env), /JWT_SECRET must be at least 16 characters/);
});

test('parseConfig: refuses ENABLE_DEMO_PAY in production unless explicitly allowed', () => {
  const env = {
    DATABASE_URL: 'postgres://user:pass@host:5432/tukra',
    JWT_SECRET: 'production_secret_key_at_least_16_chars',
    NODE_ENV: 'production',
    ENABLE_DEMO_PAY: 'true'
  };

  assert.throws(() => parseConfig(env), /ENABLE_DEMO_PAY is forbidden in production/);
});
