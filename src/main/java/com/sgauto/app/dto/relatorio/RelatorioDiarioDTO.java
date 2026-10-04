package com.sgauto.app.dto.relatorio;

import com.sgauto.app.dto.dashboard.PecaEstoqueCriticoDTO;
import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.enums.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Relatório de um dia de calendário.
 *
 * - Seções "do dia" (financeiro, movimentações, fechamentos, produção, mecânicos, entradas/saídas do pátio)
 *   consideram só o que aconteceu entre 00:00 e 23:59 da data.
 * - Seções de "posição" (pendências e veículos no pátio) mostram a situação no instante {@code referencia}:
 *   o fim do dia, para datas passadas, ou o momento da geração, para hoje.
 * - Estoque crítico é sempre a posição atual (o sistema não guarda histórico de estoque).
 */
public record RelatorioDiarioDTO(
        LocalDate data,
        LocalDateTime geradoEm,
        LocalDateTime referencia,
        Financeiro financeiro,
        List<Movimentacao> movimentacoes,
        List<Fechamento> fechamentos,
        Producao producao,
        Pendencias pendencias,
        List<Mecanico> mecanicos,
        Patio patio,
        List<PecaEstoqueCriticoDTO> estoqueCritico
) {

    /** Regime de caixa: o que entrou e saiu do caixa no dia. Recebido = entradas, exceto suprimento. */
    public record Financeiro(
            BigDecimal totalRecebido,
            BigDecimal recebidoOs,
            BigDecimal recebidoPatio,
            BigDecimal recebidoAvulso,
            BigDecimal dinheiro,
            BigDecimal debito,
            BigDecimal credito,
            BigDecimal pix,
            BigDecimal outros,
            BigDecimal despesas,
            BigDecimal resultado,
            BigDecimal suprimentos,
            BigDecimal sangrias
    ) {}

    public record Movimentacao(
            LocalDateTime data,
            TipoMovimentacao tipo,
            OrigemMovimentacao origem,
            FormaPagamento formaPagamento,
            BigDecimal valor,
            String descricao
    ) {}

    public record Fechamento(
            Long caixaId,
            LocalDateTime abertura,
            LocalDateTime fechamento,
            String usuario,
            BigDecimal esperado,
            BigDecimal contado,
            BigDecimal diferenca,
            String justificativa
    ) {}

    /** Status é o status ATUAL da O.S. (o sistema não guarda histórico de status). */
    public record OsResumo(
            Long id,
            String cliente,
            String placa,
            String mecanico,
            StatusOS status,
            BigDecimal valorTotal
    ) {}

    /** Regime de competência: valores das O.S. concluídas no dia, pagas ou não. */
    public record Producao(
            List<OsResumo> abertas,
            List<OsResumo> concluidas,
            List<OsResumo> finalizadas,
            List<OsResumo> canceladas,
            BigDecimal valorProduzido,
            BigDecimal descontos,
            BigDecimal custoPecas,
            BigDecimal margemBruta,
            BigDecimal ticketMedio
    ) {}

    /** status só é preenchido quando o relatório é de hoje; para datas passadas vem null. */
    public record OsPendente(
            Long id,
            String cliente,
            String placa,
            String mecanico,
            StatusOS status,
            LocalDateTime previsao,
            BigDecimal valorTotal,
            BigDecimal saldo
    ) {}

    public record Pendencias(
            List<OsPendente> emAndamento,
            List<OsPendente> atrasadas,
            List<OsPendente> aReceber,
            BigDecimal totalAReceber
    ) {}

    /** Considera as O.S. concluídas no dia. Sem comissão até a regra ser definida. */
    public record Mecanico(
            String nome,
            long quantidadeOs,
            BigDecimal valorServicos,
            BigDecimal valorPecas,
            BigDecimal valorTotal
    ) {}

    public record EstadiaResumo(
            String placa,
            String cliente,
            String motivo,
            LocalDateTime entrada,
            LocalDateTime saida,
            long dias,
            BigDecimal valor
    ) {}

    public record Patio(
            List<EstadiaResumo> entradas,
            List<EstadiaResumo> saidas,
            List<EstadiaResumo> noPatio
    ) {}
}