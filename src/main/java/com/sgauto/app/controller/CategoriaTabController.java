package com.sgauto.app.controller;

import com.sgauto.app.model.Categoria;
import com.sgauto.app.service.CategoriaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CategoriaTabController {

    @FXML private Label lblTotalCategorias;
    @FXML private Label lblCategoriasAtivas;
    @FXML private TextField txtBusca;
    @FXML private TableView<Categoria> tabelaCategorias;
    @FXML private TableColumn<Categoria, String> colNome;
    @FXML private TableColumn<Categoria, String> colTipo;
    @FXML private TableColumn<Categoria, String> colDescricao;
    @FXML private TableColumn<Categoria, Void> colStatus;
    @FXML private TableColumn<Categoria, Void> colAcoes;

    private final CategoriaService categoriaService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    public CategoriaTabController(CategoriaService categoriaService, ApplicationContext applicationContext) {
        this.categoriaService = categoriaService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        carregarDados();
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Categoria c = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(c.getAtivo());
                badge.setText(ativo ? "Ativo" : "Inativo");
                badge.getStyleClass().setAll("badge", ativo ? "badge-active" : "badge-inactive");
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final HBox container = new HBox(8, btnEditar, btnToggle);
            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> alternarStatus(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Categoria c = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(c.getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaCategorias.setItems(categorias);
    }

    private void carregarDados() {
        List<Categoria> todas = categoriaService.listarTodas();
        categorias.setAll(todas);
        atualizarCards(todas);
        aplicarFiltro();
    }

    private void atualizarCards(List<Categoria> todas) {
        lblTotalCategorias.setText(String.valueOf(todas.size()));
        long ativas = todas.stream().filter(c -> Boolean.TRUE.equals(c.getAtivo())).count();
        lblCategoriasAtivas.setText(String.valueOf(ativas));
    }

    private void aplicarFiltro() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        List<Categoria> filtradas = categoriaService.listarTodas().stream()
                .filter(c -> termo.isBlank() || c.getNome().toLowerCase().contains(termo))
                .toList();
        categorias.setAll(filtradas);
    }

    @FXML
    private void abrirModalNovaCategoria() {
        abrirModal(null);
    }

    private void abrirModalEdicao(Categoria categoria) {
        abrirModal(categoria);
    }

    private void abrirModal(Categoria categoriaExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/categoria-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            CategoriaFormController controller = loader.getController();
            controller.configurar(categoriaExistente, this::carregarDados);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(categoriaExistente == null ? "Nova Categoria" : "Editar Categoria");
            modal.setScene(new javafx.scene.Scene(root));
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de categoria", e);
        }
    }

    private void alternarStatus(Categoria categoria) {
        try {
            if (Boolean.TRUE.equals(categoria.getAtivo())) {
                categoriaService.desativar(categoria.getId());
            } else {
                categoriaService.ativar(categoria.getId());
            }
            carregarDados();
        } catch (IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Não é possível desativar");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}