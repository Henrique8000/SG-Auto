package com.sgauto.app.model.OrdemServico;

import com.sgauto.app.enums.FormaPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_os_pagamento")
public class OsPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "data_pagamento", nullable = false, updatable = false)
    private LocalDateTime dataPagamento = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 50)
    private FormaPagamento formaPagamento;

    @Column(name = "valor_pago", nullable = false)
    private BigDecimal valorPago;

    // Construtor Vazio (Obrigatório para o JPA)
    public OsPagamento() {
    }

    // Construtor com Argumentos
    public OsPagamento(Long id, OrdemServico ordemServico, LocalDateTime dataPagamento, FormaPagamento formaPagamento, BigDecimal valorPago) {
        this.id = id;
        this.ordemServico = ordemServico;
        this.dataPagamento = dataPagamento;
        this.formaPagamento = formaPagamento;
        this.valorPago = valorPago;
    }

    // ==========================================
    // GETTERS E SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }
}