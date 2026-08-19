package com.sgauto.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_fornecedor")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fornecedor_id")
    private Long id;

    @Column(name = "fornecedor_tipo_pessoa", nullable = false, length = 2)
    private String tipoPessoa = "PJ";

    @Column(name = "fornecedor_cpf_cnpj", unique = true, nullable = false, length = 20)
    private String cpfCnpj;

    @Column(name = "fornecedor_razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Column(name = "fornecedor_nome_fantasia", length = 150)
    private String nomeFantasia;

    @Column(name = "fornecedor_inscricao_estadual", length = 50)
    private String inscricaoEstadual;

    @Column(name = "fornecedor_inscricao_municipal", length = 50)
    private String inscricaoMunicipal;

    // --- Informações de Contato ---

    @Column(name = "fornecedor_nome_contato", length = 100)
    private String nomeContato;

    @Column(name = "fornecedor_telefone", length = 20)
    private String telefone;

    @Column(name = "fornecedor_celular", length = 20)
    private String celular;

    @Column(name = "fornecedor_email", length = 150)
    private String email;

    @Column(name = "fornecedor_site", length = 150)
    private String site;

    // --- Endereço Completo ---

    @Column(name = "fornecedor_cep", length = 10)
    private String cep;

    @Column(name = "fornecedor_logradouro", length = 150)
    private String logradouro;

    @Column(name = "fornecedor_numero", length = 20)
    private String numero;

    @Column(name = "fornecedor_complemento", length = 100)
    private String complemento;

    @Column(name = "fornecedor_bairro", length = 100)
    private String bairro;

    @Column(name = "fornecedor_cidade", length = 100)
    private String cidade;

    @Column(name = "fornecedor_uf", length = 2)
    private String uf;

    // --- Dados Operacionais e Controle ---

    @Column(name = "fornecedor_categoria", length = 100)
    private String categoria;

    @Column(name = "fornecedor_prazo_entrega_dias")
    private Integer prazoEntregaDias;

    @Column(name = "fornecedor_ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "fornecedor_observacoes", columnDefinition = "TEXT")
    private String observacoes;

    // --- Auditoria ---

    @Column(name = "fornecedor_criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "fornecedor_atualizado_em")
    private LocalDateTime atualizadoEm;

    // --- Construtores ---

    public Fornecedor() {
    }

    // --- Métodos de Ciclo de Vida (Auditoria Automática) ---

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // --- Getters e Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(String tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String inscricaoEstadual) { this.inscricaoEstadual = inscricaoEstadual; }

    public String getInscricaoMunicipal() { return inscricaoMunicipal; }
    public void setInscricaoMunicipal(String inscricaoMunicipal) { this.inscricaoMunicipal = inscricaoMunicipal; }

    public String getNomeContato() { return nomeContato; }
    public void setNomeContato(String nomeContato) { this.nomeContato = nomeContato; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getPrazoEntregaDias() { return prazoEntregaDias; }
    public void setPrazoEntregaDias(Integer prazoEntregaDias) { this.prazoEntregaDias = prazoEntregaDias; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}