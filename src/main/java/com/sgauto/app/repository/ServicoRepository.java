package com.sgauto.app.repository;

import com.sgauto.app.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Optional<Servico> findByCodigo(String codigo);
    boolean existsByCategoria(String categoria);
}