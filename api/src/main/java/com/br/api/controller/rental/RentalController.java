package com.br.api.controller.rental;

import com.br.api.model.rental.Rental;
import com.br.api.service.rental.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "http://localhost:4200")
public class RentalController {
    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Long id) {
        Optional<Rental> rental = rentalService.findById(id);
        return rental.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Rental> createRental(@RequestBody Rental rental) {
        try {
            return ResponseEntity.ok(rentalService.createRental(rental));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Rental> returnRental(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(rentalService.returnRental(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
