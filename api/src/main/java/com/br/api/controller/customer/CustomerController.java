package com.br.api.controller.customer;

import com.br.api.model.customer.Customer;
import com.br.api.service.customer.CustomerService;
import com.br.api.dto.CustomerWithStatsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @GetMapping
    public ResponseEntity<?> getAllCustomers() {
        try {
            System.out.println("=== INICIANDO CONSULTA DE CLIENTES ===");
            List<CustomerWithStatsDTO> customers = customerService.findAll();
            System.out.println("=== CLIENTES ENCONTRADOS: " + customers.size() + " ===");
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            System.err.println("=== ERRO NA CONSULTA: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno ao buscar clientes: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        try {
            Optional<CustomerWithStatsDTO> customer = customerService.findById(id);
            return customer.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno ao buscar cliente: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        try {
            Customer savedCustomer = customerService.save(customer);
            return ResponseEntity.ok(savedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno ao criar cliente: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        try {
            // Verifica se o cliente existe - usando o método correto
            Optional<Customer> existingCustomer = customerService.findEntityById(id);
            if (!existingCustomer.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            customer.setId(id);
            Customer updatedCustomer = customerService.save(customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno ao atualizar cliente: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            // Verifica se o cliente existe antes de deletar
            Optional<Customer> existingCustomer = customerService.findEntityById(id);
            if (!existingCustomer.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            customerService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno ao excluir cliente: " + e.getMessage());
        }
    }
        
    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(@RequestParam String name) {
        try {
            List<CustomerWithStatsDTO> customers = customerService.searchByName(name);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno ao buscar clientes: " + e.getMessage());
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getCustomerStats() {
        try {
            CustomerService.CustomerStats stats = customerService.getCustomerStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno ao buscar estatísticas: " + e.getMessage());
        }
    }
    
    @GetMapping("/top")
    public ResponseEntity<?> getTopCustomers() {
        try {
            List<CustomerWithStatsDTO> topCustomers = customerService.getTopCustomers();
            return ResponseEntity.ok(topCustomers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno ao buscar top clientes: " + e.getMessage());
        }
    }
}