package com.sgauto.app.controller.os;

import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.service.OrdemServicoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class OsRegistrarPagamentoModalController {

    @FXML private Label lblSaldoAtual;
    @FXML private ComboBox<FormaPagamento> cmbForma;
    @FXML private TextField txtValor;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final OrdemServicoService ordemServicoService;
    private final ApplicationContext applicationContext;
    private Long osId;
    private Runnable aoConfirmar;

    public OsRegistrarPagamentoModalController(OrdemServicoService ordemServicoService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        cmbForma.setItems(FXCollections.observableArrayList(FormaPagamento.values()));
        cmbForma.getSelectionModel().selectFirst();
    }

    public void configurar(Long osId, Runnable aoConfirmar) {
        this.osId = osId;
        this.aoConfirmar = aoConfirmar;

        BigDecimal saldo = ordemServicoService.calcularSaldoDevedor(osId);
        lblSaldoAtual.setText("Saldo devedor: R$ " + String.format("%,.2f", saldo));
    }

    @FXML
    private void confirmar() {
        try {
            FormaPagamento forma = cmbForma.getValue();
            BigDecimal valor = new BigDecimal(txtValor.getText().trim());

            ordemServicoService.registrarPagamento(osId, forma, valor);

            aoConfirmar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            mostrarErro("Informe um valor numérico válido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        fecharModal();
    }

    private void mostrarErro(String mensagem) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
        lblErro.setManaged(true);
    }

    private void fecharModal() {
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }
}