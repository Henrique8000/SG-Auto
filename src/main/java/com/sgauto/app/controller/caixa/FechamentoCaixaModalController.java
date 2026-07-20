package com.sgauto.app.controller;

import com.sgauto.app.enums.ModoConferencia;
import com.sgauto.app.model.Caixa;
import com.sgauto.app.model.ConfiguracaoCaixa;
import com.sgauto.app.service.CaixaService;
import com.sgauto.app.service.ConfiguracaoCaixaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FechamentoCaixaModalController {

    @FXML private Label lblValorEsperado;
    @FXML private VBox boxContagem;
    @FXML private TextField txtValorContado;
    @FXML private Label lblSemConferencia;
    @FXML private VBox boxJustificativa;
    @FXML private TextArea txtJustificativa;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final CaixaService caixaService;
    private final ConfiguracaoCaixaService configuracaoCaixaService;

    private Caixa caixaAtual;
    private ModoConferencia modo;
    private BigDecimal valorEsperado;
    private Runnable aoFechar;

    public FechamentoCaixaModalController(CaixaService caixaService, ConfiguracaoCaixaService configuracaoCaixaService) {
        this.caixaService = caixaService;
        this.configuracaoCaixaService = configuracaoCaixaService;
    }

    public void configurar(Caixa caixaAtual, Runnable aoFechar) {
        this.caixaAtual = caixaAtual;
        this.aoFechar = aoFechar;

        ConfiguracaoCaixa config = configuracaoCaixaService.buscarConfiguracao();
        this.modo = config.getModoConferencia();

        this.valorEsperado = caixaService.calcularValorEsperado(caixaAtual.getId());
        lblValorEsperado.setText(formatarMoeda(valorEsperado));

        ajustarCamposConformeModo();
    }

    private void ajustarCamposConformeModo() {
        boolean exibirContagem = modo != ModoConferencia.SEM_CONFERENCIA;
        boxContagem.setVisible(exibirContagem);
        boxContagem.setManaged(exibirContagem);

        lblSemConferencia.setVisible(!exibirContagem);
        lblSemConferencia.setManaged(!exibirContagem);
    }

    @FXML
    private void confirmar() {
        try {
            BigDecimal valorContado = null;

            if (modo != ModoConferencia.SEM_CONFERENCIA) {
                String texto = txtValorContado.getText().trim();

                if (modo == ModoConferencia.OBRIGATORIA && texto.isBlank()) {
                    mostrarErro("Informe o valor contado para fechar o caixa.");
                    return;
                }

                if (!texto.isBlank()) {
                    valorContado = new BigDecimal(texto);
                }
            }

            if (valorContado != null && valorContado.compareTo(valorEsperado) != 0
                    && txtJustificativa.getText().trim().isBlank()) {
                mostrarJustificativa();
                mostrarErro("Há diferença entre o valor esperado e o contado. Informe a justificativa.");
                return;
            }

            String justificativa = txtJustificativa.getText().trim();
            Caixa fechado = caixaService.fecharCaixaAtual(valorContado, justificativa.isBlank() ? null : justificativa);

            mostrarResumoFechamento(fechado);

            aoFechar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            mostrarErro("Informe um valor numérico válido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void mostrarJustificativa() {
        boxJustificativa.setVisible(true);
        boxJustificativa.setManaged(true);
    }

    private void mostrarResumoFechamento(Caixa fechado) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Caixa fechado");
        alert.setHeaderText(null);
        alert.setContentText(
                "Caixa fechado com sucesso.\n\n" +
                        "Total de entradas: " + formatarMoeda(fechado.getTotalEntradas()) + "\n" +
                        "Total de saídas: " + formatarMoeda(fechado.getTotalSaidas()) + "\n" +
                        "Diferença: " + (fechado.getDiferenca() != null ? formatarMoeda(fechado.getDiferenca()) : "não conferida") + "\n\n" +
                        "Um novo caixa foi aberto automaticamente."
        );
        alert.showAndWait();
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

    private String formatarMoeda(BigDecimal valor) {
        return String.format("R$ %,.2f", valor);
    }
}