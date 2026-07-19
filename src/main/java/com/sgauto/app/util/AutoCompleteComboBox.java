package com.sgauto.app.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;

import java.util.List;

public class AutoCompleteComboBox {

    private final ComboBox<String> comboBox;
    private final ObservableList<String> itensOriginais = FXCollections.observableArrayList();
    private final FilteredList<String> itensFiltrados = new FilteredList<>(itensOriginais, s -> true);

    public AutoCompleteComboBox(ComboBox<String> comboBox) {
        this.comboBox = comboBox;
        this.comboBox.setEditable(true);
        this.comboBox.setItems(itensFiltrados);

        this.comboBox.getEditor().textProperty().addListener((obs, textoAntigo, textoNovo) -> {
            String valorSelecionado = comboBox.getSelectionModel().getSelectedItem();

            // Se o texto mudou porque um item da lista foi selecionado
            // (não porque o usuário está digitando), não filtra de novo.
            if (valorSelecionado != null && valorSelecionado.equals(textoNovo)) {
                return;
            }

            aplicarFiltro(textoNovo);

            if (comboBox.isFocused() && !comboBox.isShowing()) {
                comboBox.show();
            }
        });

        // Ao fechar o popup (clicou fora, selecionou algo, etc.), a lista volta a ficar completa
        this.comboBox.showingProperty().addListener((obs, estavaAberto, estaAberto) -> {
            if (!estaAberto) {
                aplicarFiltro("");
            }
        });
    }

    public void definirItens(List<String> itens) {
        itensOriginais.setAll(itens);
    }

    private void aplicarFiltro(String texto) {
        if (texto == null || texto.isBlank()) {
            itensFiltrados.setPredicate(s -> true);
        } else {
            String termo = texto.toLowerCase();
            itensFiltrados.setPredicate(s -> s.toLowerCase().contains(termo));
        }
    }
}