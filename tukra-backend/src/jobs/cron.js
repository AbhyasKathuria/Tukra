import cron from 'node-cron';
import { generateDailySlots } from '../services/slotService.js';
import { markNoShowOrders } from '../services/orderService.js';
import { query } from '../db.js';

export function initCronJobs() {
  const timezone = 'Asia/Kolkata';

  // 1. Generate pickup slots at 05:00 IST daily
  cron.schedule(
    '0 5 * * *',
    async () => {
      try {
        console.log('⏰ [CRON 05:00 IST] Generating daily pickup slots...');
        const count = await generateDailySlots();
        console.log(`✅ [CRON] Successfully generated ${count} slots across all outlets.`);
      } catch (err) {
        console.error('❌ [CRON ERROR] Slot generation failed:', err.message);
      }
    },
    { timezone }
  );

  // 2. Mark abandoned ready orders as NO_SHOW every minute (ready_at > 20 min ago)
  cron.schedule(
    '* * * * *',
    async () => {
      try {
        await markNoShowOrders();
      } catch (err) {
        console.error('❌ [CRON ERROR] Mark no-show failed:', err.message);
      }
    },
    { timezone }
  );

  // 3. Auto-close stale plate sessions every 10 minutes (OPEN > 90 min)
  cron.schedule(
    '*/10 * * * *',
    async () => {
      try {
        const result = await query(
          `UPDATE plate_sessions
              SET status = 'BILLED'
            WHERE status = 'OPEN'
              AND opened_at < now() - interval '90 minutes'
            RETURNING id, plate_code`
        );
        if (result.rows.length > 0) {
          console.log(`🍽️ [CRON] Auto-closed ${result.rows.length} stale plate session(s).`);
        }
      } catch (err) {
        console.error('❌ [CRON ERROR] Close stale plates failed:', err.message);
      }
    },
    { timezone }
  );

  // 4. Nightly Menu Freshness Guard at 02:00 IST (Part 2.C)
  cron.schedule(
    '0 2 * * *',
    async () => {
      try {
        console.log('🐟 [CRON 02:00 IST] Running TinyFish Nightly Menu Freshness Guard...');
        // Runs freshness audit and stores results in agent_jobs
        const { runAgentJob } = await import('../services/agentJobService.js');
        await runAgentJob({
          kind: 'FRESHNESS_GUARD',
          goal: 'Nightly scan of campus outlet menus for stale pricing, sold-out surges, and price mismatches over 20%.',
          input: { scheduledTime: '02:00 IST' },
          ttlMinutes: 360
        });
        console.log('✅ [CRON] Nightly Menu Freshness Guard audit completed.');
      } catch (err) {
        console.error('❌ [CRON ERROR] Menu freshness guard failed:', err.message);
      }
    },
    { timezone }
  );

  console.log('🕒 [CRON] Background scheduled jobs initialized (Asia/Kolkata).');
}

export default {
  initCronJobs
};
