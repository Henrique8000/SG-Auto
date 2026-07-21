package com.sgauto.app.controller.caixa;

import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.enums.TipoMovimentacao;
import com.sgauto.app.service.CaixaService;
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
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final CaixaService caixaService;
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

    public MovimentacaoCaixaModalController(CaixaService caixaService) {
        this.caixaService = caixaService;
    }

    @FXML
    public void initialize() {
        cmbOrigem.setItems(FXCollections.observableArrayList("Venda Avulsa", "Sangria", "Suprimento"));
        cmbFormaPagamento.setItems(FXCollections.observableArrayList("Dinheiro", "Débito", "Crédito", "Pix", "Outros"));
        cmbFormaPagamento.getSelectionModel().select("Dinheiro");

        cmbOrigem.valueProperty().addListener((obs, antigo, novo) -> ajustarCamposConformeOrigem(novo));
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
    }

    @FXML
    private void confirmar() {
        try {
            String origemTexto = cmbOrigem.getValue();
            if (origemTexto == null) {
                mostrarErro("Selecione a origem da movimentação.");
                return;
            }

            OrigemMovimentacao origem = ORIGENS.get(origemTexto);
            TipoMovimentacao tipo = switch (origem) {
                case SANGRIA -> TipoMovimentacao.SAIDA;
                case SUPRIMENTO -> TipoMovimentacao.ENTRADA;
                default -> btnEntrada.isSelected() ? TipoMovimentacao.ENTRADA : TipoMovimentacao.SAIDA;
            };

            String formaTexto = cmbFormaPagamento.getValue();
            FormaPagamento forma = formaTexto != null ? FORMAS.get(formaTexto) : FormaPagamento.DINHEIRO;

            BigDecimal valor = new BigDecimal(txtValor.getText().trim());
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarErro("O valor deve ser maior que zero.");
                return;
            }

            String descricao = txtDescricao.getText().trim();
            String placa = txtPlaca.getText().trim();

            caixaService.registrarMovimentacao(tipo, origem, forma, valor, descricao, null,
                    placa.isBlank() ? null : placa);

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