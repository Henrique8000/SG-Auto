package com.sgauto.app.controller.veiculos;

import com.sgauto.app.util.ModalUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class VeiculosController {

    @FXML private Label lblTotalVeiculos;
    @FXML private Label lblTotalAtivos;
    @FXML private Label lblTotalModelos;
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroModelo;
    @FXML private ComboBox<String> cmbFiltroStatus;
    @FXML private TableView<Object> tabelaVeiculos;
    @FXML private TableColumn<Object, String> colPlaca;
    @FXML private TableColumn<Object, String> colMarca;
    @FXML private TableColumn<Object, String> colModelo;
    @FXML private TableColumn<Object, String> colAno;
    @FXML private TableColumn<Object, String> colKm;
    @FXML private TableColumn<Object, String> colDono;
    @FXML private TableColumn<Object, Boolean> colStatus;
    @FXML private TableColumn<Object, Void> colAcoes;

    private final ApplicationContext applicationContext;

    // TODO(backend): injetar VeiculoService e ModeloService aqui,
    //  trocar o tipo da tabela de Object para Veiculo.
    private final ObservableList<Object> veiculos = FXCollections.observableArrayList();

    public VeiculosController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltros();
        carregarDados();
    }

    private void configurarColunas() {
        // TODO(backend): mapear cada coluna para os getters de Veiculo.
        //  Ex.: colPlaca.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPlaca()));
        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Boolean ativo, boolean empty) {
                super.updateItem(ativo, empty);
                if (empty || ativo == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(ativo ? "Ativo" : "Inativo");
                badge.getStyleClass().setAll("badge", ativo ? "badge-active" : "badge-inactive");
                setGraphic(badge);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button("Desativar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox container = new HBox(8, btnEditar, btnToggle, btnExcluir);

            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnToggle.getStyleClass().add("btn-table-toggle-on");
                btnExcluir.getStyleClass().add("btn-table-delete");
                btnEditar.setOnAction(e -> abrirModal(getTableView().getItems().get(getIndex())));
                // TODO(backend): ligar btnToggle e btnExcluir ao VeiculoService.
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tabelaVeiculos.setItems(veiculos);
    }

    private void configurarFiltros() {
        cmbFiltroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "Ativos", "Inativos"));
        cmbFiltroStatus.getSelectionModel().selectFirst();

        // TODO(backend): popular cmbFiltroModelo com "Todos os modelos" + modeloService.listarAtivas().
        cmbFiltroModelo.setItems(FXCollections.observableArrayList("Todos os modelos"));
        cmbFiltroModelo.getSelectionModel().selectFirst();

        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        cmbFiltroModelo.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        cmbFiltroStatus.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
    }

    private void carregarDados() {
        // TODO(backend): veiculos.setAll(veiculoService.listarTodos()); depois aplicarFiltros() e atualizarCards().
        aplicarFiltros();
        atualizarCards();
    }

    private void aplicarFiltros() {
        // TODO(backend): replicar a cadeia de filtros de ClientesController
        //  (busca por placa/modelo/dono + filtro de modelo + filtro de status).
    }

    private void atualizarCards() {
        lblTotalVeiculos.setText(String.valueOf(veiculos.size()));
        // TODO(backend): contar ativos e modelos distintos a partir da lista real.
        lblTotalAtivos.setText("0");
        lblTotalModelos.setText("0");
    }

    @FXML
    private void abrirModalNovoVeiculo() {
        abrirModal(null);
    }

    private void abrirModal(Object veiculoExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/veiculos/veiculo-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            VeiculoFormController controller = loader.getController();
            controller.configurar(veiculoExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, veiculoExistente == null ? "Novo Veículo" : "Editar Veículo");
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de veículo", e);
        }
    }
}