package com.br.api.model.customer;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.br.api.model.address.Address;
import com.br.api.model.sales.Sale;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String name;

    @Email(message = "Email deve ser válido")
    @Column(unique = true)
    private String email;

    private String phone;

    @Column(nullable = false, unique = true)
    private String document; // CPF ou CNPJ

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType type; // INDIVIDUAL, COMPANY

    @Embedded
    private Address address;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Sale> sales = new ArrayList<>();

    // Construtores
    public Customer() {
        this.createdAt = LocalDateTime.now();
        this.type = CustomerType.INDIVIDUAL;
    }

    public Customer(String name, String document, String email) {
        this();
        this.name = name;
        this.document = document;
        this.email = email;
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

    public List<Sale> getSales() { return sales; }
    public void setSales(List<Sale> sales) { this.sales = sales; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
   
}