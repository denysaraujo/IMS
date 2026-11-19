CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

INSERT INTO companies (name, created_at)
SELECT 'IMS - Tenant legado', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM companies);

DO $$
DECLARE
    table_name TEXT;
    constraint_name TEXT;
    tenant_tables TEXT[] := ARRAY[
        'users',
        'customers',
        'inventories',
        'inventory_items',
        'sales',
        'rentals'
    ];
    unique_columns TEXT[][] := ARRAY[
        ARRAY['customers', 'document'],
        ARRAY['inventories', 'inventory_code'],
        ARRAY['sales', 'sale_code'],
        ARRAY['rentals', 'rental_code']
    ];
    item TEXT[];
BEGIN
    FOREACH table_name IN ARRAY tenant_tables LOOP
        IF to_regclass(table_name) IS NOT NULL THEN
            EXECUTE format(
                'ALTER TABLE %I ADD COLUMN IF NOT EXISTS company_id BIGINT',
                table_name
            );
            EXECUTE format(
                'UPDATE %I SET company_id = (SELECT id FROM companies ORDER BY id LIMIT 1) WHERE company_id IS NULL',
                table_name
            );
            EXECUTE format(
                'ALTER TABLE %I ALTER COLUMN company_id SET NOT NULL',
                table_name
            );
            EXECUTE format(
                'ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE',
                table_name,
                'fk_' || table_name || '_company'
            );
        END IF;
    END LOOP;

    FOREACH item SLICE 1 IN ARRAY unique_columns LOOP
        table_name := item[1];
        IF to_regclass(table_name) IS NOT NULL THEN
            FOR constraint_name IN
                SELECT c.conname
                FROM pg_constraint c
                WHERE c.conrelid = to_regclass(table_name)
                  AND c.contype = 'u'
                  AND (
                    SELECT array_agg(a.attname::TEXT ORDER BY a.attname::TEXT)
                    FROM unnest(c.conkey) AS key(attnum)
                    JOIN pg_attribute a
                      ON a.attrelid = c.conrelid
                     AND a.attnum = key.attnum
                  ) = ARRAY[item[2]]::TEXT[]
            LOOP
                EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', table_name, constraint_name);
            END LOOP;

            IF NOT EXISTS (
                SELECT 1
                FROM pg_constraint c
                WHERE c.conrelid = to_regclass(table_name)
                  AND c.conname = 'uk_' || table_name || '_company_' ||
                      CASE table_name
                          WHEN 'customers' THEN 'document'
                          WHEN 'inventories' THEN 'code'
                          WHEN 'sales' THEN 'code'
                          ELSE 'code'
                      END
            ) THEN
                EXECUTE format(
                    'ALTER TABLE %I ADD CONSTRAINT %I UNIQUE (company_id, %I)',
                    table_name,
                    CASE table_name
                        WHEN 'customers' THEN 'uk_customers_company_document'
                        WHEN 'inventories' THEN 'uk_inventories_company_code'
                        WHEN 'sales' THEN 'uk_sales_company_code'
                        ELSE 'uk_rentals_company_code'
                    END,
                    item[2]
                );
            END IF;
        END IF;
    END LOOP;
END $$;
