package com.br.api.dto.user;

import com.br.api.model.Role;
import com.br.api.model.User;

import java.time.LocalDateTime;

public class UserResponseDTO {

    private Long id;
    private String nomeCompleto;
    private String username;
    private String email;
    private String telefone;
    private String celular;
    private Role role;
    private String roleDisplayName;

    private boolean enabled;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;

    private Integer loginAttempts;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponseDTO() {
    }

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.nomeCompleto = user.getNomeCompleto();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.telefone = user.getTelefone();
        this.celular = user.getCelular();
        this.role = user.getRole();
        this.roleDisplayName = user.getRole() != null
                ? user.getRole().getDisplayName()
                : null;

        this.enabled = user.isEnabled();
        this.accountNonLocked = user.isAccountNonLocked();
        this.accountNonExpired = user.isAccountNonExpired();
        this.credentialsNonExpired = user.isCredentialsNonExpired();

        this.loginAttempts = user.getLoginAttempts();
        this.lastLogin = user.getLastLogin();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCelular() {
        return celular;
    }

    public Role getRole() {
        return role;
    }

    public String getRoleDisplayName() {
        return roleDisplayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}