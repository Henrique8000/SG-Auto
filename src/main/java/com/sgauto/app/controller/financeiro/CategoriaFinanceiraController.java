package com.sgauto.app.controller.financeiro;

import com.sgauto.app.controller.PaginacaoController;
import com.sgauto.app.enums.financeiro.TipoCategoriaFinanceira;
import com.sgauto.app.enums.usuario.PermissaoChave;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import com.sgauto.app.service.financeiro.CategoriaFinanceiraService;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class CategoriaFinanceiraController implements Initializable {

    @FXML private TableView<CategoriaFinanceira> tabelaCategorias;
    @FXML private TableColumn<CategoriaFinanceira, String> colId;
    @FXML private TableColumn<CategoriaFinanceira, String> colNome;
    @FXML private TableColumn<CategoriaFinanceira, String> colTipo;

    @FXML private TextField txtNome;
    @FXML private ComboBox<TipoCategoriaFinanceira> cbTipo;
    @FXML private TextField txtBuscaNome;
    @FXML private ComboBox<TipoCategoriaFinanceira> cbFiltroTipo;
    @FXML private CheckBox chkMostrarInativas;
    @FXML private Button btnAlternarStatus;

    // INJEÇÃO AUTOMÁTICA DO COMPONENTE DE PAGINAÇÃO
    @FXML private PaginacaoController paginacaoController;

    private final CategoriaFinanceiraService service;
    private final VerificaPermissaoUtil permissaoUtil;
    private ObservableList<CategoriaFinanceira> categoriasObservable;
    private CategoriaFinanceira categoriaSelecionada;

    public CategoriaFinanceiraController(CategoriaFinanceiraService service, VerificaPermissaoUtil permissaoUtil) {
        this.service = service;
        this.permissaoUtil = permissaoUtil;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColunas();
        carregarComboTipos();
        configurarEventosTabela();

        // 1. Configura os gatilhos da paginação
        paginacaoController.configurar(
                this::carregarTabela,                  // Dispara quando o usuário clica em "Próxima/Anterior"
                tamanho -> carregarTabela(0)     // Dispara quando o usuário altera de 20 para 50 itens, forçando voltar pra página 0
        );

        cbFiltroTipo.setItems(FXCollections.observableArrayList(TipoCategoriaFinanceira.values()));

        // 2. Carrega a primeira página ao abrir a tela
        carregarTabela(0);
    }

    private void configurarColunas() {
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toString()));
        colNome.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));
        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo().name()));
    }

    private void carregarComboTipos() {
        cbTipo.setItems(FXCollections.observableArrayList(TipoCategoriaFinanceira.values()));
    }

    private void configurarEventosTabela() {
        tabelaCategorias.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                preencherFormulario(newValue);
            } else {
                limparFormulario();
            }
        });
    }

    private void carregarTabela(int numeroPagina) {
        int tamanhoPagina = paginacaoController.getTamanhoPagina();
        String termoBusca = txtBuscaNome.getText();
        TipoCategoriaFinanceira tipoSelecionado = cbFiltroTipo.getValue();

        // Se a checkbox "Mostrar Inativas" NÃO estiver marcada, buscamos os ativos (true)
        boolean isBuscandoAtivas = !chkMostrarInativas.isSelected();

        // Agora o service recebe 5 parâmetros: termo, tipo, ativo, página, tamanho
        Page<CategoriaFinanceira> paginaSpring = service.listarComFiltros(
                termoBusca,
                tipoSelecionado,
                isBuscandoAtivas,
                numeroPagina,
                tamanhoPagina
        );

        categoriasObservable = FXCollections.observableArrayList(paginaSpring.getContent());
        tabelaCategorias.setItems(categoriasObservable);
        paginacaoController.atualizar(paginaSpring);
    }

    @FXML
    private void salvarCategoria() {
        if (!permissaoUtil.verificar(PermissaoChave.CATEGORIA_CONTA_GERENCIAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        }

        try {
            if (categoriaSelecionada == null) {
                CategoriaFinanceira nova = new CategoriaFinanceira(txtNome.getText(), cbTipo.getValue());
                service.salvar(nova);
            } else {
                categoriaSelecionada.setNome(txtNome.getText());
                categoriaSelecionada.setTipo(cbTipo.getValue());
                service.atualizar(categoriaSelecionada.getId(), categoriaSelecionada);
            }
            limparFormulario();
            // Após salvar, forçamos o retorno à primeira página para o usuário ver o novo registro
            paginacaoController.resetarPagina();
            carregarTabela(0);

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", e.getMessage());
        }
    }

    @FXML
    private void desativarCategoria() {
        if (!permissaoUtil.verificar(PermissaoChave.CATEGORIA_CONTA_GERENCIAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        }

        if (categoriaSelecionada != null) {
            try {
                service.desativar(categoriaSelecionada.getId());
                limparFormulario();
                // Após excluir, recarrega a página atual para refletir a saída do item
                carregarTabela(tabelaCategorias.getItems().isEmpty() ? 0 : paginacaoController.getTamanhoPagina()); // Ou pode manter o número da página atual rastreado no componente
                carregarTabela(0);
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao desativar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void alternarStatusCategoria() {
        if (!permissaoUtil.verificar(PermissaoChave.CATEGORIA_CONTA_GERENCIAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        }

        if (categoriaSelecionada != null) {
            boolean isAtiva = categoriaSelecionada.getAtivo();
            String acaoTexto = isAtiva ? "desativar" : "reativar";

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja " + acaoTexto + " esta categoria?", ButtonType.YES, ButtonType.NO);
            confirmacao.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        if (isAtiva) {
                            service.desativar(categoriaSelecionada.getId());
                        } else {
                            service.reativar(categoriaSelecionada.getId());
                        }
                        limparFormulario();
                        carregarTabela(0);
                    } catch (Exception e) {
                        mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao " + acaoTexto + ": " + e.getMessage());
                    }
                }
            });
        }
    }

    private void preencherFormulario(CategoriaFinanceira categoria) {
        this.categoriaSelecionada = categoria;
        txtNome.setText(categoria.getNome());
        cbTipo.setValue(categoria.getTipo());

        btnAlternarStatus.setDisable(false);

        if (categoria.getAtivo() != null && categoria.getAtivo()) {
            btnAlternarStatus.setText("Desativar Selecionada");
            btnAlternarStatus.setStyle("-fx-background-color: transparent; -fx-border-color: -danger; -fx-text-fill: -danger; -fx-border-radius: 4; -fx-padding: 9 18; -fx-cursor: hand; -fx-font-weight: bold;");
        } else {
            btnAlternarStatus.setText("Reativar Selecionada");
            btnAlternarStatus.setStyle("-fx-background-color: transparent; -fx-border-color: -success; -fx-text-fill: -success; -fx-border-radius: 4; -fx-padding: 9 18; -fx-cursor: hand; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void limparFormulario() {
        categoriaSelecionada = null;
        txtNome.clear();
        cbTipo.setValue(null);
        tabelaCategorias.getSelectionModel().clearSelection();
        btnAlternarStatus.setDisable(true);
    }

    @FXML
    private void aoDigitarBusca(javafx.scene.input.KeyEvent event) {
        paginacaoController.resetarPagina();
        carregarTabela(0);
    }

    @FXML
    private void aplicarFiltros() {
        paginacaoController.resetarPagina();
        carregarTabela(0);
    }

    @FXML
    private void limparFiltros() {
        txtBuscaNome.clear();
        cbFiltroTipo.setValue(null);
        chkMostrarInativas.setSelected(false);
        aplicarFiltros();
    }



    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}