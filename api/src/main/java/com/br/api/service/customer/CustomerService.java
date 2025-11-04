package com.br.api.service.customer;

import com.br.api.model.customer.Customer;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.dto.CustomerWithStatsDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public List<CustomerWithStatsDTO> findAll() {
        List<CustomerWithStatsDTO> customers = customerRepository.findAllWithStats();
        // Agora vamos calcular as estatísticas de forma programática
        return customers.stream()
                .map(this::calculateCustomerStats)
                .toList();
    }
    
    public Optional<CustomerWithStatsDTO> findById(Long id) {
        return customerRepository.findByIdWithStats(id)
                .map(this::calculateCustomerStats);
    }

    public Optional<Customer> findEntityById(Long id) {
    return customerRepository.findById(id);
}
    
    public Optional<Customer> findByDocument(String document) {
        return customerRepository.findByDocument(document);
    }
    
    public Customer save(Customer customer) {
        // Validar se documento já existe
        if (customer.getId() == null) {
            Optional<Customer> existing = customerRepository.findByDocument(customer.getDocument());
            if (existing.isPresent()) {
                throw new RuntimeException("Já existe um cliente com este documento: " + customer.getDocument());
            }
        } else {
            // Para atualização, verifica se o documento pertence a outro cliente
            Optional<Customer> existing = customerRepository.findByDocument(customer.getDocument());
            if (existing.isPresent() && !existing.get().getId().equals(customer.getId())) {
                throw new RuntimeException("Já existe um cliente com este documento: " + customer.getDocument());
            }
        }
        
        return customerRepository.save(customer);
    }
    
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }
    
    public List<CustomerWithStatsDTO> searchByName(String name) {
        List<CustomerWithStatsDTO> customers = customerRepository.findByNameWithStats(name);
        return customers.stream()
                .map(this::calculateCustomerStats)
                .toList();
    }
    
    public List<CustomerWithStatsDTO> getTopCustomers() {
        try {
            List<CustomerWithStatsDTO> topCustomers = customerRepository.findTopCustomers();
            return topCustomers.stream()
                    .map(this::calculateCustomerStats)
                    .limit(10)
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public CustomerStats getCustomerStats() {
        try {
            Long totalCustomers = customerRepository.count();
            Long individualCount = customerRepository.countIndividualCustomers();
            Long companyCount = customerRepository.countCompanyCustomers();
            
            return new CustomerStats(
                totalCustomers != null ? totalCustomers : 0L,
                individualCount != null ? individualCount : 0L,
                companyCount != null ? companyCount : 0L
            );
        } catch (Exception e) {
            return new CustomerStats(0L, 0L, 0L);
        }
    }
    
    // Método auxiliar para calcular estatísticas do cliente
    private CustomerWithStatsDTO calculateCustomerStats(CustomerWithStatsDTO customerDTO) {
        // Busca o cliente completo para calcular as estatísticas
        Optional<Customer> customerOpt = customerRepository.findById(customerDTO.getId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            
            // Calcula totais de forma programática
            Double totalPurchases = 0.0;
            Long totalOrders = 0L;
            
            if (customer.getSales() != null) {
                totalPurchases = customer.getSales().stream()
                        .filter(sale -> "COMPLETED".equals(sale.getStatus()))
                        .mapToDouble(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0)
                        .sum();
                
                totalOrders = customer.getSales().stream()
                        .filter(sale -> "COMPLETED".equals(sale.getStatus()))
                        .count();
            }
            
            customerDTO.setTotalPurchases(totalPurchases);
            customerDTO.setTotalOrders(totalOrders);
        }
        
        return customerDTO;
    }
    
    // DTO para estatísticas
    public static class CustomerStats {
        private Long totalCustomers;
        private Long individualCustomers;
        private Long companyCustomers;
        
        public CustomerStats(Long totalCustomers, Long individualCustomers, Long companyCustomers) {
            this.totalCustomers = totalCustomers;
            this.individualCustomers = individualCustomers;
            this.companyCustomers = companyCustomers;
        }
        
        // Getters e Setters
        public Long getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }
        
        public Long getIndividualCustomers() { return individualCustomers; }
        public void setIndividualCustomers(Long individualCustomers) { this.individualCustomers = individualCustomers; }
        
        public Long getCompanyCustomers() { return companyCustomers; }
        public void setCompanyCustomers(Long companyCustomers) { this.companyCustomers = companyCustomers; }
    }
}