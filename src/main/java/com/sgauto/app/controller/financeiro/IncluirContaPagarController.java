package com.sgauto.app.controller.financeiro;

import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.model.estoque.Fornecedor;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import com.sgauto.app.model.financeiro.ContaPagar;
import com.sgauto.app.repository.estoque.FornecedorRepository;
import com.sgauto.app.repository.financeiro.CategoriaFinanceiraRepository;
import com.sgauto.app.service.financeiro.ContaPagarService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cadastro manual de conta a pagar, com suporte a parcelamento.
 * Diferente de Contas a Receber, aqui não existe o conceito de "entrada" (quem financia
 * o parcelamento é a oficina comprando do fornecedor, não o cliente comprando da oficina).
 */
@Component
public class IncluirContaPagarController {

    @FXML private TextField txtDescricao;
    @FXML private ComboBox<CategoriaFinanceira> cmbCategoria;
    @FXML private ComboBox<Fornecedor> cmbFornecedor;
    @FXML private TextField txtValorTotal;
    @FXML private Spinner<Integer> spnQuantidadeParcelas;
    @FXML private DatePicker dtPrimeiraParcela;
    @FXML private CheckBox chkJaPagouPrimeira;
    @FXML private ComboBox<FormaPagamento> cmbFormaPagamento;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    @Autowired
    private ContaPagarService contaPagarService;

    @Autowired
    private CategoriaFinanceiraRepository categoriaFinanceiraRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @FXML
    public void initialize() {
        cmbCategoria.setItems(FXCollections.observableArrayList(categoriaFinanceiraRepository.findAllByAtivoTrue()));
        cmbCategoria.setConverter(converterPara(CategoriaFinanceira::getNome));

        cmbFornecedor.setItems(FXCollections.observableArrayList(fornecedorRepository.findAll()));
        cmbFornecedor.setConverter(converterPara(f -> f.getNomeFantasia() != null ? f.getNomeFantasia() : f.getRazaoSocial()));

        cmbFormaPagamento.setItems(FXCollections.observableArrayList(FormaPagamento.values()));

        spnQuantidadeParcelas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 48, 1));
        dtPrimeiraParcela.setValue(LocalDate.now());

        btnConfirmar.setOnAction(e -> confirmar());
        btnCancelar.setOnAction(e -> fechar());
    }

    private void confirmar() {
        lblErro.setText("");
        try {
            String descricao = txtDescricao.getText().trim();
            if (descricao.isBlank()) {
                lblErro.setText("Informe a descrição.");
                return;
            }

            BigDecimal valorTotal = new BigDecimal(txtValorTotal.getText().trim().replace(",", "."));

            Integer quantidadeParcelas = spnQuantidadeParcelas.getValue();
            LocalDate dataPrimeiraParcela = dtPrimeiraParcela.getValue();
            if (dataPrimeiraParcela == null) {
                lblErro.setText("Informe a data de vencimento da 1ª parcela.");
                return;
            }

            FormaPagamento forma = cmbFormaPagamento.getValue();
            if (chkJaPagouPrimeira.isSelected() && forma == null) {
                lblErro.setText("Selecione a forma de pagamento.");
                return;
            }

            Fornecedor fornecedor = cmbFornecedor.getValue();
            CategoriaFinanceira categoria = cmbCategoria.getValue();

            ContaPagar dadosBase = new ContaPagar();
            dadosBase.setDescricao(descricao);
            dadosBase.setCategoria(categoria);
            dadosBase.setFornecedor(fornecedor);
            dadosBase.setValorOriginal(valorTotal);
            dadosBase.setDataVencimento(dataPrimeiraParcela);
            dadosBase.setOrigem("MANUAL");

            List<ContaPagar> parcelas = contaPagarService.gerarParcelas(dadosBase, quantidadeParcelas, 30);

            if (chkJaPagouPrimeira.isSelected() && !parcelas.isEmpty()) {
                ContaPagar primeira = parcelas.get(0);
                contaPagarService.darBaixa(primeira.getId(), primeira.getValorOriginal(), LocalDate.now(), forma);
            }

            fechar();
        } catch (NumberFormatException ex) {
            lblErro.setText("Informe valores numéricos válidos.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            lblErro.setText(ex.getMessage());
        }
    }

    private <T> StringConverter<T> converterPara(java.util.function.Function<T, String> extrator) {
        return new StringConverter<>() {
            @Override
            public String toString(T objeto) {
                return objeto == null ? "" : extrator.apply(objeto);
            }

            @Override
            public T fromString(String texto) {
                return null;
            }
        };
    }

    private void fechar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}