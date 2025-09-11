-- V1: esquema inicial
CREATE TABLE IF NOT EXISTS plans (
  id BIGSERIAL PRIMARY KEY,
  provider_id VARCHAR(128) UNIQUE NOT NULL,
  title TEXT NOT NULL,
  sell_mode VARCHAR(16) NOT NULL,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at   TIMESTAMPTZ NOT NULL,
  min_price DOUBLE PRECISION,
  max_price DOUBLE PRECISION,
  first_seen_at TIMESTAMPTZ NOT NULL,
  last_seen_at  TIMESTAMPTZ NOT NULL,
  currently_available BOOLEAN NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_plans_window   ON plans (starts_at, ends_at);
CREATE INDEX IF NOT EXISTS idx_plans_sellmode ON plans (sell_mode);
