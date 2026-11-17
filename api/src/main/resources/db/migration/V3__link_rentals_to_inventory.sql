ALTER TABLE rentals
    ADD COLUMN inventory_item_id BIGINT,
    ALTER COLUMN product_code DROP NOT NULL;

ALTER TABLE rentals
    ADD CONSTRAINT fk_rentals_inventory_item
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id);

CREATE INDEX idx_rentals_inventory_item_id ON rentals(inventory_item_id);
