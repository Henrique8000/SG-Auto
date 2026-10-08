package com.sgauto.app.controller.caixa;

import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.enums.financeiro.OrigemMovimentacao;
import com.sgauto.app.enums.financeiro.TipoMovimentacao;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import com.sgauto.app.service.financeiro.CaixaService;
import com.sgauto.app.service.financeiro.CategoriaFinanceiraService;
import com.sgauto.app.util.ParcelasUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class MovimentacaoCaixaModalController {

    @FXML private ComboBox<String> cmbOrigem;
    @FXML private VBox boxTipo;
    @FXML private ToggleGroup grupoTipo;
    @FXML private ToggleButton btnEntrada;
    @FXML private ToggleButton btnSaida;
    @FXML private ComboBox<String> cmbFormaPagamento;
    @FXML private TextField txtValor;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtPlaca;
    @FXML private CheckBox chkParcelar;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final CaixaService caixaService;
    private final ParcelasUtil parcelasUtil;
    private final CategoriaFinanceiraService categoriaFinanceiraService;
    private Runnable aoConfirmar;

    private static final Map<String, OrigemMovimentacao> ORIGENS = Map.of(
            "Venda Avulsa", OrigemMovimentacao.AVULSO,
            "Sangria", OrigemMovimentacao.SANGRIA,
            "Suprimento", OrigemMovimentacao.SUPRIMENTO
    );

    private static final Map<String, FormaPagamento> FORMAS = Map.of(
            "Dinheiro", FormaPagamento.DINHEIRO,
            "Débito", FormaPagamento.DEBITO,
            "Crédito", FormaPagamento.CREDITO,
            "Pix", FormaPagamento.PIX,
            "Outros", FormaPagamento.OUTROS
    );

    private final String nomeCategoriaAutomatica =  "Receita Automática (Sistema)";

    // Injeção de dependência dupla agora
    public MovimentacaoCaixaModalController(CaixaService caixaService, ParcelasUtil parcelasUtil, CategoriaFinanceiraService categoriaFinanceiraService) {
        this.caixaService = caixaService;
        this.parcelasUtil = parcelasUtil;
        this.categoriaFinanceiraService = categoriaFinanceiraService;
    }

    @FXML
    public void initialize() {
        cmbOrigem.setItems(FXCollections.observableArrayList("Venda Avulsa", "Sangria", "Suprimento"));
        cmbFormaPagamento.setItems(FXCollections.observableArrayList("Dinheiro", "Débito", "Crédito", "Pix", "Outros"));
        cmbFormaPagamento.getSelectionModel().select("Dinheiro");

        // Escutador para mudança de Origem
        cmbOrigem.valueProperty().addListener((obs, antigo, novo) -> ajustarCamposConformeOrigem(novo));

        // Escutador para mudança de Tipo (Entrada/Saída)
        grupoTipo.selectedToggleProperty().addListener((obs, antigo, novo) -> avaliarVisibilidadeParcelamento());
    }

    public void configurar(Runnable aoConfirmar) {
        this.aoConfirmar = aoConfirmar;
    }

    private void ajustarCamposConformeOrigem(String origemSelecionada) {
        boolean isAvulso = "Venda Avulsa".equals(origemSelecionada);

        boxTipo.setVisible(isAvulso);
        boxTipo.setManaged(isAvulso);
        cmbFormaPagamento.setDisable(!isAvulso);

        if ("Sangria".equals(origemSelecionada)) {
            btnSaida.setSelected(true);
            cmbFormaPagamento.getSelectionModel().select("Dinheiro");
        } else if ("Suprimento".equals(origemSelecionada)) {
            btnEntrada.setSelected(true);
            cmbFormaPagamento.getSelectionModel().select("Dinheiro");
        }

        avaliarVisibilidadeParcelamento();
    }

    private void avaliarVisibilidadeParcelamento() {
        // O Checkbox de parcelamento só aparece se for Venda Avulsa E for uma Entrada de dinheiro
        boolean podeParcelar = "Venda Avulsa".equals(cmbOrigem.getValue()) && btnEntrada.isSelected();

        if (chkParcelar != null) {
            chkParcelar.setVisible(podeParcelar);
            chkParcelar.setManaged(podeParcelar);
            if (!podeParcelar) {
                chkParcelar.setSelected(false);
            }
        }
    }

    @FXML
    private void confirmar() {
        try {
            System.out.println(">>> Botão Confirmar clicado. Iniciando validações...");

            String origemTexto = cmbOrigem.getValue();
            if (origemTexto == null) {
                mostrarErro("Selecione a origem da movimentação.");
                return;
            }

            BigDecimal valor = new BigDecimal(txtValor.getText().replace(",", ".").trim());
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarErro("O valor deve ser maior que zero.");
                return;
            }

            OrigemMovimentacao origem = ORIGENS.get(origemTexto);
            String descricao = txtDescricao.getText().trim();
            String placa = txtPlaca.getText().trim();

            // ==========================================
            // FLUXO 1: PAGAMENTO PARCELADO (Vai para o Conta a Receber)
            // ==========================================
            if (chkParcelar != null && chkParcelar.isSelected()) {
                System.out.println(">>> Checkbox Parcelar está MARCADO. Abrindo utilitário...");

                // Venda avulsa não obriga cliente, mandamos null!
                Long clienteId = null;
                CategoriaFinanceira categoriaAvulsa = categoriaFinanceiraService.procurarPeloNome(nomeCategoriaAutomatica)
                        .orElseThrow(() -> new IllegalArgumentException("A categoria financeira '" + nomeCategoriaAutomatica + "' não foi encontrada no sistema."));
                Long catId = categoriaAvulsa.getId();

                // Abre a tela de pagamento ANTES de fechar o modal atual
                parcelasUtil.abrirTelaPagamento(
                        valor,
                        clienteId,
                        null,
                        catId,
                        descricao.isBlank() ? "Venda Avulsa" : descricao
                );

                System.out.println(">>> Utilitário finalizou. Executando callback e fechando modal de caixa...");
                aoConfirmar.run();
                fecharModal(); // Fecha o caixa só agora
                return;
            }

            // ==========================================
            // FLUXO 2: PAGAMENTO DIRETO (Vai direto para o Caixa)
            // ==========================================
            System.out.println(">>> Checkbox NÃO marcado. Fluxo de caixa direto.");
            TipoMovimentacao tipo = switch (origem) {
                case SANGRIA -> TipoMovimentacao.SAIDA;
                case SUPRIMENTO -> TipoMovimentacao.ENTRADA;
                default -> btnEntrada.isSelected() ? TipoMovimentacao.ENTRADA : TipoMovimentacao.SAIDA;
            };

            String formaTexto = cmbFormaPagamento.getValue();
            FormaPagamento forma = formaTexto != null ? FORMAS.get(formaTexto) : FormaPagamento.DINHEIRO;

            caixaService.registrarMovimentacao(tipo, origem, forma, valor, descricao, null,
                    placa.isBlank() ? null : placa);

            aoConfirmar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            System.out.println(">>> ERRO: Valor numérico inválido digitado.");
            mostrarErro("Informe um valor numérico válido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(">>> ERRO de regra de negócio: " + e.getMessage());
            mostrarErro(e.getMessage());
        } catch (Exception e) {
            // CAPTURA QUALQUER OUTRO ERRO OCULTO (Ex: NullPointerException)
            System.out.println(">>> ERRO CRÍTICO DESCONHECIDO NO CAIXA:");
            e.printStackTrace();
            mostrarErro("Erro interno: " + e.getMessage());
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