package com.br.api.model;

public enum Role {
    ADMIN("Administrador"),
    USER("Usuário"),
    MANAGER("Gerente"),
    SUPERVISOR("Supervisor");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER; // Valor padrão se não encontrar
        }
    }
}