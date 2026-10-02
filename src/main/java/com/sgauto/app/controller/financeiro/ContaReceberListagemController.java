package com.sgauto.app.controller.financeiro;

import com.sgauto.app.controller.PaginacaoController;
import com.sgauto.app.enums.financeiro.StatusConta;
import com.sgauto.app.model.financeiro.ContaReceber;
import com.sgauto.app.service.financeiro.ContaReceberService;
import com.sgauto.app.util.ModalUtil;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ContaReceberListagemController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TableView<ContaReceber> tabela;
    @FXML private TableColumn<ContaReceber, String> colDescricao;
    @FXML private TableColumn<ContaReceber, String> colCliente;
    @FXML private TableColumn<ContaReceber, String> colParcela;
    @FXML private TableColumn<ContaReceber, String> colValor;
    @FXML private TableColumn<ContaReceber, String> colVencimento;
    @FXML private TableColumn<ContaReceber, String> colStatus;
    @FXML private TextField txtCliente;
    @FXML private ComboBox<StatusConta> filtroStatus;
    @FXML private DatePicker dpVencimentoDe;
    @FXML private DatePicker dpVencimentoAte;
    @FXML private Button btnLimpar;
    @FXML private Button btnNovaConta;
    @FXML private Button btnDarBaixa;
    @FXML private Button btnCancelar;
    @FXML private Label lblMensagem;

    // Injetado pelo <fx:include fx:id="paginacao"> (fx:id + "Controller")
    @FXML private PaginacaoController paginacaoController;

    @Autowired
    private ContaReceberService contaReceberService;

    @Autowired
    private ApplicationContext applicationContext;

    private final PauseTransition debounceBusca = new PauseTransition(Duration.millis(400));
    private boolean atualizandoFiltros = false;
    private int paginaAtual = 0;

    @FXML
    public void initialize() {
        colDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCliente() != null ? c.getValue().getCliente().getNome() : "-"));
        colParcela.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumeroParcela() + "/" + c.getValue().getTotalParcelas()));
        colValor.setCellValueFactory(c -> new SimpleStringProperty(
                "R$ " + c.getValue().getValorOriginal().setScale(2, java.math.RoundingMode.HALF_UP)));
        colVencimento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataVencimento().format(FMT)));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));

        // Combo de status (valor null = "Todos")
        filtroStatus.setItems(FXCollections.observableArrayList(StatusConta.values()));
        filtroStatus.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(StatusConta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todos os status" : item.name());
            }
        });
        filtroStatus.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        dpVencimentoDe.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        dpVencimentoAte.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());

        // Texto: busca automática após 400ms sem digitar, ou imediata no Enter
        debounceBusca.setOnFinished(e -> aplicarFiltros());
        txtCliente.textProperty().addListener((obs, antigo, novo) -> debounceBusca.playFromStart());
        txtCliente.setOnAction(e -> {
            debounceBusca.stop();
            aplicarFiltros();
        });

        btnLimpar.setOnAction(e -> limparFiltros());
        btnNovaConta.setOnAction(e -> abrirModalIncluir());
        btnDarBaixa.setOnAction(e -> abrirModalDarBaixa());
        btnCancelar.setOnAction(e -> cancelarSelecionada());

        paginacaoController.configurar(this::carregarDados, tamanho -> carregarDados(0));

        contaReceberService.atualizarStatusVencidas();
        carregarDados(0);
    }

    private void aplicarFiltros() {
        if (atualizandoFiltros) return;
        carregarDados(0);
    }

    private void limparFiltros() {
        atualizandoFiltros = true;
        txtCliente.clear();
        filtroStatus.setValue(null);
        dpVencimentoDe.setValue(null);
        dpVencimentoAte.setValue(null);
        atualizandoFiltros = false;
        carregarDados(0);
    }

    private void carregarDados(int pagina) {
        LocalDate de = dpVencimentoDe.getValue();
        LocalDate ate = dpVencimentoAte.getValue();

        if (de != null && ate != null && de.isAfter(ate)) {
            lblMensagem.setText("A data inicial não pode ser posterior à data final.");
            return;
        }

        Pageable pageable = PageRequest.of(pagina, paginacaoController.getTamanhoPagina(),
                Sort.by(Sort.Order.asc("dataVencimento"), Sort.Order.asc("id")));

        Page<ContaReceber> resultado = contaReceberService.buscarComFiltros(
                filtroStatus.getValue(), txtCliente.getText(), de, ate, pageable);

        // Ex.: cancelou o último item da última página -> volta para a última página existente
        if (resultado.isEmpty() && pagina > 0 && resultado.getTotalPages() > 0) {
            carregarDados(resultado.getTotalPages() - 1);
            return;
        }

        paginaAtual = resultado.getNumber();
        tabela.setItems(FXCollections.observableArrayList(resultado.getContent()));
        paginacaoController.atualizar(resultado);
        lblMensagem.setText(resultado.isEmpty() ? "Nenhuma conta encontrada." : "");
    }

    private void abrirModalIncluir() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/financeiro/incluir-conta-receber.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = ModalUtil.abrir(root, "Nova conta a receber");
            stage.showAndWait();

            carregarDados(paginaAtual);
        } catch (Exception e) {
            mostrarErro("Não foi possível abrir a tela de inclusão: " + e.getMessage());
        }
    }

    private void abrirModalDarBaixa() {
        ContaReceber selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            mostrarErro("Selecione uma conta na lista para dar baixa.");
            return;
        }
        if (selecionada.getStatus() == StatusConta.PAGO || selecionada.getStatus() == StatusConta.CANCELADO) {
            mostrarErro("Esta conta não pode receber baixa (status atual: " + selecionada.getStatus() + ").");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/financeiro/dar-baixa-conta-receber.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            DarBaixaContaReceberController controller = loader.getController();
            controller.setConta(selecionada);

            Stage stage = ModalUtil.abrir(root, "Dar baixa - Conta a receber");
            stage.showAndWait();

            carregarDados(paginaAtual);
        } catch (Exception e) {
            mostrarErro("Não foi possível abrir a tela de baixa: " + e.getMessage());
        }
    }

    private void cancelarSelecionada() {
        ContaReceber selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            mostrarErro("Selecione uma conta na lista para cancelar.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar conta a receber");
        dialog.setHeaderText("Informe o motivo do cancelamento");
        dialog.showAndWait().ifPresent(motivo -> {
            try {
                contaReceberService.cancelar(selecionada.getId(), motivo);
                carregarDados(paginaAtual);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                mostrarErro(ex.getMessage());
            }
        });
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensagem, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}