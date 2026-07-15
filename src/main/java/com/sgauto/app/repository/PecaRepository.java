package com.sgauto.app.repository;

import com.sgauto.app.model.Peca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PecaRepository extends JpaRepository<Peca, Long> {
    Optional<Peca> findByCodigo(String codigo);
}