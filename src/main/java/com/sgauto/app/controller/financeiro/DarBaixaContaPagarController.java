package com.sgauto.app.controller.financeiro;

import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.model.financeiro.ContaPagar;
import com.sgauto.app.service.financeiro.ContaPagarService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DarBaixaContaPagarController {

    @FXML private Label lblDescricao;
    @FXML private Label lblValorDevido;
    @FXML private Label lblJaPago;
    @FXML private TextField txtValorPago;
    @FXML private DatePicker dtPagamento;
    @FXML private ComboBox<FormaPagamento> cmbFormaPagamento;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    @Autowired
    private ContaPagarService contaPagarService;

    private ContaPagar conta;

    public void setConta(ContaPagar conta) {
        this.conta = conta;
        preencherResumo();
    }

    @FXML
    public void initialize() {
        cmbFormaPagamento.setItems(FXCollections.observableArrayList(FormaPagamento.values()));
        dtPagamento.setValue(LocalDate.now());

        btnConfirmar.setOnAction(e -> confirmar());
        btnCancelar.setOnAction(e -> fechar());
    }

    private void preencherResumo() {
        if (conta == null) {
            return;
        }
        BigDecimal valorDevido = conta.getValorOriginal()
                .subtract(conta.getValorDesconto())
                .add(conta.getValorJuros())
                .add(conta.getValorMulta());
        BigDecimal jaPago = conta.getValorPago() != null ? conta.getValorPago() : BigDecimal.ZERO;

        lblDescricao.setText(conta.getDescricao() + " (parcela " + conta.getNumeroParcela() + "/" + conta.getTotalParcelas() + ")");
        lblValorDevido.setText("Valor devido: R$ " + valorDevido);
        lblJaPago.setText("Já pago: R$ " + jaPago + " | Restante: R$ " + valorDevido.subtract(jaPago));
    }

    private void confirmar() {
        lblErro.setText("");
        try {
            BigDecimal valor = new BigDecimal(txtValorPago.getText().trim().replace(",", "."));
            LocalDate data = dtPagamento.getValue();
            FormaPagamento forma = cmbFormaPagamento.getValue();

            contaPagarService.darBaixa(conta.getId(), valor, data, forma);
            fechar();
        } catch (NumberFormatException ex) {
            lblErro.setText("Informe um valor numérico válido.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            lblErro.setText(ex.getMessage());
        }
    }

    private void fechar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}