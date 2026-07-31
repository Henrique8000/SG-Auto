package com.sgauto.app.controller.veiculos;

import com.sgauto.app.model.Veiculo;
import com.sgauto.app.service.ModeloService;
import com.sgauto.app.service.VeiculoService;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class VeiculosController {

    @FXML private Label lblTotalVeiculos;
    @FXML private Label lblTotalAtivos;
    @FXML private Label lblTotalModelos;
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroModelo;
    @FXML private ComboBox<String> cmbFiltroStatus;
    @FXML private TableView<Veiculo> tabelaVeiculos;
    @FXML private TableColumn<Veiculo, String> colPlaca;
    @FXML private TableColumn<Veiculo, String> colMarca;
    @FXML private TableColumn<Veiculo, String> colModelo;
    @FXML private TableColumn<Veiculo, String> colAno;
    @FXML private TableColumn<Veiculo, String> colKm;
    @FXML private TableColumn<Veiculo, String> colDono;
    @FXML private TableColumn<Veiculo, Boolean> colStatus;
    @FXML private TableColumn<Veiculo, Void> colAcoes;

    private final ApplicationContext applicationContext;
    private final VeiculoService veiculoService;
    private final ModeloService modeloService;

    private final ObservableList<Veiculo> veiculos = FXCollections.observableArrayList();
    private List<Veiculo> todosVeiculos = List.of();

    public VeiculosController(ApplicationContext applicationContext,
                              VeiculoService veiculoService,
                              ModeloService modeloService) {
        this.applicationContext = applicationContext;
        this.veiculoService = veiculoService;
        this.modeloService = modeloService;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltros();
        carregarDados();
    }

    private void configurarColunas() {
        colPlaca.setCellValueFactory(d -> new SimpleStringProperty(formatarPlaca(d.getValue().getPlaca())));
        colMarca.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMarca()));
        colModelo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getModelo()));
        colAno.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getAno() == null ? "—" : String.valueOf(d.getValue().getAno())));
        colKm.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getKm() == null ? "—" : String.format("%,d", d.getValue().getKm())));
        colDono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCliente().getNome()));

        colStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getAtivo()));
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
            private final Button btnToggle = new Button();
            private final Button btnExcluir = new Button("Excluir");
            private final HBox container = new HBox(8, btnEditar, btnToggle, btnExcluir);

            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnExcluir.getStyleClass().add("btn-table-delete");
                btnEditar.setOnAction(e -> abrirModal(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> alternarStatus(getTableView().getItems().get(getIndex())));
                btnExcluir.setOnAction(e -> confirmarExclusao(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Veiculo veiculo = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(veiculo.getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaVeiculos.setItems(veiculos);
    }

    private void configurarFiltros() {
        cmbFiltroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "Ativos", "Inativos"));
        cmbFiltroStatus.getSelectionModel().selectFirst();

        List<String> modelos = new ArrayList<>();
        modelos.add("Todos os modelos");
        modeloService.listarAtivas().forEach(m -> modelos.add(m.getNome()));
        cmbFiltroModelo.setItems(FXCollections.observableArrayList(modelos));
        cmbFiltroModelo.getSelectionModel().selectFirst();

        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        cmbFiltroModelo.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        cmbFiltroStatus.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
    }

    private void carregarDados() {
        todosVeiculos = veiculoService.listarTodos().stream()
                .sorted(Comparator.comparing(Veiculo::getPlaca, String.CASE_INSENSITIVE_ORDER))
                .toList();
        aplicarFiltros();
        atualizarCards();
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String modeloSelecionado = cmbFiltroModelo.getValue();
        String statusSelecionado = cmbFiltroStatus.getValue();

        List<Veiculo> filtrados = todosVeiculos.stream()
                .filter(v -> termo.isBlank()
                        || v.getPlaca().toLowerCase().contains(termo)
                        || v.getModelo().toLowerCase().contains(termo)
                        || v.getMarca().toLowerCase().contains(termo)
                        || v.getCliente().getNome().toLowerCase().contains(termo))
                .filter(v -> modeloSelecionado == null
                        || modeloSelecionado.equals("Todos os modelos")
                        || modeloSelecionado.equals(v.getModelo()))
                .filter(v -> statusSelecionado == null
                        || statusSelecionado.equals("Todos os status")
                        || (statusSelecionado.equals("Ativos") && Boolean.TRUE.equals(v.getAtivo()))
                        || (statusSelecionado.equals("Inativos") && Boolean.FALSE.equals(v.getAtivo())))
                .toList();

        veiculos.setAll(filtrados);
    }

    private void atualizarCards() {
        lblTotalVeiculos.setText(String.valueOf(todosVeiculos.size()));
        lblTotalAtivos.setText(String.valueOf(
                todosVeiculos.stream().filter(v -> Boolean.TRUE.equals(v.getAtivo())).count()));
        lblTotalModelos.setText(String.valueOf(
                todosVeiculos.stream().map(Veiculo::getModelo).distinct().count()));
    }

    @FXML
    private void abrirModalNovoVeiculo() {
        abrirModal(null);
    }

    private void abrirModal(Veiculo veiculoExistente) {
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

    private void alternarStatus(Veiculo veiculo) {
        if (Boolean.TRUE.equals(veiculo.getAtivo())) {
            veiculoService.desativar(veiculo.getId());
        } else {
            veiculoService.ativar(veiculo.getId());
        }
        carregarDados();
    }

    private void confirmarExclusao(Veiculo veiculo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText(null);
        alert.setContentText("Deseja realmente excluir o veículo de placa \"" + formatarPlaca(veiculo.getPlaca()) + "\"?\n"
                + "Se ele possui histórico, prefira Desativar.");

        alert.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try {
                    veiculoService.excluir(veiculo.getId());
                } catch (IllegalStateException e) {
                    Alert erro = new Alert(Alert.AlertType.WARNING);
                    erro.setTitle("Exclusão não permitida");
                    erro.setHeaderText(null);
                    erro.setContentText(e.getMessage());
                    erro.showAndWait();
                }
                carregarDados();
            }
        });
    }

    /** Exibe a placa com hífen: ABC-1234 (antiga) ou ABC-1D23 (Mercosul). */
    private String formatarPlaca(String placa) {
        if (placa == null || placa.length() != 7) {
            return placa;
        }
        return placa.substring(0, 3) + "-" + placa.substring(3);
    }
}