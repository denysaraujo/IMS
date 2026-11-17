ALTER TABLE inventory_items
    ADD COLUMN available_for_rental BOOLEAN NOT NULL DEFAULT FALSE;
