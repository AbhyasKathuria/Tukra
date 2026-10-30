import config from '../config.js';
import { AppError } from '../middleware/error.js';

const TINYFISH_BASE_URL = 'https://agent.tinyfish.ai/v1';

export const MENU_GOAL =
  'Open this canteen menu page. Extract every food and drink item that is listed. For each item return its name, its price in rupees as a number, its category heading if there is one, whether it is vegetarian (true or false) and whether it is marked sold out. Ignore ads, opening hours and contact details. Do not click any order, pay or login buttons.';

export const OUTPUT_SCHEMA = {
  type: 'object',
  properties: {
    items: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          name: { type: 'string' },
          price_rupees: { type: 'number' },
          category: { type: 'string' },
          is_veg: { type: 'boolean' },
          sold_out: { type: 'boolean' }
        },
        required: ['name', 'price_rupees']
      }
    }
  },
  required: ['items']
};

/**
 * Extracts and sanitizes items from TinyFish JSON result payload.
 */
export function extractItems(resultPayload) {
  if (!resultPayload) return [];

  let rawList = [];
  if (Array.isArray(resultPayload)) {
    rawList = resultPayload;
  } else if (Array.isArray(resultPayload.items)) {
    rawList = resultPayload.items;
  } else if (Array.isArray(resultPayload.result)) {
    rawList = resultPayload.result;
  } else if (Array.isArray(resultPayload.products)) {
    rawList = resultPayload.products;
  } else if (typeof resultPayload === 'object') {
    // Try finding the first array in the object
    for (const val of Object.values(resultPayload)) {
      if (Array.isArray(val)) {
        rawList = val;
        break;
      }
    }
  }

  const items = [];
  for (const item of rawList) {
    if (!item || !item.name) continue;

    let priceRupees = item.price_rupees !== undefined ? item.price_rupees : item.price;
    if (typeof priceRupees === 'string') {
      const match = priceRupees.replace(/[^\d.]/g, '');
      priceRupees = match ? parseFloat(match) : NaN;
    }

    if (typeof priceRupees !== 'number' || isNaN(priceRupees) || priceRupees < 0) {
      continue; // Drop non-numeric price rows
    }

    items.push({
      name: String(item.name).trim(),
      priceRupees,
      pricePaise: Math.round(priceRupees * 100),
      category: item.category ? String(item.category).trim() : 'Mains',
      isVeg: item.is_veg !== undefined ? Boolean(item.is_veg) : true,
      soldOut: Boolean(item.sold_out || item.soldOut)
    });
  }

  return items;
}

export function getTinyFishApiKeys() {
  const keys = [];
  if (config.TINYFISH_API_KEY && config.TINYFISH_API_KEY.startsWith('sk-tinyfish')) {
    keys.push(config.TINYFISH_API_KEY);
  }
  if (
    config.TINYFISH_BACKUP_API_KEY &&
    config.TINYFISH_BACKUP_API_KEY.startsWith('sk-tinyfish') &&
    !keys.includes(config.TINYFISH_BACKUP_API_KEY)
  ) {
    keys.push(config.TINYFISH_BACKUP_API_KEY);
  }
  return keys;
}

/**
 * Polls run status as fallback if the SSE stream drops.
 */
export async function pollRun(runId, maxSeconds = 180, apiKey = null) {
  const activeKey = apiKey || getTinyFishApiKeys()[0] || config.TINYFISH_API_KEY;
  const startTime = Date.now();
  const headers = {
    'Content-Type': 'application/json',
    'X-API-Key': activeKey
  };

  while (Date.now() - startTime < maxSeconds * 1000) {
    await new Promise((r) => setTimeout(r, 3000));

    try {
      const res = await fetch(`${TINYFISH_BASE_URL}/runs/${runId}`, { headers });
      if (!res.ok) continue;

      const data = await res.json();
      if (data.status === 'COMPLETED') {
        return { status: 'COMPLETED', result: data.result || data.output };
      }
      if (data.status === 'FAILED') {
        return { status: 'FAILED', error: data.error_message || 'Agent run failed' };
      }
    } catch {
      // Ignore network hiccups while polling
    }
  }

  throw new Error(`Polling timed out after ${maxSeconds} seconds`);
}

/**
 * Executes a TinyFish autonomous menu synchronization task via SSE.
 */
