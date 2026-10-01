package com.sgauto.app.controller.os;

import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import com.sgauto.app.service.OrdemServicoService;
import com.sgauto.app.service.financeiro.CategoriaFinanceiraService;
import com.sgauto.app.util.ParcelasUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OsRegistrarPagamentoModalController {

    @FXML private Label lblSaldoAtual;
    @FXML private ComboBox<FormaPagamento> cmbForma;
    @FXML private TextField txtValor;
    @FXML private CheckBox chkParcelar;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final OrdemServicoService ordemServicoService;
    private final ParcelasUtil parcelasUtil;
    private final CategoriaFinanceiraService categoriaFinanceiraService;
    private final String nomeCategoriaAutomatica =  "Receita Automática (Sistema)";

    private Long osId;
    private Runnable aoConfirmar;
    private BigDecimal saldoDevedorAtual;

    public OsRegistrarPagamentoModalController(OrdemServicoService ordemServicoService, ParcelasUtil parcelasUtil, CategoriaFinanceiraService categoriaFinanceiraService) {
        this.ordemServicoService = ordemServicoService;
        this.parcelasUtil = parcelasUtil;
        this.categoriaFinanceiraService = categoriaFinanceiraService;
    }

    @FXML
    public void initialize() {
        cmbForma.setItems(FXCollections.observableArrayList(FormaPagamento.values()));
        cmbForma.getSelectionModel().selectFirst();

        // Lógica Reativa: Se marcar "Parcelar", desabilita a forma de pagamento direta
        chkParcelar.selectedProperty().addListener((obs, oldVal, isParcelado) -> {
            cmbForma.setDisable(isParcelado);
            if (isParcelado) {
                cmbForma.setValue(null);
            } else {
                cmbForma.getSelectionModel().selectFirst();
            }
        });
    }

    public void configurar(Long osId, Runnable aoConfirmar) {
        this.osId = osId;
        this.aoConfirmar = aoConfirmar;

        this.saldoDevedorAtual = ordemServicoService.calcularSaldoDevedor(osId);
        lblSaldoAtual.setText("Saldo devedor: R$ " + String.format("%,.2f", saldoDevedorAtual));

        // Preenche o campo de valor automaticamente com o saldo devedor restante
        txtValor.setText(saldoDevedorAtual.toString());
    }

    @FXML
    private void confirmar() {
        try {
            lblErro.setVisible(false);
            lblErro.setManaged(false);

            String valorTexto = txtValor.getText().replace(",", ".").trim();
            BigDecimal valorDigitado = new BigDecimal(valorTexto);

            if (valorDigitado.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarErro("O valor deve ser maior que zero.");
                return;
            }

            if (valorDigitado.compareTo(saldoDevedorAtual) > 0) {
                mostrarErro("O valor não pode ser maior que o saldo devedor atual (R$ " + saldoDevedorAtual + ").");
                return;
            }

            // FLUXO 1: PARCELAMENTO (Vai para o Contas a Receber)
            if (chkParcelar.isSelected()) {

                Long clienteId = null;
                try {
                    var ordemServico = ordemServicoService.buscarPorId(osId);
                    clienteId = ordemServico.getCliente().getId();
                } catch (Exception e) {
                    System.out.println("Aviso: Cliente não foi extraído automaticamente. O modal permitirá busca manual.");
                }

                CategoriaFinanceira categoriaAvulsa = categoriaFinanceiraService.procurarPeloNome(nomeCategoriaAutomatica)
                        .orElseThrow(() -> new IllegalArgumentException("A categoria financeira '" + nomeCategoriaAutomatica + "' não foi encontrada no sistema."));
                Long catId = categoriaAvulsa.getId();
                String descricao = "Pagamento de O.S. #" + osId;

                // CORREÇÃO: Adicionado o "false" no final para não lançar os 40,00 no caixa!
                ordemServicoService.registrarPagamento(osId, FormaPagamento.OUTROS, valorDigitado, false);

                aoConfirmar.run();

                // 2. Fecha a janela atual (Pagamento de OS)
                fecharModal();

                // 3. Abre a janela de Parcelamento numa nova thread gráfica
                final Long finalClienteId = clienteId;
                Platform.runLater(() -> {
                    parcelasUtil.abrirTelaPagamento(
                            valorDigitado,
                            finalClienteId,
                            osId,
                            catId,
                            descricao
                    );
                });
                return;
            }

            // ==========================================
            // FLUXO 2: PAGAMENTO DIRETO À VISTA
            // ==========================================
            FormaPagamento forma = cmbForma.getValue();
            if (forma == null) {
                mostrarErro("Selecione a forma de pagamento.");
                return;
            }

            ordemServicoService.registrarPagamento(osId, forma, valorDigitado);

            aoConfirmar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            mostrarErro("Informe um valor numérico válido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro interno ao processar pagamento: " + e.getMessage());
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

    private void fecharModal() {
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }
}