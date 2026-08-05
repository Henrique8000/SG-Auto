package com.sgauto.app.repository.patio;

import com.sgauto.app.enums.StatusEstadiaPatio;
import com.sgauto.app.model.patio.EstadiaPatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadiaPatioRepository extends JpaRepository<EstadiaPatio, Long> {
    List<EstadiaPatio> findByStatus(StatusEstadiaPatio status);
    Optional<EstadiaPatio> findByVeiculoIdAndStatus(Long veiculoId, StatusEstadiaPatio status);
    boolean existsByVeiculoIdAndStatus(Long veiculoId, StatusEstadiaPatio status);
    List<EstadiaPatio> findByClienteId(Long clienteId);
    Optional<EstadiaPatio> findByOrdemServicoIdAndStatus(Long ordemServicoId, StatusEstadiaPatio status);
    List<EstadiaPatio> findByOrdemServicoIdOrderByDataEntradaDesc(Long ordemServicoId);
}
