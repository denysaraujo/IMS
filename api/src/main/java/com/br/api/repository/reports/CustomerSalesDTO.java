package com.br.api.repository.reports;

import com.br.api.model.customer.Customer;

public class CustomerSalesDTO {
    private Customer customer;
    private Double totalPurchases;

    public CustomerSalesDTO(Customer customer, Double totalPurchases) {
        this.customer = customer;
        this.totalPurchases = totalPurchases;
    }

    // Getters e Setters
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Double getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(Double totalPurchases) {
        this.totalPurchases = totalPurchases;
    }
}