package com.sgauto.app.controller.clientes;

import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.ClientePF;
import com.sgauto.app.model.ClientePJ;
import com.sgauto.app.service.ClienteService;
import com.sgauto.app.util.ModalUtil;
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
import java.util.Comparator;
import java.util.List;

@Component
public class ClientesController {

    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalPF;
    @FXML private Label lblTotalPJ;
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colTipo;
    @FXML private TableColumn<Cliente, String> colDocumento;
    @FXML private TableColumn<Cliente, String> colCelular;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, Boolean> colStatus;
    @FXML private TableColumn<Cliente, Void> colAcoes;

    private final ApplicationContext applicationContext;
    private final ClienteService clienteService;

    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private List<Cliente> todosClientes = List.of();

    public ClientesController(ApplicationContext applicationContext, ClienteService clienteService) {
        this.applicationContext = applicationContext;
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltros();
        carregarDados();
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNome()));
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo()));
        colDocumento.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDocumento() == null ? "—" : data.getValue().getDocumentoFormatado()));
        colCelular.setCellValueFactory(data -> new SimpleStringProperty(formatarTelefone(data.getValue().getCelular())));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEmail() == null || data.getValue().getEmail().isBlank() ? "—" : data.getValue().getEmail()));

        // Status com badge (mesmo visual das outras telas)
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getAtivo()));
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
                Cliente cliente = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(cliente.getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaClientes.setItems(clientes);
    }

    private void configurarFiltros() {
        cmbFiltroTipo.setItems(FXCollections.observableArrayList(
                "Todos os tipos", "Pessoa Física", "Pessoa Jurídica"));
        cmbFiltroTipo.getSelectionModel().selectFirst();

        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        cmbFiltroTipo.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
    }

    private void carregarDados() {
        todosClientes = clienteService.listarTodos().stream()
                .sorted(Comparator.comparing(Cliente::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        aplicarFiltros();
        atualizarCards();
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String termoDigitos = termo.replaceAll("\\D", "");
        String tipoSelecionado = cmbFiltroTipo.getValue();

        List<Cliente> filtrados = todosClientes.stream()
                .filter(c -> termo.isBlank()
                        || c.getNome().toLowerCase().contains(termo)
                        || (c.getDocumento() != null && !termoDigitos.isEmpty()
                        && c.getDocumento().contains(termoDigitos)))
                .filter(c -> tipoSelecionado == null
                        || tipoSelecionado.equals("Todos os tipos")
                        || (tipoSelecionado.equals("Pessoa Física") && c instanceof ClientePF)
                        || (tipoSelecionado.equals("Pessoa Jurídica") && c instanceof ClientePJ))
                .toList();

        clientes.setAll(filtrados);
    }

    private void atualizarCards() {
        lblTotalClientes.setText(String.valueOf(todosClientes.size()));
        lblTotalPF.setText(String.valueOf(todosClientes.stream().filter(c -> c instanceof ClientePF).count()));
        lblTotalPJ.setText(String.valueOf(todosClientes.stream().filter(c -> c instanceof ClientePJ).count()));
    }

    @FXML
    private void abrirModalNovoCliente() {
        abrirModal(null);
    }

    private void abrirModal(Cliente clienteExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/clientes/cliente-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            ClienteFormController controller = loader.getController();
            controller.configurar(clienteExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, clienteExistente == null ? "Novo Cliente" : "Editar Cliente");
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de cliente", e);
        }
    }

    private void alternarStatus(Cliente cliente) {
        if (Boolean.TRUE.equals(cliente.getAtivo())) {
            clienteService.desativar(cliente.getId());
        } else {
            clienteService.ativar(cliente.getId());
        }
        carregarDados();
    }

    private void confirmarExclusao(Cliente cliente) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText(null);
        alert.setContentText("Deseja realmente excluir o cliente \"" + cliente.getNome() + "\"?\n"
                + "Se ele possui histórico, prefira Desativar.");

        alert.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try {
                    clienteService.excluir(cliente.getId());
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

    /** Formata 11 dígitos como (XX) XXXXX-XXXX e 10 como (XX) XXXX-XXXX. */
    private String formatarTelefone(String numero) {
        if (numero == null || numero.isBlank()) {
            return "—";
        }
        if (numero.length() == 11) {
            return "(" + numero.substring(0, 2) + ") " + numero.substring(2, 7) + "-" + numero.substring(7);
        }
        if (numero.length() == 10) {
            return "(" + numero.substring(0, 2) + ") " + numero.substring(2, 6) + "-" + numero.substring(6);
        }
        return numero;
    }
}