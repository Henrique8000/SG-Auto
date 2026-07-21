package com.sgauto.app.model;

import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.enums.TipoMovimentacao;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_caixa_movimentacao")
public class CaixaMovimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movimentacao_caixa_id", nullable = false)
    private Caixa caixa;

    @Enumerated(EnumType.STRING)
    @Column(name = "movimentacao_tipo", nullable = false, length = 20)
    private TipoMovimentacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "movimentacao_origem", nullable = false, length = 30)
    private OrigemMovimentacao origem;

    @Enumerated(EnumType.STRING)
    @Column(name = "movimentacao_forma_pagamento", length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "movimentacao_valor", nullable = false)
    private BigDecimal valor;

    @Column(name = "movimentacao_descricao", length = 255)
    private String descricao;

    @Column(name = "movimentacao_data", nullable = false)
    private LocalDateTime data;

    @Column(name = "movimentacao_referencia_id")
    private Long referenciaId;

    @Column(name = "movimentacao_cliente_id")
    private Long clienteId;

    @Column(name = "movimentacao_placa", length = 10)
    private String placa;

    public CaixaMovimentacao() {}

    public CaixaMovimentacao(Caixa caixa, TipoMovimentacao tipo, OrigemMovimentacao origem,
                             FormaPagamento formaPagamento, BigDecimal valor, String descricao) {
        this.caixa = caixa;
        this.tipo = tipo;
        this.origem = origem;
        this.formaPagamento = formaPagamento;
        this.valor = valor;
        this.descricao = descricao;
        this.data = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }

    public Caixa getCaixa() { return caixa; }
    public void setCaixa(Caixa caixa) { this.caixa = caixa; }

    public TipoMovimentacao getTipo() { return tipo; }
    public void setTipo(TipoMovimentacao tipo) { this.tipo = tipo; }

    public OrigemMovimentacao getOrigem() { return origem; }
    public void setOrigem(OrigemMovimentacao origem) { this.origem = origem; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
}