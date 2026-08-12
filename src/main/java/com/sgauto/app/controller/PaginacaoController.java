package com.sgauto.app.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

@Component
@Scope("prototype")
public class PaginacaoController {

    @FXML private Label lblTotalRegistros;
    @FXML private Label lblPagina;
    @FXML private Button btnAnterior;
    @FXML private Button btnProxima;
    @FXML private ComboBox<Integer> cmbTamanhoPagina;

    private int paginaAtual = 0;
    private IntConsumer aoMudarPagina;
    private Consumer<Integer> aoMudarTamanho;
    private boolean inicializando = true;

    @FXML
    public void initialize() {
        cmbTamanhoPagina.setItems(FXCollections.observableArrayList(10, 20, 50, 100));
        cmbTamanhoPagina.getSelectionModel().select(Integer.valueOf(20));
        cmbTamanhoPagina.valueProperty().addListener((obs, antigo, novo) -> {
            if (!inicializando && novo != null && aoMudarTamanho != null) {
                paginaAtual = 0;
                aoMudarTamanho.accept(novo);
            }
        });
        inicializando = false;
    }

    public void configurar(IntConsumer aoMudarPagina, Consumer<Integer> aoMudarTamanho) {
        this.aoMudarPagina = aoMudarPagina;
        this.aoMudarTamanho = aoMudarTamanho;
    }

    public int getTamanhoPagina() {
        Integer valor = cmbTamanhoPagina.getValue();
        return valor != null ? valor : 20;
    }

    public void atualizar(Page<?> pagina) {
        this.paginaAtual = pagina.getNumber();
        int totalPaginas = Math.max(pagina.getTotalPages(), 1);

        lblPagina.setText("Página " + (paginaAtual + 1) + " de " + totalPaginas);
        lblTotalRegistros.setText(pagina.getTotalElements() + " registro(s)");

        btnAnterior.setDisable(pagina.isFirst());
        btnProxima.setDisable(pagina.isLast());
    }

    public void resetarPagina() {
        this.paginaAtual = 0;
    }

    @FXML
    private void paginaAnterior() {
        if (paginaAtual > 0) {
            paginaAtual--;
            aoMudarPagina.accept(paginaAtual);
        }
    }

    @FXML
    private void proximaPagina() {
        paginaAtual++;
        aoMudarPagina.accept(paginaAtual);
    }
}
