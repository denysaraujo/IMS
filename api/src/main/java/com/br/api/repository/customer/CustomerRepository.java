package com.br.api.repository.customer;

import com.br.api.model.customer.Customer;
import com.br.api.dto.CustomerWithStatsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByDocument(String document);
    
    List<Customer> findByNameContainingIgnoreCase(String name);
    
    // Consulta simplificada - primeiro vamos fazer funcionar o básico
    @Query("SELECT new com.br.api.dto.CustomerWithStatsDTO(" +
           "c.id, c.name, c.email, c.phone, c.document, c.type, c.address, " +
           "c.createdAt, c.updatedAt, " +
           "0.0, 0L) " + // Valores fixos por enquanto
           "FROM Customer c")
    List<CustomerWithStatsDTO> findAllWithStats();
    
    // Consulta para buscar um cliente específico com estatísticas
    @Query("SELECT new com.br.api.dto.CustomerWithStatsDTO(" +
           "c.id, c.name, c.email, c.phone, c.document, c.type, c.address, " +
           "c.createdAt, c.updatedAt, " +
           "0.0, 0L) " + // Valores fixos por enquanto
           "FROM Customer c " +
           "WHERE c.id = :id")
    Optional<CustomerWithStatsDTO> findByIdWithStats(@Param("id") Long id);
    
    // Buscar clientes por nome com estatísticas
    @Query("SELECT new com.br.api.dto.CustomerWithStatsDTO(" +
           "c.id, c.name, c.email, c.phone, c.document, c.type, c.address, " +
           "c.createdAt, c.updatedAt, " +
           "0.0, 0L) " + // Valores fixos por enquanto
           "FROM Customer c " +
           "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<CustomerWithStatsDTO> findByNameWithStats(@Param("name") String name);
    
    // Estatísticas
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.type = 'INDIVIDUAL'")
    Long countIndividualCustomers();
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.type = 'COMPANY'")
    Long countCompanyCustomers();
    
    // Método para buscar top clientes
    @Query("SELECT new com.br.api.dto.CustomerWithStatsDTO(" +
           "c.id, c.name, c.email, c.phone, c.document, c.type, c.address, " +
           "c.createdAt, c.updatedAt, " +
           "0.0, 0L) " + // Valores fixos por enquanto
           "FROM Customer c " +
           "ORDER BY c.createdAt DESC")
    List<CustomerWithStatsDTO> findTopCustomers();
}