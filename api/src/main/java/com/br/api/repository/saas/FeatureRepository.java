package com.br.api.repository.saas;

import com.br.api.model.saas.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
    List<Feature> findAllByOrderByNameAsc();
    List<Feature> findByActiveTrueOrderByNameAsc();
    Optional<Feature> findByCode(String code);
    boolean existsByCode(String code);
}
