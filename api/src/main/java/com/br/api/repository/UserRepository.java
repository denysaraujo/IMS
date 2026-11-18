package com.br.api.repository;

import com.br.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    long countByRoleNot(com.br.api.model.Role role);

    @Query(value = "SELECT company_id, COUNT(*) FROM users WHERE role <> 'PLATFORM_ADMIN' GROUP BY company_id", nativeQuery = true)
    java.util.List<Object[]> countAllByCompany();
}