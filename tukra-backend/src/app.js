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
import tinyfishRouter from './routes/tinyfishRoutes.js';
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

// Login Rate Limiter (100 attempts / 15 min / IP for easy testing)
const loginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
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
  // If explicitly requested JSON or from an API client
  if (req.query.format === 'json' || (!req.accepts('html') && req.accepts('json'))) {
    return res.json({
      name: 'Tukra Backend API',
      status: 'online',
      version: '1.0.0',
      database: 'connected (Neon PostgreSQL)',
      documentation: 'https://github.com/AbhyasKathuria/Tukra/blob/main/docs/API.md',
      health: '/health'
    });
  }

  // Friendly, interactive status page for human browser visits
  const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Tukra API — System Online</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #FE81D4;
      --primary-dark: #D442A0;
      --primary-soft: #FFF0F8;
      --ink: #2B1A27;
      --ink-soft: #6E5D6A;
      --bg: #FAFAFC;
      --surface: #FFFFFF;
      --success: #10B981;
      --border: #EFEBF0;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      background: var(--bg);
      color: var(--ink);
      line-height: 1.6;
      padding: 32px 16px;
      display: flex;
      justify-content: center;
      min-height: 100vh;
    }
    .container {
      width: 100%;
      max-width: 720px;
      background: var(--surface);
      border-radius: 20px;
      box-shadow: 0 10px 40px rgba(43, 26, 39, 0.06);
      border: 1px solid var(--border);
      padding: 36px 32px;
      height: fit-content;
    }
    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid var(--border);
      padding-bottom: 24px;
      margin-bottom: 24px;
      flex-wrap: wrap;
      gap: 16px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .logo {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      background: linear-gradient(135deg, #FE81D4 0%, #D442A0 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 800;
      font-size: 22px;
      box-shadow: 0 4px 12px rgba(254, 129, 212, 0.35);
    }
    .brand-title {
      font-size: 24px;
      font-weight: 800;
      color: var(--ink);
      letter-spacing: -0.5px;
    }
    .brand-tag {
      font-size: 13px;
      color: var(--ink-soft);
      font-weight: 500;
    }
    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: #ECFDF5;
      color: #065F46;
      border: 1px solid #A7F3D0;
      font-size: 12px;
      font-weight: 700;
      padding: 6px 14px;
      border-radius: 999px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--success);
      box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.4; }
    }
    .hero-card {
      background: var(--primary-soft);
      border: 1px solid #FED3ED;
      border-radius: 16px;
      padding: 20px 24px;
      margin-bottom: 28px;
    }
    .hero-card h2 {
      font-size: 17px;
      font-weight: 700;
      color: var(--ink);
      margin-bottom: 6px;
    }
    .hero-card p {
      font-size: 14px;
      color: var(--ink-soft);
    }
    .grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      margin-bottom: 28px;
    }
    @media (max-width: 600px) {
      .grid { grid-template-columns: 1fr; }
      .container { padding: 24px 18px; }
    }
    .card {
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 16px 18px;
      background: #FFFFFF;
    }
    .card-label {
      font-size: 11px;
      font-weight: 700;
      color: var(--ink-soft);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin-bottom: 4px;
    }
    .card-value {
      font-size: 15px;
      font-weight: 700;
      color: var(--ink);
    }
    .section-title {
      font-size: 13px;
      font-weight: 700;
      color: var(--ink-soft);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin-bottom: 12px;
    }
    .accounts-table {
      width: 100%;
      border-collapse: collapse;
      border: 1px solid var(--border);
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 28px;
      font-size: 13px;
    }
    .accounts-table th {
      background: #F9F7FA;
      text-align: left;
      padding: 10px 14px;
      font-weight: 600;
      color: var(--ink-soft);
      border-bottom: 1px solid var(--border);
    }
    .accounts-table td {
      padding: 10px 14px;
      border-bottom: 1px solid var(--border);
      color: var(--ink);
    }
    .accounts-table tr:last-child td { border-bottom: none; }
    .badge {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 700;
    }
    .badge-student { background: #E0F2FE; color: #0369A1; }
    .badge-vendor { background: #FEF3C7; color: #B45309; }
    .badge-mess { background: #EDE9FE; color: #6D28D9; }
    .badge-admin { background: #FCE7F3; color: #BE185D; }
    .code {
      font-family: monospace;
      background: #F4F1F5;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 12px;
    }
    .actions {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      margin-bottom: 20px;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 10px 16px;
      border-radius: 10px;
      text-decoration: none;
      font-size: 13px;
      font-weight: 600;
      transition: all 0.2s;
    }
    .btn-primary {
      background: var(--primary);
      color: #FFFFFF;
    }
    .btn-primary:hover {
      background: var(--primary-dark);
    }
    .btn-secondary {
      background: #F4F1F5;
      color: var(--ink);
    }
    .btn-secondary:hover {
      background: #EBE6ED;
    }
    .footer {
      border-top: 1px solid var(--border);
      padding-top: 16px;
      font-size: 12px;
      color: var(--ink-soft);
      display: flex;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 8px;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div class="brand">
        <div class="logo">टुक</div>
        <div>
          <div class="brand-title">Tukra (टुकड़ा)</div>
          <div class="brand-tag">Campus Dining & Micro-Portions Platform</div>
        </div>
      </div>
      <div class="status-badge">
        <div class="status-dot"></div>
        API Online
      </div>
    </div>

    <div class="hero-card">
      <h2>Vercel Serverless Backend is Active</h2>
      <p>This server powers the Tukra Android mobile application with real-time pickup slots, micro-portions ordering, plate waste tracking, and wallet management.</p>
    </div>

    <div class="grid">
      <div class="card">
        <div class="card-label">Database Connection</div>
        <div class="card-value" style="color: var(--success);">Neon PostgreSQL (Live)</div>
      </div>
      <div class="card">
        <div class="card-label">Serverless Host</div>
        <div class="card-value">Vercel (Production)</div>
      </div>
      <div class="card">
        <div class="card-label">Active Campus</div>
        <div class="card-value">Presidency University</div>
      </div>
      <div class="card">
        <div class="card-label">API Health</div>
        <div class="card-value"><a href="/health" style="color: var(--primary-dark); text-decoration: none;">View /health JSON</a></div>
      </div>
    </div>

    <div class="section-title">Pre-Seeded Demo Accounts for Android App</div>
    <table class="accounts-table">
      <thead>
        <tr>
          <th>Role</th>
          <th>Email</th>
          <th>Password</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><span class="badge badge-student">Student</span></td>
          <td><span class="code">aarav@presidency.ac.in</span></td>
          <td><span class="code">demo1234</span></td>
        </tr>
        <tr>
          <td><span class="badge badge-vendor">Vendor</span></td>
          <td><span class="code">ramesh@southtiffin.com</span></td>
          <td><span class="code">demo1234</span></td>
        </tr>
        <tr>
          <td><span class="badge badge-mess">Mess Op</span></td>
          <td><span class="code">mess@presidency.ac.in</span></td>
          <td><span class="code">demo1234</span></td>
        </tr>
        <tr>
          <td><span class="badge badge-admin">Admin</span></td>
          <td><span class="code">admin@presidency.ac.in</span></td>
          <td><span class="code">demo1234</span></td>
        </tr>
      </tbody>
    </table>

    <div class="actions">
      <a href="/health" class="btn btn-primary">Check Health JSON</a>
      <a href="/?format=json" class="btn btn-secondary">Raw Root JSON</a>
      <a href="https://github.com/AbhyasKathuria/Tukra" target="_blank" class="btn btn-secondary">GitHub Repository</a>
    </div>

    <div class="footer">
      <div>Tukra API v1.0.0 • Mobile Client ready</div>
      <div>Base URL: <code>https://tukra.vercel.app/</code></div>
    </div>
  </div>
</body>
</html>`;

  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(html);
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
app.use('/tinyfish', tinyfishRouter);

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
