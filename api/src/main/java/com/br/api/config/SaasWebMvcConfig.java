package com.br.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaasWebMvcConfig implements WebMvcConfigurer {
    private final SaasAccessInterceptor saasAccessInterceptor;

    public SaasWebMvcConfig(SaasAccessInterceptor saasAccessInterceptor) {
        this.saasAccessInterceptor = saasAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(saasAccessInterceptor).addPathPatterns("/api/**");
    }
}
