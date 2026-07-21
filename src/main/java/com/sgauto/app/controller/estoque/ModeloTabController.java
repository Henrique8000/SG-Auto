package com.sgauto.app.controller.estoque;

import com.sgauto.app.model.Modelo;
import com.sgauto.app.service.ModeloService;
import com.sgauto.app.util.ModalUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ModeloTabController {

    @FXML private Label lblTotalModelos;
    @FXML private Label lblModelosAtivos;
    @FXML private Label lblModelosEmUso;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivos;
    @FXML private ToggleButton btnStatusInativos;
    @FXML private TableView<Modelo> tabelaModelos;
    @FXML private TableColumn<Modelo, String> colNome;
    @FXML private TableColumn<Modelo, String> colDescricao;
    @FXML private TableColumn<Modelo, Void> colUso;
    @FXML private TableColumn<Modelo, Void> colStatus;
    @FXML private TableColumn<Modelo, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final ModeloService modeloService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Modelo> modelos = FXCollections.observableArrayList();

    public ModeloTabController(ModeloService modeloService, ApplicationContext applicationContext) {
        this.modeloService = modeloService;
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
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        colUso.setCellFactory(coluna -> new TableCell<>() {
            private final Circle dot = new Circle(4);
            private final Label texto = new Label();
            private final HBox container = new HBox(6, dot, texto);
            { container.setAlignment(Pos.CENTER_LEFT); }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Modelo m = getTableView().getItems().get(getIndex());
                boolean emUso = modeloService.estaEmUso(m.getNome());
                dot.getStyleClass().setAll("dot-indicator", emUso ? "dot-em-uso" : "dot-livre");
                texto.setText(emUso ? "Em uso" : "Livre");
                texto.getStyleClass().setAll(emUso ? "form-label" : "results-count");
                setGraphic(container);
            }
        });

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Modelo m = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(m.getAtivo());
                badge.setText(ativo ? "Ativo" : "Inativo");
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
                Modelo m = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(m.getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaModelos.setItems(modelos);
    }

    private void carregarDados() {
        List<Modelo> todos = modeloService.listarTodas();
        atualizarCards(todos);
        aplicarFiltro();
    }

    private void atualizarCards(List<Modelo> todos) {
        lblTotalModelos.setText(String.valueOf(todos.size()));
        long ativos = todos.stream().filter(m -> Boolean.TRUE.equals(m.getAtivo())).count();
        lblModelosAtivos.setText(String.valueOf(ativos));
        long emUso = todos.stream().filter(m -> modeloService.estaEmUso(m.getNome())).count();
        lblModelosEmUso.setText(String.valueOf(emUso));
    }

    private void aplicarFiltro() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        Toggle statusSelecionado = grupoStatus.getSelectedToggle();

        List<Modelo> filtrados = modeloService.listarTodas().stream()
                .filter(m -> termo.isBlank() || m.getNome().toLowerCase().contains(termo))
                .filter(m -> {
                    if (statusSelecionado == btnStatusAtivos) return Boolean.TRUE.equals(m.getAtivo());
                    if (statusSelecionado == btnStatusInativos) return Boolean.FALSE.equals(m.getAtivo());
                    return true;
                })
                .toList();

        modelos.setAll(filtrados);
        lblContagem.setText(filtrados.size() + " resultado(s)");
        atualizarEstadoVazio(filtrados.isEmpty());
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaModelos.setVisible(!vazio);
        tabelaModelos.setManaged(!vazio);
    }

    @FXML
    private void abrirModalNovoModelo() {
        abrirModal(null);
    }

    private void abrirModalEdicao(Modelo modelo) {
        abrirModal(modelo);
    }

    private void abrirModal(Modelo modeloExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/modelo-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            ModeloFormController controller = loader.getController();
            controller.configurar(modeloExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, modeloExistente == null ? "Novo Modelo" : "Editar Modelo",
                    tabelaModelos.getScene().getWindow());
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de modelo", e);
        }
    }

    private void alternarStatus(Modelo modelo) {
        try {
            if (Boolean.TRUE.equals(modelo.getAtivo())) {
                modeloService.desativar(modelo.getId());
            } else {
                modeloService.ativar(modelo.getId());
            }
            carregarDados();
        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não é possível desativar", e.getMessage());
        }
    }

    private void confirmarExclusao(Modelo modelo) {
        if (modeloService.estaEmUso(modelo.getNome())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não é possível excluir",
                    "Este modelo possui peças associadas. Reatribua ou remova as peças antes de excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente excluir o modelo \"" + modelo.getNome() + "\"?");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try {
                    modeloService.excluir(modelo.getId());
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