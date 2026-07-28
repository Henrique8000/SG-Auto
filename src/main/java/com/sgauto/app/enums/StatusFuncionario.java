package com.sgauto.app.enums;

public enum StatusFuncionario {
    ATIVO("Ativo"),
    INATIVO("Inativo"),
    FERIAS("Em Férias"),
    AFASTADO("Afastado / Licença"),
    DEMITIDO("Demitido");

    private final String descricao;

    StatusFuncionario(String descricao) {
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