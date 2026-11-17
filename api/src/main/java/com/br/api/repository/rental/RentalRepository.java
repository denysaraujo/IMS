package com.br.api.repository.rental;

import com.br.api.model.rental.Rental;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByCustomerId(Long customerId);
    List<Rental> findByStatus(String status);

    @Query("SELECT rental FROM Rental rental JOIN FETCH rental.customer ORDER BY rental.createdAt DESC")
    List<Rental> findAllWithCustomerOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rental FROM Rental rental JOIN FETCH rental.customer WHERE rental.id = :id")
    Optional<Rental> findByIdForUpdate(@Param("id") Long id);
}
