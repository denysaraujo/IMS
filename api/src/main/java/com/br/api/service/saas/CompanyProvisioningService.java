package com.br.api.service.saas;

import com.br.api.dto.saas.SignupRequest;
import com.br.api.model.Company;
import com.br.api.repository.CompanyRepository;
import com.br.api.repository.UserRepository;
import com.br.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanyProvisioningService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyProvisioningService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Company createCompany(SignupRequest request) {
        TenantContext.setTenantId(0L);
        try {
            String username = request.username().trim();
            String email = request.email().trim().toLowerCase();
            String document = request.companyDocument() == null ? null : request.companyDocument().trim();
            if (userRepository.existsByUsername(username)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Nome de usuário já cadastrado.");
            }
            if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
            }
            if (document != null && !document.isBlank() && companyRepository.existsByDocument(document)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ/CPF já cadastrado.");
            }
            Company company = new Company();
            company.setName(request.companyName().trim());
            company.setEmail(email);
            company.setDocument(document == null || document.isBlank() ? null : document);
            return companyRepository.saveAndFlush(company);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public void deleteUnpaidCompany(Long companyId) {
        TenantContext.setTenantId(0L);
        try {
            companyRepository.deleteById(companyId);
        } finally {
            TenantContext.clear();
        }
    }
}
