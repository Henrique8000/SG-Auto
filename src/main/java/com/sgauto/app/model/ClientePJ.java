package com.sgauto.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PJ")
public class ClientePJ extends Cliente {

    @Column(name = "cliente_nome_fantasia", length = 150)
    private String nomeFantasia;

    @Column(name = "cliente_inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    public ClientePJ() {}

    public ClientePJ(String nome, String documento, String celular, String telefone,
                     String email, String observacoes, Boolean ativo,
                     String nomeFantasia, String inscricaoEstadual) {
        super(nome, documento, celular, telefone, email, observacoes, ativo);
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
    }

    @Override
    public String getTipo() {
        return "PJ";
    }

    @Override
    public String getDocumentoFormatado() {
        String cnpj = getDocumento();
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8)
                + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
    }
}