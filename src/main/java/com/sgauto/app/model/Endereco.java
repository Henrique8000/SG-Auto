package com.sgauto.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Endereco {

    @Column(name = "cliente_cep", length = 8)
    private String cep;

    @Column(name = "cliente_logradouro", length = 150)
    private String logradouro;

    @Column(name = "cliente_numero", length = 10)
    private String numero;

    @Column(name = "cliente_complemento", length = 50)
    private String complemento;

    @Column(name = "cliente_bairro", length = 100)
    private String bairro;

    @Column(name = "cliente_cidade", length = 100)
    private String cidade;

    @Column(name = "cliente_uf", length = 2)
    private String uf;

    public Endereco() {
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}