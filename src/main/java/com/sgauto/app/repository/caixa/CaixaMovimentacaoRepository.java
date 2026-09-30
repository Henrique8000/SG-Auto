package com.sgauto.app.repository.caixa;

import com.sgauto.app.dto.dashboard.FaturamentoDiarioDTO;
import com.sgauto.app.dto.dashboard.FaturamentoPorFormaPagamentoDTO;
import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.enums.TipoMovimentacao;
import com.sgauto.app.model.caixa.CaixaMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CaixaMovimentacaoRepository extends JpaRepository<CaixaMovimentacao, Long> {
    List<CaixaMovimentacao> findByCaixaId(Long caixaId);
    List<CaixaMovimentacao> findByCaixaIdAndTipo(Long caixaId, TipoMovimentacao tipo);
    List<CaixaMovimentacao> findByCaixaIdAndOrigem(Long caixaId, OrigemMovimentacao origem);

    @Query("""
        SELECT COALESCE(SUM(m.valor), 0)
        FROM CaixaMovimentacao m
        WHERE m.tipo = com.sgauto.app.enums.TipoMovimentacao.ENTRADA
        AND m.origem <> com.sgauto.app.enums.OrigemMovimentacao.SUPRIMENTO
        AND m.data BETWEEN :inicio AND :fim
        """)
    BigDecimal somarEntradasPorPeriodo(@Param("inicio") LocalDateTime inicio,
                                       @Param("fim") LocalDateTime fim);

    @Query("""
        SELECT new com.sgauto.app.dto.dashboard.FaturamentoDiarioDTO(CAST(m.data AS LocalDate), SUM(m.valor))
        FROM CaixaMovimentacao m
        WHERE m.tipo = com.sgauto.app.enums.TipoMovimentacao.ENTRADA
        AND m.origem <> com.sgauto.app.enums.OrigemMovimentacao.SUPRIMENTO
        AND m.data BETWEEN :inicio AND :fim
        GROUP BY CAST(m.data AS LocalDate)
        ORDER BY CAST(m.data AS LocalDate)
        """)
    List<FaturamentoDiarioDTO> faturamentoDiarioPorPeriodo(@Param("inicio") LocalDateTime inicio,
                                                           @Param("fim") LocalDateTime fim);

    @Query("""
        SELECT new com.sgauto.app.dto.dashboard.FaturamentoPorFormaPagamentoDTO(m.formaPagamento, SUM(m.valor))
        FROM CaixaMovimentacao m
        WHERE m.tipo = com.sgauto.app.enums.TipoMovimentacao.ENTRADA
        AND m.origem <> com.sgauto.app.enums.OrigemMovimentacao.SUPRIMENTO
        AND m.formaPagamento IS NOT NULL
        AND m.data BETWEEN :inicio AND :fim
        GROUP BY m.formaPagamento
        ORDER BY SUM(m.valor) DESC
        """)
    List<FaturamentoPorFormaPagamentoDTO> faturamentoPorFormaPagamento(@Param("inicio") LocalDateTime inicio,
                                                                       @Param("fim") LocalDateTime fim);

    // Relatório diário: intervalo [inicio, fim)
    @Query("SELECT m FROM CaixaMovimentacao m WHERE m.data >= :inicio AND m.data < :fim ORDER BY m.data")
    List<CaixaMovimentacao> buscarPorPeriodo(@Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim);
}