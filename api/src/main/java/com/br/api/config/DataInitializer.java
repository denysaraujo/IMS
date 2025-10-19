package com.br.api.config;

import com.br.api.model.User;
import com.br.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                String hashedPassword = passwordEncoder.encode("admin123");
                User admin = new User("Administrador", "admin", hashedPassword, "ADMIN");
                userRepository.save(admin);
                System.out.println("Usuário ADMIN criado: admin/admin123");
            }
        };
    }
}
