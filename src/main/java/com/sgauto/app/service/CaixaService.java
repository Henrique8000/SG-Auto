package com.sgauto.app.service;

import com.sgauto.app.enums.StatusCaixa;
import com.sgauto.app.model.Caixa;
import com.sgauto.app.model.CaixaMovimentacao;
import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.enums.TipoMovimentacao;
import com.sgauto.app.model.ConfiguracaoCaixa;
import com.sgauto.app.repository.CaixaMovimentacaoRepository;
import com.sgauto.app.repository.CaixaRepository;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.classfile.instruction.ThrowInstruction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final CaixaMovimentacaoRepository caixaMovimentacaoRepository;
    private final ConfiguracaoCaixaService configuracaoCaixaService;

    public CaixaService(CaixaRepository caixaRepository,
                        CaixaMovimentacaoRepository caixaMovimentacaoRepository,
                        ConfiguracaoCaixaService configuracaoCaixaService) {
        this.caixaRepository = caixaRepository;
        this.caixaMovimentacaoRepository = caixaMovimentacaoRepository;
        this.configuracaoCaixaService = configuracaoCaixaService;
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
                .filter(m -> m.getTipo() == TipoMovimentacao.SAIDA)
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

        caixa.setTotalVendasPecas(somarPor(movimentacoes, m -> m.getOrigem() == OrigemMovimentacao.VENDA_PECA));
        caixa.setTotalServicos(somarPor(movimentacoes, m -> m.getOrigem() == OrigemMovimentacao.SERVICO));
        caixa.setTotalAvulso(somarPor(movimentacoes, m -> m.getOrigem() == OrigemMovimentacao.AVULSO));
        caixa.setTotalSangria(somarPor(movimentacoes, m -> m.getOrigem() == OrigemMovimentacao.SANGRIA));
        caixa.setTotalSuprimento(somarPor(movimentacoes, m -> m.getOrigem() == OrigemMovimentacao.SUPRIMENTO));

        caixa.setTotalDinheiro(somarPor(movimentacoes, m -> m.getFormaPagamento() == FormaPagamento.DINHEIRO));
        caixa.setTotalDebito(somarPor(movimentacoes, m -> m.getFormaPagamento() == FormaPagamento.DEBITO));
        caixa.setTotalCredito(somarPor(movimentacoes, m -> m.getFormaPagamento() == FormaPagamento.CREDITO));
        caixa.setTotalPix(somarPor(movimentacoes, m -> m.getFormaPagamento() == FormaPagamento.PIX));
    }

    private BigDecimal somarPor(List<CaixaMovimentacao> movimentacoes, java.util.function.Predicate<CaixaMovimentacao> filtro) {
        return movimentacoes.stream()
                .filter(filtro)
                .map(CaixaMovimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}