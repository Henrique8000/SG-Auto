package com.sgauto.app.service;

import com.sgauto.app.enums.*;
import com.sgauto.app.model.caixa.Caixa;
import com.sgauto.app.model.caixa.CaixaMovimentacao;
import com.sgauto.app.model.caixa.ConfiguracaoCaixa;
import com.sgauto.app.repository.caixa.CaixaMovimentacaoRepository;
import com.sgauto.app.repository.caixa.CaixaRepository;
import com.sgauto.app.service.backup.BackupService;
import com.sgauto.app.util.VerificaPermissaoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final CaixaMovimentacaoRepository caixaMovimentacaoRepository;
    private final ConfiguracaoCaixaService configuracaoCaixaService;
    private final ConfigSistemaService configSistemaService;
    private final BackupService backupService;
    private final VerificaPermissaoUtil permissaoUtil;

    public CaixaService(CaixaRepository caixaRepository,
                        CaixaMovimentacaoRepository caixaMovimentacaoRepository,
                        ConfiguracaoCaixaService configuracaoCaixaService, ConfigSistemaService configSistemaService, BackupService backupService, VerificaPermissaoUtil permissaoUtil) {
        this.caixaRepository = caixaRepository;
        this.caixaMovimentacaoRepository = caixaMovimentacaoRepository;
        this.configuracaoCaixaService = configuracaoCaixaService;
        this.configSistemaService = configSistemaService;
        this.backupService = backupService;
        this.permissaoUtil = permissaoUtil;
    }

    public void garantirCaixaAberto() {
        caixaRepository.findByStatus(StatusCaixa.ABERTO).orElseGet((this::abrirNovoCaixa));
    }

    private Caixa abrirNovoCaixa() {
        Caixa caixa = new Caixa("Sistema", BigDecimal.ZERO);
        return caixaRepository.save(caixa);
    }


    @Transactional(readOnly = true)
    public Caixa buscarCaixaAberto() {
        Caixa caixa = caixaRepository.findByStatus(StatusCaixa.ABERTO).orElseThrow(() -> new IllegalStateException("Não foi localizado nenhum caixa aberto. Verificar com suporte do sistema"));
        return caixa;
    }

    @Transactional
    public CaixaMovimentacao registrarMovimentacao(TipoMovimentacao tipo, OrigemMovimentacao origem,
                                                   FormaPagamento formaPagamento, BigDecimal valor,
                                                   String descricao, Long clienteId, String placa) {
        if(!permissaoUtil.verificar(PermissaoChave.CAIXA_MOVIMENTAR)){
            throw new IllegalStateException("Seu usuário não possui permissão movimentar no caixa.");
        }

        Caixa caixaAberto = buscarCaixaAberto();

        CaixaMovimentacao mov = new CaixaMovimentacao(caixaAberto, tipo, origem, formaPagamento, valor, descricao);
        mov.setClienteId(clienteId);
        mov.setPlaca(placa);
        return caixaMovimentacaoRepository.save(mov);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularValorEsperado(Long caixaId) {
        Caixa caixa = caixaRepository.findById(caixaId)
                .orElseThrow(() -> new IllegalArgumentException("Caixa não encontrado: " + caixaId));
        List<CaixaMovimentacao> movimentacoes = caixaMovimentacaoRepository.findByCaixaId(caixa.getId());

        BigDecimal entradasDinheiro = movimentacoes.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.ENTRADA && m.getFormaPagamento() == FormaPagamento.DINHEIRO)
                .map(CaixaMovimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saidasDinheiro = movimentacoes.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.SAIDA && m.getFormaPagamento() == FormaPagamento.DINHEIRO)
                .map(CaixaMovimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return caixa.getValorAbertura().add(entradasDinheiro).subtract(saidasDinheiro);
    }
    @Transactional(readOnly = true)
    public BigDecimal calcularValorBruto(Long caixaId) {
        List<CaixaMovimentacao> movimentacoes = caixaMovimentacaoRepository.findByCaixaId(caixaId);

        return movimentacoes.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.ENTRADA)
                .map(CaixaMovimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<Caixa> listarHistorico() {
        return caixaRepository.findAllByStatus(StatusCaixa.FECHADO);
    }

    @Transactional(readOnly = true)
    public List<CaixaMovimentacao> listarMovimentacoes(Long caixaId) {
        return caixaMovimentacaoRepository.findByCaixaId(caixaId);
    }

    @Transactional
    public Long retornaIdCaixaAtual(){
        Caixa caixa = buscarCaixaAberto();
        Long id = caixa.getId();
        return id;
    }

    @Transactional
    public Caixa fecharCaixaAtual(BigDecimal valorContado, String justificativaDiferenca) {
        if(!permissaoUtil.verificar(PermissaoChave.CAIXA_FECHAR)){
            throw new IllegalStateException("Seu usuário não possui permissão fechar o caixa.");
        }

        ConfiguracaoCaixa config = configuracaoCaixaService.buscarConfiguracao();
        Caixa caixa = buscarCaixaAberto();

        BigDecimal valorEsperado = calcularValorEsperado(caixa.getId());
        BigDecimal valorContadoFinal;
        BigDecimal diferenca;

        switch (config.getModoConferencia()) {
            case SEM_CONFERENCIA -> {
                valorContadoFinal = valorEsperado;
                diferenca = BigDecimal.ZERO;
            }
            case OBRIGATORIA -> {
                if (valorContado == null) {
                    throw new IllegalArgumentException("Informe o valor contado para fechar o caixa.");
                }
                valorContadoFinal = valorContado;
                diferenca = calcularDiferenca(valorEsperado, valorContadoFinal);
                validarJustificativaSeNecessario(diferenca, justificativaDiferenca);
            }
            case OPCIONAL -> {
                valorContadoFinal = valorContado;
                if (valorContadoFinal != null) {
                    diferenca = calcularDiferenca(valorEsperado, valorContadoFinal);
                    validarJustificativaSeNecessario(diferenca, justificativaDiferenca);
                } else {
                    diferenca = null;
                }
            }
            default -> throw new IllegalStateException("Modo de conferência não suportado.");
        }

        preencherTotais(caixa);

        caixa.setValorEsperado(valorEsperado);
        caixa.setValorContado(valorContadoFinal);
        caixa.setDiferenca(diferenca);
        caixa.setModoConferenciaUsado(config.getModoConferencia());
        caixa.setJustificativaDiferenca(justificativaDiferenca);
        caixa.setUsuarioFechamento("Sistema"); // trocar quando existir usuário logado
        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setStatus(StatusCaixa.FECHADO);

        Caixa caixaFechado = caixaRepository.save(caixa);

        abrirNovoCaixa();

        if (configSistemaService.isBackupAposFechamentoCaixaAtivo()) {
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                backupService.executarBackupPadrao(TipoBackup.CAIXA);
            });
        }

        return caixaFechado;
    }

    private BigDecimal calcularDiferenca(BigDecimal esperado, BigDecimal contado) {
        return contado.subtract(esperado);
    }

    private void validarJustificativaSeNecessario(BigDecimal diferenca, String justificativa) {
        if (diferenca.compareTo(BigDecimal.ZERO) != 0
                && (justificativa == null || justificativa.isBlank())) {
            throw new IllegalArgumentException("Há uma diferença de caixa. Informe uma justificativa para fechar.");
        }
    }

    private void preencherTotais(Caixa caixa) {
        List<CaixaMovimentacao> movimentacoes = caixaMovimentacaoRepository.findByCaixaId(caixa.getId());

        caixa.setTotalEntradas(somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.ENTRADA));
        caixa.setTotalSaidas(somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.SAIDA));

        // Categorias. Fecham a conta com os totais acima:
        //   entradas = O.S. + pátio + avulso + suprimento
        //   saídas   = sangria + despesas
        caixa.setTotalOs(somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.ENTRADA && m.getOrigem() == OrigemMovimentacao.OS_PAGAMENTO));
        caixa.setTotalPatio(somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.ENTRADA && m.getOrigem() == OrigemMovimentacao.PATIO));
        caixa.setTotalAvulso(somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.ENTRADA && m.getOrigem() == OrigemMovimentacao.AVULSO));
        caixa.setTotalSuprimento(somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.ENTRADA && m.getOrigem() == OrigemMovimentacao.SUPRIMENTO));
        caixa.setTotalSangria(somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.SAIDA && m.getOrigem() == OrigemMovimentacao.SANGRIA));
        caixa.setTotalDespesas(somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.SAIDA && m.getOrigem() == OrigemMovimentacao.AVULSO));

        // Formas de pagamento = recebimentos. O saldo físico da gaveta é o valorEsperado.
        caixa.setTotalDinheiro(somarRecebimentosPor(movimentacoes, FormaPagamento.DINHEIRO));
        caixa.setTotalDebito(somarRecebimentosPor(movimentacoes, FormaPagamento.DEBITO));
        caixa.setTotalCredito(somarRecebimentosPor(movimentacoes, FormaPagamento.CREDITO));
        caixa.setTotalPix(somarRecebimentosPor(movimentacoes, FormaPagamento.PIX));
    }

    // Recebimento = entrada que não seja suprimento (suprimento é troco colocado na gaveta, não receita)
    private BigDecimal somarRecebimentosPor(List<CaixaMovimentacao> movimentacoes, FormaPagamento forma) {
        return somarPor(movimentacoes, m -> m.getTipo() == TipoMovimentacao.ENTRADA
                && m.getOrigem() != OrigemMovimentacao.SUPRIMENTO
                && m.getFormaPagamento() == forma);
    }

    private BigDecimal somarPor(List<CaixaMovimentacao> movimentacoes, java.util.function.Predicate<CaixaMovimentacao> filtro) {
        return movimentacoes.stream()
                .filter(filtro)
                .map(CaixaMovimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}