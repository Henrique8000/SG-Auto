package com.sgauto.app.controller;

import com.sgauto.app.model.Peca;
import com.sgauto.app.service.EstoqueService;
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
public class EstoqueController {

    @FXML private Label lblTotalItens;
    @FXML private Label lblEstoqueBaixo;
    @FXML private Label lblValorEstoque;
    @FXML private TextField txtBusca;
    @FXML private TableView<Peca> tabelaPecas;
    @FXML private TableColumn<Peca, String> colCodigo;
    @FXML private TableColumn<Peca, String> colDescricao;
    @FXML private TableColumn<Peca, String> colPrecoCusto;
    @FXML private TableColumn<Peca, String> colPrecoVenda;
    @FXML private TableColumn<Peca, String> colEstoque;
    @FXML private TableColumn<Peca, Void> colAcoes;
    @FXML private TableColumn<Peca, String> colModelo;
    @FXML private ComboBox<String> cmbFiltroModelo;

    private final EstoqueService estoqueService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Peca> pecas = FXCollections.observableArrayList();

    public EstoqueController(EstoqueService estoqueService, ApplicationContext applicationContext) {
        this.estoqueService = estoqueService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarBusca();
        carregarDados();
        configurarFiltroModelo();
    }

    private void configurarColunas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        colPrecoCusto.setCellValueFactory(data ->
                new SimpleStringProperty(formatarMoeda(data.getValue().getPrecoCusto())));
        colPrecoVenda.setCellValueFactory(data ->
                new SimpleStringProperty(formatarMoeda(data.getValue().getPrecoVenda())));

        colEstoque.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getQuantidadeEstoque())));

        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));

        // Coluna de ações - cada linha ganha seus próprios botões Editar/Excluir
        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox container = new HBox(8, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnExcluir.getStyleClass().addAll("btn-table-action", "btn-table-danger");
                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnExcluir.setOnAction(e -> confirmarExclusao(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tabelaPecas.setItems(pecas);
    }

    private void configurarBusca() {
        txtBusca.textProperty().addListener((obs, valorAntigo, valorNovo) -> aplicarFiltros());
    }

    private void carregarDados() {
        List<Peca> todas = estoqueService.listarTodas();
        pecas.setAll(todas);
        atualizarCards(todas);
        configurarFiltroModelo();
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String modeloSelecionado = cmbFiltroModelo.getValue();

        List<Peca> filtradas = estoqueService.listarTodas().stream()
                .filter(p -> termo.isBlank()
                        || p.getCodigo().toLowerCase().contains(termo)
                        || p.getDescricao().toLowerCase().contains(termo))
                .filter(p -> modeloSelecionado == null
                        || modeloSelecionado.equals("Todos os modelos")
                        || modeloSelecionado.equals(p.getModelo()))
                .toList();

        pecas.setAll(filtradas);
    }

    private void atualizarCards(List<Peca> todas) {
        lblTotalItens.setText(String.valueOf(todas.size()));

        long comEstoqueBaixo = todas.stream()
                .filter(p -> p.getQuantidadeEstoque() <= p.getEstoqueMinimo())
                .count();
        lblEstoqueBaixo.setText(String.valueOf(comEstoqueBaixo));

        BigDecimal valorTotal = todas.stream()
                .map(p -> p.getPrecoCusto().multiply(BigDecimal.valueOf(p.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblValorEstoque.setText(formatarMoeda(valorTotal));
    }

    @FXML
    private void abrirModalNovaPeca() {
        abrirModal(null);
    }

    private void abrirModalEdicao(Peca peca) {
        abrirModal(peca);
    }

    private void abrirModal(Peca pecaExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/peca-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            PecaFormController controller = loader.getController();
            controller.configurar(pecaExistente, this::carregarDados);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(pecaExistente == null ? "Nova Peça" : "Editar Peça");
            modal.setScene(new javafx.scene.Scene(root));
            modal.showAndWait();

            carregarDados(); // recarrega a tabela ao fechar o modal
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de peça", e);
        }
    }

    private void confirmarExclusao(Peca peca) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText(null);
        alert.setContentText("Deseja realmente excluir a peça \"" + peca.getDescricao() + "\"?");

        alert.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                estoqueService.excluir(peca.getId());
                carregarDados();
            }
        });
    }

    private String formatarMoeda(BigDecimal valor) {
        return String.format("R$ %,.2f", valor);
    }

    private void configurarFiltroModelo() {
        ObservableList<String> opcoes = FXCollections.observableArrayList();
        opcoes.add("Todos os modelos");
        opcoes.addAll(estoqueService.listarModelosDistintos());
        cmbFiltroModelo.setItems(opcoes);
        cmbFiltroModelo.getSelectionModel().selectFirst();

        cmbFiltroModelo.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
    }
}