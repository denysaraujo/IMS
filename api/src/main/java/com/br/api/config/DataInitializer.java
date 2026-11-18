package com.br.api.config;

import com.br.api.model.Role;
import com.br.api.model.User;
import com.br.api.repository.UserRepository;
import com.br.api.repository.CompanyRepository;
import com.br.api.model.Company;
import com.br.api.tenant.TenantContext;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createAdminUser(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            Company company = companyRepository.findFirstByOrderByIdAsc()
                    .orElseGet(() -> {
                        Company legacyCompany = new Company();
                        legacyCompany.setName("IMS - Tenant legado");
                        return companyRepository.save(legacyCompany);
                    });

            try {
                TenantContext.setTenantId(company.getId());
                if (userRepository.findByUsername("admin").isEmpty()) {
                    User admin = new User();

                    admin.setNomeCompleto("Administrador");
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("admin123"));

                    admin.setRole(Role.ADMIN);

                    admin.setEmail(null);
                    admin.setTelefone(null);
                    admin.setCelular(null);

                    admin.setEnabled(true);
                    admin.setAccountNonLocked(true);
                    admin.setAccountNonExpired(true);
                    admin.setCredentialsNonExpired(true);
                    admin.setLoginAttempts(0);

                    userRepository.save(admin);

                    System.out.println("Usuário ADMIN criado: admin/admin123");
                }
            } finally {
                TenantContext.clear();
            }
        };
    }

    @Bean
    public CommandLineRunner createPlatformAdmin(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            @Value("${platform.admin.username:}") String username,
            @Value("${platform.admin.password:}") String password,
            @Value("${platform.admin.name:Administrador da plataforma}") String name) {
        return args -> {
            if (username.isBlank() && password.isBlank()) {
                return;
            }
            if (username.isBlank() || password.length() < 12) {
                throw new IllegalStateException(
                        "Configure PLATFORM_ADMIN_USERNAME e uma PLATFORM_ADMIN_PASSWORD com pelo menos 12 caracteres."
                );
            }

            Company company = companyRepository.findFirstByOrderByIdAsc()
                    .orElseThrow(() -> new IllegalStateException("Empresa inicial não foi criada."));
            TenantContext.setTenantId(company.getId());
            try {
                var existing = userRepository.findByUsername(username.trim());
                if (existing.isPresent()) {
                    if (existing.get().getRole() == Role.PLATFORM_ADMIN) {
                        return;
                    }
                    throw new IllegalStateException("PLATFORM_ADMIN_USERNAME pertence a outra conta.");
                }
                User admin = new User();
                admin.setNomeCompleto(name);
                admin.setUsername(username.trim());
                admin.setPassword(passwordEncoder.encode(password));
                admin.setRole(Role.PLATFORM_ADMIN);
                admin.setEnabled(true);
                admin.setAccountNonLocked(true);
                admin.setAccountNonExpired(true);
                admin.setCredentialsNonExpired(true);
                admin.setLoginAttempts(0);
                userRepository.save(admin);
            } finally {
                TenantContext.clear();
            }
        };
    }
}