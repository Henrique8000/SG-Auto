package com.sgauto.app.controller.patio;

import com.sgauto.app.dto.patio.PatioItemDashboardDTO;
import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import com.sgauto.app.service.PatioService;
import com.sgauto.app.service.financeiro.CategoriaFinanceiraService;
import com.sgauto.app.util.ParcelasUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    @FXML private CheckBox chkParcelar;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final PatioService patioService;
    private final ParcelasUtil parcelasUtil; // NOVO: Injeção do Utilitário
    private final CategoriaFinanceiraService categoriaFinanceiraService;
    private final String nomeCategoriaAutomatica =  "Receita Automática (Sistema)";

    private Long estadiaId;
    private Runnable aoConfirmar;
    private PatioItemDashboardDTO itemAtual;

    public SaidaPatioModalController(PatioService patioService, ParcelasUtil parcelasUtil, CategoriaFinanceiraService categoriaFinanceiraService) {
        this.patioService = patioService;
        this.parcelasUtil = parcelasUtil;
        this.categoriaFinanceiraService = categoriaFinanceiraService;
    }

    @FXML
    public void initialize() {
        // Lógica Reativa: Se marcar "Parcelar", desabilita a forma de pagamento direta
        chkParcelar.selectedProperty().addListener((obs, oldVal, isParcelado) -> {
            if (itemAtual != null && itemAtual.getValorEstimadoOuFinal().compareTo(BigDecimal.ZERO) > 0) {
                cmbFormaPagamento.setDisable(isParcelado);
                if (isParcelado) {
                    cmbFormaPagamento.setValue(null);
                }
            }
        });
    }

    public void configurar(Long estadiaId, Runnable aoConfirmar) {
        this.estadiaId = estadiaId;
        this.aoConfirmar = aoConfirmar;

        cmbFormaPagamento.setItems(FXCollections.observableArrayList(
                FormaPagamento.DINHEIRO, FormaPagamento.DEBITO, FormaPagamento.CREDITO, FormaPagamento.PIX, FormaPagamento.OUTROS));

        atualizarValor();
    }

    private void atualizarValor() {
        itemAtual = patioService.buscarItemPorId(estadiaId);

        lblPlaca.setText(itemAtual.getPlaca());
        lblCliente.setText(itemAtual.getClienteNome());
        lblEntrada.setText(itemAtual.getDataEntrada().format(FORMATO_DATA));
        lblValorDevido.setText(formatarMoeda(itemAtual.getValorEstimadoOuFinal()));

        boolean semCobranca = itemAtual.getValorEstimadoOuFinal().compareTo(BigDecimal.ZERO) == 0;

        if (semCobranca) {
            chkParcelar.setDisable(true);
            chkParcelar.setSelected(false);
            cmbFormaPagamento.setDisable(true);
            cmbFormaPagamento.setPromptText("Isento — sem cobrança");
            cmbFormaPagamento.setValue(null);
        } else {
            chkParcelar.setDisable(false);
            cmbFormaPagamento.setDisable(chkParcelar.isSelected());
            cmbFormaPagamento.setPromptText("Selecione a forma de pagamento");
        }
    }

    @FXML
    private void confirmar() {
        lblErro.setVisible(false);
        lblErro.setManaged(false);

        BigDecimal valorAntesDeConfirmar = itemAtual.getValorEstimadoOuFinal();
        atualizarValor();

        boolean virouCobrancaAgora = valorAntesDeConfirmar.compareTo(BigDecimal.ZERO) == 0
                && itemAtual.getValorEstimadoOuFinal().compareTo(BigDecimal.ZERO) != 0;

        if (virouCobrancaAgora) {
            mostrarErro("O valor da estadia mudou desde que esta tela foi aberta (agora é "
                    + formatarMoeda(itemAtual.getValorEstimadoOuFinal()) + "). Selecione a forma de pagamento e confirme novamente.");
            return;
        }

        try {
            // FLUXO 1: PARCELAMENTO
            if (chkParcelar.isSelected()) {
                // Fechamos a tela do pátio ANTES de abrir a de pagamento, igual fizemos no Caixa
                fecharModal();

                // ID sugerido para a categoria de Estadia de Pátio (ajuste conforme o seu banco)
                CategoriaFinanceira categoriaAvulsa = categoriaFinanceiraService.procurarPeloNome(nomeCategoriaAutomatica)
                        .orElseThrow(() -> new IllegalArgumentException("A categoria financeira '" + nomeCategoriaAutomatica + "' não foi encontrada no sistema."));
                Long catId = categoriaAvulsa.getId();

                // Nota: Assumi que o seu DTO tem um "getClienteId()".
                // Se não tiver, pode passar null que o modal vai deixar buscar.
                Long clienteId = null;
                try {
                    // Tenta usar o método se ele existir no seu DTO, senão apague este try-catch e mande null direto.
                    clienteId = (Long) itemAtual.getClass().getMethod("getClienteId").invoke(itemAtual);
                } catch (Exception ignored) {}

                parcelasUtil.abrirTelaPagamento(
                        itemAtual.getValorEstimadoOuFinal(),
                        clienteId,
                        null, // osId
                        catId,
                        "Estadia Pátio - Placa: " + itemAtual.getPlaca()
                );

                patioService.registrarSaida(estadiaId, FormaPagamento.OUTROS);
                aoConfirmar.run();
                return;
            }

            // FLUXO 2: PAGAMENTO DIRETO À VISTA
            if (itemAtual.getValorEstimadoOuFinal().compareTo(BigDecimal.ZERO) > 0 && cmbFormaPagamento.getValue() == null) {
                mostrarErro("Selecione a forma de pagamento.");
                return;
            }

            patioService.registrarSaida(estadiaId, cmbFormaPagamento.getValue());
            aoConfirmar.run();
            fecharModal();

        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro interno ao processar saída: " + e.getMessage());
            e.printStackTrace();
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