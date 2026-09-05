package com.sgauto.app.controller.estoque.fornecedor;

import com.sgauto.app.controller.PaginacaoController;
import com.sgauto.app.dto.estoque.FiltroFornecedorDTO;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.estoque.Fornecedor;
import com.sgauto.app.service.estoque.FornecedorService;
import com.sgauto.app.util.AutoCompleteComboBox;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.ModalUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class FornecedorController {

    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivos;
    @FXML private ToggleButton btnStatusInativos;
    @FXML private Button btnNovoFornecedor;

    @FXML private TableView<Fornecedor> tabelaFornecedores;
    @FXML private TableColumn<Fornecedor, String> colRazao;
    @FXML private TableColumn<Fornecedor, String> colDocumento;
    @FXML private TableColumn<Fornecedor, String> colContato;
    @FXML private TableColumn<Fornecedor, String> colTelefone;
    @FXML private TableColumn<Fornecedor, String> colCategoria;
    @FXML private TableColumn<Fornecedor, Void> colStatus;
    @FXML private TableColumn<Fornecedor, Void> colAcoes;
    @FXML private Label lblTotalFornecedores;
    @FXML private Label lblFornecedoresAtivos;
    @FXML private Label lblFornecedoresInativos;
    @FXML private VBox painelVazio;

    @FXML private PaginacaoController paginacaoController;

    private final FornecedorService fornecedorService;
    private final ApplicationContext applicationContext;
    private final VerificaPermissaoUtil permissaoUtil;
    private final ObservableList<Fornecedor> fornecedoresExibidos = FXCollections.observableArrayList();

    public FornecedorController(FornecedorService fornecedorService, ApplicationContext applicationContext, VerificaPermissaoUtil permissaoUtil) {
        this.fornecedorService = fornecedorService;
        this.applicationContext = applicationContext;
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {
        btnNovoFornecedor.setDisable(!permissaoUtil.verificar(PermissaoChave.FORNECEDOR_CRIAR));

        configurarColunas();

        if (permissaoUtil.verificar(PermissaoChave.FORNECEDOR_VISUALIZAR)) {
            carregarCategorias();

            paginacaoController.configurar(
                    pagina -> carregarPagina(pagina),
                    tamanho -> carregarPagina(0)
            );

            // Listeners para busca em tempo real
            txtBusca.textProperty().addListener((obs, a, n) -> reiniciarBusca());
            cmbCategoria.valueProperty().addListener((obs, a, n) -> reiniciarBusca());
            grupoStatus.selectedToggleProperty().addListener((obs, antigo, novo) -> {
                if (novo == null) antigo.setSelected(true);
                else reiniciarBusca();
            });

            carregarPagina(0);
        } else {
            tabelaFornecedores.setPlaceholder(new Label("Seu usuário não possui permissão para visualizar os fornecedores."));

            txtBusca.setDisable(true);
            cmbCategoria.setDisable(true);

            btnStatusTodos.setDisable(true);
            btnStatusAtivos.setDisable(true);
            btnStatusInativos.setDisable(true);
        }
    }

    private void carregarCategorias() {
        List<String> categoriasNoBanco = fornecedorService.listarCategoriasDisponiveis();
        List<String> opcoes = new ArrayList<>();
        opcoes.add("Todas as categorias");
        opcoes.addAll(categoriasNoBanco);

        AutoCompleteComboBox autoComplete = new AutoCompleteComboBox(cmbCategoria);
        autoComplete.definirItens(opcoes);
        cmbCategoria.getSelectionModel().selectFirst();
    }

    private void configurarColunas() {
        colRazao.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRazaoSocial()));
        colDocumento.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCpfCnpj()));
        colContato.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNomeContato() != null ? d.getValue().getNomeContato() : "-"));
        colTelefone.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCelular() != null ? d.getValue().getCelular() : (d.getValue().getTelefone() != null ? d.getValue().getTelefone() : "-")));
        colCategoria.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCategoria() != null ? d.getValue().getCategoria() : "-"));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                boolean ativo = getTableRow().getItem().getAtivo();
                badge.setText(ativo ? "Ativo" : "Inativo");
                badge.getStyleClass().setAll("badge", ativo ? "badge-active" : "badge-inactive");
                setGraphic(badge);
            }
        });

        boolean podeEditar = permissaoUtil.verificar(PermissaoChave.FORNECEDOR_EDITAR);
        boolean podeExcluir = permissaoUtil.verificar(PermissaoChave.FORNECEDOR_EXCLUIR);

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnMensagem = new Button("Mensagem");
            private final Button btnToggle = new Button();
            private final HBox container = new HBox(6);

            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnMensagem.getStyleClass().add("btn-table-action");

                btnEditar.setDisable(!podeEditar);
                btnToggle.setDisable(!podeExcluir);

                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> alternarStatus(getTableView().getItems().get(getIndex())));

                Tooltip tooltipMensagem = new Tooltip("Este botão abre o WhatsApp já no contato do fornecedor.\nÉ necessário verificar se o número de telefone cadastrado esta correto.\n\nÉ necessário estar com o WhatsApp Web conectado.");
                tooltipMensagem.setShowDelay(javafx.util.Duration.millis(300));
                btnMensagem.setTooltip(tooltipMensagem);

                btnMensagem.setOnAction(e -> abrirWhatsApp(getTableView().getItems().get(getIndex()).getCelular()));

                container.getChildren().addAll(btnEditar, btnMensagem, btnToggle);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                boolean ativo = getTableRow().getItem().getAtivo();
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });
        tabelaFornecedores.setItems(fornecedoresExibidos);
    }

    private void reiniciarBusca() {
        paginacaoController.resetarPagina();
        carregarPagina(0);
    }

    private void carregarPagina(int pagina) {
        String termo = txtBusca.getText();
        String categoria = cmbCategoria.getValue();
        Boolean ativo = null;

        if (grupoStatus.getSelectedToggle() == btnStatusAtivos) ativo = true;
        else if (grupoStatus.getSelectedToggle() == btnStatusInativos) ativo = false;

        FiltroFornecedorDTO filtro = new FiltroFornecedorDTO(termo, categoria, ativo);
        PageRequest pageRequest = PageRequest.of(pagina, paginacaoController.getTamanhoPagina(), Sort.by(Sort.Direction.ASC, "razaoSocial"));

        Page<Fornecedor> resultado = fornecedorService.pesquisar(filtro, pageRequest);

        fornecedoresExibidos.setAll(resultado.getContent());
        paginacaoController.atualizar(resultado);

        boolean vazio = resultado.isEmpty();
        tabelaFornecedores.setVisible(!vazio);
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);

        atualizarCards();
    }

    private void atualizarCards() {
        lblTotalFornecedores.setText(String.valueOf(fornecedorService.contarTotal()));
        lblFornecedoresAtivos.setText(String.valueOf(fornecedorService.contarPorStatus(true)));
        lblFornecedoresInativos.setText(String.valueOf(fornecedorService.contarPorStatus(false)));
    }

    @FXML
    private void abrirModalNovo() {
        if (permissaoUtil.verificar(PermissaoChave.FORNECEDOR_CRIAR)) {
            abrirModal(null);
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }

    private void abrirModalEdicao(Fornecedor fornecedor) {
        if (permissaoUtil.verificar(PermissaoChave.FORNECEDOR_EDITAR)) {
            abrirModal(fornecedor);
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }

    private void abrirModal(Fornecedor fornecedorExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/fornecedor-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            FornecedorFormController controller = loader.getController();
            controller.configurar(fornecedorExistente, () -> {
                carregarCategorias(); // Recarrega caso o cara crie uma categoria nova
                carregarPagina(0);
            });

            Stage modal = ModalUtil.abrir(root, fornecedorExistente == null ? "Novo Fornecedor" : "Editar Fornecedor", tabelaFornecedores.getScene().getWindow());
            modal.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de fornecedor", e);
        }
    }

    private void alternarStatus(Fornecedor fornecedor) {
        try {
            if (permissaoUtil.verificar(PermissaoChave.FORNECEDOR_EXCLUIR)) {
                fornecedorService.alternarStatus(fornecedor.getId());
                carregarPagina(0);
            } else {
                ExibirMensagemBloqueioUtil.exibir();
            }
        } catch (Exception e) {
            ExibirMensagemBloqueioUtil.exibirMensagemPersonalizada(e.getMessage());
        }
    }

    private void abrirWhatsApp(String cel) {
        if (cel == null || cel.isBlank()) {
            ExibirMensagemBloqueioUtil.exibirMensagemPersonalizada("O fornecedor selecionado não possui um número de celular cadastrado.");
            return;
        }
        String numeroLimpo = cel.replaceAll("[^0-9]", "");

        if (numeroLimpo.length() == 10 || numeroLimpo.length() == 11) {
            numeroLimpo = "55" + numeroLimpo;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Contato via WhatsApp");
        alert.setHeaderText("Deseja entrar em contato com este fornecedor pelo WhatsApp?");
        alert.setContentText("Número de destino: " + cel);

        ButtonType btnSim = new ButtonType("Sim", ButtonBar.ButtonData.YES);
        ButtonType btnNao = new ButtonType("Não", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnSim, btnNao);

        final String numeroFinal = numeroLimpo;

        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == btnSim) {
                try {
                    // Monta o Link flexível do WhatsApp (wa.me)
                    String url = "https://wa.me/" + numeroFinal;

                    // Abre o link usando o navegador padrão do sistema
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(url));
                    } else {
                        ExibirMensagemBloqueioUtil.exibirMensagemPersonalizada("Abertura de navegador não suportada neste sistema.");
                    }
                } catch (Exception e) {
                    ExibirMensagemBloqueioUtil.exibirMensagemPersonalizada("Erro ao tentar abrir o WhatsApp: " + e.getMessage());
                }
            }
        });
    }
}
