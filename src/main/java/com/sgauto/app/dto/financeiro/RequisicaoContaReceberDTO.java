package com.sgauto.app.dto.financeiro;

import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.enums.financeiro.OrigemMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RequisicaoContaReceberDTO {

    // 1. RASTREABILIDADE E RELACIONAMENTOS

    private Long clienteId;

    private Long ordemServicoId;

    private Long categoriaFinanceiraId;

    // 2. DADOS BASE DA COBRANÇA

    @NotBlank(message = "A descrição da conta é obrigatória.")
    private String descricao;

    @NotNull(message = "O valor total da negociação é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor total deve ser maior que zero.")
    private BigDecimal valorTotal;

    // 3. CONFIGURAÇÕES DE PAGAMENTO / ACORDO

    @DecimalMin(value = "0.00", message = "O valor de entrada não pode ser negativo.")
    private BigDecimal valorEntrada = BigDecimal.ZERO;

    private FormaPagamento formaPagamentoEntrada;

    @NotNull(message = "A quantidade de parcelas é obrigatória.")
    @Min(value = 1, message = "A quantidade mínima de parcelas é 1.")
    private Integer quantidadeParcelas = 1;

    @NotNull(message = "O intervalo de dias entre parcelas é obrigatório.")
    @Min(value = 1, message = "O intervalo de dias deve ser de pelo menos 1 dia.")
    private Integer intervaloDias = 30; // Padrão: mensal

    @NotNull(message = "A data de vencimento inicial é obrigatória.")
    private LocalDate dataVencimentoInicial;

    private boolean primeiraParcelaAVista = false;

    // CONSTRUTORES, GETTERS E SETTERS


    public RequisicaoContaReceberDTO() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(Long ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public Long getCategoriaFinanceiraId() {
        return categoriaFinanceiraId;
    }

    public void setCategoriaFinanceiraId(Long categoriaFinanceiraId) {
        this.categoriaFinanceiraId = categoriaFinanceiraId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorEntrada() {
        return valorEntrada;
    }

    public void setValorEntrada(BigDecimal valorEntrada) {
        this.valorEntrada = valorEntrada;
    }

    public FormaPagamento getFormaPagamentoEntrada() {
        return formaPagamentoEntrada;
    }

    public void setFormaPagamentoEntrada(FormaPagamento formaPagamentoEntrada) {
        this.formaPagamentoEntrada = formaPagamentoEntrada;
    }

    public Integer getQuantidadeParcelas() {
        return quantidadeParcelas;
    }

    public void setQuantidadeParcelas(Integer quantidadeParcelas) {
        this.quantidadeParcelas = quantidadeParcelas;
    }

    public Integer getIntervaloDias() {
        return intervaloDias;
    }

    public void setIntervaloDias(Integer intervaloDias) {
        this.intervaloDias = intervaloDias;
    }

    public LocalDate getDataVencimentoInicial() {
        return dataVencimentoInicial;
    }

    public void setDataVencimentoInicial(LocalDate dataVencimentoInicial) {
        this.dataVencimentoInicial = dataVencimentoInicial;
    }

    public boolean isPrimeiraParcelaAVista() {
        return primeiraParcelaAVista;
    }

    public void setPrimeiraParcelaAVista(boolean primeiraParcelaAVista) {
        this.primeiraParcelaAVista = primeiraParcelaAVista;
    }
}