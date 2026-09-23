package com.sgauto.app.controller.financeiro;

import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import com.sgauto.app.model.financeiro.ContaReceber;
import com.sgauto.app.repository.ClienteRepository;
import com.sgauto.app.repository.financeiro.CategoriaFinanceiraRepository;
import com.sgauto.app.service.financeiro.ContaReceberService;
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
 * Cadastro manual de conta a receber, com suporte a parcelamento e entrada.
 *
 * IMPORTANTE: quando a tela de confirmação de pagamento de OS/venda avulsa for
 * desenvolvida, ela NÃO deve abrir este modal. Ela deve chamar diretamente
 * ContaReceberService.registrarEntrada(...) e ContaReceberService.gerarParcelas(...)
 * (o mesmo fluxo implementado aqui em confirmar()), passando origem = OS/VENDA_AVULSA
 * e o cliente/ordemServico já conhecidos pelo contexto da venda, em vez de origem = MANUAL.
 * Este modal serve apenas para lançamentos manuais (crediário direto sem OS, por exemplo).
 */
@Component
public class IncluirContaReceberController {

    @FXML private TextField txtDescricao;
    @FXML private ComboBox<CategoriaFinanceira> cmbCategoria;
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private TextField txtValorTotal;
    @FXML private TextField txtValorEntrada;
    @FXML private Spinner<Integer> spnQuantidadeParcelas;
    @FXML private DatePicker dtPrimeiraParcela;
    @FXML private CheckBox chkJaRecebeuPrimeira;
    @FXML private ComboBox<FormaPagamento> cmbFormaPagamento;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    @Autowired
    private ContaReceberService contaReceberService;

    @Autowired
    private CategoriaFinanceiraRepository categoriaFinanceiraRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @FXML
    public void initialize() {
        cmbCategoria.setItems(FXCollections.observableArrayList(categoriaFinanceiraRepository.findAllByAtivoTrue()));
        cmbCategoria.setConverter(converterPara(CategoriaFinanceira::getNome));

        cmbCliente.setItems(FXCollections.observableArrayList(clienteRepository.findAll()));
        cmbCliente.setConverter(converterPara(Cliente::getNome));

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
            BigDecimal valorEntrada = txtValorEntrada.getText() == null || txtValorEntrada.getText().isBlank()
                    ? BigDecimal.ZERO
                    : new BigDecimal(txtValorEntrada.getText().trim().replace(",", "."));

            if (valorEntrada.compareTo(BigDecimal.ZERO) < 0 || valorEntrada.compareTo(valorTotal) >= 0) {
                lblErro.setText("O valor de entrada deve ser maior ou igual a zero e menor que o valor total.");
                return;
            }

            Integer quantidadeParcelas = spnQuantidadeParcelas.getValue();
            LocalDate dataPrimeiraParcela = dtPrimeiraParcela.getValue();
            if (dataPrimeiraParcela == null) {
                lblErro.setText("Informe a data de vencimento da 1ª parcela.");
                return;
            }

            FormaPagamento forma = cmbFormaPagamento.getValue();
            boolean precisaDeForma = valorEntrada.compareTo(BigDecimal.ZERO) > 0 || chkJaRecebeuPrimeira.isSelected();
            if (precisaDeForma && forma == null) {
                lblErro.setText("Selecione a forma de pagamento.");
                return;
            }

            Cliente cliente = cmbCliente.getValue();
            CategoriaFinanceira categoria = cmbCategoria.getValue();

            if (valorEntrada.compareTo(BigDecimal.ZERO) > 0) {
                Long clienteId = cliente != null ? cliente.getId() : null;
                contaReceberService.registrarEntrada(OrigemMovimentacao.AVULSO, forma, valorEntrada,
                        "Entrada - " + descricao, clienteId);
            }

            ContaReceber dadosBase = new ContaReceber();
            dadosBase.setDescricao(descricao);
            dadosBase.setCategoria(categoria);
            dadosBase.setCliente(cliente);
            dadosBase.setValorOriginal(valorTotal.subtract(valorEntrada));
            dadosBase.setDataVencimento(dataPrimeiraParcela);
            dadosBase.setOrigem("MANUAL");

            List<ContaReceber> parcelas = contaReceberService.gerarParcelas(dadosBase, quantidadeParcelas, 30);

            if (chkJaRecebeuPrimeira.isSelected() && !parcelas.isEmpty()) {
                ContaReceber primeira = parcelas.get(0);
                contaReceberService.darBaixa(primeira.getId(), primeira.getValorOriginal(), LocalDate.now(), forma);
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