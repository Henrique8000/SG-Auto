package com.sgauto.app.model.financeiro;

import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.StatusConta;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_conta_receber")
public class ContaReceber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_receber_descricao", nullable = false, length = 255)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_receber_categoria_id")
    private CategoriaFinanceira categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_receber_cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_receber_ordem_servico_id")
    private OrdemServico ordemServico;

    @Column(name = "conta_receber_numero_parcela", nullable = false)
    private Integer numeroParcela = 1;

    @Column(name = "conta_receber_total_parcelas", nullable = false)
    private Integer totalParcelas = 1;

    @Column(name = "conta_receber_valor_original", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorOriginal;

    @Column(name = "conta_receber_valor_desconto", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "conta_receber_valor_juros", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorJuros = BigDecimal.ZERO;

    @Column(name = "conta_receber_valor_multa", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorMulta = BigDecimal.ZERO;

    @Column(name = "conta_receber_valor_recebido", precision = 12, scale = 2)
    private BigDecimal valorRecebido;

    @Column(name = "conta_receber_data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "conta_receber_data_recebimento")
    private LocalDate dataRecebimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "conta_receber_status", nullable = false, length = 20)
    private StatusConta status = StatusConta.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "conta_receber_forma_recebimento", length = 20)
    private FormaPagamento formaRecebimento;

    @Column(name = "conta_receber_origem", nullable = false, length = 30)
    private String origem = "MANUAL";

    @Column(name = "conta_receber_observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "conta_receber_criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "conta_receber_atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoPersistir() {
        LocalDateTime agora = LocalDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public ContaReceber() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public CategoriaFinanceira getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaFinanceira categoria) {
        this.categoria = categoria;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }

    public Integer getNumeroParcela() {
        return numeroParcela;
    }

    public void setNumeroParcela(Integer numeroParcela) {
        this.numeroParcela = numeroParcela;
    }

    public Integer getTotalParcelas() {
        return totalParcelas;
    }

    public void setTotalParcelas(Integer totalParcelas) {
        this.totalParcelas = totalParcelas;
    }

    public BigDecimal getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(BigDecimal valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public BigDecimal getValorJuros() {
        return valorJuros;
    }

    public void setValorJuros(BigDecimal valorJuros) {
        this.valorJuros = valorJuros;
    }

    public BigDecimal getValorMulta() {
        return valorMulta;
    }

    public void setValorMulta(BigDecimal valorMulta) {
        this.valorMulta = valorMulta;
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(BigDecimal valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDate dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public StatusConta getStatus() {
        return status;
    }

    public void setStatus(StatusConta status) {
        this.status = status;
    }

    public FormaPagamento getFormaRecebimento() {
        return formaRecebimento;
    }

    public void setFormaRecebimento(FormaPagamento formaRecebimento) {
        this.formaRecebimento = formaRecebimento;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContaReceber that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
