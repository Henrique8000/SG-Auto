package com.sgauto.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoria_nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "categoria_descricao", length = 255)
    private String descricao;

    @Column(name = "categoria_tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "categoria_ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "categoria_data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "categoria_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public Categoria() {}

    public Categoria(String nome, String descricao, String tipo, Boolean ativo) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
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

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}