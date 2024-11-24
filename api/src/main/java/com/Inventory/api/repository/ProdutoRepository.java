package com.Inventory.api.repository;

import com.Inventory.api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Métodos customizados podem ser adicionados aqui
}
