package com.br.api.dto;

import com.br.api.model.address.Address;
import com.br.api.model.customer.CustomerType;

import java.time.LocalDateTime;

public class CustomerWithStatsDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String document;
    private CustomerType type;
    private Address address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double totalPurchases;
    private Long totalOrders; // Mudei de Integer para Long

    // Construtor vazio
    public CustomerWithStatsDTO() {}

    // Construtor para a consulta JPQL - CORRIGIDO
    public CustomerWithStatsDTO(Long id, String name, String email, String phone, 
                               String document, CustomerType type, Address address,
                               LocalDateTime createdAt, LocalDateTime updatedAt,
                               Double totalPurchases, Long totalOrders) { // Mudei para Long
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
        this.type = type;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalPurchases = totalPurchases != null ? totalPurchases : 0.0;
        this.totalOrders = totalOrders != null ? totalOrders : 0L;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public CustomerType getType() { return type; }
    public void setType(CustomerType type) { this.type = type; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Double getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(Double totalPurchases) { this.totalPurchases = totalPurchases; }

    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
}