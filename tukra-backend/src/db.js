import pg from 'pg';
import config from './config.js';

const { Pool } = pg;

function isRemoteDatabase(url) {
  if (!url) return false;
  return !url.includes('localhost') && !url.includes('127.0.0.1');
}

const poolConfig = {
  connectionString: config.DATABASE_URL,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 5000,
  allowExitOnIdle: true
};

if (isRemoteDatabase(config.DATABASE_URL)) {
  poolConfig.ssl = { rejectUnauthorized: false };
}

export const pool = new Pool(poolConfig);

export async function query(text, params) {
  const start = Date.now();
  const res = await pool.query(text, params);
  const duration = Date.now() - start;
  if (config.NODE_ENV === 'development' && duration > 100) {
    console.debug(`[DB SLOW QUERY] ${duration}ms: ${text}`);
  }
  return res;
}

export async function getClient() {
  const client = await pool.connect();
  return client;
}

/**
 * Transforms snake_case keys in an object or array to camelCase.
 */
export function toCamelCase(data) {
  if (!data || typeof data !== 'object') return data;
  if (data instanceof Date) return data.toISOString();

  if (Array.isArray(data)) {
    return data.map(toCamelCase);
  }

  const result = {};
  for (const [key, value] of Object.entries(data)) {
    const camelKey = key.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
    if (value !== null && typeof value === 'object' && !(value instanceof Date)) {
      result[camelKey] = toCamelCase(value);
    } else if (value instanceof Date) {
      result[camelKey] = value.toISOString();
    } else {
      result[camelKey] = value;
    }
  }
  return result;
}

export default {
  pool,
  query,
  getClient,
  toCamelCase
};
