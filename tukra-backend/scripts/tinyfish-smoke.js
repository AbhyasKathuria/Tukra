import config from '../src/config.js';
import { runTinyFishSync, extractItems } from '../src/services/tinyfish.js';

async function runSmokeTest() {
  console.log('🐟 [TINYFISH SMOKE TEST] Initializing web agent smoke test...');

  if (!config.TINYFISH_API_KEY) {
    console.error('\n❌ ERROR: TINYFISH_API_KEY is not set in tukra-backend/.env');
    console.error('Please add your TinyFish API key to tukra-backend/.env:');
    console.error('  TINYFISH_API_KEY=your_key_here\n');
    process.exit(1);
  }

  const targetUrl = config.DEMO_MENU_URL || 'https://raw.githubusercontent.com';
  console.log(`🌐 Target Menu URL: ${targetUrl}`);
  console.log('📡 Connecting to TinyFish SSE automation endpoint...\n');

  let itemCount = 0;

  await runTinyFishSync(targetUrl, {
    onStarted: (runId) => {
      console.log(`🚀 [STARTED] Agent session created. Run ID: ${runId}`);
    },
    onStreamingUrl: (streamingUrl) => {
      console.log(`📺 [STREAMING URL] Live browser viewer: ${streamingUrl}`);
    },
    onProgress: (progress) => {
      console.log(`⏳ [PROGRESS] ${progress}`);
    },
    onComplete: (items, runId, status) => {
      console.log(`\n✅ [COMPLETE] Status: ${status} | Extracted ${items.length} items from target page.`);
      itemCount = items.length;

      if (items.length > 0) {
        console.log('\nExtracted Menu Items:');
        console.table(
          items.map((i) => ({
            Name: i.name,
            Category: i.category,
            'Price (₹)': `₹${(i.pricePaise / 100).toFixed(2)}`,
            Veg: i.isVeg ? 'Veg' : 'Non-Veg',
            'Sold Out': i.soldOut ? 'Yes' : 'No'
          }))
        );
      } else {
        console.log('ℹ️  No items parsed (make sure targetUrl contains food menu data).');
      }

      console.log('\n🎉 [TINYFISH SMOKE TEST SUCCESSFUL] API key & SSE client verified.\n');
      process.exit(0);
    },
    onError: (err) => {
      console.error(`\n❌ [TINYFISH SMOKE TEST FAILED] ${err.message}\n`);
      process.exit(1);
    }
  });
}

runSmokeTest();
