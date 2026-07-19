package com.sgauto.app.controller.servicos;

import com.sgauto.app.model.Categoria;
import com.sgauto.app.service.CategoriaService;
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
public class CategoriaTabController {

    @FXML private Label lblTotalCategorias;
    @FXML private Label lblCategoriasAtivas;
    @FXML private Label lblCategoriasEmUso;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivas;
    @FXML private ToggleButton btnStatusInativas;
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private TableView<Categoria> tabelaCategorias;
    @FXML private TableColumn<Categoria, String> colNome;
    @FXML private TableColumn<Categoria, String> colTipo;
    @FXML private TableColumn<Categoria, String> colDescricao;
    @FXML private TableColumn<Categoria, Void> colUso;
    @FXML private TableColumn<Categoria, Void> colStatus;
    @FXML private TableColumn<Categoria, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final CategoriaService categoriaService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    public CategoriaTabController(CategoriaService categoriaService, ApplicationContext applicationContext) {
        this.categoriaService = categoriaService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        cmbFiltroTipo.setItems(FXCollections.observableArrayList("Todos os tipos",
                "MECÂNICA",
                "ESTÉTICA",
                "ELÉTRICA",
                "GEOMETRIA",
                "GERAL"));
        cmbFiltroTipo.getSelectionModel().selectFirst();

        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltro());
        cmbFiltroTipo.valueProperty().addListener((obs, a, n) -> aplicarFiltro());
        grupoStatus.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else aplicarFiltro();
        });

        carregarDados();
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        colUso.setCellFactory(coluna -> new TableCell<>() {
            private final javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
            private final Label texto = new Label();
            private final HBox container = new HBox(6, dot, texto);
            { container.setAlignment(javafx.geometry.Pos.CENTER_LEFT); }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Categoria c = getTableView().getItems().get(getIndex());
                boolean emUso = categoriaService.estaEmUso(c.getNome());
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
                Categoria c = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(c.getAtivo());
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
                Categoria c = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(c.getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaCategorias.setItems(categorias);
    }

    private void carregarDados() {
        List<Categoria> todas = categoriaService.listarTodas();
        atualizarCards(todas);
        aplicarFiltro();
    }

    private void atualizarCards(List<Categoria> todas) {
        lblTotalCategorias.setText(String.valueOf(todas.size()));
        long ativas = todas.stream().filter(c -> Boolean.TRUE.equals(c.getAtivo())).count();
        lblCategoriasAtivas.setText(String.valueOf(ativas));
        long emUso = todas.stream().filter(c -> categoriaService.estaEmUso(c.getNome())).count();
        lblCategoriasEmUso.setText(String.valueOf(emUso));
    }

    private void aplicarFiltro() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String tipoSelecionado = cmbFiltroTipo.getValue();
        Toggle statusSelecionado = grupoStatus.getSelectedToggle();

        List<Categoria> filtradas = categoriaService.listarTodas().stream()
                .filter(c -> termo.isBlank() || c.getNome().toLowerCase().contains(termo))
                .filter(c -> tipoSelecionado == null
                        || tipoSelecionado.equals("Todos os tipos")
                        || tipoSelecionado.equals(c.getTipo()))
                .filter(c -> {
                    if (statusSelecionado == btnStatusAtivas) return Boolean.TRUE.equals(c.getAtivo());
                    if (statusSelecionado == btnStatusInativas) return Boolean.FALSE.equals(c.getAtivo());
                    return true;
                })
                .toList();

        categorias.setAll(filtradas);
        lblContagem.setText(filtradas.size() + " resultado(s)");
        atualizarEstadoVazio(filtradas.isEmpty());
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaCategorias.setVisible(!vazio);
        tabelaCategorias.setManaged(!vazio);
    }

    @FXML
    private void abrirModalNovaCategoria() {
        abrirModal(null);
    }

    private void abrirModalEdicao(Categoria categoria) {
        abrirModal(categoria);
    }

    private void abrirModal(Categoria categoriaExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/servicos/categoria-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            CategoriaFormController controller = loader.getController();
            controller.configurar(categoriaExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, categoriaExistente == null ? "Nova Categoria" : "Editar Categoria");
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de categoria", e);
        }
    }

    private void alternarStatus(Categoria categoria) {
        try {
            if (Boolean.TRUE.equals(categoria.getAtivo())) {
                categoriaService.desativar(categoria.getId());
            } else {
                categoriaService.ativar(categoria.getId());
            }
            carregarDados();
        } catch (IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não é possível desativar", e.getMessage());
        }
    }

    private void confirmarExclusao(Categoria categoria) {
        if (categoriaService.estaEmUso(categoria.getNome())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não é possível excluir",
                    "Esta categoria possui serviços associados. Remova ou reatribua os serviços antes de excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente excluir a categoria \"" + categoria.getNome() + "\"?");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try {
                    categoriaService.excluir(categoria.getId());
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