export async function runTinyFishSync(targetUrl, { onStarted, onStreamingUrl, onProgress, onComplete, onError }) {
  const availableKeys = getTinyFishApiKeys();
  if (availableKeys.length === 0) {
    const errorMsg = 'TinyFish API key is not configured';
    if (onError) onError(new AppError('TINYFISH_NOT_CONFIGURED', errorMsg, 400));
    return;
  }

  const payload = {
    url: targetUrl,
    goal: MENU_GOAL,
    output_schema: OUTPUT_SCHEMA,
    browser_profile: 'lite',
    agent_config: {
      max_duration_seconds: 180
    }
  };

  let runId = null;
  let isComplete = false;

  async function executeRequest() {
    let lastErr = null;
    for (let i = 0; i < availableKeys.length; i++) {
      const activeKey = availableKeys[i];
      const headers = {
        'Content-Type': 'application/json',
        'X-API-Key': activeKey
      };

      try {
        const response = await fetch(`${TINYFISH_BASE_URL}/automation/run-sse`, {
          method: 'POST',
          headers,
          body: JSON.stringify(payload)
        });

        if (response.status === 401) {
          console.error(`[TINYFISH AUTH ERROR] 401 Unauthorized with key index ${i}.`);
          if (i + 1 < availableKeys.length) {
            console.log(`[TINYFISH FAILOVER] Switching to backup key...`);
            continue;
          }
          throw new AppError('UNAUTHENTICATED', 'Menu sync is not available right now', 401);
        }

        if (response.status === 429) {
          console.warn(`[TINYFISH RATE LIMIT] 429 received on key index ${i}.`);
          if (i + 1 < availableKeys.length) {
            console.log(`[TINYFISH FAILOVER] Switching to backup key...`);
            continue;
          }
          await new Promise((r) => setTimeout(r, 10000));
          // Retry once on last key
          const retryRes = await fetch(`${TINYFISH_BASE_URL}/automation/run-sse`, {
            method: 'POST',
            headers,
            body: JSON.stringify(payload)
          });
          if (retryRes.ok) return retryRes;
        }

        if (!response.ok) {
          const errText = await response.text();
          throw new Error(`TinyFish API request failed with status ${response.status}: ${errText}`);
        }

        return response;
      } catch (err) {
        lastErr = err;
        if (i + 1 < availableKeys.length) {
          console.warn(`[TINYFISH BACKUP NOTICE] Key ${i + 1} failed: ${err.message}. Trying backup key...`);
        }
      }
    }
    throw lastErr || new Error('All TinyFish API keys failed');
  }

  try {
    const response = await executeRequest();
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const events = buffer.split('\n\n');
      buffer = events.pop(); // Keep last partial chunk

      for (const eventBlock of events) {
        const lines = eventBlock.split('\n');
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const dataStr = line.slice(6).trim();
            if (!dataStr) continue;

            try {
              const eventData = JSON.parse(dataStr);

              if (eventData.run_id && !runId) {
                runId = eventData.run_id;
              }

              if (eventData.type === 'STARTED' && onStarted) {
                onStarted(eventData.run_id);
              } else if (eventData.type === 'STREAMING_URL' && onStreamingUrl) {
                onStreamingUrl(eventData.streaming_url, eventData.run_id || runId);
              } else if (eventData.type === 'PROGRESS' && onProgress) {
                onProgress(eventData.purpose || eventData.text || 'Scanning menu items...', eventData.run_id || runId);
              } else if (eventData.type === 'COMPLETE') {
                isComplete = true;
                const items = extractItems(eventData.result);
                if (onComplete) {
                  onComplete(items, eventData.run_id || runId, eventData.status);
                }
                return;
              }
            } catch {
              // Ignore partial JSON parses
            }
          }
        }
      }
    }

    // Stream finished without explicit COMPLETE event; trigger polling recovery
    if (!isComplete && runId) {
      console.log(`[TINYFISH RECOVERY] Stream ended without COMPLETE. Polling run ${runId}...`);
      const pollResult = await pollRun(runId);
      if (pollResult.status === 'COMPLETED') {
        const items = extractItems(pollResult.result);
        if (onComplete) onComplete(items, runId, 'COMPLETED');
      } else {
        throw new Error(pollResult.error || 'Agent run failed');
      }
    }
  } catch (err) {
    console.error('[TINYFISH RUN ERROR]', err.message);
    if (onError) onError(err);
  }
}

export default {
  extractItems,
  pollRun,
  runTinyFishSync,
  MENU_GOAL,
  OUTPUT_SCHEMA
};
