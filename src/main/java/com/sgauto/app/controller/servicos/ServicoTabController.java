package com.sgauto.app.controller.servicos;

import com.sgauto.app.model.Servico;
import com.sgauto.app.service.CategoriaService;
import com.sgauto.app.service.ServicoService;
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
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
public class ServicoTabController {

    @FXML private Label lblTotalServicos;
    @FXML private Label lblServicosAtivos;
    @FXML private Label lblValorMedio;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivos;
    @FXML private ToggleButton btnStatusInativos;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbOrdenar;
    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colCodigo;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colCategoria;
    @FXML private TableColumn<Servico, String> colValor;
    @FXML private TableColumn<Servico, Void> colStatus;
    @FXML private TableColumn<Servico, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final ServicoService servicoService;
    private final CategoriaService categoriaService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Servico> servicos = FXCollections.observableArrayList();

    private static final String ORD_NOME_AZ = "Nome (A-Z)";
    private static final String ORD_NOME_ZA = "Nome (Z-A)";
    private static final String ORD_VALOR_MENOR = "Valor (menor primeiro)";
    private static final String ORD_VALOR_MAIOR = "Valor (maior primeiro)";
    private AutoCompleteComboBox autoCompleteCategoria;

    public ServicoTabController(ServicoService servicoService, CategoriaService categoriaService,
                                ApplicationContext applicationContext) {
        this.servicoService = servicoService;
        this.categoriaService = categoriaService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarOrdenacao();

        autoCompleteCategoria = new AutoCompleteComboBox(cmbFiltroCategoria);

        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/servicos/servico-configuracoes-avancadas-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage modal = ModalUtil.abrir(root, "Configurações Avançadas — Serviços",
                    tabelaServicos.getScene().getWindow());
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir configurações avançadas", e);
        }
    }

    private void configurarOrdenacao() {
        cmbOrdenar.setItems(FXCollections.observableArrayList(
                ORD_NOME_AZ, ORD_NOME_ZA, ORD_VALOR_MENOR, ORD_VALOR_MAIOR));
        cmbOrdenar.getSelectionModel().select(ORD_NOME_AZ);
    }

    private void configurarColunas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colValor.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getValor())));

        colCategoria.setCellFactory(coluna -> new TableCell<>() {
            private final Label chip = new Label();
            { chip.getStyleClass().add("chip"); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Servico s = getTableView().getItems().get(getIndex());
                chip.setText(s.getCategoria());
                setGraphic(chip);
            }
        });

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
        autoCompleteCategoria.definirItens(opcoes);

        if (selecionadoAntes != null && opcoes.contains(selecionadoAntes)) {
            cmbFiltroCategoria.setValue(selecionadoAntes);
        } else {
            cmbFiltroCategoria.getSelectionModel().selectFirst();
        }
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String categoriaSelecionada = cmbFiltroCategoria.getValue();
        Toggle statusSelecionado = grupoStatus.getSelectedToggle();

        List<Servico> filtrados = servicoService.listarTodos().stream()
                .filter(s -> termo.isBlank()
                        || s.getCodigo().toLowerCase().contains(termo)
                        || s.getNome().toLowerCase().contains(termo))
                .filter(s -> categoriaSelecionada == null
                        || categoriaSelecionada.equals("Todas as categorias")
                        || categoriaSelecionada.equals(s.getCategoria()))
                .filter(s -> {
                    if (statusSelecionado == btnStatusAtivos) return Boolean.TRUE.equals(s.getAtivo());
                    if (statusSelecionado == btnStatusInativos) return Boolean.FALSE.equals(s.getAtivo());
                    return true; // Todos
                })
                .sorted(obterComparador())
                .toList();

        servicos.setAll(filtrados);
        lblContagem.setText(filtrados.size() + " resultado(s)");
        atualizarEstadoVazio(filtrados.isEmpty());
    }

    private Comparator<Servico> obterComparador() {
        String ordem = cmbOrdenar.getValue();
        if (ORD_NOME_ZA.equals(ordem)) return Comparator.comparing(Servico::getNome).reversed();
        if (ORD_VALOR_MENOR.equals(ordem)) return Comparator.comparing(Servico::getValor);
        if (ORD_VALOR_MAIOR.equals(ordem)) return Comparator.comparing(Servico::getValor).reversed();
        return Comparator.comparing(Servico::getNome); // default: Nome A-Z
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaServicos.setVisible(!vazio);
        tabelaServicos.setManaged(!vazio);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/servicos/servico-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            ServicoFormController controller = loader.getController();
            controller.configurar(servicoExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, servicoExistente == null ? "Novo Serviço" : "Editar Serviço", tabelaServicos.getScene().getWindow());
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

    private void confirmarExclusao(Servico servico) {
        if (servicoService.estaEmUso(servico.getId())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não é possível excluir",
                    "Este serviço está vinculado a uma ou mais Ordens de Serviço.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente excluir o serviço \"" + servico.getNome() + "\"?");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try {
                    servicoService.excluir(servico.getId());
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