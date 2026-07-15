package com.sgauto.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "t_peca")
public class Peca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "peca_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "peca_descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "peca_preco_custo", nullable = false)
    private BigDecimal precoCusto;

    @Column(name = "peca_preco_venda", nullable = false)
    private BigDecimal precoVenda;

    @Column(name = "peca_quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque;

    @Column(name = "peca_estoque_minimo", nullable = false)
    private Integer estoqueMinimo;

    @Column(name = "peca_modelo", nullable = false, length = 100)
    private String modelo;

    public Peca() {}

    public Peca(String codigo, String descricao, String modelo, BigDecimal precoCusto, BigDecimal precoVenda,
                Integer quantidadeEstoque, Integer estoqueMinimo) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.modelo = modelo;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.quantidadeEstoque = quantidadeEstoque;
        this.estoqueMinimo = estoqueMinimo;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(BigDecimal precoCusto) { this.precoCusto = precoCusto; }
    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal precoVenda) { this.precoVenda = precoVenda; }
    public Integer getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(Integer quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }
    public Integer getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(Integer estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
}