package com.sgauto.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_cliente")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "cliente_tipo_pessoa", discriminatorType = DiscriminatorType.STRING, length = 2)
public abstract class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_nome", nullable = false, length = 150)
    private String nome;

    /** CPF ou CNPJ, armazenado somente com dígitos. Nullable: cliente de balcão pode não ter. */
    @Column(name = "cliente_documento", unique = true, length = 14)
    private String documento;

    @Column(name = "cliente_celular", length = 11)
    private String celular;

    @Column(name = "cliente_telefone", length = 10)
    private String telefone;

    @Column(name = "cliente_email", length = 150)
    private String email;

    @Column(name = "cliente_observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "cliente_ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "cliente_data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "cliente_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Embedded
    private Endereco endereco;

    protected Cliente() {}

    protected Cliente(String nome, String documento, String celular, String telefone,
                      String email, String observacoes, Boolean ativo) {
        this.nome = nome;
        this.documento = documento;
        this.celular = celular;
        this.telefone = telefone;
        this.email = email;
        this.observacoes = observacoes;
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

    /** "PF" ou "PJ" — usado pela tabela e pelos filtros da tela. */
    public abstract String getTipo();

    /** Documento com máscara (000.000.000-00 ou 00.000.000/0000-00) para exibição. */
    public abstract String getDocumentoFormatado();

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDocumento() {
        return documento; }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular; }


    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
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

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}