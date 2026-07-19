package com.sgauto.app.controller.dto;

import com.sgauto.app.model.Peca;
import javafx.beans.property.SimpleBooleanProperty;

public class ItemSelecionavelPeca {
    private final Peca peca;
    private final SimpleBooleanProperty selecionado = new SimpleBooleanProperty(false);

    public ItemSelecionavelPeca(Peca peca) {
        this.peca = peca;
    }

    public Peca getPeca() { return peca; }
    public boolean isSelecionado() { return selecionado.get(); }
    public void setSelecionado(boolean valor) { selecionado.set(valor); }
    public SimpleBooleanProperty selecionadoProperty() { return selecionado; }
}