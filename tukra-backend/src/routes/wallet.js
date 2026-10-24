import express from 'express';
import { z } from 'zod';
import { auth } from '../middleware/auth.js';
import { validate } from '../middleware/validate.js';
import {
  getWallet,
  createRazorpayOrder,
  verifyRazorpayPayment,
  demoTopup
} from '../services/walletService.js';

const router = express.Router();

const topupOrderSchema = z.object({
  amountPaise: z.number().int().min(1000).max(500000)
});

const topupVerifySchema = z.object({
  razorpayOrderId: z.string().min(1),
  razorpayPaymentId: z.string().min(1),
  razorpaySignature: z.string().min(1)
});

// GET /wallet
router.get('/', auth(['STUDENT', 'STAFF']), async (req, res, next) => {
  try {
    const data = await getWallet(req.user.id);
    res.json(data);
  } catch (err) {
    next(err);
  }
});

// POST /wallet/topup/order (Create Razorpay test-mode order)
router.post('/topup/order', auth(['STUDENT', 'STAFF']), validate(topupOrderSchema), async (req, res, next) => {
  try {
    const result = await createRazorpayOrder(req.user.id, req.body.amountPaise);
    res.status(201).json(result);
  } catch (err) {
    next(err);
  }
});

// POST /wallet/topup/verify (Verify HMAC signature and credit wallet)
router.post('/topup/verify', auth(['STUDENT', 'STAFF']), validate(topupVerifySchema), async (req, res, next) => {
  try {
    const result = await verifyRazorpayPayment(req.user.id, req.body);
    res.json(result);
  } catch (err) {
    next(err);
  }
});

// POST /wallet/topup/demo (Demo top-up without real Razorpay)
router.post('/topup/demo', auth(['STUDENT', 'STAFF']), validate(topupOrderSchema), async (req, res, next) => {
  try {
    const result = await demoTopup(req.user.id, req.body.amountPaise);
    res.json(result);
  } catch (err) {
    next(err);
  }
});

export default router;
