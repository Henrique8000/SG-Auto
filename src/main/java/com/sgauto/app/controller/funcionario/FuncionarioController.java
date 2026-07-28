package com.sgauto.app.controller;

import com.sgauto.app.controller.funcionario.FuncionarioFormController;
import com.sgauto.app.enums.CargoFuncionario;
import com.sgauto.app.enums.StatusFuncionario;
import com.sgauto.app.model.Funcionario;
import com.sgauto.app.service.FuncionarioService;
import com.sgauto.app.util.AutoCompleteComboBox;
import com.sgauto.app.util.ModalUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class FuncionarioController {

    @FXML private Label lblTotal;
    @FXML private Label lblAtivos;
    @FXML private Label lblAptosOs;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoRemovido;
    @FXML private ToggleButton btnAtivos;
    @FXML private ToggleButton btnRemovidos;
    @FXML private ComboBox<String> cmbFiltroStatus;
    @FXML private ComboBox<String> cmbFiltroCargo;
    @FXML private TableView<Funcionario> tabelaFuncionarios;
    @FXML private TableColumn<Funcionario, String> colMatricula;
    @FXML private TableColumn<Funcionario, String> colNome;
    @FXML private TableColumn<Funcionario, String> colCargo;
    @FXML private TableColumn<Funcionario, String> colContrato;
    @FXML private TableColumn<Funcionario, Void> colStatus;
    @FXML private TableColumn<Funcionario, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final FuncionarioService funcionarioService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Funcionario> funcionariosExibidos = FXCollections.observableArrayList();

    private List<Funcionario> todosFuncionarios = List.of();
    private AutoCompleteComboBox autoCompleteCargo;

    public FuncionarioController(FuncionarioService funcionarioService, ApplicationContext applicationContext) {
        this.funcionarioService = funcionarioService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        autoCompleteCargo = new AutoCompleteComboBox(cmbFiltroCargo);

        configurarColunas();
        configurarFiltros();
        carregarDados();
    }

    private void configurarFiltros() {
        List<String> statusOpcoes = new java.util.ArrayList<>();
        statusOpcoes.add("Todos os status");
        Arrays.stream(StatusFuncionario.values()).forEach(s -> statusOpcoes.add(s.getDescricao()));
        cmbFiltroStatus.setItems(FXCollections.observableArrayList(statusOpcoes));
        cmbFiltroStatus.getSelectionModel().selectFirst();

        List<String> cargoOpcoes = new java.util.ArrayList<>();
        cargoOpcoes.add("Todos os cargos");
        Arrays.stream(CargoFuncionario.values()).forEach(c -> cargoOpcoes.add(c.getDescricao()));
        autoCompleteCargo.definirItens(cargoOpcoes);
        cmbFiltroCargo.getSelectionModel().selectFirst();

        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbFiltroStatus.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbFiltroCargo.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        grupoRemovido.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else carregarDados();
        });
    }

    private void configurarColunas() {
        colMatricula.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getMatricula()));
        colNome.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNomeCompleto()));
        colCargo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCargo() != null ? data.getValue().getCargo().getDescricao() : "-"));
        colContrato.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTipoContrato() != null ? data.getValue().getTipoContrato().getDescricao() : "-"));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Funcionario f = getTableView().getItems().get(getIndex());
                StatusFuncionario status = f.getStatus();
                badge.setText(status != null ? status.getDescricao() : "-");
                badge.getStyleClass().setAll("badge", classeParaStatus(status));
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final Button btnRestaurar = new Button("Restaurar");
            private final HBox container = new HBox(6);
            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnExcluir.getStyleClass().add("btn-table-action");
                btnRestaurar.getStyleClass().add("btn-table-toggle-off");
                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnExcluir.setOnAction(e -> confirmarExclusao(getTableView().getItems().get(getIndex())));
                btnRestaurar.setOnAction(e -> restaurar(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Funcionario f = getTableView().getItems().get(getIndex());
                boolean removido = f.getRemovidoEm() != null;
                container.getChildren().setAll(removido ? List.of(btnRestaurar) : List.of(btnEditar, btnExcluir));
                setGraphic(container);
            }
        });

        tabelaFuncionarios.setItems(funcionariosExibidos);
    }

    private String classeParaStatus(StatusFuncionario status) {
        if (status == null) return "badge-normal";
        return switch (status) {
            case ATIVO -> "badge-active";
            case FERIAS, AFASTADO -> "badge-baixo";
            case INATIVO, DEMITIDO -> "badge-inactive";
        };
    }

    private void carregarDados() {
        boolean mostrarRemovidos = btnRemovidos.isSelected();

        todosFuncionarios = mostrarRemovidos
                ? funcionarioService.listarTodos().stream().filter(f -> f.getRemovidoEm() != null).toList()
                : funcionarioService.listarNaoRemovidos();

        atualizarCards();
        aplicarFiltros();
    }

    private void atualizarCards() {
        List<Funcionario> ativosNoSistema = funcionarioService.listarNaoRemovidos();
        lblTotal.setText(String.valueOf(ativosNoSistema.size()));

        long ativos = ativosNoSistema.stream().filter(f -> f.getStatus() == StatusFuncionario.ATIVO).count();
        lblAtivos.setText(String.valueOf(ativos));

        long aptosOs = funcionarioService.listarAptosParaOrdemServico().size();
        lblAptosOs.setText(String.valueOf(aptosOs));
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String statusSelecionado = cmbFiltroStatus.getValue();
        String cargoSelecionado = cmbFiltroCargo.getValue();

        List<Funcionario> filtrados = todosFuncionarios.stream()
                .filter(f -> termo.isBlank()
                        || f.getNomeCompleto().toLowerCase().contains(termo)
                        || f.getMatricula().toLowerCase().contains(termo)
                        || f.getCpf().contains(termo))
                .filter(f -> statusSelecionado == null || statusSelecionado.equals("Todos os status")
                        || (f.getStatus() != null && statusSelecionado.equals(f.getStatus().getDescricao())))
                .filter(f -> cargoSelecionado == null || cargoSelecionado.equals("Todos os cargos")
                        || (f.getCargo() != null && cargoSelecionado.equals(f.getCargo().getDescricao())))
                .toList();

        funcionariosExibidos.setAll(filtrados);
        lblContagem.setText(filtrados.size() + " resultado(s)");
        atualizarEstadoVazio(filtrados.isEmpty());
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaFuncionarios.setVisible(!vazio);
        tabelaFuncionarios.setManaged(!vazio);
    }

    @FXML
    private void abrirModalNovoFuncionario() {
        abrirModal(null);
    }

    private void abrirModalEdicao(Funcionario funcionario) {
        abrirModal(funcionario);
    }

    private void abrirModal(Funcionario funcionarioExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/funcionario/funcionario-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            FuncionarioFormController controller = loader.getController();
            controller.configurar(funcionarioExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, funcionarioExistente == null ? "Novo Funcionário" : "Editar Funcionário",
                    tabelaFuncionarios.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de funcionário", e);
        }
    }

    private void confirmarExclusao(Funcionario funcionario) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente excluir \"" + funcionario.getNomeCompleto() + "\"? "
                + "O funcionário poderá ser restaurado depois, se necessário.");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                funcionarioService.remover(funcionario.getId());
                carregarDados();
            }
        });
    }

    private void restaurar(Funcionario funcionario) {
        funcionarioService.cancelarRemocao(funcionario.getId());
        carregarDados();
    }

    @FXML
    private void abrirModalAvancadas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/funcionario/funcionario-configuracoes-avancadas-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage modal = ModalUtil.abrir(root, "Configurações Avançadas — Funcionários",
                    tabelaFuncionarios.getScene().getWindow());
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir configurações avançadas", e);
        }
    }
}