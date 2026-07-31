package com.sgauto.app.model.OrdemServico;

import com.sgauto.app.model.Servico;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "t_os_servico")
public class OsServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(name = "valor_unitario", nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    // Construtor Vazio (Obrigatório para o JPA)
    public OsServico() {
    }

    // Construtor com Argumentos
    public OsServico(Long id, OrdemServico ordemServico, Servico servico, Integer quantidade, BigDecimal valorUnitario, BigDecimal valorTotal) {
        this.id = id;
        this.ordemServico = ordemServico;
        this.servico = servico;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
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

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}