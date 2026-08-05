package com.sgauto.app.controller.patio;

import com.sgauto.app.model.patio.TabelaPrecoPatio;
import com.sgauto.app.service.TabelaPrecoPatioService;
import com.sgauto.app.util.ModalUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Component
public class TarifaPatioTabController {

    @FXML private Label lblTotalTarifas;
    @FXML private Label lblTarifasAtivas;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivas;
    @FXML private ToggleButton btnStatusInativas;
    @FXML private TableView<TabelaPrecoPatio> tabelaTarifas;
    @FXML private TableColumn<TabelaPrecoPatio, String> colDescricao;
    @FXML private TableColumn<TabelaPrecoPatio, String> colCategoria;
    @FXML private TableColumn<TabelaPrecoPatio, String> colValorDiaria;
    @FXML private TableColumn<TabelaPrecoPatio, String> colCarencia;
    @FXML private TableColumn<TabelaPrecoPatio, Void> colStatus;
    @FXML private TableColumn<TabelaPrecoPatio, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final TabelaPrecoPatioService tabelaPrecoPatioService;
    private final ApplicationContext applicationContext;
    private final ObservableList<TabelaPrecoPatio> tarifas = FXCollections.observableArrayList();

    public TarifaPatioTabController(TabelaPrecoPatioService tabelaPrecoPatioService, ApplicationContext applicationContext) {
        this.tabelaPrecoPatioService = tabelaPrecoPatioService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();

        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltro());
        grupoStatus.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else aplicarFiltro();
        });

        carregarDados();
    }

    private void configurarColunas() {
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCategoria.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                descreverCategoria(data.getValue())));
        colValorDiaria.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                formatarMoeda(data.getValue().getValorDiaria())));
        colCarencia.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getDiasCarencia())));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                TabelaPrecoPatio t = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(t.getAtivo());
                badge.setText(ativo ? "Ativa" : "Inativa");
                badge.getStyleClass().setAll("badge", ativo ? "badge-active" : "badge-inactive");
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final Button btnExcluir = new Button("Excluir");
            private final HBox container = new HBox(6, btnEditar, btnToggle, btnExcluir);
            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnExcluir.getStyleClass().add("btn-table-action");
                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> alternarStatus(getTableView().getItems().get(getIndex())));
                btnExcluir.setOnAction(e -> confirmarExclusao(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                TabelaPrecoPatio t = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(t.getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaTarifas.setItems(tarifas);
    }

    private String descreverCategoria(TabelaPrecoPatio t) {
        return switch (t.getCategoria()) {
            case MOTO -> "Moto";
            case PASSEIO -> "Passeio";
            case SUV_CAMINHONETE -> "SUV/Caminhonete";
            case PESADO -> "Pesado";
            case OUTROS -> "Outros";
        };
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor == null ? "R$ 0,00" : String.format("R$ %,.2f", valor);
    }

    private void carregarDados() {
        List<TabelaPrecoPatio> todas = tabelaPrecoPatioService.listarTodas();
        lblTotalTarifas.setText(String.valueOf(todas.size()));
        long ativas = todas.stream().filter(t -> Boolean.TRUE.equals(t.getAtivo())).count();
        lblTarifasAtivas.setText(String.valueOf(ativas));
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        Toggle statusSelecionado = grupoStatus.getSelectedToggle();

        List<TabelaPrecoPatio> filtradas = tabelaPrecoPatioService.listarTodas().stream()
                .filter(t -> termo.isBlank() || t.getDescricao().toLowerCase().contains(termo))
                .filter(t -> {
                    if (statusSelecionado == btnStatusAtivas) return Boolean.TRUE.equals(t.getAtivo());
                    if (statusSelecionado == btnStatusInativas) return Boolean.FALSE.equals(t.getAtivo());
                    return true;
                })
                .toList();

        tarifas.setAll(filtradas);
        lblContagem.setText(filtradas.size() + " resultado(s)");
        atualizarEstadoVazio(filtradas.isEmpty());
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaTarifas.setVisible(!vazio);
        tabelaTarifas.setManaged(!vazio);
    }

    @FXML
    private void abrirModalNovaTarifa() {
        abrirModal(null);
    }

    private void abrirModalEdicao(TabelaPrecoPatio tarifa) {
        abrirModal(tarifa);
    }

    private void abrirModal(TabelaPrecoPatio tarifaExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/patio/tarifa-patio-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            TarifaPatioFormController controller = loader.getController();
            controller.configurar(tarifaExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, tarifaExistente == null ? "Nova Tarifa" : "Editar Tarifa",
                    tabelaTarifas.getScene().getWindow());
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de tarifa", e);
        }
    }

    private void alternarStatus(TabelaPrecoPatio tarifa) {
        try {
            if (Boolean.TRUE.equals(tarifa.getAtivo())) tabelaPrecoPatioService.desativar(tarifa.getId());
            else tabelaPrecoPatioService.ativar(tarifa.getId());
            carregarDados();
        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não é possível alterar", e.getMessage());
        }
    }

    private void confirmarExclusao(TabelaPrecoPatio tarifa) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente excluir a tarifa \"" + tarifa.getDescricao() + "\"?");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try {
                    tabelaPrecoPatioService.excluir(tarifa.getId());
                    carregarDados();
                } catch (IllegalStateException e) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Não é possível excluir", e.getMessage());
                }
            }
        });
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}