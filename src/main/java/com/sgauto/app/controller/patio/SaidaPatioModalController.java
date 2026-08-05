package com.sgauto.app.controller.patio;

import com.sgauto.app.controller.dto.patio.PatioItemDashboardDTO;
import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.service.PatioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Component
public class SaidaPatioModalController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblPlaca;
    @FXML private Label lblCliente;
    @FXML private Label lblEntrada;
    @FXML private Label lblValorDevido;
    @FXML private ComboBox<FormaPagamento> cmbFormaPagamento;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final PatioService patioService;
    private Long estadiaId;
    private Runnable aoConfirmar;

    public SaidaPatioModalController(PatioService patioService) {
        this.patioService = patioService;
    }

    public void configurar(Long estadiaId, Runnable aoConfirmar) {
        this.estadiaId = estadiaId;
        this.aoConfirmar = aoConfirmar;

        PatioItemDashboardDTO item = patioService.buscarItemPorId(estadiaId);
        lblPlaca.setText(item.getPlaca());
        lblCliente.setText(item.getClienteNome());
        lblEntrada.setText(item.getDataEntrada().format(FORMATO_DATA));
        lblValorDevido.setText(formatarMoeda(item.getValorEstimadoOuFinal()));

        boolean semCobranca = item.getValorEstimadoOuFinal().compareTo(BigDecimal.ZERO) == 0;
        cmbFormaPagamento.setItems(FXCollections.observableArrayList(
                FormaPagamento.DINHEIRO, FormaPagamento.DEBITO, FormaPagamento.CREDITO, FormaPagamento.PIX, FormaPagamento.OUTROS));
        cmbFormaPagamento.setDisable(semCobranca);
        cmbFormaPagamento.setPromptText(semCobranca ? "Isento — sem cobrança" : "Selecione a forma de pagamento");
    }

    @FXML
    private void confirmar() {
        try {
            patioService.registrarSaida(estadiaId, cmbFormaPagamento.getValue());
            aoConfirmar.run();
            fecharModal();
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

    private String formatarMoeda(BigDecimal valor) {
        return valor == null ? "R$ 0,00" : String.format("R$ %,.2f", valor);
    }

    private void fecharModal() {
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }
}