package com.sgauto.app.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PrincipalController {

    @FXML private StackPane contentArea;
    @FXML private Label lblTituloPagina;
    @FXML private Label lblSubtituloPagina;

    @FXML private ToggleGroup menuLateral;

    private final ApplicationContext applicationContext;

    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnOrdens;
    @FXML private ToggleButton btnClientes;
    @FXML private ToggleButton btnServicos;
    @FXML private ToggleButton btnPatioAtual;
    @FXML private ToggleButton btnEstoque;
    @FXML private ToggleButton btnCaixa;
    @FXML private ToggleButton btnConfiguracoes;

    public PrincipalController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/clientes/clientes.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent tela = loader.load();
            mostrarTela("Clientes", "Cadastro e histórico de clientes", tela);
        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Clientes", e);
        }
    }

    @FXML
    private void irParaVeiculos() {
        mostrarTela("Veículos", "Cadastro e visualização de Veículos", montarPlaceholder("Tela de Veículos em construção"));
    }

    @FXML
    private void irParaCatalogoServicos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/servicos/catalogo-servico.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent tela = loader.load();
            mostrarTela("Catálogo de Serviços", "Serviços e categorias disponíveis para uso em Ordens de Serviço", tela);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Catálogo de Serviços", e);
        }
    }

    @FXML
    private void irParaPatioAtual() {
        mostrarTela("Pátio Atual", "Pátio atual da oficina", montarPlaceholder("Tela de Pátio Atual em construção"));
    }

    @FXML
    private void irParaEstoque() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/catalogo-estoque.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent tela = loader.load();
            mostrarTela("Estoque", "Peças e controle de estoque", tela);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Estoque", e);
        }
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