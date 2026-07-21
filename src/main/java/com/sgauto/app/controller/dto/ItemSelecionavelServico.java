package com.sgauto.app.controller.dto;

import com.sgauto.app.model.Servico;
import javafx.beans.property.SimpleBooleanProperty;

public class ItemSelecionavelServico {
    private final Servico servico;
    private final SimpleBooleanProperty selecionado = new SimpleBooleanProperty(false);

    public ItemSelecionavelServico(Servico servico) {
        this.servico = servico;
    }

    public Servico getServico() { return servico; }
    public boolean isSelecionado() { return selecionado.get(); }
    public void setSelecionado(boolean valor) { selecionado.set(valor); }
    public SimpleBooleanProperty selecionadoProperty() { return selecionado; }
}