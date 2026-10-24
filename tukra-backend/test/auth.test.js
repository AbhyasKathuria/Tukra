import { test } from 'node:test';
import assert from 'node:assert/strict';
import jwt from 'jsonwebtoken';
import { auth } from '../src/middleware/auth.js';
import config from '../src/config.js';

function createMockReqRes(headers = {}) {
  const req = { headers };
  const res = {
    statusCode: 200,
    body: null,
    status(code) {
      this.statusCode = code;
      return this;
    },
    json(data) {
      this.body = data;
      return this;
    }
  };
  return { req, res };
}

test('auth middleware: rejects missing Authorization header with 401 UNAUTHENTICATED', () => {
  const { req, res } = createMockReqRes({});
  let nextCalled = false;

  auth()(req, res, () => { nextCalled = true; });

  assert.equal(nextCalled, false);
  assert.equal(res.statusCode, 401);
  assert.equal(res.body.error.code, 'UNAUTHENTICATED');
});

test('auth middleware: rejects malformed token with 401 UNAUTHENTICATED', () => {
  const { req, res } = createMockReqRes({ authorization: 'Bearer invalid.token.signature' });
  let nextCalled = false;

  auth()(req, res, () => { nextCalled = true; });

  assert.equal(nextCalled, false);
  assert.equal(res.statusCode, 401);
  assert.equal(res.body.error.code, 'UNAUTHENTICATED');
});

test('auth middleware: accepts valid JWT and attaches user to req', () => {
  const payload = {
    id: 'a1111111-1111-1111-1111-111111111111',
    role: 'STUDENT',
    campusId: 'c1111111-1111-1111-1111-111111111111'
  };
  const token = jwt.sign(payload, config.JWT_SECRET);
  const { req, res } = createMockReqRes({ authorization: `Bearer ${token}` });
  let nextCalled = false;

  auth()(req, res, () => { nextCalled = true; });

  assert.equal(nextCalled, true);
  assert.equal(req.user.id, payload.id);
  assert.equal(req.user.role, 'STUDENT');
});

test('auth middleware: enforces role permissions (403 FORBIDDEN on unauthorized role)', () => {
  const payload = {
    id: 'a1111111-1111-1111-1111-111111111111',
    role: 'STUDENT',
    campusId: 'c1111111-1111-1111-1111-111111111111'
  };
  const token = jwt.sign(payload, config.JWT_SECRET);
  const { req, res } = createMockReqRes({ authorization: `Bearer ${token}` });
  let nextCalled = false;

  auth(['VENDOR', 'ADMIN'])(req, res, () => { nextCalled = true; });

  assert.equal(nextCalled, false);
  assert.equal(res.statusCode, 403);
  assert.equal(res.body.error.code, 'FORBIDDEN');
});
