package com.br.api.config;

import com.br.api.model.Role;
import com.br.api.model.User;
import com.br.api.repository.CompanyRepository;
import com.br.api.service.saas.SaasAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class SaasAccessInterceptor implements HandlerInterceptor {
    private final CompanyRepository companyRepository;
    private final SaasAccessService accessService;

    public SaasAccessInterceptor(CompanyRepository companyRepository, SaasAccessService accessService) {
        this.companyRepository = companyRepository;
        this.accessService = accessService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)
                || user.getRole() == Role.PLATFORM_ADMIN) {
            return true;
        }

        if (!companyRepository.findById(user.getTenantId()).map(company -> company.isEnabled()).orElse(false)) {
            return reject(response, HttpStatus.FORBIDDEN, "Acesso à empresa suspenso. Fale com o suporte.");
        }

        String feature = featureForPath(request.getRequestURI());
        if (feature == null) {
            return true;
        }
        if (!accessService.hasActiveSubscription()) {
            return reject(response, HttpStatus.FORBIDDEN, "Assinatura inativa. Acesse Cobrança para regularizar.");
        }
        if (!accessService.hasFeature(feature)) {
            return reject(response, HttpStatus.FORBIDDEN, "Esta funcionalidade não está incluída no plano atual.");
        }
        return true;
    }

    private String featureForPath(String path) {
        if (path.startsWith("/api/users") && !path.equals("/api/users/me")
                && !path.startsWith("/api/users/me/")) return "users";
        if (path.startsWith("/api/customers")) return "customers";
        if (path.startsWith("/api/inventory") || path.startsWith("/api/alerts")) return "inventory";
        if (path.startsWith("/api/sales")) return "sales";
        if (path.startsWith("/api/rentals")) return "rentals";
        if (path.startsWith("/api/reports")) return "reports";
        if (path.startsWith("/api/company")) return "company";
        return null;
    }

    private boolean reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
        return false;
    }
}
