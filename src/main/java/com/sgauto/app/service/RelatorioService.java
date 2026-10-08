package com.sgauto.app.service;

import com.sgauto.app.dto.relatorio.RelatorioDiarioDTO;
import com.sgauto.app.dto.relatorio.RelatorioDiarioDTO.*;
import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.enums.financeiro.OrigemMovimentacao;
import com.sgauto.app.enums.usuario.PermissaoChave;
import com.sgauto.app.enums.financeiro.TipoMovimentacao;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.model.OrdemServico.OsPeca;
import com.sgauto.app.model.caixa.Caixa;
import com.sgauto.app.model.caixa.CaixaMovimentacao;
import com.sgauto.app.model.patio.EstadiaPatio;
import com.sgauto.app.repository.OrdemServico.OrdemServicoRepository;
import com.sgauto.app.repository.OrdemServico.OsPagamentoRepository;
import com.sgauto.app.repository.caixa.CaixaMovimentacaoRepository;
import com.sgauto.app.repository.caixa.CaixaRepository;
import com.sgauto.app.repository.estoque.PecaRepository;
import com.sgauto.app.repository.patio.EstadiaPatioRepository;
import com.sgauto.app.util.VerificaPermissaoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@Transactional(readOnly = true)
public class RelatorioService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RelatorioService.class);

    private final CaixaMovimentacaoRepository caixaMovimentacaoRepository;
    private final CaixaRepository caixaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OsPagamentoRepository osPagamentoRepository;
    private final EstadiaPatioRepository estadiaPatioRepository;
    private final PecaRepository pecaRepository;
    private final VerificaPermissaoUtil permissaoUtil;

    public RelatorioService(CaixaMovimentacaoRepository caixaMovimentacaoRepository,
                            CaixaRepository caixaRepository,
                            OrdemServicoRepository ordemServicoRepository,
                            OsPagamentoRepository osPagamentoRepository,
                            EstadiaPatioRepository estadiaPatioRepository,
                            PecaRepository pecaRepository,
                            VerificaPermissaoUtil permissaoUtil) {
        this.caixaMovimentacaoRepository = caixaMovimentacaoRepository;
        this.caixaRepository = caixaRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.osPagamentoRepository = osPagamentoRepository;
        this.estadiaPatioRepository = estadiaPatioRepository;
        this.pecaRepository = pecaRepository;
        this.permissaoUtil = permissaoUtil;
    }

    public RelatorioDiarioDTO montarRelatorioDiario(LocalDate data) {
        if (!permissaoUtil.verificar(PermissaoChave.RELATORIO_DIARIO_VISUALIZAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para visualizar o relatório diário.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Informe a data do relatório.");
        }

        LocalDate hoje = LocalDate.now();
        if (data.isAfter(hoje)) {
            throw new IllegalArgumentException("Não é possível gerar o relatório de uma data futura.");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay(); // exclusivo
        // Instante da "posição": o que aconteceu ANTES dele já conta como ocorrido
        boolean ehHoje = data.equals(hoje);
        LocalDateTime referencia = ehHoje ? agora : fim;

        log.debug("Montando relatório diário de {} (referência {})", data, referencia);

        List<CaixaMovimentacao> movimentacoes = caixaMovimentacaoRepository.buscarPorPeriodo(inicio, fim);
        List<OrdemServico> osDoDia = ordemServicoRepository.buscarComEventoNoPeriodo(inicio, fim);
        List<OrdemServico> osEmAberto = ordemServicoRepository.buscarEmAbertoNaReferencia(referencia);
        List<EstadiaPatio> estadias = estadiaPatioRepository.buscarParaRelatorio(inicio, fim, referencia);

        List<OrdemServico> concluidas = filtrar(osDoDia, os -> estaNoDia(os.getDataConclusao(), inicio, fim));

        return new RelatorioDiarioDTO(
                data,
                agora,
                referencia,
                montarFinanceiro(movimentacoes),
                montarMovimentacoes(movimentacoes),
                montarFechamentos(inicio, fim),
                montarProducao(osDoDia, concluidas, inicio, fim),
                montarPendencias(osEmAberto, referencia, ehHoje),
                montarMecanicos(concluidas),
                montarPatio(estadias, inicio, fim, referencia),
                pecaRepository.buscarAbaixoDoEstoqueMinimo()
        );
    }

    // ===== Financeiro (regime de caixa) =====

    private Financeiro montarFinanceiro(List<CaixaMovimentacao> movs) {
        // Recebimento = entrada que não seja suprimento (troco colocado na gaveta não é receita)
        Predicate<CaixaMovimentacao> recebimento =
                m -> m.getTipo() == TipoMovimentacao.ENTRADA && m.getOrigem() != OrigemMovimentacao.SUPRIMENTO;

        BigDecimal totalRecebido = somar(movs, recebimento);
        BigDecimal despesas = somar(movs, m -> m.getTipo() == TipoMovimentacao.SAIDA && m.getOrigem() == OrigemMovimentacao.AVULSO);
        BigDecimal contasPagas = somar(movs, m -> m.getTipo() == TipoMovimentacao.SAIDA && m.getOrigem() == OrigemMovimentacao.CONTA_PAGAR);

        return new Financeiro(
                totalRecebido,
                somar(movs, recebimento.and(m -> m.getOrigem() == OrigemMovimentacao.OS_PAGAMENTO)),
                somar(movs, recebimento.and(m -> m.getOrigem() == OrigemMovimentacao.PATIO)),
                somar(movs, recebimento.and(m -> m.getOrigem() == OrigemMovimentacao.AVULSO)),
                somar(movs, recebimento.and(m -> m.getOrigem() == OrigemMovimentacao.CONTA_RECEBER)),
                somar(movs, recebimento.and(m -> m.getFormaPagamento() == FormaPagamento.DINHEIRO)),
                somar(movs, recebimento.and(m -> m.getFormaPagamento() == FormaPagamento.DEBITO)),
                somar(movs, recebimento.and(m -> m.getFormaPagamento() == FormaPagamento.CREDITO)),
                somar(movs, recebimento.and(m -> m.getFormaPagamento() == FormaPagamento.PIX)),
                somar(movs, recebimento.and(m -> m.getFormaPagamento() == FormaPagamento.OUTROS)),
                despesas,
                contasPagas,
                totalRecebido.subtract(despesas).subtract(contasPagas),
                somar(movs, m -> m.getTipo() == TipoMovimentacao.ENTRADA && m.getOrigem() == OrigemMovimentacao.SUPRIMENTO),
                somar(movs, m -> m.getTipo() == TipoMovimentacao.SAIDA && m.getOrigem() == OrigemMovimentacao.SANGRIA)
        );
    }

    // Saídas isentas do pátio geram entradas de R$ 0,00: não interessam ao gestor
    private List<Movimentacao> montarMovimentacoes(List<CaixaMovimentacao> movs) {
        return movs.stream()
                .filter(m -> m.getFormaPagamento() != FormaPagamento.ISENTO)
                .map(m -> new Movimentacao(m.getData(), m.getTipo(), m.getOrigem(),
                        m.getFormaPagamento(), m.getValor(), m.getDescricao()))
                .toList();
    }

    private List<Fechamento> montarFechamentos(LocalDateTime inicio, LocalDateTime fim) {
        return caixaRepository.buscarFechadosNoPeriodo(inicio, fim).stream()
                .map(this::paraFechamento)
                .toList();
    }

    private Fechamento paraFechamento(Caixa c) {
        return new Fechamento(c.getId(), c.getDataAbertura(), c.getDataFechamento(), c.getUsuarioFechamento(),
                c.getValorEsperado(), c.getValorContado(), c.getDiferenca(), c.getJustificativaDiferenca());
    }

    // ===== Produção (regime de competência) =====

    private Producao montarProducao(List<OrdemServico> osDoDia, List<OrdemServico> concluidas,
                                    LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal valorProduzido = somarOs(concluidas, OrdemServico::getValorTotalOs);
        BigDecimal descontos = somarOs(concluidas, OrdemServico::getValorDesconto);
        BigDecimal custoPecas = somarOs(concluidas, this::custoPecas);
        BigDecimal ticketMedio = concluidas.isEmpty()
                ? BigDecimal.ZERO
                : valorProduzido.divide(BigDecimal.valueOf(concluidas.size()), 2, RoundingMode.HALF_UP);

        return new Producao(
                paraResumo(filtrar(osDoDia, os -> estaNoDia(os.getDataAbertura(), inicio, fim))),
                paraResumo(concluidas),
                paraResumo(filtrar(osDoDia, os -> estaNoDia(os.getDataFinalizacao(), inicio, fim))),
                paraResumo(filtrar(osDoDia, os -> estaNoDia(os.getDataCancelamento(), inicio, fim))),
                valorProduzido,
                descontos,
                custoPecas,
                valorProduzido.subtract(custoPecas),
                ticketMedio
        );
    }

    // Usa o custo congelado na O.S. (valor_custo_unitario), não o custo atual da peça
    private BigDecimal custoPecas(OrdemServico os) {
        BigDecimal total = BigDecimal.ZERO;
        for (OsPeca p : os.getPecas()) {
            total = total.add(p.getValorCustoUnitario().multiply(BigDecimal.valueOf(p.getQuantidade())));
        }
        return total;
    }

    private List<OsResumo> paraResumo(List<OrdemServico> lista) {
        return lista.stream()
                .map(os -> new OsResumo(os.getId(), os.getCliente().getNome(), os.getVeiculo().getPlaca(),
                        os.getFuncionario().getNomeCompleto(), os.getStatus(), os.getValorTotalOs()))
                .toList();
    }

    // ===== Pendências (posição na referência) =====

    private Pendencias montarPendencias(List<OrdemServico> osEmAberto, LocalDateTime referencia, boolean ehHoje) {
        List<OsPendente> emAndamento = new ArrayList<>();
        List<OsPendente> atrasadas = new ArrayList<>();
        List<OsPendente> aReceber = new ArrayList<>();
        BigDecimal totalAReceber = BigDecimal.ZERO;

        for (OrdemServico os : osEmAberto) {
            boolean concluida = os.getDataConclusao() != null && os.getDataConclusao().isBefore(referencia);

            if (concluida) {
                BigDecimal pago = osPagamentoRepository.somarPagamentosPorOsIdAte(os.getId(), referencia);
                BigDecimal saldo = os.getValorTotalOs().subtract(pago);
                if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                    aReceber.add(paraPendente(os, saldo, ehHoje));
                    totalAReceber = totalAReceber.add(saldo);
                }
            } else {
                OsPendente pendente = paraPendente(os, null, ehHoje);
                emAndamento.add(pendente);
                if (os.getDataPrevisao() != null && os.getDataPrevisao().isBefore(referencia)) {
                    atrasadas.add(pendente);
                }
            }
        }

        return new Pendencias(emAndamento, atrasadas, aReceber, totalAReceber);
    }

    // Para datas passadas o status atual da O.S. não representa aquele dia, então vai null
    private OsPendente paraPendente(OrdemServico os, BigDecimal saldo, boolean ehHoje) {
        return new OsPendente(os.getId(), os.getCliente().getNome(), os.getVeiculo().getPlaca(),
                os.getFuncionario().getNomeCompleto(), ehHoje ? os.getStatus() : null,
                os.getDataPrevisao(), os.getValorTotalOs(), saldo);
    }

    // ===== Mecânicos =====

    private List<Mecanico> montarMecanicos(List<OrdemServico> concluidas) {
        Map<Long, List<OrdemServico>> porMecanico = new LinkedHashMap<>();
        for (OrdemServico os : concluidas) {
            porMecanico.computeIfAbsent(os.getFuncionario().getId(), id -> new ArrayList<>()).add(os);
        }

        return porMecanico.values().stream()
                .map(lista -> new Mecanico(
                        lista.get(0).getFuncionario().getNomeCompleto(),
                        lista.size(),
                        somarOs(lista, OrdemServico::getValorTotalServicos),
                        somarOs(lista, OrdemServico::getValorTotalPecas),
                        somarOs(lista, OrdemServico::getValorTotalOs)))
                .toList();
    }

    // ===== Pátio =====

    private Patio montarPatio(List<EstadiaPatio> estadias, LocalDateTime inicio, LocalDateTime fim,
                              LocalDateTime referencia) {
        List<EstadiaResumo> entradas = new ArrayList<>();
        List<EstadiaResumo> saidas = new ArrayList<>();
        List<EstadiaResumo> noPatio = new ArrayList<>();

        for (EstadiaPatio e : estadias) {
            if (estaNoDia(e.getDataEntrada(), inicio, fim)) {
                entradas.add(paraEstadia(e, referencia));
            }
            if (estaNoDia(e.getDataSaida(), inicio, fim)) {
                saidas.add(paraEstadia(e, referencia));
            }
            boolean estavaNoPatio = e.getDataEntrada().isBefore(referencia)
                    && (e.getDataSaida() == null || !e.getDataSaida().isBefore(referencia));
            if (estavaNoPatio) {
                noPatio.add(paraEstadia(e, referencia));
            }
        }

        return new Patio(entradas, saidas, noPatio);
    }

    private EstadiaResumo paraEstadia(EstadiaPatio e, LocalDateTime referencia) {
        LocalDateTime ate = e.getDataSaida() != null && e.getDataSaida().isBefore(referencia)
                ? e.getDataSaida()
                : referencia;
        long dias = ChronoUnit.DAYS.between(e.getDataEntrada().toLocalDate(), ate.toLocalDate());

        return new EstadiaResumo(e.getPlaca(), e.getCliente().getNome(), e.getMotivo().getNome(),
                e.getDataEntrada(), e.getDataSaida(), dias, e.getValorTotal());
    }

    // ===== Utilitários =====

    private boolean estaNoDia(LocalDateTime instante, LocalDateTime inicio, LocalDateTime fim) {
        return instante != null && !instante.isBefore(inicio) && instante.isBefore(fim);
    }

    private <T> List<T> filtrar(List<T> lista, Predicate<T> criterio) {
        return lista.stream().filter(criterio).toList();
    }

    private BigDecimal somar(List<CaixaMovimentacao> movs, Predicate<CaixaMovimentacao> criterio) {
        return movs.stream().filter(criterio).map(CaixaMovimentacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somarOs(List<OrdemServico> lista, Function<OrdemServico, BigDecimal> valor) {
        return lista.stream().map(valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}