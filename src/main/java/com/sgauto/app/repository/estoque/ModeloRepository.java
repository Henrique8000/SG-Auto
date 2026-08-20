package com.sgauto.app.repository.estoque;

import com.sgauto.app.model.estoque.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeloRepository extends JpaRepository<Modelo, Long> {
    Optional<Modelo> findByNome(String nome);
    List<Modelo> findByAtivoTrue();
}
