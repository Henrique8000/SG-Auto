package com.sgauto.app.model.patio;

import com.sgauto.app.enums.CategoriaVeiculoPatio;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_tabela_preco_patio")
public class TabelaPrecoPatio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tpreco_descricao", nullable = false, length = 100)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tpreco_categoria", nullable = false, length = 30)
    private CategoriaVeiculoPatio categoria;

    @Column(name = "tpreco_valor_diaria", nullable = false)
    private BigDecimal valorDiaria;

    @Column(name = "tpreco_dias_carencia", nullable = false)
    private Integer diasCarencia;

    @Column(name = "tpreco_ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "tpreco_data_criacao")
    private LocalDateTime dataCriacao;

    public TabelaPrecoPatio() {}

    public TabelaPrecoPatio(String descricao, CategoriaVeiculoPatio categoria,
                            BigDecimal valorDiaria, Integer diasCarencia, Boolean ativo) {
        this.descricao = descricao;
        this.categoria = categoria;
        this.valorDiaria = valorDiaria;
        this.diasCarencia = diasCarencia;
        this.ativo = ativo != null ? ativo : true;
    }

    @PrePersist
    protected void aoCriar() {
        dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public CategoriaVeiculoPatio getCategoria() { return categoria; }
    public void setCategoria(CategoriaVeiculoPatio categoria) { this.categoria = categoria; }

    public BigDecimal getValorDiaria() { return valorDiaria; }
    public void setValorDiaria(BigDecimal valorDiaria) { this.valorDiaria = valorDiaria; }

    public Integer getDiasCarencia() { return diasCarencia; }
    public void setDiasCarencia(Integer diasCarencia) { this.diasCarencia = diasCarencia; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
}