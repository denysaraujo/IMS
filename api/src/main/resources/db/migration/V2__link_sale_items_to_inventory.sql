DO $$
BEGIN
    IF to_regclass('sale_items') IS NOT NULL
       AND to_regclass('inventory_items') IS NOT NULL THEN
        ALTER TABLE sale_items
            ADD COLUMN IF NOT EXISTS inventory_item_id BIGINT,
            ADD COLUMN IF NOT EXISTS category VARCHAR(255),
            ADD COLUMN IF NOT EXISTS description VARCHAR(2000);

        ALTER TABLE sale_items
            ALTER COLUMN product_code DROP NOT NULL;

        ALTER TABLE sale_items
            ADD CONSTRAINT fk_sale_items_inventory_item
            FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id);
    END IF;
END
$$;
