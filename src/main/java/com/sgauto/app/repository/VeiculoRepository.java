package com.sgauto.app.repository;

import com.sgauto.app.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    Optional<Veiculo> findByPlaca(String placa);
    List<Veiculo> findByAtivo(Boolean ativo);
    List<Veiculo> findByClienteId(Long clienteId);
    boolean existsByClienteId(Long clienteId);
}