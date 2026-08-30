package com.br.api.config;

import com.br.api.model.Role;
import com.br.api.model.User;
import com.br.api.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();

                admin.setNomeCompleto("Administrador");
                admin.setUsername("admin");
                admin.setPassword(
                        passwordEncoder.encode("admin123")
                );

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

                System.out.println(
                        "Usuário ADMIN criado: admin/admin123"
                );
            }
        };
    }
}