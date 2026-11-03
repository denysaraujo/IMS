package com.br.api.model.sales;

import com.br.api.model.inventory.InventoryItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "sale_items")
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnore // 🔥 CORREÇÃO: Evita recursão com Sale
    private Sale sale;

    @Column
    private String productCode;

    @Column(nullable = false)
    private String productName;

    @Column
    private String category;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    @JsonIgnore
    private InventoryItem inventoryItem;

    // Construtores
    public SaleItem() {}

    public SaleItem(String productCode, String productName, Integer quantity, Double unitPrice) {
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Long getInventoryItemId() {
        return inventoryItem == null ? null : inventoryItem.getId();
    }

    public void setInventoryItemId(Long inventoryItemId) {
        if (inventoryItemId == null) {
            inventoryItem = null;
        } else {
            inventoryItem = new InventoryItem();
            inventoryItem.setId(inventoryItemId);
        }
    }

    public InventoryItem getInventoryItem() { return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }

    // Método para calcular subtotal
    public Double getSubtotal() {
        return quantity * unitPrice;
    }

    // Métodos para controle de estoque
    public void processInventoryDeduction() {
        if (inventoryItem != null) {
            inventoryItem.removeStock(this.quantity);
        }
    }

    public void restoreInventory() {
        if (inventoryItem != null) {
            inventoryItem.addStock(this.quantity);
        }
    }
}