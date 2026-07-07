package com.sgauto.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;

public class PrincipalController {

    @FXML private StackPane contentArea;
    @FXML private Label lblTituloPagina;
    @FXML private Label lblSubtituloPagina;

    @FXML private ToggleGroup menuLateral;

    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnOrdens;
    @FXML private ToggleButton btnClientes;
    @FXML private ToggleButton btnServicos;
    @FXML private ToggleButton btnPatioAtual;
    @FXML private ToggleButton btnEstoque;
    @FXML private ToggleButton btnCaixa;
    @FXML private ToggleButton btnConfiguracoes;

    @FXML
    public void initialize() {

        // Impedir nenhuma seleção no menu lateral
        menuLateral.selectedToggleProperty().addListener((obs, toggleAntigo, toggleNovo) -> {
            if (toggleNovo == null) {
                toggleAntigo.setSelected(true);
            }
        });

        irParaDashboard();
    }

    @FXML
    private void irParaDashboard() {
        mostrarTela("Dashboard", "Visão geral da oficina", montarPlaceholder("Tela de Dashboard em construção"));
    }

    @FXML
    private void irParaOrdens() {
        mostrarTela("Ordens de Serviço", "Acompanhe e gerencie as O.S. em andamento", montarPlaceholder("Tela de Ordens de Serviço em construção"));
    }

    @FXML
    private void irParaClientes() {
        mostrarTela("Clientes", "Cadastro e histórico de clientes", montarPlaceholder("Tela de Clientes em construção"));
    }

    @FXML
    private void irParaVeiculos() {
        mostrarTela("Veículos", "Cadastro e visualização de Veículos", montarPlaceholder("Tela de Veículos em construção"));
    }

    @FXML
    private void irParaServicos() {
        mostrarTela("Serviços", "Cadastro e visualização de serviços", montarPlaceholder("Tela de Serviços em construção"));
    }

    @FXML
    private void irParaPatioAtual() {
        mostrarTela("Pátio Atual", "Pátio atual da oficina", montarPlaceholder("Tela de Pátio Atual em construção"));
    }

    @FXML
    private void irParaEstoque() {
        mostrarTela("Estoque", "Peças e controle de estoque", montarPlaceholder("Tela de Estoque em construção"));
    }

    @FXML
    private void irParaCaixa() {
        mostrarTela("Caixa", "Contas a pagar, a receber e fluxo de caixa", montarPlaceholder("Tela de Caixa em construção"));
    }

    @FXML
    private void irParaConfiguracoes() {
        mostrarTela("Configurações", "Preferências do sistema", montarPlaceholder("Tela de Configurações em construção"));
    }

    private void mostrarTela(String titulo, String subtitulo, javafx.scene.Node conteudo) {
        lblTituloPagina.setText(titulo);
        lblSubtituloPagina.setText(subtitulo);
        contentArea.getChildren().setAll(conteudo);
    }

    private VBox montarPlaceholder(String texto) {
        VBox box = new VBox();
        box.setAlignment(javafx.geometry.Pos.CENTER);
        Label label = new Label(texto);
        label.getStyleClass().add("placeholder-text");
        box.getChildren().add(label);
        VBox.setVgrow(box, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }
}