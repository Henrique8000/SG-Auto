package com.sgauto.app.model.patio;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_motivo_estadia")
public class MotivoEstadia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motivo_nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "motivo_descricao", length = 255)
    private String descricao;

    @Column(name = "motivo_ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "motivo_protegido", nullable = false)
    private Boolean protegido;

    @Column(name = "motivo_data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "motivo_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public MotivoEstadia() {}

    public MotivoEstadia(String nome, String descricao, Boolean ativo, Boolean protegido) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo != null ? ativo : true;
        this.protegido = protegido != null ? protegido : false;
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

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Boolean getProtegido() { return protegido; }
    public void setProtegido(Boolean protegido) { this.protegido = protegido; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}