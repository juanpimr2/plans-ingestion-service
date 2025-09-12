-- Composite index para rango temporal (acelera overlap)
CREATE INDEX IF NOT EXISTS idx_plans_time_window
    ON plans (starts_at, ends_at);

-- Filtro por sell_mode (online)
CREATE INDEX IF NOT EXISTS idx_plans_sell_mode
    ON plans (sell_mode);

-- Consulta combinada típica
CREATE INDEX IF NOT EXISTS idx_plans_sell_mode_time
    ON plans (sell_mode, starts_at, ends_at);
