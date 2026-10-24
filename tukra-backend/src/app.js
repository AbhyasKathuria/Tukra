import express from 'express';
import helmet from 'helmet';
import cors from 'cors';
import rateLimit from 'express-rate-limit';

import healthRouter from './routes/health.js';
import authRouter from './routes/auth.js';
import meRouter from './routes/me.js';
import campusRouter from './routes/campus.js';
import outletsRouter from './routes/outlets.js';
import ordersRouter from './routes/orders.js';
import walletRouter from './routes/wallet.js';
import vendorRouter from './routes/vendor.js';
import plateRouter from './routes/plate.js';
import messRouter from './routes/mess.js';
import adminRouter from './routes/admin.js';
import { errorHandler } from './middleware/error.js';

const app = express();

app.disable('x-powered-by');

// Security & Parsing
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '1mb' }));

// General Rate Limiter (120 req / min / IP)
const generalLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 120,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: {
      code: 'RATE_LIMIT_EXCEEDED',
      message: 'Too many requests, please slow down'
    }
  }
});
app.use(generalLimiter);

// Strict Rate Limiter for Login (10 attempts / 15 min / IP)
const loginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 10,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: {
      code: 'TOO_MANY_ATTEMPTS',
      message: 'Too many login attempts, please try again in 15 minutes'
    }
  }
});

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    name: 'Tukra Backend API',
    status: 'online',
    version: '1.0.0',
    documentation: 'https://github.com/AbhyasKathuria/Tukra/blob/main/docs/API.md',
    health: '/health'
  });
});

// Mount Routes
app.use('/health', healthRouter);
app.use('/auth/login', loginLimiter);
app.use('/auth', authRouter);
app.use('/me', meRouter);
app.use('/campus', campusRouter);
app.use('/outlets', outletsRouter);
app.use('/orders', ordersRouter);
app.use('/wallet', walletRouter);
app.use('/vendor', vendorRouter);
app.use('/plate', plateRouter);
app.use('/mess', messRouter);
app.use('/admin', adminRouter);

// 404 Handler
app.use((req, res) => {
  res.status(404).json({
    error: {
      code: 'NOT_FOUND',
      message: `Endpoint ${req.method} ${req.path} not found`
    }
  });
});

// Global Error Handler
app.use(errorHandler);

export default app;
