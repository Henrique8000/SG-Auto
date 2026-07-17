package com.sgauto.app.repository;

import com.sgauto.app.model.Categoria;
import com.sgauto.app.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Optional<Servico> findByCodigo(String codigo);
    boolean existsByCategoria(String categoria);
    List<Servico> findByAtivo(Boolean ativo);
}