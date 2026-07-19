package com.sgauto.app.controller.estoque;

import com.sgauto.app.model.Peca;
import com.sgauto.app.service.EstoqueService;
import com.sgauto.app.util.AutoCompleteComboBox;
import com.sgauto.app.util.ModalUtil;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.Comparator;
import java.util.List;

@Component
public class EstoqueController {

    @FXML private Label lblTotalItens;
    @FXML private Label lblEstoqueBaixo;
    @FXML private Label lblValorEstoque;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusNormal;
    @FXML private ToggleButton btnStatusBaixo;
    @FXML private ToggleButton btnStatusZerado;
    @FXML private ComboBox<String> cmbFiltroModelo;
    @FXML private ComboBox<String> cmbOrdenar;
    @FXML private TableView<Peca> tabelaPecas;
    @FXML private TableColumn<Peca, String> colCodigo;
    @FXML private TableColumn<Peca, String> colDescricao;
    @FXML private TableColumn<Peca, String> colModelo;
    @FXML private TableColumn<Peca, String> colPrecoCusto;
    @FXML private TableColumn<Peca, String> colPrecoVenda;
    @FXML private TableColumn<Peca, String> colEstoque;
    @FXML private TableColumn<Peca, Void> colSituacao;
    @FXML private TableColumn<Peca, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final EstoqueService estoqueService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Peca> pecas = FXCollections.observableArrayList();
    private AutoCompleteComboBox autoCompleteModelo;

    private static final String ORD_DESC_AZ = "Descrição (A-Z)";
    private static final String ORD_DESC_ZA = "Descrição (Z-A)";
    private static final String ORD_ESTOQUE_MENOR = "Estoque (menor primeiro)";
    private static final String ORD_ESTOQUE_MAIOR = "Estoque (maior primeiro)";

    public EstoqueController(EstoqueService estoqueService, ApplicationContext applicationContext) {
        this.estoqueService = estoqueService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarOrdenacao();

        autoCompleteModelo = new AutoCompleteComboBox(cmbFiltroModelo);

        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbFiltroModelo.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbOrdenar.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        grupoStatus.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else aplicarFiltros();
        });

        carregarDados();
    }

    @FXML
    private void abrirModalAvancadas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/estoque-configuracoes-avancadas-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage modal = ModalUtil.abrir(root, "Configurações Avançadas — Estoque",
                    tabelaPecas.getScene().getWindow());
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir configurações avançadas", e);
        }
    }


    private void configurarOrdenacao() {
        cmbOrdenar.setItems(FXCollections.observableArrayList(
                ORD_DESC_AZ, ORD_DESC_ZA, ORD_ESTOQUE_MENOR, ORD_ESTOQUE_MAIOR));
        cmbOrdenar.getSelectionModel().select(ORD_DESC_AZ);
    }

    private void configurarColunas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colPrecoCusto.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getPrecoCusto())));
        colPrecoVenda.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getPrecoVenda())));
        colEstoque.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantidadeEstoque())));

        colSituacao.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Peca p = getTableView().getItems().get(getIndex());
                String situacao = situacaoDe(p);
                badge.setText(situacao);
                badge.getStyleClass().setAll("badge",
                        situacao.equals("Zerado") ? "badge-zerado" :
                                situacao.equals("Baixo") ? "badge-baixo" : "badge-normal");
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnMovimentar = new Button("Movimentar");
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox container = new HBox(6, btnMovimentar, btnEditar, btnExcluir);
            {
                btnMovimentar.getStyleClass().add("btn-table-action");
                btnEditar.getStyleClass().add("btn-table-action");
                btnExcluir.getStyleClass().addAll("btn-table-action", "btn-table-danger");
                btnMovimentar.setOnAction(e -> abrirMovimentacao(getTableView().getItems().get(getIndex())));
                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnExcluir.setOnAction(e -> confirmarExclusao(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : container);
            }
        });

        tabelaPecas.setItems(pecas);
    }

    private String situacaoDe(Peca p) {
        if (p.getQuantidadeEstoque() == 0) return "Zerado";
        if (p.getQuantidadeEstoque() <= p.getEstoqueMinimo()) return "Baixo";
        return "Normal";
    }

    private void carregarDados() {
        List<Peca> todas = estoqueService.listarTodas();
        atualizarCards(todas);
        atualizarComboModelos();
        aplicarFiltros();
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

    private void atualizarComboModelos() {
        String selecionadoAntes = cmbFiltroModelo.getValue();
        ObservableList<String> opcoes = FXCollections.observableArrayList();
        opcoes.add("Todos os modelos");
        opcoes.addAll(estoqueService.listarModelosDistintos());
        autoCompleteModelo.definirItens(opcoes);

        if (selecionadoAntes != null && opcoes.contains(selecionadoAntes)) {
            cmbFiltroModelo.setValue(selecionadoAntes);
        } else {
            cmbFiltroModelo.getSelectionModel().selectFirst();
        }
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String modeloSelecionado = cmbFiltroModelo.getValue();
        Toggle statusSelecionado = grupoStatus.getSelectedToggle();

        List<Peca> filtradas = estoqueService.listarTodas().stream()
                .filter(p -> termo.isBlank()
                        || p.getCodigo().toLowerCase().contains(termo)
                        || p.getDescricao().toLowerCase().contains(termo))
                .filter(p -> modeloSelecionado == null
                        || modeloSelecionado.equals("Todos os modelos")
                        || modeloSelecionado.equals(p.getModelo()))
                .filter(p -> {
                    if (statusSelecionado == btnStatusNormal) return situacaoDe(p).equals("Normal");
                    if (statusSelecionado == btnStatusBaixo) return situacaoDe(p).equals("Baixo");
                    if (statusSelecionado == btnStatusZerado) return situacaoDe(p).equals("Zerado");
                    return true;
                })
                .sorted(obterComparador())
                .toList();

        pecas.setAll(filtradas);
        lblContagem.setText(filtradas.size() + " resultado(s)");
        atualizarEstadoVazio(filtradas.isEmpty());
    }

    private Comparator<Peca> obterComparador() {
        String ordem = cmbOrdenar.getValue();
        if (ORD_DESC_ZA.equals(ordem)) return Comparator.comparing(Peca::getDescricao).reversed();
        if (ORD_ESTOQUE_MENOR.equals(ordem)) return Comparator.comparing(Peca::getQuantidadeEstoque);
        if (ORD_ESTOQUE_MAIOR.equals(ordem)) return Comparator.comparing(Peca::getQuantidadeEstoque).reversed();
        return Comparator.comparing(Peca::getDescricao);
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaPecas.setVisible(!vazio);
        tabelaPecas.setManaged(!vazio);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/peca-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            PecaFormController controller = loader.getController();
            controller.configurar(pecaExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, pecaExistente == null ? "Nova Peça" : "Editar Peça", tabelaPecas.getScene().getWindow());
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de peça", e);
        }
    }

    private void abrirMovimentacao(Peca peca) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/movimentacao-estoque-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            MovimentacaoEstoqueController controller = loader.getController();
            controller.configurar(peca, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, "Movimentar Estoque", tabelaPecas.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir movimentação de estoque", e);
        }
    }

    @FXML
    private void abrirModalEstoqueBaixo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/estoque-baixo-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            EstoqueBaixoModalController controller = loader.getController();
            controller.configurar(this::carregarDados);

            Stage modal = ModalUtil.abrir(root, "Peças com Estoque Baixo", tabelaPecas.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir estoque baixo", e);
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

}