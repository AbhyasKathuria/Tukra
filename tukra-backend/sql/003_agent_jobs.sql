-- 003_agent_jobs.sql: Shared TinyFish Agent Layer
CREATE TABLE IF NOT EXISTS agent_jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  kind TEXT NOT NULL,                             -- 'MENU_SYNC', 'DISLIKE_RESCUE', 'VENDOR_ONBOARD', 'NEARBY_DISCOVER', 'PRICE_BENCHMARK', 'FRESHNESS_GUARD'
  requested_by UUID REFERENCES users(id) ON DELETE SET NULL,
  role TEXT,
  input JSONB NOT NULL DEFAULT '{}',
  status TEXT NOT NULL DEFAULT 'QUEUED' CHECK (status IN ('QUEUED','RUNNING','DONE','PARTIAL','FAILED')),
  tinyfish_run_id TEXT,
  streaming_url TEXT,
  result JSONB,
  error TEXT,
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  finished_at TIMESTAMPTZ,
  duration_ms INT,
  cache_key TEXT,
  from_cache BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_agent_jobs_kind ON agent_jobs(kind);
CREATE INDEX IF NOT EXISTS idx_agent_jobs_cache ON agent_jobs(cache_key) WHERE status = 'DONE';
CREATE INDEX IF NOT EXISTS idx_agent_jobs_started_at ON agent_jobs(started_at DESC);

-- Student food dislikes and feedback table
CREATE TABLE IF NOT EXISTS food_dislikes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id) ON DELETE SET NULL,
  dish_name TEXT NOT NULL,
  reason TEXT,
  detected_location JSONB,
  agent_job_id UUID REFERENCES agent_jobs(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_food_dislikes_created_at ON food_dislikes(created_at DESC);

-- Menu Freshness Alerts
CREATE TABLE IF NOT EXISTS menu_freshness_alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
  alert_type TEXT NOT NULL CHECK (alert_type IN ('STALE_MENU','SOLD_OUT_SURGE','PRICE_MISMATCH')),
  message TEXT NOT NULL,
  severity TEXT NOT NULL DEFAULT 'MEDIUM' CHECK (severity IN ('LOW','MEDIUM','HIGH')),
  details JSONB,
  is_resolved BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_menu_freshness_created ON menu_freshness_alerts(created_at DESC);
