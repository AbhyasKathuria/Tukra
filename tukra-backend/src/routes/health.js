import express from 'express';
import config from '../config.js';

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    ok: true,
    time: new Date().toISOString(),
    features: {
      tinyfish: Boolean(config.TINYFISH_API_KEY),
      razorpay: Boolean(config.RAZORPAY_KEY_ID && config.RAZORPAY_KEY_SECRET),
      push: Boolean(config.FIREBASE_SERVICE_ACCOUNT),
      demoPay: config.ENABLE_DEMO_PAY
    }
  });
});

export default router;
