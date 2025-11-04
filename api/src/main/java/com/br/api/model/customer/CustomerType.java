// CustomerType.java
package com.br.api.model.customer;

public enum CustomerType {
    INDIVIDUAL("Pessoa Física"),
    COMPANY("Pessoa Jurídica");

    private final String description;

    CustomerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}