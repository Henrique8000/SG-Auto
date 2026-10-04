package com.sgauto.app.repository.OrdemServico;

import com.sgauto.app.model.OrdemServico.OsPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OsPagamentoRepository extends JpaRepository<OsPagamento, Long> {
    @Query("SELECT COALESCE(SUM(p.valorPago), 0) FROM OsPagamento p WHERE p.ordemServico.id = :osId")
    BigDecimal somarPagamentosPorOsId(@Param("osId") Long osId);

    List<OsPagamento> findByOrdemServicoIdOrderByDataPagamentoDesc(Long osId);

    // Relatório diário: quanto a O.S. tinha recebido antes da referência
    @Query("SELECT COALESCE(SUM(p.valorPago), 0) FROM OsPagamento p WHERE p.ordemServico.id = :osId AND p.dataPagamento < :referencia")
    BigDecimal somarPagamentosPorOsIdAte(@Param("osId") Long osId, @Param("referencia") LocalDateTime referencia);
}
