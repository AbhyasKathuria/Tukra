import dotenv from 'dotenv';
import { z } from 'zod';

dotenv.config();

const envSchema = z.object({
  PORT: z.string().default('3000').transform((val) => parseInt(val, 10)),
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
  DATABASE_URL: z.string({ required_error: 'DATABASE_URL is required' }).min(1, 'DATABASE_URL is required'),
  JWT_SECRET: z.string({ required_error: 'JWT_SECRET is required' }).min(16, 'JWT_SECRET must be at least 16 characters long'),
  TINYFISH_API_KEY: z.string().optional().default(''),
  RAZORPAY_KEY_ID: z.string().optional().default(''),
  RAZORPAY_KEY_SECRET: z.string().optional().default(''),
  FIREBASE_SERVICE_ACCOUNT: z.string().optional().default(''),
  ENABLE_DEMO_PAY: z
    .string()
    .optional()
    .default('false')
    .transform((val) => val === 'true'),
  DEMO_MENU_URL: z.string().optional().default('')
}).refine(
  (data) => {
    // In production, ENABLE_DEMO_PAY is forbidden unless explicitly permitted
    if (data.NODE_ENV === 'production' && data.ENABLE_DEMO_PAY) {
      return process.env.ALLOW_DEMO_PAY_IN_PROD === 'true';
    }
    return true;
  },
  {
    message: 'ENABLE_DEMO_PAY is forbidden in production unless ALLOW_DEMO_PAY_IN_PROD=true is explicitly set',
    path: ['ENABLE_DEMO_PAY']
  }
);

export function parseConfig(env = process.env) {
  const result = envSchema.safeParse(env);
  if (!result.success) {
    const issues = result.error.issues || result.error.errors || [];
    const errorDetails = issues.map((err) => `  - ${err.path.join('.')}: ${err.message}`).join('\n');
    const message = `Configuration validation error(s):\n${errorDetails}`;
    throw new Error(message);
  }
  return result.data;
}

let config;
try {
  config = parseConfig(process.env);
} catch (error) {
  if (process.env.NODE_ENV !== 'test') {
    console.error('\x1b[31m[TUKRA CONFIG ERROR]\x1b[0m Failed to validate environment variables:');
    console.error(error.message);
    process.exit(1);
  } else {
    // In test environment, fallback gracefully if not configured yet
    config = {
      PORT: 3000,
      NODE_ENV: 'test',
      DATABASE_URL: process.env.DATABASE_URL || 'postgres://localhost:5432/tukra_test',
      JWT_SECRET: process.env.JWT_SECRET || 'test_jwt_secret_must_be_at_least_16_chars',
      TINYFISH_API_KEY: '',
      RAZORPAY_KEY_ID: '',
      RAZORPAY_KEY_SECRET: '',
      FIREBASE_SERVICE_ACCOUNT: '',
      ENABLE_DEMO_PAY: true,
      DEMO_MENU_URL: ''
    };
  }
}

export default config;
