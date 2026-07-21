package com.sgauto.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "servico_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "servico_nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "servico_categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "servico_descricao", length = 255)
    private String descricao;

    @Column(name = "servico_valor", nullable = false)
    private BigDecimal valor;

    @Column(name = "servico_tempo_estimado_minutos", nullable = false)
    private Integer tempoEstimadoMinutos;

    @Column(name = "servico_garantia_dias", nullable = false)
    private Integer garantiaDias;

    @Column(name = "servico_comissao_porcentagem", nullable = false)
    private BigDecimal comissaoPorcentagem;

    @Column(name = "servico_observacoes_tecnicas", columnDefinition = "TEXT")
    private String observacoesTecnicas;

    @Column(name = "servico_ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "servico_data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "servico_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public Servico() {}

    public Servico(String codigo, String nome, String categoria, String descricao, BigDecimal valor,
                   Integer tempoEstimadoMinutos, Integer garantiaDias, BigDecimal comissaoPorcentagem,
                   String observacoesTecnicas, Boolean ativo) {
        this.codigo = codigo;
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor != null ? valor : BigDecimal.ZERO;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos != null ? tempoEstimadoMinutos : 60;
        this.garantiaDias = garantiaDias != null ? garantiaDias : 90;
        this.comissaoPorcentagem = comissaoPorcentagem != null ? comissaoPorcentagem : BigDecimal.ZERO;
        this.observacoesTecnicas = observacoesTecnicas;
        this.ativo = ativo != null ? ativo : true;
    }

    @PrePersist
    protected void aoCriar() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void aoAtualizar() {
        dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Integer getTempoEstimadoMinutos() { return tempoEstimadoMinutos; }
    public void setTempoEstimadoMinutos(Integer tempoEstimadoMinutos) { this.tempoEstimadoMinutos = tempoEstimadoMinutos; }

    public Integer getGarantiaDias() { return garantiaDias; }
    public void setGarantiaDias(Integer garantiaDias) { this.garantiaDias = garantiaDias; }

    public BigDecimal getComissaoPorcentagem() { return comissaoPorcentagem; }
    public void setComissaoPorcentagem(BigDecimal comissaoPorcentagem) { this.comissaoPorcentagem = comissaoPorcentagem; }

    public String getObservacoesTecnicas() { return observacoesTecnicas; }
    public void setObservacoesTecnicas(String observacoesTecnicas) { this.observacoesTecnicas = observacoesTecnicas; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}