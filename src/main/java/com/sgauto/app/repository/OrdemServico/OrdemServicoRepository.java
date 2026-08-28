package com.sgauto.app.repository.OrdemServico;

import com.sgauto.app.dto.dashboard.OsPorStatusDTO;
import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    boolean existsByVeiculoIdAndStatusNotIn(Long veiculoId, List<StatusOS> status);

    @Query("SELECT os FROM OrdemServico os JOIN FETCH os.cliente JOIN FETCH os.veiculo WHERE os.id = :id")
    Optional<OrdemServico> findByIdComDetalhes(@Param("id") Long id);

    List<OrdemServico> findByStatusIn(List<StatusOS> status);

    List<OrdemServico> findByFicarNoPatioTrueAndStatusNotIn(List<StatusOS> status);

    List<OrdemServico> findByFicarNoPatioTrue();

    List<OrdemServico> findByStatusNotInAndDataPrevisaoBefore(List<StatusOS> statusEncerrados, LocalDateTime dataAtual);

    long countByStatus(StatusOS status);

    @Query("SELECT COALESCE(SUM(os.valorTotalOs), 0) FROM OrdemServico os WHERE os.status = :status AND os.dataConclusao BETWEEN :inicio AND :fim")
    BigDecimal somarFaturamentoPorPeriodo(@Param("status") StatusOS status, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<OrdemServico> findByVeiculoIdOrderByDataAberturaDesc(Long veiculoId);

    Long countByStatusAndDataConclusaoBetween(StatusOS status, LocalDateTime inicio, LocalDateTime fim);

    List<OrdemServico> findByStatus(StatusOS status);

    @Query("SELECT os FROM OrdemServico os " +
            "JOIN FETCH os.cliente " +
            "JOIN FETCH os.veiculo " +
            "ORDER BY os.dataAbertura DESC")
    List<OrdemServico> findAllComClienteEVeiculo();

    @Query("SELECT os FROM OrdemServico os JOIN FETCH os.cliente JOIN FETCH os.veiculo JOIN FETCH os.funcionario WHERE os.id = :id")
    Optional<OrdemServico> findByIdDetalhado(@Param("id") Long id);

    // ___________

    @Query("""
            SELECT new com.sgauto.app.dto.OsPorStatusDTO(os.status, COUNT(os))
            FROM OrdemServico os
            WHERE os.ativo = true
            GROUP BY os.status
            """)
    List<OsPorStatusDTO> contarPorStatus();

    @Query("""
            SELECT COUNT(os)
            FROM OrdemServico os
            WHERE os.ativo = true
            AND os.status NOT IN (:statusEncerrados)
            """)
    long contarOsAbertas(@Param("statusEncerrados") List<StatusOS> statusEncerrados);

    @Query("""
            SELECT COUNT(os)
            FROM OrdemServico os
            WHERE os.ativo = true
            AND os.status = :status
            """)
    long contarPorStatusUnico(@Param("status") StatusOS status);

    @Query("""
            SELECT AVG(os.valorTotal)
            FROM OrdemServico os
            WHERE os.status = com.sgauto.app.enums.StatusOS.FINALIZADA
            AND os.dataFinalizacao BETWEEN :inicio AND :fim
            """)
    BigDecimal calcularTicketMedio(@Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim);
}

