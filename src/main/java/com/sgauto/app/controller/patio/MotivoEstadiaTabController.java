package com.sgauto.app.controller.patio;

import com.sgauto.app.model.patio.MotivoEstadia;
import com.sgauto.app.service.MotivoEstadiaService;
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
import java.util.List;

@Component
public class MotivoEstadiaTabController {

    @FXML private Label lblTotalMotivos;
    @FXML private Label lblMotivosAtivos;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivos;
    @FXML private ToggleButton btnStatusInativos;
    @FXML private TableView<MotivoEstadia> tabelaMotivos;
    @FXML private TableColumn<MotivoEstadia, String> colNome;
    @FXML private TableColumn<MotivoEstadia, String> colDescricao;
    @FXML private TableColumn<MotivoEstadia, Void> colProtegido;
    @FXML private TableColumn<MotivoEstadia, Void> colStatus;
    @FXML private TableColumn<MotivoEstadia, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final MotivoEstadiaService motivoEstadiaService;
    private final ApplicationContext applicationContext;
    private final ObservableList<MotivoEstadia> motivos = FXCollections.observableArrayList();

    public MotivoEstadiaTabController(MotivoEstadiaService motivoEstadiaService, ApplicationContext applicationContext) {
        this.motivoEstadiaService = motivoEstadiaService;
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

        colProtegido.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                MotivoEstadia m = getTableView().getItems().get(getIndex());
                boolean protegido = Boolean.TRUE.equals(m.getProtegido());
                badge.setText(protegido ? "Sim" : "Não");
                badge.getStyleClass().setAll("badge", protegido ? "badge-active" : "chip-neutral");
                setGraphic(protegido ? badge : null);
            }
        });

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                MotivoEstadia m = getTableView().getItems().get(getIndex());
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
                MotivoEstadia m = getTableView().getItems().get(getIndex());
                boolean protegido = Boolean.TRUE.equals(m.getProtegido());
                boolean ativo = Boolean.TRUE.equals(m.getAtivo());

                // Motivo protegido não pode ser editado, ativado/desativado, nem excluído pela tela.
                btnEditar.setDisable(protegido);
                btnToggle.setDisable(protegido);
                btnExcluir.setDisable(protegido);
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaMotivos.setItems(motivos);
    }

    private void carregarDados() {
        List<MotivoEstadia> todos = motivoEstadiaService.listarTodos();
        lblTotalMotivos.setText(String.valueOf(todos.size()));
        long ativos = todos.stream().filter(m -> Boolean.TRUE.equals(m.getAtivo())).count();
        lblMotivosAtivos.setText(String.valueOf(ativos));
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        Toggle statusSelecionado = grupoStatus.getSelectedToggle();

        List<MotivoEstadia> filtrados = motivoEstadiaService.listarTodos().stream()
                .filter(m -> termo.isBlank() || m.getNome().toLowerCase().contains(termo))
                .filter(m -> {
                    if (statusSelecionado == btnStatusAtivos) return Boolean.TRUE.equals(m.getAtivo());
                    if (statusSelecionado == btnStatusInativos) return Boolean.FALSE.equals(m.getAtivo());
                    return true;
                })
                .toList();

        motivos.setAll(filtrados);
        lblContagem.setText(filtrados.size() + " resultado(s)");
        atualizarEstadoVazio(filtrados.isEmpty());
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaMotivos.setVisible(!vazio);
        tabelaMotivos.setManaged(!vazio);
    }

    @FXML
    private void abrirModalNovoMotivo() {
        abrirModal(null);
    }

    private void abrirModalEdicao(MotivoEstadia motivo) {
        abrirModal(motivo);
    }

    private void abrirModal(MotivoEstadia motivoExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/patio/motivo-estadia-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            MotivoEstadiaFormController controller = loader.getController();
            controller.configurar(motivoExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, motivoExistente == null ? "Novo Motivo" : "Editar Motivo",
                    tabelaMotivos.getScene().getWindow());
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de motivo", e);
        }
    }

    private void alternarStatus(MotivoEstadia motivo) {
        try {
            if (Boolean.TRUE.equals(motivo.getAtivo())) motivoEstadiaService.desativar(motivo.getId());
            else motivoEstadiaService.ativar(motivo.getId());
            carregarDados();
        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não é possível alterar", e.getMessage());
        }
    }

    private void confirmarExclusao(MotivoEstadia motivo) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente excluir o motivo \"" + motivo.getNome() + "\"?");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try {
                    motivoEstadiaService.excluir(motivo.getId());
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