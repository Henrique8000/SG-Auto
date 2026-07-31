package com.sgauto.app.enums;

public enum CargoFuncionario {
    MECANICO("Mecânico"),
    ELETRICISTA("Eletricista"),
    FUNILEIRO("Funileiro"),
    PINTOR("Pintor"),
    ATENDENTE("Atendente"),
    RECEPCIONISTA("Recepcionista"),
    CAIXA("Operador de Caixa"),
    GERENTE("Gerente"),
    CONSULTOR_TECNICO("Consultor Técnico"),
    LAVADOR("Lavador / Estética"),
    AUXILIAR("Auxiliar Geral"),
    ADMINISTRATIVO("Administrativo"),
    OUTROS("Outros");

    private final String descricao;

    CargoFuncionario(String descricao) {
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