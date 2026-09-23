package com.sgauto.app.controller.financeiro;

import com.sgauto.app.enums.StatusConta;
import com.sgauto.app.model.financeiro.ContaReceber;
import com.sgauto.app.service.financeiro.ContaReceberService;
import com.sgauto.app.util.ModalUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    @FXML private ComboBox<StatusConta> filtroStatus;
    @FXML private Button btnNovaConta;
    @FXML private Button btnDarBaixa;
    @FXML private Button btnCancelar;
    @FXML private Label lblMensagem;

    @Autowired
    private ContaReceberService contaReceberService;

    @Autowired
    private ApplicationContext applicationContext;

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

        filtroStatus.setItems(FXCollections.observableArrayList(StatusConta.values()));
        filtroStatus.setPromptText("Todos os status");
        filtroStatus.valueProperty().addListener((obs, antigo, novo) -> carregarDados());

        btnNovaConta.setOnAction(e -> abrirModalIncluir());
        btnDarBaixa.setOnAction(e -> abrirModalDarBaixa());
        btnCancelar.setOnAction(e -> cancelarSelecionada());

        carregarDados();
    }

    private void carregarDados() {
        ObservableList<ContaReceber> dados;
        StatusConta status = filtroStatus.getValue();
        dados = FXCollections.observableArrayList(
                status != null ? contaReceberService.listarPorStatus(status) : contaReceberService.listarTodas());
        tabela.setItems(dados);
        lblMensagem.setText(dados.isEmpty() ? "Nenhuma conta encontrada." : "");
    }

    private void abrirModalIncluir() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/financeiro/incluir-conta-receber.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = ModalUtil.abrir(root, "\"Nova conta a receber\"");
            stage.showAndWait();

            carregarDados();
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

            carregarDados();
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
                carregarDados();
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