package com.sgauto.app.controller;

import com.sgauto.app.model.Servico;
import com.sgauto.app.service.CategoriaService;
import com.sgauto.app.service.ServicoService;
import javafx.beans.property.SimpleStringProperty;
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
import java.math.BigDecimal;
import java.util.List;

@Component
public class ServicoTabController {

    @FXML private Label lblTotalServicos;
    @FXML private Label lblServicosAtivos;
    @FXML private Label lblValorMedio;
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colCodigo;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colCategoria;
    @FXML private TableColumn<Servico, String> colValor;
    @FXML private TableColumn<Servico, Void> colStatus;
    @FXML private TableColumn<Servico, Void> colAcoes;

    private final ServicoService servicoService;
    private final CategoriaService categoriaService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Servico> servicos = FXCollections.observableArrayList();

    public ServicoTabController(ServicoService servicoService, CategoriaService categoriaService,
                                ApplicationContext applicationContext) {
        this.servicoService = servicoService;
        this.categoriaService = categoriaService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        carregarDados();
    }

    private void configurarColunas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colValor.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getValor())));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Servico s = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(s.getAtivo());
                badge.setText(ativo ? "Ativo" : "Inativo");
                badge.getStyleClass().setAll("badge", ativo ? "badge-active" : "badge-inactive");
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final Button btnExcluir = new Button("Excluir");

            private final HBox container = new HBox(8, btnEditar, btnToggle, btnExcluir);

            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> alternarStatus(getTableView().getItems().get(getIndex())));
                btnExcluir.getStyleClass().add("btn-table-delete");
                btnExcluir.setOnAction(e -> excluirServico(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Servico s = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(s.getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaServicos.setItems(servicos);
    }

    private void carregarDados() {
        List<Servico> todos = servicoService.listarTodos();
        servicos.setAll(todos);
        atualizarCards(todos);
        atualizarComboCategorias();
        aplicarFiltros();
    }

    private void atualizarCards(List<Servico> todos) {
        lblTotalServicos.setText(String.valueOf(todos.size()));

        long ativos = todos.stream().filter(s -> Boolean.TRUE.equals(s.getAtivo())).count();
        lblServicosAtivos.setText(String.valueOf(ativos));

        BigDecimal media = todos.isEmpty() ? BigDecimal.ZERO :
                todos.stream().map(Servico::getValor).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(todos.size()), 2, java.math.RoundingMode.HALF_UP);
        lblValorMedio.setText(formatarMoeda(media));
    }

    private void atualizarComboCategorias() {
        String selecionadoAntes = cmbFiltroCategoria.getValue();
        ObservableList<String> opcoes = FXCollections.observableArrayList();
        opcoes.add("Todas as categorias");
        opcoes.addAll(categoriaService.listarAtivas().stream().map(c -> c.getNome()).toList());
        cmbFiltroCategoria.setItems(opcoes);

        if (selecionadoAntes != null && opcoes.contains(selecionadoAntes)) {
            cmbFiltroCategoria.setValue(selecionadoAntes);
        } else {
            cmbFiltroCategoria.getSelectionModel().selectFirst();
        }
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String categoriaSelecionada = cmbFiltroCategoria.getValue();

        List<Servico> filtrados = servicoService.listarTodos().stream()
                .filter(s -> termo.isBlank()
                        || s.getCodigo().toLowerCase().contains(termo)
                        || s.getNome().toLowerCase().contains(termo))
                .filter(s -> categoriaSelecionada == null
                        || categoriaSelecionada.equals("Todas as categorias")
                        || categoriaSelecionada.equals(s.getCategoria()))
                .toList();

        servicos.setAll(filtrados);
    }

    @FXML
    private void abrirModalNovoServico() {
        abrirModal(null);
    }

    private void abrirModalEdicao(Servico servico) {
        abrirModal(servico);
    }

    private void abrirModal(Servico servicoExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/servico-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            ServicoFormController controller = loader.getController();
            controller.configurar(servicoExistente, this::carregarDados);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(servicoExistente == null ? "Novo Serviço" : "Editar Serviço");
            modal.setScene(new javafx.scene.Scene(root));
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de serviço", e);
        }
    }

    private void alternarStatus(Servico servico) {
        if (Boolean.TRUE.equals(servico.getAtivo())) {
            servicoService.desativar(servico.getId());
        } else {
            servicoService.ativar(servico.getId());
        }
        carregarDados();
    }

    private String formatarMoeda(BigDecimal valor) {
        return String.format("R$ %,.2f", valor);
    }

    private void excluirServico(Servico servico) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Exclusão");
        alerta.setHeaderText("Deseja realmente excluir o serviço: " + servico.getNome() + "?");
        alerta.setContentText("Esta ação não poderá ser desfeita.");

        alerta.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    servicoService.deletar(servico.getId());
                    carregarDados();
                } catch (Exception e) {
                    Alert erro = new Alert(Alert.AlertType.ERROR);
                    erro.setTitle("Erro na Exclusão");
                    erro.setHeaderText("Não foi possível excluir o serviço.");
                    erro.setContentText("O serviço pode estar vinculado a ordens de serviço existentes.");
                    erro.showAndWait();
                }
            }
        });
    }
}