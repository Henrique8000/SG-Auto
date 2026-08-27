package com.sgauto.app.repository.estoque;

import com.sgauto.app.model.estoque.CategoriaFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaFornecedorRepository extends JpaRepository<CategoriaFornecedor, Long>, JpaSpecificationExecutor<CategoriaFornecedor> {
    Optional<CategoriaFornecedor> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<CategoriaFornecedor> findByAtivoTrueOrderByNomeAsc();
}
