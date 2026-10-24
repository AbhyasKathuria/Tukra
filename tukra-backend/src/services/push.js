import config from '../config.js';
import { query } from '../db.js';

let firebaseApp = null;

async function initFirebase() {
  if (firebaseApp || !config.FIREBASE_SERVICE_ACCOUNT) return;

  try {
    const { default: admin } = await import('firebase-admin');
    let serviceAccount;

    if (config.FIREBASE_SERVICE_ACCOUNT.startsWith('{')) {
      serviceAccount = JSON.parse(config.FIREBASE_SERVICE_ACCOUNT);
    } else {
      const fs = await import('node:fs');
      serviceAccount = JSON.parse(fs.readFileSync(config.FIREBASE_SERVICE_ACCOUNT, 'utf8'));
    }

    firebaseApp = admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    console.log('🔔 [PUSH] Firebase Cloud Messaging initialized');
  } catch (err) {
    console.warn('⚠️  [PUSH] Failed to initialize Firebase Admin SDK:', err.message);
  }
}

initFirebase().catch(() => {});

/**
 * Sends a push notification to all active devices registered by the user.
 */
export async function sendPush(userId, { title, body, data = {} }) {
  if (!config.FIREBASE_SERVICE_ACCOUNT || !firebaseApp) {
    if (config.NODE_ENV === 'development') {
      console.log(`[PUSH SIMULATED] To User ${userId}: "${title}" - "${body}"`);
    }
    return { delivered: 0, simulated: true };
  }

  try {
    const tokenRes = await query('SELECT token FROM device_tokens WHERE user_id = $1', [userId]);
    if (tokenRes.rows.length === 0) return { delivered: 0 };

    const tokens = tokenRes.rows.map((r) => r.token);
    const { default: admin } = await import('firebase-admin');

    const message = {
      notification: { title, body },
      data: {
        ...data,
        click_action: 'FLUTTER_NOTIFICATION_CLICK'
      },
      tokens
    };

    const response = await admin.messaging().sendEachForMulticast(message);
    return { delivered: response.successCount };
  } catch (err) {
    console.error('[PUSH ERROR]', err.message);
    return { delivered: 0, error: err.message };
  }
}

export default {
  sendPush
};
