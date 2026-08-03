package com.sgauto.app.repository.OrdemServico;

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
}
