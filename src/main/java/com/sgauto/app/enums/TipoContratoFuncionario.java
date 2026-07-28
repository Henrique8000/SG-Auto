package com.sgauto.app.enums;

public enum TipoContratoFuncionario {
    CLT("CLT"),
    PJ("Pessoa Jurídica (PJ)"),
    ESTAGIO("Estágio"),
    TEMPORARIO("Temporário"),
    AUTONOMO("Autônomo / Terceirizado");

    private final String descricao;

    TipoContratoFuncionario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}