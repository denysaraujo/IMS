package com.br.api.model.sales;

import com.br.api.model.customer.Customer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String saleCode;

    @Column(nullable = false)
    private LocalDateTime saleDate;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, CANCELLED

    @Column(length = 500)
    private String notes;

    // Relacionamento com Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore // 🔥 CORREÇÃO: Evita recursão infinita
    private Customer customer;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // 🔥 CORREÇÃO: Evita recursão com SaleItem se necessário
    private List<SaleItem> items = new ArrayList<>();

    // Construtores
    public Sale() {
        this.saleDate = LocalDateTime.now();
        this.status = "PENDING";
        this.totalAmount = 0.0;
    }

    public Sale(String saleCode, Customer customer) {
        this();
        this.saleCode = saleCode;
        this.customer = customer;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSaleCode() { return saleCode; }
    public void setSaleCode(String saleCode) { this.saleCode = saleCode; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }

    // Métodos de negócio
    public void calculateTotal() {
        if (items == null || items.isEmpty()) {
            this.totalAmount = 0.0;
            return;
        }
        this.totalAmount = items.stream()
            .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
            .sum();
    }

    public void processSale() {
        this.status = "COMPLETED";
        this.saleDate = LocalDateTime.now();
        calculateTotal();
        
        // Baixa de estoque para cada item
        if (items != null) {
            for (SaleItem item : items) {
                item.processInventoryDeduction();
            }
        }
    }

    public void cancelSale() {
        this.status = "CANCELLED";
        
        // Restaurar estoque para cada item
        if (items != null) {
            for (SaleItem item : items) {
                item.restoreInventory();
            }
        }
    }

    public void addItem(SaleItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        item.setSale(this);
        items.add(item);
        calculateTotal();
    }

    public void removeItem(SaleItem item) {
        if (items != null) {
            items.remove(item);
            item.setSale(null);
            calculateTotal();
        }
    }
}