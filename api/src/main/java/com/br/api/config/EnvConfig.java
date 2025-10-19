package com.br.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("!prod")
public class EnvConfig {
    
    public EnvConfig(Environment env) {
    }
    
    // Métodos adicionais se necessário
}
