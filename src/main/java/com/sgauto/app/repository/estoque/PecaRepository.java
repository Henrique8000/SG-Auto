package com.sgauto.app.repository.estoque;

import com.sgauto.app.model.estoque.Peca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PecaRepository extends JpaRepository<Peca, Long> {
    Optional<Peca> findByCodigo(String codigo);
    boolean existsByModelo(String modelo);
}