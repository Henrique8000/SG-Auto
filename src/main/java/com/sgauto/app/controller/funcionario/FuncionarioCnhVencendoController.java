package com.sgauto.app.controller;

import com.sgauto.app.model.Funcionario;
import com.sgauto.app.service.FuncionarioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class FuncionarioCnhVencendoController {

    @FXML private ComboBox<String> cmbPeriodo;
    @FXML private Label lblContagem;
    @FXML private TableView<Funcionario> tabela;
    @FXML private TableColumn<Funcionario, String> colNome;
    @FXML private TableColumn<Funcionario, String> colCargo;
    @FXML private TableColumn<Funcionario, String> colCategoria;
    @FXML private TableColumn<Funcionario, String> colValidade;
    @FXML private TableColumn<Funcionario, Void> colSituacao;
    @FXML private VBox painelVazio;

    private final FuncionarioService funcionarioService;
    private final ObservableList<Funcionario> lista = FXCollections.observableArrayList();

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FuncionarioCnhVencendoController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomeCompleto()));
        colCargo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCargo() != null ? data.getValue().getCargo().getDescricao() : "-"));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCategoriaCnh() != null ? data.getValue().getCategoriaCnh() : "-"));
        colValidade.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getValidadeCnh().format(FORMATADOR)));

        colSituacao.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Funcionario f = getTableView().getItems().get(getIndex());
                boolean vencida = f.getValidadeCnh().isBefore(LocalDate.now());
                badge.setText(vencida ? "Vencida" : "A vencer");
                badge.getStyleClass().setAll("badge", vencida ? "badge-zerado" : "badge-baixo");
                setGraphic(badge);
            }
        });

        tabela.setItems(lista);

        cmbPeriodo.setItems(FXCollections.observableArrayList(
                "30 dias", "60 dias", "90 dias"));
        cmbPeriodo.getSelectionModel().select("30 dias");
        cmbPeriodo.valueProperty().addListener((obs, a, n) -> carregarDados());

        carregarDados();
    }

    private void carregarDados() {
        int dias = switch (cmbPeriodo.getValue()) {
            case "60 dias" -> 60;
            case "90 dias" -> 90;
            default -> 30;
        };

        LocalDate limite = LocalDate.now().plusDays(dias);

        List<Funcionario> filtrados = funcionarioService.listarNaoRemovidos().stream()
                .filter(f -> f.getValidadeCnh() != null)
                .filter(f -> !f.getValidadeCnh().isAfter(limite))
                .sorted((a, b) -> a.getValidadeCnh().compareTo(b.getValidadeCnh()))
                .toList();

        lista.setAll(filtrados);
        lblContagem.setText(filtrados.size() + " funcionário(s)");

        painelVazio.setVisible(filtrados.isEmpty());
        painelVazio.setManaged(filtrados.isEmpty());
        tabela.setVisible(!filtrados.isEmpty());
        tabela.setManaged(!filtrados.isEmpty());
    }
}