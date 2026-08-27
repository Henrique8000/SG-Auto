package com.sgauto.app.controller.estoque.fornecedor;

import com.sgauto.app.controller.dto.estoque.FiltroCategoriaFornecedorDTO;
import com.sgauto.app.model.estoque.CategoriaFornecedor;
import com.sgauto.app.service.estoque.CategoriaFornecedorService;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CategoriaFornecedorModalController {

    // Formulário de Criação Rápida
    @FXML private TextField txtNovaCategoriaNome;
    @FXML private TextField txtNovaCategoriaDesc;

    // Filtros
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivos;
    @FXML private ToggleButton btnStatusInativos;

    // Tabela
    @FXML private TableView<CategoriaFornecedor> tabelaCategorias;
    @FXML private TableColumn<CategoriaFornecedor, String> colNome;
    @FXML private TableColumn<CategoriaFornecedor, String> colDescricao;
    @FXML private TableColumn<CategoriaFornecedor, Void> colStatus;
    @FXML private TableColumn<CategoriaFornecedor, Void> colAcoes;

    private final CategoriaFornecedorService categoriaService;
    private final ObservableList<CategoriaFornecedor> categoriasExibidas = FXCollections.observableArrayList();
    private Consumer<CategoriaFornecedor> onCategoriaSelecionada;

    public CategoriaFornecedorModalController(CategoriaFornecedorService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @FXML
    public void initialize() {
        configurarColunas();

        // Listeners para busca em tempo real
        txtBusca.textProperty().addListener((obs, antigo, novo) -> carregarDados());
        grupoStatus.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else carregarDados();
        });

        carregarDados();
    }

    public void configurar(Consumer<CategoriaFornecedor> onCategoriaSelecionada) {
        this.onCategoriaSelecionada = onCategoriaSelecionada;
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNome()));
        colDescricao.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getDescricao() != null ? d.getValue().getDescricao() : "-"
        ));

        // Coluna de Status (Badge)
        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                boolean ativo = getTableRow().getItem().getAtivo();
                badge.setText(ativo ? "Ativa" : "Inativa");
                badge.getStyleClass().setAll("badge", ativo ? "badge-active" : "badge-inactive");
                setGraphic(badge);
            }
        });

        // Coluna de Ações (Selecionar e Alternar Status)
        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnSelecionar = new Button("Selecionar");
            private final Button btnToggle = new Button();
            private final HBox container = new HBox(6);
            {
                btnSelecionar.getStyleClass().add("btn-table-action");

                btnSelecionar.setOnAction(e -> selecionarCategoria(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> alternarStatus(getTableView().getItems().get(getIndex())));

                container.getChildren().addAll(btnSelecionar, btnToggle);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }

                boolean ativo = getTableRow().getItem().getAtivo();

                // Só permite selecionar categorias ativas
                btnSelecionar.setDisable(!ativo);

                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaCategorias.setItems(categoriasExibidas);
    }

    private void carregarDados() {
        String termo = txtBusca.getText();
        Boolean ativo = null;

        if (grupoStatus.getSelectedToggle() == btnStatusAtivos) ativo = true;
        else if (grupoStatus.getSelectedToggle() == btnStatusInativos) ativo = false;

        FiltroCategoriaFornecedorDTO filtro = new FiltroCategoriaFornecedorDTO(termo, ativo);

        // Carrega as primeiras 100 categorias ordenadas por nome (como é uma tabela de apoio, dispensa paginação visual complexa)
        PageRequest pageRequest = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "nome"));
        Page<CategoriaFornecedor> resultado = categoriaService.pesquisar(filtro, pageRequest);

        categoriasExibidas.setAll(resultado.getContent());
    }

    @FXML
    private void adicionarCategoria() {
        try {
            CategoriaFornecedor novaCategoria = new CategoriaFornecedor();
            novaCategoria.setNome(txtNovaCategoriaNome.getText());
            novaCategoria.setDescricao(txtNovaCategoriaDesc.getText());

            categoriaService.cadastrar(novaCategoria);

            // Limpa os campos após salvar
            txtNovaCategoriaNome.clear();
            txtNovaCategoriaDesc.clear();

            // Recarrega a tabela para mostrar a nova categoria
            carregarDados();

        } catch (IllegalArgumentException | IllegalStateException e) {
            ExibirMensagemBloqueioUtil.exibirMensagemPersonalizada(e.getMessage());
        }
    }

    private void alternarStatus(CategoriaFornecedor categoria) {
        try {
            categoriaService.alternarStatus(categoria.getId());
            carregarDados(); // Atualiza a lista para refletir a cor/status novo
        } catch (Exception e) {
            ExibirMensagemBloqueioUtil.exibirMensagemPersonalizada("Erro ao alterar status: " + e.getMessage());
        }
    }

    private void selecionarCategoria(CategoriaFornecedor categoria) {
        if (onCategoriaSelecionada != null) {
            onCategoriaSelecionada.accept(categoria);
        }
        fecharModal();
    }

    @FXML
    private void fecharModal() {
        Stage stage = (Stage) txtBusca.getScene().getWindow();
        stage.close();
    }
}