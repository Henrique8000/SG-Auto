package com.sgauto.app.repository.patio;

import com.sgauto.app.enums.StatusEstadiaPatio;
import com.sgauto.app.model.patio.EstadiaPatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstadiaPatioRepository extends JpaRepository<EstadiaPatio, Long>, JpaSpecificationExecutor<EstadiaPatio> {

    List<EstadiaPatio> findByStatus(StatusEstadiaPatio status);

    Optional<EstadiaPatio> findByVeiculoIdAndStatus(Long veiculoId, StatusEstadiaPatio status);

    Optional<EstadiaPatio> findByOrdemServicoIdAndStatus(Long ordemServicoId, StatusEstadiaPatio status);

    boolean existsByVeiculoIdAndStatus(Long veiculoId, StatusEstadiaPatio status);

    List<EstadiaPatio> findByClienteId(Long clienteId);

    List<EstadiaPatio> findByOrdemServicoIdOrderByDataEntradaDesc(Long ordemServicoId);

    boolean existsByTarifaId(Long tarifaId);

    boolean existsByMotivoId(Long motivoId);

    List<EstadiaPatio> findByStatusOrderByDataEntradaAsc(StatusEstadiaPatio status);

    long countByStatus(StatusEstadiaPatio status);

    // Relatório diário: estadias com entrada ou saída no intervalo [inicio, fim), ou no pátio na referência
    @Query("""
            SELECT e FROM EstadiaPatio e
            JOIN FETCH e.cliente
            JOIN FETCH e.motivo
            WHERE (e.dataEntrada >= :inicio AND e.dataEntrada < :fim)
               OR (e.dataSaida >= :inicio AND e.dataSaida < :fim)
               OR (e.dataEntrada < :referencia AND (e.dataSaida IS NULL OR e.dataSaida >= :referencia))
            ORDER BY e.dataEntrada
            """)
    List<EstadiaPatio> buscarParaRelatorio(@Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim,
                                           @Param("referencia") LocalDateTime referencia);
}