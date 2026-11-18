ALTER TABLE saas_plans
    ADD COLUMN IF NOT EXISTS max_users INTEGER,
    ADD COLUMN IF NOT EXISTS max_customers INTEGER,
    ADD COLUMN IF NOT EXISTS max_inventory_items INTEGER;

ALTER TABLE saas_subscriptions
    ADD COLUMN IF NOT EXISTS trial_ends_at TIMESTAMP;

DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    IF to_regclass('customers') IS NOT NULL THEN
        FOR constraint_name IN
            SELECT c.conname
            FROM pg_constraint c
            WHERE c.conrelid = to_regclass('customers')
              AND c.contype = 'u'
              AND (
                  SELECT array_agg(a.attname::TEXT ORDER BY a.attname::TEXT)
                  FROM unnest(c.conkey) AS key(attnum)
                  JOIN pg_attribute a
                    ON a.attrelid = c.conrelid
                   AND a.attnum = key.attnum
              ) = ARRAY['email']::TEXT[]
        LOOP
            EXECUTE format('ALTER TABLE customers DROP CONSTRAINT %I', constraint_name);
        END LOOP;

        ALTER TABLE customers
            ADD CONSTRAINT uk_customers_company_email UNIQUE (company_id, email);
    END IF;
END $$;

ALTER TABLE saas_plans
    DROP CONSTRAINT IF EXISTS saas_plans_amount_check;

ALTER TABLE saas_plans
    ADD CONSTRAINT saas_plans_amount_check CHECK (amount >= 0);

INSERT INTO saas_plans (
    code, name, description, amount, currency, frequency, frequency_type, active
)
VALUES (
    'legacy-grandfathered',
    'Plano legado',
    'Acesso mantido para empresas existentes antes da implantação do SaaS.',
    0,
    'BRL',
    1,
    'months',
    FALSE
)
ON CONFLICT (code) DO NOTHING;

INSERT INTO saas_plan_features (plan_id, feature_id)
SELECT p.id, f.id
FROM saas_plans p
CROSS JOIN saas_features f
WHERE p.code = 'legacy-grandfathered'
  AND f.active = TRUE
ON CONFLICT DO NOTHING;

INSERT INTO saas_subscriptions (
    company_id, plan_id, provider, status, amount, currency
)
SELECT c.id, p.id, 'LEGACY', 'ACTIVE', 0, 'BRL'
FROM companies c
CROSS JOIN saas_plans p
WHERE p.code = 'legacy-grandfathered'
  AND NOT EXISTS (
      SELECT 1
      FROM saas_subscriptions s
      WHERE s.company_id = c.id
  );
