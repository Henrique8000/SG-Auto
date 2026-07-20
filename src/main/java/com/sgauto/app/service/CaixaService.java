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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Caixa caixa = new Caixa();
        caixa.setUsuarioAbertura("teste");
        caixa.setDataAbertura(LocalDateTime.now());
        caixa.setStatus(StatusCaixa.ABERTO);
        caixa.setValorAbertura(BigDecimal.ZERO);
        caixaRepository.save(caixa);
        return caixa;
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
        CaixaMovimentacao mov = new CaixaMovimentacao();
        mov.setTipo(tipo);
        mov.setOrigem(origem);
        mov.setFormaPagamento(formaPagamento);
        mov.setValor(valor);
        mov.setDescricao(descricao);
        mov.setClienteId(clienteId);
        mov.setPlaca(placa);

        return mov;
    }

    @Transactional
    public Caixa fecharCaixaAtual(BigDecimal valorContado, String justificativaDiferenca) {
        ConfiguracaoCaixa config = configuracaoCaixaService.buscarConfiguracao();

        switch (config.getModoConferencia()) {
            case OBRIGATORIA -> {
            }
            case OPCIONAL -> {
            }
            case SEM_CONFERENCIA -> {
            }
            default -> throw new IllegalStateException("Modo de conferência não suportado.");


        }
        return null;
    }

    /**
     * Calcula o valor esperado em dinheiro no caixa, com base no
     * valorAbertura + entradas em dinheiro - saídas em dinheiro
     * (sangria). Usado tanto para exibir na tela antes do fechamento
     * quanto internamente por fecharCaixaAtual().
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularValorEsperado(Long caixaId) {
        Optional<Caixa> caixa = caixaRepository.findById(caixaId);
        BigDecimal soma = BigDecimal.ZERO;
        soma = caixa.get().getValorAbertura();
        soma = soma.add(caixa.get().getTotalDinheiro());
        soma = soma.subtract(caixa.get().getTotalSaidas());
        return soma;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularValorBruto(Long caixaId) {
        Optional<Caixa> caixa = caixaRepository.findById(caixaId);
        BigDecimal soma = BigDecimal.ZERO;
        soma = caixa.get().getValorAbertura();
        soma = soma.add(caixa.get().getTotalDinheiro());
        soma = soma.add(caixa.get().getTotalAvulso());
        soma = soma.add(caixa.get().getTotalCredito());
        soma = soma.add(caixa.get().getTotalDebito());
        return soma;
    }

    /**
     * Lista o histórico de caixas já fechados, para relatório/consulta.
     */
    @Transactional(readOnly = true)
    public List<Caixa> listarHistorico() {
        return caixaRepository.findByStatus(StatusCaixa.FECHADO);
    }
}