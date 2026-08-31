package com.sgauto.app.service;

import com.sgauto.app.dto.dashboard.*;
import com.sgauto.app.enums.PeriodoDashboard;
import com.sgauto.app.enums.StatusEstadiaPatio;
import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.model.patio.EstadiaPatio;
import com.sgauto.app.repository.CaixaMovimentacaoRepository;
import com.sgauto.app.repository.OrdemServico.OrdemServicoRepository;
import com.sgauto.app.repository.OrdemServico.OsServicoRepository;
import com.sgauto.app.repository.estoque.PecaRepository;
import com.sgauto.app.repository.patio.EstadiaPatioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<StatusOS> STATUS_ENCERRADOS = List.of(
            StatusOS.FINALIZADA,
            StatusOS.CANCELADA
    );

    private final OrdemServicoRepository ordemServicoRepository;
    private final OsServicoRepository osServicoRepository;
    private final CaixaMovimentacaoRepository caixaMovimentacaoRepository;
    private final PecaRepository pecaRepository;
    private final EstadiaPatioRepository estadiaPatioRepository;

    public DashboardService(OrdemServicoRepository ordemServicoRepository,
                            OsServicoRepository osServicoRepository,
                            CaixaMovimentacaoRepository caixaMovimentacaoRepository,
                            PecaRepository pecaRepository,
                            EstadiaPatioRepository estadiaPatioRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.osServicoRepository = osServicoRepository;
        this.caixaMovimentacaoRepository = caixaMovimentacaoRepository;
        this.pecaRepository = pecaRepository;
        this.estadiaPatioRepository = estadiaPatioRepository;
    }

    // ------------------------------------------------------------------
    // KPIs do topo
    // ------------------------------------------------------------------

    public DashboardResumoDTO montarResumo() {
        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime fimHoje = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal faturamentoCaixaDia = caixaMovimentacaoRepository.somarEntradasPorPeriodo(inicioHoje, fimHoje);
        BigDecimal faturamentoCaixaMes = caixaMovimentacaoRepository.somarEntradasPorPeriodo(inicioMes, fimMes);
        BigDecimal faturamentoOsFinalizadasMes =
                ordemServicoRepository.somarFaturamentoPorPeriodo(StatusOS.FINALIZADA, inicioMes, fimMes);

        long osAbertas = ordemServicoRepository.contarOsAbertas(STATUS_ENCERRADOS);
        long osAguardando = ordemServicoRepository.countByStatus(StatusOS.AGUARDANDO);

        BigDecimal ticketMedio = calcularTicketMedio(inicioMes, fimMes);

        long pecasCriticas = pecaRepository.contarPecasEstoqueCritico();
        long veiculosPatio = estadiaPatioRepository.countByStatus(StatusEstadiaPatio.NO_PATIO);

        return new DashboardResumoDTO(
                nvl(faturamentoCaixaDia),
                nvl(faturamentoCaixaMes),
                nvl(faturamentoOsFinalizadasMes),
                osAbertas,
                osAguardando,
                ticketMedio,
                pecasCriticas,
                veiculosPatio
        );
    }

    private BigDecimal calcularTicketMedio(LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal total = ordemServicoRepository.somarFaturamentoPorPeriodo(StatusOS.FINALIZADA, inicio, fim);
        Long quantidade = ordemServicoRepository.countByStatusAndDataConclusaoBetween(StatusOS.FINALIZADA, inicio, fim);

        if (total == null || quantidade == null || quantidade == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------
    // Gráfico: OS por status
    // ------------------------------------------------------------------

    public List<OsPorStatusDTO> osPorStatus() {
        return ordemServicoRepository.contarPorStatus();
    }

    // ------------------------------------------------------------------
    // Gráfico: faturamento diário (fonte: Caixa) no período selecionado
    // ------------------------------------------------------------------

    public List<FaturamentoDiarioDTO> faturamentoNoPeriodo(PeriodoDashboard periodo) {
        return caixaMovimentacaoRepository.faturamentoDiarioPorPeriodo(periodo.getInicio(), periodo.getFim());
    }

    // ------------------------------------------------------------------
    // Gráfico: comissão por funcionário no período selecionado
    // ------------------------------------------------------------------

    public List<ComissaoFuncionarioDTO> comissaoPorFuncionario(PeriodoDashboard periodo) {
        return osServicoRepository.comissaoPorFuncionario(periodo.getInicio(), periodo.getFim());
    }

    // ------------------------------------------------------------------
    // Alertas: estoque crítico
    // ------------------------------------------------------------------

    public List<PecaEstoqueCriticoDTO> pecasEstoqueCritico() {
        return pecaRepository.buscarAbaixoDoEstoqueMinimo();
    }

    // ------------------------------------------------------------------
    // Alertas: veículos no pátio
    // ------------------------------------------------------------------

    public List<VeiculoPatioDTO> veiculosNoPatio() {
        LocalDateTime agora = LocalDateTime.now();
        return estadiaPatioRepository.findByStatusOrderByDataEntradaAsc(StatusEstadiaPatio.NO_PATIO).stream()
                .map(estadia -> mapearVeiculoPatio(estadia, agora))
                .toList();
    }

    private VeiculoPatioDTO mapearVeiculoPatio(EstadiaPatio estadia, LocalDateTime agora) {
        long dias = Duration.between(estadia.getDataEntrada(), agora).toDays();
        return new VeiculoPatioDTO(
                estadia.getId(),
                estadia.getVeiculo().getPlaca(),
                estadia.getCliente().getNome(),
                dias
        );
    }

    // ------------------------------------------------------------------
    private static BigDecimal nvl(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}