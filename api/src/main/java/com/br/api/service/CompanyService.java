package com.br.api.service;

import com.br.api.dto.company.CompanyDTO;
import com.br.api.model.Company;
import com.br.api.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public CompanyDTO getCurrent() {
        return companyRepository.findFirstByOrderByIdAsc()
                .map(CompanyDTO::new)
                .orElseGet(CompanyDTO::new);
    }

    public CompanyDTO save(CompanyDTO dto) {
        Company company = companyRepository.findFirstByOrderByIdAsc().orElseGet(Company::new);
        company.setName(dto.getName());
        company.setDocument(dto.getDocument());
        company.setEmail(dto.getEmail());
        company.setPhone(dto.getPhone());
        company.setAddress(dto.getAddress());
        company.setCity(dto.getCity());
        company.setState(dto.getState());
        company.setLogoData(dto.getLogoData());
        company.setPrintFooter(dto.getPrintFooter());
        return new CompanyDTO(companyRepository.save(company));
    }
}