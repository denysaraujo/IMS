package com.br.api.repository;

import com.br.api.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findFirstByOrderByIdAsc();
}