package com.br.api.repository;

import com.br.api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Métodos customizados podem ser adicionados aqui
}
