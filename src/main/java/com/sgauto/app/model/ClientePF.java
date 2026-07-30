package com.sgauto.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("PF")
public class ClientePF extends Cliente {

    @Column(name = "cliente_rg", length = 20)
    private String rg;

    @Column(name = "cliente_data_nascimento")
    private LocalDate dataNascimento;

    public ClientePF() {}

    public ClientePF(String nome, String documento, String celular, String telefone,
                     String email, String observacoes, Boolean ativo,
                     String rg, LocalDate dataNascimento) {
        super(nome, documento, celular, telefone, email, observacoes, ativo);
        this.rg = rg;
        this.dataNascimento = dataNascimento;
    }

    @Override
    public String getTipo() {
        return "PF";
    }

    @Override
    public String getDocumentoFormatado() {
        String cpf = getDocumento();
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}