package com.sgauto.app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ClientesController {

    /*
     TODO: substituir por Cliente (entidade JPA) quando o backend for implementado
     */
    public record ClienteRow(String nome, String tipo, String documento, String celular, String email) {}

    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalPF;
    @FXML private Label lblTotalPJ;
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private TableView<ClienteRow> tabelaClientes;
    @FXML private TableColumn<ClienteRow, String> colNome;
    @FXML private TableColumn<ClienteRow, String> colTipo;
    @FXML private TableColumn<ClienteRow, String> colDocumento;
    @FXML private TableColumn<ClienteRow, String> colCelular;
    @FXML private TableColumn<ClienteRow, String> colEmail;
    @FXML private TableColumn<ClienteRow, Void> colAcoes;

    private final ApplicationContext applicationContext;
    private final ObservableList<ClienteRow> clientes = FXCollections.observableArrayList();

    /*
     Dados de exemplo só pra visualizar o layout de clientes
     TODO: remover quando ClienteService.listarTodos() existir
     */
    private final List<ClienteRow> todosClientes = new ArrayList<>(List.of(
            new ClienteRow("Six Sevenaldo da Silva Jr", "PF", "123.456.789-00", "(67) 99999-00067", "aura+ego@gmail.com"),
            new ClienteRow("Auto Peças Santos LTDA", "PJ", "12.345.678/0001-90", "(11) 3333-0002", "contato@apsantos.com.br"),
            new ClienteRow("Neymar Jr", "PF", "987.654.321-00", "(13) 98888-0003", "ney@gmail.com")
    ));

    public ClientesController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltros();
        carregarDados();
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nome()));
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().tipo()));
        colDocumento.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().documento()));
        colCelular.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().celular()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().email()));

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox container = new HBox(8, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnExcluir.getStyleClass().addAll("btn-table-action", "btn-table-danger");
                btnEditar.setOnAction(e -> abrirModal(getTableView().getItems().get(getIndex())));
                btnExcluir.setOnAction(e -> confirmarExclusao(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
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
        clientes.setAll(todosClientes);
        atualizarCards();
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String tipoSelecionado = cmbFiltroTipo.getValue();

        List<ClienteRow> filtrados = todosClientes.stream()
                .filter(c -> termo.isBlank()
                        || c.nome().toLowerCase().contains(termo)
                        || c.documento().replaceAll("\\D", "").contains(termo.replaceAll("\\D", "")))
                .filter(c -> tipoSelecionado == null
                        || tipoSelecionado.equals("Todos os tipos")
                        || (tipoSelecionado.equals("Pessoa Física") && c.tipo().equals("PF"))
                        || (tipoSelecionado.equals("Pessoa Jurídica") && c.tipo().equals("PJ")))
                .toList();

        clientes.setAll(filtrados);
    }

    private void atualizarCards() {
        lblTotalClientes.setText(String.valueOf(todosClientes.size()));
        lblTotalPF.setText(String.valueOf(todosClientes.stream().filter(c -> c.tipo().equals("PF")).count()));
        lblTotalPJ.setText(String.valueOf(todosClientes.stream().filter(c -> c.tipo().equals("PJ")).count()));
    }

    @FXML
    private void abrirModalNovoCliente() {
        abrirModal(null);
    }

    private void abrirModal(ClienteRow clienteExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/cliente-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            ClienteFormController controller = loader.getController();
            controller.configurar(clienteExistente, this::carregarDados);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(clienteExistente == null ? "Novo Cliente" : "Editar Cliente");
            modal.setScene(new javafx.scene.Scene(root));
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de cliente", e);
        }
    }

    private void confirmarExclusao(ClienteRow cliente) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText(null);
        alert.setContentText("Deseja realmente excluir o cliente \"" + cliente.nome() + "\"?");

        alert.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
            	
                // TODO: chamar ClienteService !!!
            	
                todosClientes.remove(cliente);
                carregarDados();
            }
        });
    }
}