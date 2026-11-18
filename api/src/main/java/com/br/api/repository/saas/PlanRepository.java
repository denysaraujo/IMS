package com.br.api.repository.saas;

import com.br.api.model.saas.Plan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    @EntityGraph(attributePaths = "features")
    List<Plan> findByActiveTrueOrderByAmountAsc();

    @EntityGraph(attributePaths = "features")
    List<Plan> findAllByOrderByAmountAsc();

    @EntityGraph(attributePaths = "features")
    Optional<Plan> findWithFeaturesById(Long id);

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}
