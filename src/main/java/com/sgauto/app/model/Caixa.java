package com.sgauto.app.model;

import com.sgauto.app.enums.ModoConferencia;
import com.sgauto.app.enums.StatusCaixa;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_caixa")
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caixa_data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "caixa_data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "caixa_usuario_abertura", nullable = false, length = 100)
    private String usuarioAbertura;

    @Column(name = "caixa_usuario_fechamento", length = 100)
    private String usuarioFechamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "caixa_status", nullable = false, length = 20)
    private StatusCaixa status;

    @Column(name = "caixa_valor_abertura", nullable = false)
    private BigDecimal valorAbertura;

    @Column(name = "caixa_total_entradas")
    private BigDecimal totalEntradas;

    @Column(name = "caixa_total_saidas")
    private BigDecimal totalSaidas;

    @Column(name = "caixa_total_vendas_pecas")
    private BigDecimal totalVendasPecas;

    @Column(name = "caixa_total_servicos")
    private BigDecimal totalServicos;

    @Column(name = "caixa_total_avulso")
    private BigDecimal totalAvulso;

    @Column(name = "caixa_total_sangria")
    private BigDecimal totalSangria;

    @Column(name = "caixa_total_suprimento")
    private BigDecimal totalSuprimento;

    @Column(name = "caixa_total_dinheiro")
    private BigDecimal totalDinheiro;

    @Column(name = "caixa_total_debito")
    private BigDecimal totalDebito;

    @Column(name = "caixa_total_credito")
    private BigDecimal totalCredito;

    @Column(name = "caixa_total_pix")
    private BigDecimal totalPix;

    @Column(name = "caixa_valor_esperado")
    private BigDecimal valorEsperado;

    @Column(name = "caixa_valor_contado")
    private BigDecimal valorContado;

    @Column(name = "caixa_diferenca")
    private BigDecimal diferenca;

    @Enumerated(EnumType.STRING)
    @Column(name = "caixa_modo_conferencia_usado", length = 20)
    private ModoConferencia modoConferenciaUsado;

    @Column(name = "caixa_justificativa_diferenca", columnDefinition = "TEXT")
    private String justificativaDiferenca;

    @Column(name = "caixa_observacoes", columnDefinition = "TEXT")
    private String observacoes;

    public Caixa() {}

    public Caixa(String usuarioAbertura, BigDecimal valorAbertura) {
        this.usuarioAbertura = usuarioAbertura;
        this.valorAbertura = valorAbertura;
        this.status = StatusCaixa.ABERTO;
        this.dataAbertura = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }

    public String getUsuarioAbertura() { return usuarioAbertura; }
    public void setUsuarioAbertura(String usuarioAbertura) { this.usuarioAbertura = usuarioAbertura; }

    public String getUsuarioFechamento() { return usuarioFechamento; }
    public void setUsuarioFechamento(String usuarioFechamento) { this.usuarioFechamento = usuarioFechamento; }

    public StatusCaixa getStatus() { return status; }
    public void setStatus(StatusCaixa status) { this.status = status; }

    public BigDecimal getValorAbertura() { return valorAbertura; }
    public void setValorAbertura(BigDecimal valorAbertura) { this.valorAbertura = valorAbertura; }

    public BigDecimal getTotalEntradas() { return totalEntradas; }
    public void setTotalEntradas(BigDecimal totalEntradas) { this.totalEntradas = totalEntradas; }

    public BigDecimal getTotalSaidas() { return totalSaidas; }
    public void setTotalSaidas(BigDecimal totalSaidas) { this.totalSaidas = totalSaidas; }

    public BigDecimal getTotalVendasPecas() { return totalVendasPecas; }
    public void setTotalVendasPecas(BigDecimal totalVendasPecas) { this.totalVendasPecas = totalVendasPecas; }

    public BigDecimal getTotalServicos() { return totalServicos; }
    public void setTotalServicos(BigDecimal totalServicos) { this.totalServicos = totalServicos; }

    public BigDecimal getTotalAvulso() { return totalAvulso; }
    public void setTotalAvulso(BigDecimal totalAvulso) { this.totalAvulso = totalAvulso; }

    public BigDecimal getTotalSangria() { return totalSangria; }
    public void setTotalSangria(BigDecimal totalSangria) { this.totalSangria = totalSangria; }

    public BigDecimal getTotalSuprimento() { return totalSuprimento; }
    public void setTotalSuprimento(BigDecimal totalSuprimento) { this.totalSuprimento = totalSuprimento; }

    public BigDecimal getTotalDinheiro() { return totalDinheiro; }
    public void setTotalDinheiro(BigDecimal totalDinheiro) { this.totalDinheiro = totalDinheiro; }

    public BigDecimal getTotalDebito() { return totalDebito; }
    public void setTotalDebito(BigDecimal totalDebito) { this.totalDebito = totalDebito; }

    public BigDecimal getTotalCredito() { return totalCredito; }
    public void setTotalCredito(BigDecimal totalCredito) { this.totalCredito = totalCredito; }

    public BigDecimal getTotalPix() { return totalPix; }
    public void setTotalPix(BigDecimal totalPix) { this.totalPix = totalPix; }

    public BigDecimal getValorEsperado() { return valorEsperado; }
    public void setValorEsperado(BigDecimal valorEsperado) { this.valorEsperado = valorEsperado; }

    public BigDecimal getValorContado() { return valorContado; }
    public void setValorContado(BigDecimal valorContado) { this.valorContado = valorContado; }

    public BigDecimal getDiferenca() { return diferenca; }
    public void setDiferenca(BigDecimal diferenca) { this.diferenca = diferenca; }

    public ModoConferencia getModoConferenciaUsado() { return modoConferenciaUsado; }
    public void setModoConferenciaUsado(ModoConferencia modoConferenciaUsado) { this.modoConferenciaUsado = modoConferenciaUsado; }

    public String getJustificativaDiferenca() { return justificativaDiferenca; }
    public void setJustificativaDiferenca(String justificativaDiferenca) { this.justificativaDiferenca = justificativaDiferenca; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}