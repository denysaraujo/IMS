ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS saas_features (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS saas_plans (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    frequency INTEGER NOT NULL DEFAULT 1 CHECK (frequency > 0),
    frequency_type VARCHAR(16) NOT NULL DEFAULT 'months'
        CHECK (frequency_type IN ('months', 'days')),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS saas_plan_features (
    plan_id BIGINT NOT NULL REFERENCES saas_plans(id) ON DELETE CASCADE,
    feature_id BIGINT NOT NULL REFERENCES saas_features(id) ON DELETE RESTRICT,
    PRIMARY KEY (plan_id, feature_id)
);

CREATE TABLE IF NOT EXISTS saas_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES saas_plans(id) ON DELETE RESTRICT,
    provider VARCHAR(32) NOT NULL DEFAULT 'MERCADO_PAGO',
    provider_subscription_id VARCHAR(120) UNIQUE,
    checkout_url VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT',
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_saas_subscriptions_company
    ON saas_subscriptions (company_id, created_at DESC);

CREATE TABLE IF NOT EXISTS saas_payments (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    subscription_id BIGINT NOT NULL REFERENCES saas_subscriptions(id) ON DELETE RESTRICT,
    provider_payment_id VARCHAR(120) NOT NULL UNIQUE,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_saas_payments_company
    ON saas_payments (company_id, created_at DESC);

CREATE TABLE IF NOT EXISTS saas_billing_events (
    id BIGSERIAL PRIMARY KEY,
    provider_event_id VARCHAR(160) NOT NULL UNIQUE,
    provider_subscription_id VARCHAR(120),
    type VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO saas_features (code, name, description)
VALUES
    ('users', 'Usuários', 'Gestão dos usuários da empresa'),
    ('customers', 'Clientes', 'Cadastro e gestão de clientes'),
    ('inventory', 'Estoque', 'Inventários, produtos e alertas de estoque'),
    ('sales', 'Vendas', 'Registro e consulta de vendas'),
    ('rentals', 'Locações', 'Gestão de locações'),
    ('reports', 'Relatórios', 'Relatórios gerenciais'),
    ('company', 'Empresa', 'Configurações da empresa')
ON CONFLICT (code) DO NOTHING;
