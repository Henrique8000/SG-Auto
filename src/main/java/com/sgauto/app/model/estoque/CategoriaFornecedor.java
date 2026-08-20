package com.sgauto.app.model.estoque;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_fornecedor_categoria")
public class CategoriaFornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fornec_categoria_nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "fornec_categoria_descricao", length = 255)
    private String descricao;

    @Column(name = "fornec_categoria_ativo", nullable = false)
    private Boolean ativo = true;

    // --- Auditoria ---

    @Column(name = "fornec_categoria_data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "fornec_categoria_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // --- Construtores ---

    public CategoriaFornecedor() {
    }

    public CategoriaFornecedor(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = true;
    }

    // --- Métodos de Ciclo de Vida (Auditoria Automática) ---

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
